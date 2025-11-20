package com.lora.bi.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.util.Scanner;

public class DlxProductor {

    private static final String DEAD_EXCHANGE_NAME = "dlx-direct_exchange"; // 死信交换机
    private static final String WORK_EXCHANGE_NAME = "direct2_exchange";
    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明死信交换机
            channel.exchangeDeclare(DEAD_EXCHANGE_NAME, "direct");
            // 创建死信队列

            String queueName1 = "boss_dlx_queue";
            channel.queueDeclare(queueName1, true, false, false, null);
            channel.queueBind(queueName1, DEAD_EXCHANGE_NAME, "Boss");

            String queueName2 = "waibao_dlx_queue";
            channel.queueDeclare(queueName2, true, false, false, null);
            channel.queueBind(queueName2, DEAD_EXCHANGE_NAME, "waibao");


            DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                // 拒绝消息
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                System.out.println(" [boss] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };
            DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                System.out.println(" [waibao] Received '" +
                        delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
            };
            channel.basicConsume(queueName1, false, deliverCallback1, consumerTag -> {
            });
            channel.basicConsume(queueName2, false, deliverCallback2, consumerTag -> {
            });

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String userInput = scanner.nextLine();
                String[] strings = userInput.split(" ");
                if (strings.length < 1) {
                    continue;
                }
                String message = strings[0];
                String routingKey = strings[1];

                // 作为生产者, 创建频道连接交换机发送消息
                channel.basicPublish(WORK_EXCHANGE_NAME, routingKey, null, message.getBytes("UTF-8"));
                System.out.println("[x] sent " + " " + message + "with rout1ing key " + routingKey + " ");
            }
        }
    }
    //..
}














