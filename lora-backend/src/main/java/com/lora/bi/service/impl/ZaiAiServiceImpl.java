package com.lora.bi.service.impl;



import ai.z.openapi.ZaiClient;

import ai.z.openapi.service.model.*;

import ai.z.openapi.service.image.CreateImageRequest;

import ai.z.openapi.service.image.ImageResponse;

import ai.z.openapi.core.Constants;


import com.lora.bi.model.entity.ChatBot;

import com.lora.bi.service.AiService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Qualifier;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;



import java.util.ArrayList;

import java.util.List;

import java.util.concurrent.TimeUnit;

/**
 * z.ai服务实现类
 *
 * @author lora
 */
@Service
@Slf4j
public class ZaiAiServiceImpl implements AiService {

    @Autowired
    private ZaiClient zaiClient;
    
    @Autowired
    @Qualifier("chartChatBot")
    private ChatBot chartChatBot;

    @Value("${zai.client.model}")
    private String model;

    @Value("${zai.client.temperature}")
    private Double temperature;

    @Value("${zai.client.max-tokens}")
    private Integer maxTokens;

    /**
     * 发送消息到z.ai（非流式）
     *
     * @param prompt 提示词
     * @return 响应内容
     */
    @Override
    public String sendMessage(String prompt) {
        try {
            // 记录发送给AI的完整提示词，便于调试和优化
            log.info("发送给AI的提示词: {}", prompt);
            
            // 获取聊天机器人的对话历史
            List<ChatMessage> messages = new ArrayList<>(chartChatBot.getConversation());
            // 添加用户消息
            messages.add(ChatMessage.builder()
                    .role(ChatMessageRole.USER.value())
                    .content(prompt)
                    .build());

            // 创建聊天请求
            ChatCompletionCreateParams request = ChatCompletionCreateParams.builder()
                    .model(model)
                    .messages(messages)
                    .stream(false)
                    .temperature(temperature.floatValue())
                    .maxTokens(maxTokens)
                    .build();

            // 执行请求
            log.info("正在调用AI服务...");
            ChatCompletionResponse response = zaiClient.chat().createChatCompletion(request);
            log.info("AI服务调用完成");

            if (response.isSuccess()) {

                String content = response.getData().getChoices().get(0).getMessage().getContent().toString();

                // 检查内容是否为空

                if (content == null || content.trim().isEmpty()) {

                    log.error("AI返回了空内容，响应状态为成功但内容为空");

                    throw new RuntimeException("AI返回了空内容，请稍后重试");

                }

                // 记录AI响应

                log.info("AI响应内容长度: {}", content.length());

                log.debug("AI响应内容: {}", content);

                return content;

            } else {

                log.error("调用z.ai API失败: " + response.getMsg());

                throw new RuntimeException("调用z.ai API失败: " + response.getMsg());

            }
        } catch (Exception e) {
            log.error("调用z.ai API失败", e);
            throw new RuntimeException("调用z.ai API失败: " + e.getMessage());
        }
    }

    /**
     * 发送消息到z.ai（流式）
     *
     * @param prompt 提示词
     * @return 流式响应
     *  不是原来的rxjava 而使用springwebflux ， 所以返回值是Flux<String>
     */
    @Override
    public Flux<String> sendMessageStream(String prompt) {
        return Flux.create(sink -> {
            try {
                // 记录发送给AI的完整提示词，便于调试和优化
                log.info("发送给AI的提示词(流式): {}", prompt);
                
                // 获取聊天机器人的对话历史
                List<ChatMessage> messages = new ArrayList<>(chartChatBot.getConversation());
                // 添加用户消息
                messages.add(ChatMessage.builder()
                        .role(ChatMessageRole.USER.value())
                        .content(prompt)
                        .build());

                // 创建流式聊天请求
                ChatCompletionCreateParams streamRequest = ChatCompletionCreateParams.builder()
                        .model(model)
                        .messages(messages)
                        .stream(true) // 启用流式
                        .temperature(temperature.floatValue())
                        .maxTokens(maxTokens)
                        .build();

                // 执行流式请求
                ChatCompletionResponse response = zaiClient.chat().createChatCompletion(streamRequest);

                if (response.isSuccess() && response.getFlowable() != null) {
                    response.getFlowable().subscribe(
                            data -> {
                                // 处理流式数据块
                                if (data.getChoices() != null && !data.getChoices().isEmpty()) {
                                    Delta delta = data.getChoices().get(0).getDelta();
                                    if (delta != null) {
                                        // 将Delta对象转换为字符串
                                        String content = delta.toString();
                                        sink.next(content);
                                    }
                                }
                            },
                            error -> {
                                log.error("流式请求错误: " + error.getMessage());
                                sink.error(new RuntimeException("流式请求错误: " + error.getMessage()));
                            },
                            () -> {
                                log.info("流式请求完成");
                                sink.complete();
                            }
                    );
                } else {
                    log.error("调用z.ai API失败: " + response.getMsg());
                    sink.error(new RuntimeException("调用z.ai API失败: " + response.getMsg()));
                }
            } catch (Exception e) {
                log.error("调用z.ai API失败", e);
                sink.error(new RuntimeException("调用z.ai API失败: " + e.getMessage()));
            }
        });
    }
    
    /**
     * 生成图像
     * @param prompt 图像生成的提示词
     * @return 生成的图像URL
     */
    @Override
    public String generateImage(String prompt) {
        try {
            // 图像生成
            CreateImageRequest request = CreateImageRequest.builder()
                    .model(Constants.ModelCogView3) // 使用 CogView3 模型进行图像生成
                    .prompt(prompt)
                    .size("1024x1024")
                    .build();

            ImageResponse response = zaiClient.images().createImage(request);

            if (response.isSuccess()) {
                String imageUrl = response.getData().getData().get(0).getUrl();
                log.info("生成的图像 URL: " + imageUrl);
                return imageUrl;
            } else {
                log.error("图像生成失败: " + response.getMsg());
                throw new RuntimeException("图像生成失败: " + response.getMsg());
            }
        } catch (Exception e) {
            log.error("调用图像生成API失败", e);
            throw new RuntimeException("调用图像生成API失败: " + e.getMessage());
        }
    }
}