package com.lora.bi.model.vo;



import lombok.Data;



import java.io.Serializable;



/**

 * BI 智能分析返回值对象

 *

 * @author lora

 */

@Data
public class BiGenVO implements Serializable {



    /**

     * 生成的图表配置（ECharts配置）

     */

    private String chartOption;



    /**

     * 生成的分析结论

     */

    private String conclusion;



    /**

     * 是否生成成功

     */

    private Boolean success;



    /**

     * 原始请求参数（用于调试）

     */

    private Object genChartByAIRequest;



    private static final long serialVersionUID = 1L;

}
