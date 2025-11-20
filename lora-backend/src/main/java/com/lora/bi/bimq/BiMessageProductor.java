package com.lora.bi.bimq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;


@Component
public class BiMessageProductor {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BiConstant.EXCHANGE_NAME, BiConstant.BI_ROUTING_KEY,message);
    }


}
