package com.lora.bi.bimq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


//@Component
public class MyMessageProductor {

    @Resource
    private RabbitTemplate rabbitTemplate;


    public void sendMessage(String exchange, String routingKey, String message) {

        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }

}
