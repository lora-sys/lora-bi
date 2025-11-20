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
