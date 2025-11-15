package com.lora.bi.service;

import com.lora.bi.model.entity.ChatBot;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AI服务简单测试类
 */
@SpringBootTest
@Slf4j
public class SimpleAiServiceTest {

    @Autowired
    private AiService aiService;

    @Autowired
    @Qualifier("chartChatBot")
    private ChatBot chartChatBot;

    /**
     * 测试AI服务是否能正常响应
     */
    @Test
    public void testAiServiceSimpleResponse() {
        log.info("开始测试AI服务简单响应...");
        
        try {
            String testPrompt = "你好，请简单介绍一下你自己，只需要一句话。";
            log.info("发送测试提示词: {}", testPrompt);
            
            String response = aiService.sendMessage(testPrompt);
            log.info("AI响应: {}", response);
            
            // 验证响应不为null或空
            assertNotNull(response, "AI response should not be null");
            assertFalse(response.trim().isEmpty(), "AI response should not be empty");
            log.info("AI服务简单响应测试通过");
            
        } catch (Exception e) {
            log.error("AI服务简单响应测试失败", e);
            fail("AI服务调用失败: " + e.getMessage());
        }
    }
}