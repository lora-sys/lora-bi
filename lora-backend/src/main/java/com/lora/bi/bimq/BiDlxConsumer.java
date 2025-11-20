package com.lora.bi.bimq;

import com.lora.bi.service.ChartService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class BiDlxConsumer {

    @Resource
    private ChartService chartService;

    @RabbitListener(queues = BiConstant.DLX_QUEUE_NAME, ackMode = "MANUAL")
    public void handleDeadLetter(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            long chartId = Long.parseLong(message);
            log.error("图表任务进入死信队列，chartId: {}", chartId);

            // 更新图表状态为失败
            chartService.updateChartStatus(chartId, "failed", "任务超时或处理失败");

            // 确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理死信消息失败: {}", e.getMessage(), e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("NACK失败: {}", ex.getMessage());
            }
        }
    }
}