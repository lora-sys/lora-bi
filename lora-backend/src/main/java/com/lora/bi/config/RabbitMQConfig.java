package com.lora.bi.config;

import org.springframework.amqp.core.Queue;
import com.lora.bi.bimq.BiConstant;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.*;

/**
 * 场景：
 * 用户上传文件 → 消息进队列 → 消费者处理
 *     ↓
 * AI 调用卡住了（网络问题、API 慢）
 *     ↓
 * 5分钟后，消息自动进入死信队列
 *     ↓
 * 死信队列消费者：标记图表为 failed
 *
 *
 * 没有死信队列：
 * - 消息一直在队列里
 * - 图表状态永远是 running
 * - 用户不知道失败了
 *
 * 有死信队列：
 * - 5分钟后自动处理
 * - 图表状态变为 failed
 * - 用户知道失败，可以重试
 *
 *
 * 消费者处理消息时崩溃
 *     ↓
 * 消息被 NACK（拒绝）
 *     ↓
 * 如果配置了死信队列，消息进入死信队列
 *     ↓
 * 死信队列消费者：记录错误、标记失败
 *
 *
 * 好处：
 * - 不会无限重试（避免你之前遇到的 429 限流问题）
 * - 失败的消息有专门的地方处理
 * - 可以人工介入或自动补偿
 */
@Configuration
public class RabbitMQConfig {

 @Bean
    public Queue biQueue(){
     Map<String,Object> args = new HashMap<>();
       // 配置死信交换机
     args.put("x-dead-letter-exchange", BiConstant.EXCHANGE_NAME);
     args.put("x-dead-letter-routing-key",BiConstant.BI_ROUTING_KEY);

     // 消息超时5分钟，
     args.put("x-message-ttl",300000);
     return new Queue(BiConstant.BI_QUEUE_NAME,true,false,false,args);

 }
    @Bean
    public DirectExchange biExchange() {
        return new DirectExchange(BiConstant.EXCHANGE_NAME, true, false);
    }
    @Bean
    public Binding biBinding() {
        return BindingBuilder.bind(biQueue())
                .to(biExchange())
                .with(BiConstant.BI_ROUTING_KEY);
    }
    // 死信队列配置
    @Bean
    public Queue dlxQueue() {
        return new Queue(BiConstant.DLX_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(BiConstant.DLX_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue())
                .to(dlxExchange())
                .with(BiConstant.DLX_ROUTING_KEY);
    }

}
