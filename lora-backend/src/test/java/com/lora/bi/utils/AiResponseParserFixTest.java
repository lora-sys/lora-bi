package com.lora.bi.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AI响应解析器测试类 - 验证修复后的功能
 */
public class AiResponseParserFixTest {

    @Test
    public void testExtractContentByTagWithIncompleteText() {
        // 测试处理不完整标签的情况
        String aiResponse = "<execute>\noption = {\n  title: {\n    text: '网站数据图'\n  },\n  tooltip: {\n    trigger: 'axis'\n  },\n  xAxis: {\n    type: 'category',\n    data: ['1', '2'],\n    name: '日期'\n  },\n  yAxis: {\n    type: 'value',\n    name: '出现次数'\n  },\n  series: [\n    {\n      name: '小米',\n      data: [1, 1],\n      type: 'line',\n      smooth: true\n    }\n  ]\n};\n</execute>\n<text>\n本次分析旨在基于提供的\"网站数据图\"数据，探究\"小米\"在特定时间范围内的表现趋势。原始数据集非常精简，仅包含两个时间点（日期1和日期2）以及对应的\"名字\"信息。\n\n从数据解读和可视化呈现来看，我们将原始数据处理后绘制成折线图。图表的横轴（X轴）代表日期，纵轴（Y轴）代表\"小米\"的出现次数。通过图表可以直观地观察到，在日期1，\"小米\"的出现次数为1次；在日期2，其出现次数同样为1次。连接这两个数据点的折线呈水平状态，这表明在这两个连续的时间点内，\"小米\"的出现频率保持不变，没有发生任何波动。\n\n在趋势分析方面，基于当前仅有的两个数据点，我们可以得出的唯一结论是：\"小米\"的出现频率在从日期1到日期2这个极短的时间窗口内表现稳定，既没有显示出增长的势头，也没有出现下降的迹象。这种平稳的状态可能暗示在此期间，与\"小米\"相关的活动、用户关注度或记录频率处于一个相对恒定的水平。然而，这仅仅是基于两个孤立点的观察。\n\n必须着重指出，本次分析的结论具有极大的局限性，无法满足\"分析这几个月情况\"这一宏观目标。首先，数据样本量过小是核心问题。仅凭两个时间点的数据，我们无法洞察任何长期趋势，例如季节性波动、周期性变化或持续的增长/衰退模式。任何关于\"几个月情况\"的推断都将是缺乏根据的猜测。其次，数据的维度单一，缺乏上下文信息。我们只知道\"小米\"的出现次数，但这个指标的具体业务含义并不明确。它可能代表网站某个特定页面的访问量、某个产品的销量、某个关键词的搜索\n2025-11-15 15:38:19.065  WARN 22728 --- [.0-8101-exec-10] com.lora.bi.utils.AiResponseParser       : 未找到标签内容: text";

        String executeContent = AiResponseParser.extractContentByTag(aiResponse, "execute");
        String textContent = AiResponseParser.extractContentByTag(aiResponse, "text");

        assertNotNull(executeContent, "execute标签内容不应为空");
        assertNotNull(textContent, "text标签内容不应为空");

        System.out.println("提取的图表配置:");
        System.out.println(executeContent);
        System.out.println("\n提取的分析结论:");
        System.out.println(textContent);

        assertTrue(executeContent.contains("option = {"), "图表配置应包含option对象");
        assertTrue(textContent.contains("本次分析旨在"), "分析结论应包含相关信息");
        assertTrue(textContent.length() > 100, "分析结论应该有足够长度");
    }
}