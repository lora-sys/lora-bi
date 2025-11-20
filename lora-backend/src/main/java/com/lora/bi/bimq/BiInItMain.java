package com.lora.bi.bimq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BiInItMain {


    public static void main(String[] args) {

        // 创建频道,交换机,声明交换机
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = BiConstant.EXCHANGE_NAME;
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");


            // 创建队列, 随机分配 一个队列
            String queueName1 = BiConstant.BI_QUEUE_NAME;
            channel.queueDeclare(queueName1, true, false, false, null);
            channel.queueBind(queueName1, EXCHANGE_NAME, BiConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
