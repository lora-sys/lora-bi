package com.lora.bi.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI响应解析工具类，解析ai的输出，返回理想需要值
 * 示例输出
 * <execute>
 * option = {
 * title: {
 * text: '示例图表'
 * },
 * tooltip: {},
 * xAxis: {
 * type: 'category',
 * data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
 * },
 * yAxis: {
 * type: 'value'
 * },
 * series: [
 * {
 * name: '销售额',
 * data: [150, 230, 224, 218, 135, 147, 260],
 * type: 'line'
 * }
 * ]
 * };
 * </execute>
 *
 *
 *
 */
@Slf4j
public class AiResponseParser {
    /*

     *  提取内容中指定标签的内容

     * @param response

     * @param TagName

     * @return

     */

    public static String extractContentByTag(String response, String TagName) {

        // 检查不为空

        if (StrUtil.isBlank(response) || StrUtil.isBlank(TagName)) {

            return null;

        }



        // 首先尝试匹配完整标签对，使用简单的字符串查找方法

        String startTag = "<" + TagName + ">";

        String endTag = "</" + TagName + ">";



        int startIndex = response.indexOf(startTag);

        if (startIndex != -1) {

            startIndex += startTag.length();

            int endIndex = response.indexOf(endTag, startIndex);

            if (endIndex != -1) {

                // 找到完整的标签对，返回内容

                return response.substring(startIndex, endIndex).trim();

            } else {

                // 没有找到结束标签，返回从开始标签到字符串末尾的内容

                log.warn("未找到结束标签: {}", endTag);

                return response.substring(startIndex).trim();

            }

        } else {

            // 如果没有找到完整标签对，尝试不同的策略

            if ("execute".equals(TagName)) {

                // 对于execute标签，尝试查找ECharts配置部分

                return extractEChartsConfig(response);

            } else if ("text".equals(TagName)) {

                // 对于text标签，尝试从可能的分析结论部分提取内容

                return extractAnalysisText(response);

            } else {

                // 没有找到匹配内容，返回null而不是抛出异常

                log.warn("未找到标签内容: {}", TagName);

                return null;

            }

        }

    }

    /**

     * 从AI响应中提取ECharts配置代码

     *

     * @param response AI响应内容

     * @return ECharts配置代码

     */

    private static String extractEChartsConfig(String response) {

        if (response == null) return null;

        // 查找option = { 开始的JavaScript对象

        int optionIndex = response.indexOf("option = {");

        if (optionIndex != -1) {

            // 从option = {开始查找，直到找到结束的大括号

            int braceCount = 0;

            boolean inString = false;

            char stringChar = 0;

            

            for (int i = optionIndex; i < response.length(); i++) {

                char c = response.charAt(i);

                

                // 处理字符串内的字符，避免将字符串内的引号计入

                if (!inString && (c == '"' || c == '\"')) {

                    inString = true;

                    stringChar = c;

                } else if (inString && c == stringChar) {

                    // 检查是否是转义字符

                    if (i > 0 && response.charAt(i-1) != '\\') {

                        inString = false;

                    }

                }

                

                // 只有在字符串外才计算大括号

                if (!inString) {

                    if (c == '{') {

                        braceCount++;

                    } else if (c == '}') {

                        braceCount--;

                        // 当大括号匹配完时，找到了结束位置

                        if (braceCount == 0) {

                            return response.substring(optionIndex, i + 1).trim();

                        }

                    }

                }

            }

            // 如果没有找到匹配的大括号，返回从option开始到字符串末尾的内容

            log.warn("只找到部分ECharts配置，可能不完整");

            return response.substring(optionIndex).trim();

        }



        log.warn("未找到ECharts配置代码");

        return null;

    }

    /**

     * 从AI响应中提取分析结论文本

     *

     * @param response AI响应内容

     * @return 分析结论文本

     */

    private static String extractAnalysisText(String response) {

        if (response == null) return null;



        // 查找 <text> 标签内的内容

        int textStartIndex = response.indexOf("<text>");

        if (textStartIndex != -1) {

            textStartIndex += 6; // "<text>"的长度

            int textEndIndex = response.indexOf("</text>", textStartIndex);

            if (textEndIndex != -1) {

                // 找到完整的text标签对

                return response.substring(textStartIndex, textEndIndex).trim();

            } else {

                // 没有找到结束标签，返回从开始标签到字符串末尾的内容

                log.warn("未找到</text>结束标签");

                return response.substring(textStartIndex).trim();

            }

        }



        // 如果没有找到 <text> 标签，尝试查找可能的分析结论部分

        // 查找包含"分析"、"结论"、"趋势"等关键词的内容

        String[] keywords = {"分析", "结论", "趋势", "数据"};

        for (String keyword : keywords) {

            int keywordIndex = response.indexOf(keyword);

            if (keywordIndex != -1) {

                // 从关键词位置向前查找，找到一个合适的起始位置

                int startIndex = Math.max(0, keywordIndex - 10);

                return response.substring(startIndex).trim();

            }

        }



        log.warn("未找到分析结论文本");

        return null;

    }

}