
package com.lora.bi.controller;

import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.google.gson.Gson;
import com.lora.bi.annotation.AuthCheck;
import com.lora.bi.common.BaseResponse;
import com.lora.bi.common.DeleteRequest;
import com.lora.bi.common.ErrorCode;
import com.lora.bi.common.ResultUtils;
import com.lora.bi.constant.CommonConstant;
import com.lora.bi.constant.UserConstant;
import com.lora.bi.exception.BusinessException;
import com.lora.bi.exception.ThrowUtils;
import com.lora.bi.manager.RedissonLimiterManager;
import com.lora.bi.model.dto.chart.*;
import com.lora.bi.model.entity.Chart;
import com.lora.bi.model.entity.User;
import com.lora.bi.model.vo.BiGenVO;
import com.lora.bi.service.ChartService;
import com.lora.bi.service.UserService;
import com.lora.bi.utils.AiResponseParser;
import com.lora.bi.utils.ExcelUtils;
import com.lora.bi.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 帖子接口
 *
 * @author <a href="https://github.com/lilora">程序员lora</a>
 */
@RestController
@RequestMapping("/chart")
@Slf4j
public class ChartController {
    @Resource
    private ChartService chartService;
    @Resource
    private UserService userService;
    @Resource
    private com.lora.bi.service.AiService aiService;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    private final static Gson GSON = new Gson();
    @Autowired
    private RedissonLimiterManager redissonLimiterManager;
    // region 增删改查
    /**
     * 创建
     *
     * @param chartAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addChart(@RequestBody ChartAddRequest chartAddRequest, HttpServletRequest request) {
        if (chartAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartAddRequest, chart);
        User loginUser = userService.getLoginUser(request);
        chart.setUserId(loginUser.getId());
        boolean result = chartService.save(chart);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newChartId = chart.getId();
        return ResultUtils.success(newChartId);
    }
    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteChart(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldChart.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean b = chartService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新（仅管理员）
     *
     * @param chartUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateChart(@RequestBody ChartUpdateRequest chartUpdateRequest) {
        if (chartUpdateRequest == null || chartUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartUpdateRequest, chart);
        long id = chartUpdateRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Chart> getChartById(long id, HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = chartService.getById(id);
        if (chart == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return ResultUtils.success(chart);
    }

    /**
     * 分页获取列表（封装类）
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Chart>> listChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                     HttpServletRequest request) {
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    /**
     * 分页获取当前用户创建的资源列表
     *
     * @param chartQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<Chart>> listMyChartByPage(@RequestBody ChartQueryRequest chartQueryRequest,
                                                       HttpServletRequest request) {
        if (chartQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        chartQueryRequest.setUserId(loginUser.getId());
        long current = chartQueryRequest.getCurrent();
        long size = chartQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> chartPage = chartService.page(new Page<>(current, size),
                getQueryWrapper(chartQueryRequest));
        return ResultUtils.success(chartPage);
    }

    // endregion

    /**
     * 编辑（用户）
     *
     * @param chartEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editChart(@RequestBody ChartEditRequest chartEditRequest, HttpServletRequest request) {
        if (chartEditRequest == null || chartEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Chart chart = new Chart();
        BeanUtils.copyProperties(chartEditRequest, chart);
        User loginUser = userService.getLoginUser(request);
        long id = chartEditRequest.getId();
        // 判断是否存在
        Chart oldChart = chartService.getById(id);
        ThrowUtils.throwIf(oldChart == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldChart.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = chartService.updateById(chart);
        return ResultUtils.success(result);
    }

    /**
     * 获取查询包装类
     *
     * @param chartQueryRequest
     * @return
     */
    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest chartQueryRequest) {
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        if (chartQueryRequest == null) {
            return queryWrapper;
        }
        // 获取查询条件
        Long id = chartQueryRequest.getId();
        String name = chartQueryRequest.getName();
        String goal = chartQueryRequest.getGoal();
        String chartType = chartQueryRequest.getChartType();
        Long userId = chartQueryRequest.getUserId();
        String sortField = chartQueryRequest.getSortField();
        String sortOrder = chartQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(id != null && id > 0, "id", id);
        queryWrapper.like(StringUtils.isNotBlank(name), "name", name);
        queryWrapper.eq(StringUtils.isNotBlank(goal), "goal", goal);
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType", chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 智能分析（异步）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param request
     * @return
     */
    @PostMapping("/gen/async")
    public BaseResponse<BiGenVO> genChartByAiAsync(@RequestPart("file") MultipartFile multipartFile,
                                                   GenChartByAIRequest genChartByAiRequest, HttpServletRequest request) throws IOException {
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        // 校验
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "目标为空");
        ThrowUtils.throwIf(StringUtils.isNotBlank(name) && name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过 1M");
        // 校验文件后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> validFileSuffixList = Arrays.asList("xlsx", "xls");
        ThrowUtils.throwIf(!validFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        User loginUser = userService.getLoginUser(request);
        // 限流判断，每个用户一个限流器
        redissonLimiterManager.doRateLimit("genChartByAi_" + loginUser.getId());
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        userInput.append(csvData).append("\n");
        // 插入到数据库
        Chart chart = new Chart();
        chart.setName(name);
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setChartType(chartType);
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        boolean saveResult = chartService.save(chart);
        ThrowUtils.throwIf(!saveResult, ErrorCode.SYSTEM_ERROR, "图表保存失败");
        // try 任务队列满了后抛异常
        // 提交任务

        try {
            CompletableFuture.runAsync(() -> {
                // 进行重试机制 ！！！ 使用guava retrying,在每次进行异步任务时候
                RetryerBuilder<String> Retryer = RetryerBuilder.<String>newBuilder()
                        .retryIfExceptionOfType(Exception.class)
                        .retryIfResult(result -> result == null || result.isEmpty())
                        .withStopStrategy(StopStrategies.stopAfterAttempt(3));
                // 最多重试3次
                try {
                    Chart newChart = new Chart();
                    newChart.setId(chart.getId());
                    newChart.setStatus("running");
                    newChart.setExecMessage("图表正在生成中");
                    boolean b = chartService.updateById(newChart);
                    if (!b) {
                        handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                    }

                    // 调用AI服务，使用配置好的chartChatBot进行数据分析
                    String aiResult =  Retryer.build().call(() -> {
                        // 调用ai服务，加入重试机制
                       return  aiService.sendMessage(String.valueOf(userInput));
                    });
                    // 记录AI原始响应的前500个字符，便于调试
                    if (aiResult != null && aiResult.length() > 0) {
                        String preview = aiResult.length() > 500 ? aiResult.substring(0, 500) + "..." : aiResult;
                        log.info("AI原始响应预览: {}", preview);
                    }
                    log.info("AI原始响应长度: {}", aiResult != null ? aiResult.length() : 0);
                    log.debug("AI原始响应内容: {}", aiResult);
                    // 检查AI是否返回了有效内容
                    if (aiResult == null || aiResult.trim().isEmpty()) {
                        log.error("AI返回了空内容，可能API调用失败或超时");
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI服务未返回有效内容，请稍后重试");
                    }
                    // 解析AI响应，提取图表配置和分析结论
                    String chartOption = AiResponseParser.extractContentByTag(aiResult, "execute");
                    String conclusion = AiResponseParser.extractContentByTag(aiResult, "text");
                    log.info("提取的图表配置: {}", chartOption);
                    log.info("提取的分析结论: {}", conclusion);
                    log.info("提取的图表配置长度: {}", chartOption != null ? chartOption.length() : 0);
                    log.info("提取的分析结论长度: {}", conclusion != null ? conclusion.length() : 0);

                    // 检查解析结果是否有效
                    if (chartOption == null || chartOption.trim().isEmpty()) {
                        log.error("未能从AI响应中提取到有效的图表配置");
                        throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI未能生成有效的图表配置，请检查输入数据或稍后重试");
                    }
                    if (conclusion == null || conclusion.trim().isEmpty()) {
                        log.warn("未能从AI响应中提取到分析结论");
                        // 这里我们不抛出异常，因为图表配置是主要的，分析结论是次要的
                    }
                    // 更新图表信息
                    Chart updateChart = new Chart();
                    updateChart.setId(chart.getId());
                    updateChart.setStatus("succeed");
                    updateChart.setGenChart(chartOption);
                    updateChart.setGenResult(conclusion);
                    updateChart.setExecMessage("AI图表生成成功");
                    boolean c = chartService.updateById(updateChart);
                    if (!c) {
                        handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
                    }
                } catch (Exception e) {
                    log.error("ai图表生成任务执行失败", e);
                    handleChartUpdateError(chart.getId(), "ai生成图表失败:" + e.getMessage());
                }
            }, threadPoolExecutor);
        } catch (RejectedExecutionException e) {
            log.error("任务队列已经满了，无法提交ai图表生成任务", e);
            // 更新数据库状态
            handleChartUpdateError(chart.getId(), "系统繁忙，任务队列已经满了，请稍后重试");
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "系统繁忙，任务队列已满，请稍后重试");

        }

        BiGenVO biGenVO = new BiGenVO();
        biGenVO.setSuccess(true);
        return ResultUtils.success(biGenVO);
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean updateResult = chartService.updateById(updateChart);
        if (!updateResult) {
            log.error("更新图表失败状态失败{},{}", chartId, execMessage);
        }

    }


    /**
     * 智能分析，根据用户上传数据类型,结合用户的建议图表类型和提示词，智能生成图表
     *
     * @param multipartFile
     * @param genChartByAIRequest
     * @param request
     * @return
     */
    @PostMapping("/gen")
    public BaseResponse<BiGenVO> genChartByAI(@RequestPart("file") MultipartFile multipartFile,
                                              GenChartByAIRequest genChartByAIRequest, HttpServletRequest request) throws IOException {
        String chartType = genChartByAIRequest.getChartType();
        String name = genChartByAIRequest.getName();
        String goal = genChartByAIRequest.getGoal();
        // 校验
        // 校验文件
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();
        // 检查文件大小
        final long ONE_MB = 1024 * 1024L;
        if (size > ONE_MB) {
            ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件超过一兆");
        }
        // 检查原始文件名,校验后缀 aaa.png
        String suffix = FileUtil.getSuffix(originalFilename);
        List<String> validSuffix = Arrays.asList("png", "jpg", "jpeg", "gif", "svg");
        ThrowUtils.throwIf(!validSuffix.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
        User loginUser = userService.getLoginUser(request);
        //限流判断，每个用户一个限流器
        redissonLimiterManager.doRateLimit("genChartByAI" + loginUser.getId());
        // 校验参数
        ThrowUtils.throwIf(StringUtils.isBlank(name) || name.length() > 100, ErrorCode.PARAMS_ERROR, "名称过长");
        ThrowUtils.throwIf(StringUtils.isBlank(goal), ErrorCode.PARAMS_ERROR, "分析目标不能为空");
        ThrowUtils.throwIf(multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "上传的文件不能为空");
        try {
            //压缩后的数据
            String csvData = ExcelUtils.excelToCsv(multipartFile);
            // 数据采样优化：如果数据行数过多，只取前100行
            log.info("处理后的CSV数据: {}", csvData);
            // 构造用户输入
            String userInput = String.format("分析目标：%s\n图表类型：%s\n名称：%s\n原始数据：\n%s",
                    goal, chartType, name, csvData);
            // 记录发送给AI的完整提示词
            log.info("发送给AI的完整提示词: {}", userInput);
            ThrowUtils.throwIf(userInput.isEmpty(), ErrorCode.PARAMS_ERROR, "用户输入不能为空" + userInput);
            // 调用AI服务，使用配置好的chartChatBot进行数据分析
            String aiResult = aiService.sendMessage(userInput);
            // 记录AI原始响应的前500个字符，便于调试
            if (aiResult != null && aiResult.length() > 0) {
                String preview = aiResult.length() > 500 ? aiResult.substring(0, 500) + "..." : aiResult;
                log.info("AI原始响应预览: {}", preview);
            }
            log.info("AI原始响应长度: {}", aiResult != null ? aiResult.length() : 0);
            log.debug("AI原始响应内容: {}", aiResult);
            // 检查AI是否返回了有效内容
            if (aiResult == null || aiResult.trim().isEmpty()) {
                log.error("AI返回了空内容，可能API调用失败或超时");
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI服务未返回有效内容，请稍后重试");
            }
            // 解析AI响应，提取图表配置和分析结论
            String chartOption = AiResponseParser.extractContentByTag(aiResult, "execute");
            String conclusion = AiResponseParser.extractContentByTag(aiResult, "text");
            log.info("提取的图表配置: {}", chartOption);
            log.info("提取的分析结论: {}", conclusion);
            log.info("提取的图表配置长度: {}", chartOption != null ? chartOption.length() : 0);
            log.info("提取的分析结论长度: {}", conclusion != null ? conclusion.length() : 0);

            // 检查解析结果是否有效
            if (chartOption == null || chartOption.trim().isEmpty()) {
                log.error("未能从AI响应中提取到有效的图表配置");
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI未能生成有效的图表配置，请检查输入数据或稍后重试");
            }
            if (conclusion == null || conclusion.trim().isEmpty()) {
                log.warn("未能从AI响应中提取到分析结论");
                // 这里我们不抛出异常，因为图表配置是主要的，分析结论是次要的
            }
            // 插入数据库
            Chart chart = new Chart();
            chart.setChartData(csvData);
            chart.setGoal(goal);
            chart.setChartType(chartType);
            chart.setName(name);
            chart.setUserId(loginUser.getId());
            chart.setGenChart(chartOption);
            chart.setGenResult(conclusion);
            boolean save = chartService.save(chart);
            ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "数据图表保存失败");
            // 构造返回结果
            BiGenVO biGenVO = new BiGenVO();
            biGenVO.setChartOption(chartOption);
            biGenVO.setConclusion(conclusion);
            biGenVO.setSuccess(true);
            biGenVO.setGenChartByAIRequest(genChartByAIRequest); // 返回请求参数，便于调试
            return ResultUtils.success(biGenVO);
        } catch (Exception e) {
            log.error("AI生成图表失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI生成图表失败: " + e.getMessage());
        }
    }
}


