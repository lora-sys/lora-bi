package com.lora.bi.model.dto.chart;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建请求
 *
 * @author lora
 *
 */
@Data
public class GenChartByAIRequest implements Serializable {

    /**
     * 图表名称
     */
    private String name;


    /**
     * 分析目标
     */
    private String goal;

    /**
     * 图表类型
     */
    private String chartType;



    private static final long serialVersionUID = 1L;
}