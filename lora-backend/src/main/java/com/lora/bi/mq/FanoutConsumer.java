package com.lora.bi.mq;
import com.rabbitmq.client.*;


public class FanoutConsumer {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel1 = connection.createChannel();
        Channel channel2 = connection.createChannel();
        // 声明交换机
        //queueDeclare() 声明队列，参数为 (名称, 持久化, 独占, 自动删除, 参数)
        channel1.exchangeDeclare(EXCHANGE_NAME, "fanout");
//        channel2.exchangeDeclare(EXCHANGE_NAME, "fanout");
        String queueName1 = "xiaoli_queue1"; // 获取当前队列，随机分配
        queueName1 = channel1.queueDeclare().getQueue();
        channel1.queueBind(queueName1, EXCHANGE_NAME, "");
        // 监听
        String queueName2= "xiaoli_queue2";
        queueName2 = channel2.queueDeclare().getQueue();
        channel2.queueBind(queueName2, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback1 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小王] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        DeliverCallback deliverCallback2 = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [小李] Received '" +
                    delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };
        channel1.basicConsume(queueName1, true, deliverCallback1, consumerTag -> { });
        channel2.basicConsume(queueName2, true, deliverCallback2, consumerTag -> { });
    }
}