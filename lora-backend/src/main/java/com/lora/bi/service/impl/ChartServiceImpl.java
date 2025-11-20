package com.lora.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lora.bi.common.ErrorCode;
import com.lora.bi.exception.ThrowUtils;
import com.lora.bi.service.ChartService;
import com.lora.bi.model.entity.Chart;
import com.lora.bi.mapper.ChartMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yanBingZhao
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2025-11-13 21:24:21
 */
@Service
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {
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




