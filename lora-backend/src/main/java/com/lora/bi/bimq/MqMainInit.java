package com.lora.bi.bimq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 *  创建测试环境用的交换机和队列 只用执行一次，程序运行前
 */
public class MqMainInit {

    public static void main(String[] args) {

        // 创建频道,交换机,声明交换机
        try{
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            String EXCHANGE_NAME = "test_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");


            // 创建队列, 随机分配 一个队列
            String queueName1 = "code_queue";
            channel.queueDeclare(queueName1, true, false, false, null);
            channel.queueBind(queueName1, EXCHANGE_NAME, "myRoutingKey");
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
