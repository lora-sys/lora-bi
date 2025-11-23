package com.lora.bi.service.impl;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lora.bi.common.ErrorCode;
import com.lora.bi.constant.CommonConstant;
import com.lora.bi.exception.ThrowUtils;
import com.lora.bi.model.dto.chart.ChartQueryRequest;
import com.lora.bi.model.entity.User;
import com.lora.bi.service.ChartService;
import com.lora.bi.model.entity.Chart;
import com.lora.bi.mapper.ChartMapper;
import com.lora.bi.service.UserService;
import com.lora.bi.utils.SqlUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @author yanBingZhao
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2025-11-13 21:24:21
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart> implements ChartService {

    @Resource
    private RedissonClient redissonClient;
    @Autowired
    private UserService userService;

    /**
     * 存储缓存为查询图表添加缓存
     *
     * @param chartId
     * @return
     */
    @Override
    public Chart getChartWithCache(Long chartId) {
        // 构造缓存key
        String cacheKey = "chart:" + chartId;
        RBucket<Chart> bucket = redissonClient.getBucket(cacheKey);

        // 先从缓存获取
        Chart chart = bucket.get();
        if (chart != null) {
            return chart;
        }
        // 缓存未命中，查询数据库
        chart = this.getById(chartId);
        if (chart != null) {
            // 存入缓存，设置30分钟过期，防止雪崩
            bucket.set(chart, 30, TimeUnit.MINUTES);
        }
        return chart;

    }

    /**
     * 删除缓存
     *
     * @param chartId
     */
    @Override
    public void clearChartCache(Long chartId) {
        String cacheKey = "chart:" + chartId;
        RBucket<Chart> bucket = redissonClient.getBucket(cacheKey);
        bucket.delete();
    }

    @Override
    public Page<Chart> listChartByPageWithCache(ChartQueryRequest chartQueryRequest, HttpServletRequest request) {
        // 构造缓存key
        User loginUser = userService.getLoginUser(request);
        String cacheKey = "chart:page" + loginUser.getId() + ":" + JSONUtil.toJsonStr(chartQueryRequest);
        RBucket<String> bucket = redissonClient.getBucket(cacheKey);
        // 使用String类型存储序列化后的Page
        // 先从缓存获取
        String chartPageJson = bucket.get();
        if (StringUtils.isNotBlank(chartPageJson)) {
            return JSONUtil.toBean(chartPageJson, new TypeReference<Page<Chart>>() {
            }, false);
        }
        // 访问数据库
        long current = chartQueryRequest.getCurrent();
        long pageSize = chartQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR);
        Page<Chart> page = this.page(new Page<>(current, pageSize), getQueryWrapper(chartQueryRequest));
        // 存入缓存
        if (page != null) {
            bucket.set(JSONUtil.toJsonStr(page), 15, TimeUnit.MINUTES);
        }
        return page;
    }

    @Override
    public void clearChartListCache() {
        // 清理所有图表列表缓存
        Iterable<String> keys =
                redissonClient.getKeys().getKeysByPattern("chart:page:*");
        for (String key : keys) {
            redissonClient.getBucket(key).delete();
        }

    }

    private QueryWrapper<Chart> getQueryWrapper(ChartQueryRequest
                                                        chartQueryRequest) {
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
        queryWrapper.eq(StringUtils.isNotBlank(chartType), "chartType"
                , chartType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId",
                userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     * 解决如果出现刚开始更新数据库成功时候后，如果出现问题导致异常，两者的状态对不上，使用事务，要么都成功，要么都失败
     *
     * @param chartId
     * @param status
     * @param execMessage
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChartStatus(long chartId, String status, String execMessage) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus(status);
        chart.setExecMessage(execMessage);
        boolean result = this.updateById(chart);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "图表状态更新失败");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateChartWithAiResult(long chartId, String chartOption, String conclusion) {
        Chart chart = new Chart();
        chart.setId(chartId);
        chart.setStatus("succeed");
        chart.setGenChart(chartOption);
        chart.setGenResult(conclusion);
        chart.setExecMessage("AI图表生成成功");

        boolean result = this.updateById(chart);
        ThrowUtils.throwIf(!result, ErrorCode.SYSTEM_ERROR, "图表结果状态更新失败");
    }
}




