package com.lora.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.HashMap;
import java.util.Map;

public class DlxConsumer {
    private static final String DEAD_EXCHANGE_NAME = "dlx-direct_exchange"; // 死信交换机
    private static final String EXCHANGE_NAME = "direct2_exchange";

    public static void main(String[] argv) throws Exception {
        // 创建频道,交换机,声明交换机
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        // Important: prefer using policies over hardcoded x-arguments.
        Map<String, Object> args1 = new HashMap<String, Object>();
        args1.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
        args1.put("x-dead-letter-routing-key", "waibao");

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        // 创建队列, 随机分配 一个队列
        String queueName1 = "xiaodog_queue";
        channel.queueDeclare(queueName1, true, false, false, args1);
        channel.queueBind(queueName1, EXCHANGE_NAME, "xiaodog");


        // 绑定死信交换机
        Map<String, Object> args2 = new HashMap<String, Object>();
        args2.put("x-dead-letter-exchange", DEAD_EXCHANGE_NAME);
      // 绑定死信队列，转发规则
        args2.put("x-dead-letter-routing-key", "Boss");
       // 创建队列, 随机分配 一个队列
        String queueName2 = "xiaoop_queue";
        channel.queueDeclare(queueName2, true, false, false, args2);
        channel.queueBind(queueName2, EXCHANGE_NAME, "xiaoop");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");



        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            // 拒绝消息
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
            System.out.println(" [x] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
        });
        channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> {
        });
    }
}