package com.lora.bi.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * AI响应解析器测试类
 */
public class AiResponseParserTest {

    @Test
    public void testExtractContentByTag() {
        // 测试提取execute标签内容
        String aiResponse = "<execute>\noption = {\n  title: {\n    text: '示例图表'\n  },\n  tooltip: {},\n  xAxis: {\n    type: 'category',\n    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']\n  },\n  yAxis: {\n    type: 'value'\n  },\n  series: [\n    {\n      name: '销售额',\n      data: [150, 230, 224, 218, 135, 147, 260],\n      type: 'line'\n    }\n  ]\n};\n</execute>\n<text>\n这是一个数据分析结论示例。根据销售数据，我们可以看到周三销售额最高，周六销售额最低。建议在周三增加库存，在周六进行促销活动。\n</text>";

        String executeContent = AiResponseParser.extractContentByTag(aiResponse, "execute");
        String textContent = AiResponseParser.extractContentByTag(aiResponse, "text");

        assertNotNull(executeContent, "execute标签内容不应为空");
        assertNotNull(textContent, "text标签内容不应为空");

        System.out.println("提取的图表配置:");
        System.out.println(executeContent);
        System.out.println("\n提取的分析结论:");
        System.out.println(textContent);

        assertTrue(executeContent.contains("option = {"), "图表配置应包含option对象");
        assertTrue(textContent.contains("数据分析结论"), "分析结论应包含相关信息");
    }

    @Test
    public void testExtractContentByTagWithMissingTag() {
        // 测试当标签不存在时的情况
        String aiResponse = "这是一个没有标签的普通文本";

        String executeContent = AiResponseParser.extractContentByTag(aiResponse, "execute");
        String textContent = AiResponseParser.extractContentByTag(aiResponse, "text");

        assertNull(executeContent, "execute标签内容应为null");
        assertNull(textContent, "text标签内容应为null");
    }

    @Test
    public void testExtractContentByTagWithEmptyInput() {
        // 测试空输入
        String executeContent = AiResponseParser.extractContentByTag(null, "execute");
        String textContent = AiResponseParser.extractContentByTag("", "text");

        assertNull(executeContent, "空输入时execute标签内容应为null");
        assertNull(textContent, "空输入时text标签内容应为null");
    }

    @Test
    public void testExtractContentByTagWithMultipleTags() {
        // 测试多个相同标签时是否只提取第一个
        String aiResponse = "<execute>第一个内容</execute>\n其他内容\n<execute>第二个内容</execute>";

        String executeContent = AiResponseParser.extractContentByTag(aiResponse, "execute");

        assertNotNull(executeContent, "应能提取到内容");
        assertEquals("第一个内容", executeContent, "应只提取第一个标签的内容");
    }
}