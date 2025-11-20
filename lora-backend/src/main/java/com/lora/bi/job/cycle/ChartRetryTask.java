package com.lora.bi.job.cycle;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lora.bi.bimq.BiMessageProductor;
import com.lora.bi.model.entity.Chart;
import com.lora.bi.service.AiService;
import com.lora.bi.service.ChartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
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
    @Autowired
    private BiMessageProductor biMessageProductor;

    @Scheduled(fixedRate = 300000) // 每5分钟执行一次
    public void retryFailedCharts() {
        // 查询状态为"failed"的重试次数小于3次的图表
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<Chart>();
        queryWrapper.eq("status", "failed")
                .lt("retry_num", 3)
                .orderByAsc("createTime");

        List<Chart> failedCharts = chartService.list(queryWrapper);
        //如果更新成功，但发送消息失败，图表会卡在 wait 状态。
        // 遍历每个失败的图表
        for (Chart chart : failedCharts) {
            try {
                // 更新状态
                Chart updateChart = new Chart();
                updateChart.setId(chart.getId());
                updateChart.setStatus("wait");
                updateChart.setRetryNum(chart.getRetryNum() + 1);
                boolean updated = chartService.updateById(updateChart);

                if (!updated) {
                    log.error("更新图表状态失败，chartId: {}", chart.getId());
                    continue; // 跳过这个，处理下一个
                }

                // 发送消息到队列
                biMessageProductor.sendMessage(String.valueOf(chart.getId()));
                log.info("已提交重试任务到队列，chartId: {}", chart.getId());

            } catch (Exception e) {
                log.error("提交重试任务失败，chartId: {}", chart.getId(), e);
                // 回滚状态（可选）
                try {
                    Chart rollback = new Chart();
                    rollback.setId(chart.getId());
                    rollback.setStatus("failed");
                    chartService.updateById(rollback);
                } catch (Exception ex) {
                    log.error("回滚状态失败: {}", ex.getMessage());
                }
            }
        }
    }

//    private void retryFailedChartsGeneration(Chart chart) {
//        try {
//            // 更新状态为运行中，
//            Chart runningChart = new Chart();
//            runningChart.setId(chart.getId());
//            runningChart.setStatus("running");
//            runningChart.setExecMessage("重试中");
//            chartService.updateById(runningChart);
//
//            // 调用AI服务，使用配置好的chartChatBot进行数据分析
//
//
//            String userInput = "分析需求:" + chart.getGoal() + "\n原始数据:\n" + chart.getChartData();
//            String aiResult = aiService.sendMessage(String.valueOf(userInput));
//            String chartOption = AiResponseParser.extractContentByTag(aiResult, "execute");
//            String conclusion = AiResponseParser.extractContentByTag(aiResult, "text");
//            // 检查解析结果是否有效
//            if (StringUtils.isNotBlank(chartOption) && StringUtils.isNotBlank(conclusion)) {
//
//                // 更新图表信息
//                Chart updateChart = new Chart();
//                updateChart.setId(chart.getId());
//                updateChart.setStatus("succeed");
//                updateChart.setGenChart(chartOption);
//                updateChart.setGenResult(conclusion);
//                updateChart.setExecMessage("重试成功");
//
//                chartService.updateById(updateChart);
//
//
//            } else {
//                handleChartUpdateError(chart.getId(), "重试后ai仍然还是失败");
//
//            }
//
//        } catch (Exception e) {
//            log.error("重试图表生成失败,chartId:{}", chart.getId(), e);
//            handleChartUpdateError(chart.getId(), "重试失败:" + e.getMessage());
//        }
//
//    }

//    private void handleChartUpdateError(long chartId, String execMessage) {
//        Chart updateChart = new Chart();
//        updateChart.setId(chartId);
//        updateChart.setStatus("failed");
//        updateChart.setExecMessage(execMessage);
//        boolean updateResult =
//                chartService.updateById(updateChart);
//        if (!updateResult) {
//            log.error("更新图表失败状态失败，chartId: {}, 错误信息: {}", chartId, execMessage);
//        }
//    }
}





