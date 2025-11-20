package com.lora.bi.mq;

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;
import retrofit2.http.Header;

import javax.annotation.Resource;
import java.io.IOException;

/**
 *  Producer -routing_key:"error"→ Direct Exchange → Queue1 (error logs)
 * Producer -routing_key:"info"→  Direct Exchange → Queue2 (info logs)
 *
 * Direct 交换机
 * 日志级别路由：error、warn、info 分别发送到不同队列处理
 * 任务分发：不同类型任务（email、sms、push）到不同处理队列
 * 条件路由：根据用户等级、地区等条件路由消息
 * RPC 模式：请求-应答通信
 */
public class DirectConsumer {
    private static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] argv) throws Exception {
        // 创建频道,交换机,声明交换机

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 创建队列, 随机分配 一个队列
        String queueName1 = "xiaowang_queue";
        channel.queueDeclare(queueName1, true, false, false, null);
        channel.queueBind(queueName1, EXCHANGE_NAME, "xiaowang");
        // 创建队列, 随机分配 一个队列
        String queueName2 = "xiaoLi_queue";
        channel.queueDeclare(queueName2, true, false, false, null);
        channel.queueBind(queueName2, EXCHANGE_NAME, "xiaoLi");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");



        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName1, true, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, true, deliverCallback2, consumerTag -> {
        });
    }
}