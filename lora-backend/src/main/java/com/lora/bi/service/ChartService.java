package com.lora.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lora.bi.model.entity.Chart;

/**
* @author yanBingZhao
* @description 针对表【chart(图表信息表)】的数据库操作Service
* @createDate 2025-11-13 21:24:21
*/
public interface ChartService extends IService<Chart> {
    /**
     *   提升查询图表的速度
     * @param chartId
     * @return
     */
    Chart genChartWithCache(Long chartId);

    /**
     *  清理缓存速度
     */
    void clearChartCache(Long chartId);



    /**
     *  带事务办法 更新图表状态
      * @param chartId
     * @param status
     * @param execMessage
     */
    void updateChartStatus(long chartId,String status,String execMessage );

    /**
     *  带事务办法，更新图表结果状态
     * @param chartId
     * @param chartOption
     * @param conclusion
     */
    void updateChartWithAiResult(long chartId,String chartOption,String conclusion );
}
