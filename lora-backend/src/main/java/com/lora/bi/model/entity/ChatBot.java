package com.lora.bi.model.entity;

import ai.z.openapi.ZaiClient;
import ai.z.openapi.service.model.ChatMessage;
import ai.z.openapi.service.model.ChatMessageRole;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatBot实体类，用于管理与AI的对话
 */
@Data
public class ChatBot {
    private final ZaiClient client;
    private final List<ChatMessage> conversation;

    public ChatBot(ZaiClient zaiClient, String systemMessage) {
        this.client = zaiClient;
        this.conversation = new ArrayList<>();
        
        // 添加系统消息
        this.conversation.add(ChatMessage.builder()
            .role(ChatMessageRole.SYSTEM.value())
            .content(systemMessage)
            .build());
    }
}