package com.lora.bi.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lora.bi.model.entity.Chart;
import com.lora.bi.service.AiService;
import com.lora.bi.service.ChartService;
import com.lora.bi.utils.AiResponseParser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@Component
@Slf4j
public class ChartRetryTask {
    @Autowired
    private ChartService chartService;

    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    private AiService aiService;

    @Scheduled(fixedRate = 30000) // 每5分钟执行一次
    public void retryFailedCharts() {
        // 查询状态为"failed"的重试次数小于3次的图表
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<Chart>();
        queryWrapper.eq("status", "failed")
                .lt("retry_num", 3)
                .orderByAsc("createTime");

        List<Chart> fieldCharts = chartService.list(queryWrapper);


        for (Chart chart : fieldCharts) {
            try {
                // 提交到线程池重试
                CompletableFuture.runAsync(() -> {

                    retryFailedChartsGeneration(chart);

                }, threadPoolExecutor);
                // 更新状态为重试中
                Chart updateChart = new Chart();
                updateChart.setId(chart.getId());
                updateChart.setStatus("wait");
                updateChart.setRetryNum(chart.getRetryNum() + 1); // 增加重试次数
                chartService.updateById(updateChart);
                log.info("已经提交重试任务，chartId: {}", chart.getId());
            } catch (RejectedExecutionException e) {
                log.error("提交重试任务失败，线程池已经满，chartId:{}", chart.getId());
            }


        }

    }

    private void retryFailedChartsGeneration(Chart chart) {
        try {
            // 更新状态为运行中，
            Chart runningChart = new Chart();
            runningChart.setId(chart.getId());
            runningChart.setStatus("running");
            runningChart.setExecMessage("重试中");
            chartService.updateById(runningChart);

            // 调用AI服务，使用配置好的chartChatBot进行数据分析


            String userInput = "分析需求:" + chart.getGoal() + "\n原始数据:\n" + chart.getChartData();
            String aiResult = aiService.sendMessage(String.valueOf(userInput));
            String chartOption = AiResponseParser.extractContentByTag(aiResult, "execute");
            String conclusion = AiResponseParser.extractContentByTag(aiResult, "text");
            // 检查解析结果是否有效
            if (StringUtils.isNotBlank(chartOption) && StringUtils.isNotBlank(conclusion)) {

                // 更新图表信息
                Chart updateChart = new Chart();
                updateChart.setId(chart.getId());
                updateChart.setStatus("succeed");
                updateChart.setGenChart(chartOption);
                updateChart.setGenResult(conclusion);
                updateChart.setExecMessage("重试成功");

                chartService.updateById(updateChart);


            } else {
                handleChartUpdateError(chart.getId(), "重试后ai仍然还是失败");

            }

        } catch (Exception e) {
            log.error("重试图表生成失败,chartId:{}", chart.getId(), e);
            handleChartUpdateError(chart.getId(), "重试失败:" + e.getMessage());
        }

    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("failed");
        updateChart.setExecMessage(execMessage);
        boolean updateResult =
                chartService.updateById(updateChart);
        if (!updateResult) {
            log.error("更新图表失败状态失败，chartId: {}, 错误信息: {}", chartId, execMessage);
        }
    }
}





