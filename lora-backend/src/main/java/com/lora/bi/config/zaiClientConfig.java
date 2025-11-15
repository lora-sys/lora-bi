package com.lora.bi.config;

import ai.z.openapi.ZaiClient;
import com.lora.bi.model.entity.ChatBot;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "zai.client")
@Data
public class zaiClientConfig {
    /**
     * apiKey
     */
    private String apiKey;
     /**
      * model
      */
     private String model;
      /**
       * temperature
       */
      private Double temperature;
       /**
        * maxTokens
        */
       private Integer maxTokens;
       /**
        * baseUrl
        */
       private String baseUrl;
       /**
        * system message for AI
        */
       private String systemMessage = "你是一个友好的 AI 助手";
       
       /**
        * chart system message for AI
        */
       @Value("${ai.system.chart-prompt}")
       private String chartSystemMessage;

       @Bean
    public ZaiClient zaiClient() {
           return  ZaiClient.builder()
                   .apiKey(apiKey)
                   .baseUrl(baseUrl)
                   .enableTokenCache()
                   .tokenExpire(3600000) // 1 hour
                   .connectionPool(10, 5, TimeUnit.MINUTES)
                   .build();
       }
       
       @Bean
       public ChatBot chatBot(ZaiClient zaiClient) {
           return new ChatBot(zaiClient, systemMessage);
       }
       
       @Bean("chartChatBot")
       public ChatBot chartChatBot(ZaiClient zaiClient) {
           return new ChatBot(zaiClient, chartSystemMessage);
       }
}