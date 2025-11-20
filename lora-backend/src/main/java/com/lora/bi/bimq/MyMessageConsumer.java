package com.lora.bi.bimq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 *   1. 消息到达 code_queue
 *    ↓
 * 2. Spring 调用 receiveMessage()
 *    ↓
 * 3. 获取 deliveryTag (投递标签)
 *    ↓
 * 4. 处理业务逻辑
 *    ↓
 * 5. 成功 → channel.basicAck() 确认
 *    失败 → channel.basicNack() 拒绝 + 重新入队
 */
//@Component
@Slf4j
public class MyMessageConsumer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @SneakyThrows
    @RabbitListener(queues = "code_queue", ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {

        log.info("revive message:{}", message);

        channel.basicAck(deliveryTag, false);
    }


}

