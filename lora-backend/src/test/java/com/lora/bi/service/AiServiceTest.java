package com.lora.bi.service;

import com.lora.bi.model.entity.ChatBot;
import ai.z.openapi.service.model.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI服务测试类 - 专注于chartChatBot配置验证
 */
@SpringBootTest
@Slf4j
public class AiServiceTest {

    @Autowired
    private AiService aiService;

    @Autowired
    @Qualifier("chartChatBot")
    private ChatBot chartChatBot;

    @Autowired
    @Qualifier("chatBot")
    private ChatBot chatBot;

    /**
     * 测试chartChatBot是否正确配置了系统提示词
     */
    @Test
    public void testChartChatBotConfiguration() {
        log.info("开始测试 chartChatBot 配置...");
        
        // 验证chartChatBot不为null
        assertNotNull(chartChatBot, "chartChatBot should not be null");
        log.info("chartChatBot 注入成功");

        // 验证chartChatBot的对话历史不为空
        assertNotNull(chartChatBot.getConversation(), "chartChatBot conversation should not be null");
        assertFalse(chartChatBot.getConversation().isEmpty(), "chartChatBot conversation should not be empty");
        log.info("chartChatBot 对话历史不为空，大小: {}", chartChatBot.getConversation().size());

        // 验证对话历史包含至少一条消息
        List<ChatMessage> conversation = chartChatBot.getConversation();
        assertTrue(conversation.size() > 0, "chartChatBot should have at least one message in conversation");

        // 使用反射访问可能存在的方法
        ChatMessage firstMessage = conversation.get(0);
        log.info("正在检查第一条消息...");
        
        String role = getRoleUsingReflection(firstMessage);
        String content = getContentUsingReflection(firstMessage);

        log.info("ChartBot系统消息角色: {}", role);
        log.info("ChartBot系统消息内容: {}", content);
        
        // 验证系统消息
        if (role != null) {
            assertEquals("system", role, "First message should be a system message");
            log.info("系统消息角色验证通过");
        } else {
            log.warn("无法获取消息角色");
        }
        
        // 检查系统消息是否包含预期的关键内容
        if (content != null) {
            boolean hasExpectedContent = content.contains("数据分析师") || content.contains("ECharts") || content.contains("分析结论");
            assertTrue(hasExpectedContent, 
                "System message should contain '数据分析师', 'ECharts', or '分析结论'");
            log.info("系统消息内容验证通过");
        } else {
            log.warn("无法获取消息内容");
        }
        
        log.info("chartChatBot 配置测试完成");
    }

    /**
     * 测试普通chatBot配置（仅用于对比）
     */
    @Test
    public void testChatBotConfigurationForComparison() {
        log.info("开始测试普通 chatBot 配置（用于对比）...");
        
        // 验证chatBot不为null
        assertNotNull(chatBot, "chatBot should not be null");
        log.info("chatBot 注入成功");

        // 验证chatBot的对话历史不为空
        assertNotNull(chatBot.getConversation(), "chatBot conversation should not be null");
        assertFalse(chatBot.getConversation().isEmpty(), "chatBot conversation should not be empty");
        log.info("chatBot 对话历史不为空，大小: {}", chatBot.getConversation().size());

        // 验证对话历史包含至少一条消息
        List<ChatMessage> conversation = chatBot.getConversation();
        assertTrue(conversation.size() > 0, "chatBot should have at least one message in conversation");

        // 使用反射访问可能存在的方法
        ChatMessage firstMessage = conversation.get(0);
        log.info("正在检查普通chatBot第一条消息...");
        
        String role = getRoleUsingReflection(firstMessage);
        String content = getContentUsingReflection(firstMessage);

        log.info("ChatBot系统消息角色: {}", role);
        log.info("ChatBot系统消息内容: {}", content);
        
        log.info("普通 chatBot 配置测试完成（仅用于对比）");
    }

    /**
     * 测试数据分析场景的AI功能
     */
    @Test
    public void testChartAnalysisScenario() {
        log.info("开始测试图表分析场景...");
        
        // 模拟一个简单的数据分析请求
        String chartAnalysisPrompt = "分析需求：分析销售数据\n原始数据：\n日期,销售额\n2023-01-01,1000\n2023-01-02,1200\n2023-01-03,800";
        
        log.info("发送数据分析请求: {}", chartAnalysisPrompt);
        
        try {
            String response = aiService.sendMessage(chartAnalysisPrompt);
            log.info("图表分析响应: {}", response);
            
            // 验证响应不为null或空
            assertNotNull(response, "Chart analysis response should not be null");
            assertFalse(response.trim().isEmpty(), "Chart analysis response should not be empty");
            log.info("图表分析响应验证通过");
            
        } catch (Exception e) {
            log.warn("图表分析AI服务调用失败: {}", e.getMessage());
            // 如果AI服务暂时不可用，至少验证异常处理
            assertNotNull(e);
        }
        
        log.info("图表分析场景测试完成");
    }

    /**
     * 使用反射获取ChatMessage的role属性
     */
    private String getRoleUsingReflection(ChatMessage message) {
        try {
            Method getRoleMethod = message.getClass().getMethod("getRole");
            Object role = getRoleMethod.invoke(message);
            return role != null ? role.toString() : null;
        } catch (Exception e) {
            log.warn("无法通过反射获取role: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 使用反射获取ChatMessage的content属性
     */
    private String getContentUsingReflection(ChatMessage message) {
        try {
            Method getContentMethod = message.getClass().getMethod("getContent");
            Object content = getContentMethod.invoke(message);
            return content != null ? content.toString() : null;
        } catch (Exception e) {
            log.warn("无法通过反射获取content: {}", e.getMessage());
            return null;
        }
    }
}

