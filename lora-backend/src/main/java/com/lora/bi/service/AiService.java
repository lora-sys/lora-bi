package com.lora.bi.service;

import reactor.core.publisher.Flux;

/**
 * AI 服务接口
 *
 * @author lora
 */
public interface AiService {
    
    /**
     * 发送消息到 AI（非流式）
     *
     * @param prompt 提示词
     * @return 响应内容
     */
    String sendMessage(String prompt);
    
    /**
     * 发送消息到 AI（流式）
     *
     * @param prompt 提示词
     * @return 流式响应
     */
    Flux<String> sendMessageStream(String prompt);
    
    /**
     * 生成图像
     *
     * @param prompt 图像生成的提示词
     * @return 生成的图像URL
     */
    String generateImage(String prompt);
}