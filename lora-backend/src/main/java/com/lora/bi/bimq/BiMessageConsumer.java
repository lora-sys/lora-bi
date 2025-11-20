package com.lora.bi.bimq;

import com.lora.bi.common.ErrorCode;
import com.lora.bi.exception.BusinessException;
import com.lora.bi.model.entity.Chart;
import com.lora.bi.service.AiService;
import com.lora.bi.service.ChartService;
import com.lora.bi.service.UserService;
import com.lora.bi.utils.AiResponseParser;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;


//类级别的 @Transactional 不会对 @RabbitListener 方法生效！
//
//原因：
//- @RabbitListener 是由 RabbitMQ 容器直接调用的
//- 不经过 Spring 的代理，事务拦截器无法生效
//- 需要把事务逻辑**提取到单独的方法**中

/**
 * 消息队列消费者
 * 完整的消息队列处理流程：
 * <p>
 * 用户上传文件
 * ↓
 * Controller 保存图表（状态：wait）
 * ↓
 * 发送消息到 RabbitMQ 队列
 * ↓
 * Consumer 接收消息
 * ↓
 * 更新状态为 running（事务保护）
 * ↓
 * 调用 AI 分析（异常捕获）
 * ↓
 * 校验返回结果
 * ↓
 * 更新状态为 succeed/failed（事务保护）
 * ↓
 * ACK 消息
 * ↓
 * 定时任务（5分钟）检查失败任务
 * ↓
 * 重新发送到队列（最多重试3次
 */
@Component
@Slf4j
public class BiMessageConsumer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private UserService userService;

    @Resource
    private ChartService chartService;
    @Resource
    private AiService aiService;

    @SneakyThrows
    @RabbitListener(queues = BiConstant.BI_QUEUE_NAME, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info(" 消费者接受到消息:{}", message);
        if (StringUtils.isBlank(message)) {
            // 消息拒绝
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
        }
        long chartId = Long.parseLong(message);
        Chart chart = chartService.getById(chartId);
        if (chart == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表为空");
        }
        // 防止前面的状态更新成功了，结果再ai失败时候更新状态又失败了，必须抛出异常！！
        try {
            chartService.updateChartStatus(chart.getId(), "running", "图表正在生成中");
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
            try {
                chartService.updateChartStatus(chart.getId(), "failed", "更新图表执行 中状态失败");
            } catch (Exception ex) {
                log.error("更新失败状态失败: {}", ex.getMessage());
            }


            return;
        }

//        - AI 失败后，又更新为 failed（新事务）
//        - 如果第二次更新失败，图表会永远卡在 running 状态

        // 调用AI服务，使用配置好的chartChatBot进行数据分析
        String aiResult;
        try {
            aiResult = aiService.sendMessage(buildUserInput(chart));
        } catch (Exception e) {
            log.error("ai调用失败，chartId:{},错误:{}", chart.getId(), e.getMessage());
            // 拒绝消息，不重新入队，拒绝重试
            channel.basicNack(deliveryTag, false, false);// 第三个参数，不重新入队,避免无限重试
            try {
                chartService.updateChartStatus(chart.getId(), "failed", "ai调用失败:" + e.getMessage());
            } catch (Exception ex) {
                log.error("更新图表执行中状态失败: {}", ex.getMessage());
            }

            return;
        }

//        // 记录AI原始响应的前500个字符，便于调试
//        if (aiResult != null && aiResult.length() > 0) {
//            String preview = aiResult.length() > 500 ? aiResult.substring(0, 500) + "..." : aiResult;
//            log.info("AI原始响应预览: {}", preview);
//        }
//        log.info("AI原始响应长度: {}", aiResult != null ? aiResult.length() : 0);
//        log.debug("AI原始响应内容: {}", aiResult);
//        // 检查AI是否返回了有效内容
//        if (aiResult == null || aiResult.trim().isEmpty()) {
//            log.error("AI返回了空内容，可能API调用失败或超时");
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI服务未返回有效内容，请稍后重试");
//        }
        // 解析AI响应，提取图表配置和分析结论
        String chartOption = AiResponseParser.extractContentByTag(aiResult, "execute");
        String conclusion = AiResponseParser.extractContentByTag(aiResult, "text");

        if (StringUtils.isBlank(chartOption)) {
            log.error("AI返回格式错误，未提取到图表配置");
            channel.basicNack(deliveryTag, false, false);
            try {
                chartService.updateChartStatus(chart.getId(), "failed", "AI返回格式错 误");
            } catch (Exception ex) {
                log.error("更新失败状态失败: {}", ex.getMessage());
            }
            return;
        }


//        log.info("提取的图表配置: {}", chartOption);
//        log.info("提取的分析结论: {}", conclusion);
//        log.info("提取的图表配置长度: {}", chartOption != null ? chartOption.length() : 0);
//        log.info("提取的分析结论长度: {}", conclusion != null ? conclusion.length() : 0);

//        // 检查解析结果是否有效
//        if (chartOption == null || chartOption.trim().isEmpty()) {
//            log.error("未能从AI响应中提取到有效的图表配置");
//            throw new BusinessException(ErrorCode.OPERATION_ERROR, "AI未能生成有效的图表配置，请检查输入数据或稍后重试");
//        }
//        if (conclusion == null || conclusion.trim().isEmpty()) {
//            log.warn("未能从AI响应中提取到分析结论");
//            // 这里我们不抛出异常，因为图表配置是主要的，分析结论是次要的
//        }
        // 更新图表信息
        try {
            chartService.updateChartWithAiResult(chart.getId(), chartOption, conclusion);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
            //如果这次更新失败，会抛异常导致消息没有 ACK
            try {
                chartService.updateChartStatus(chart.getId(), "failed", "更新图表成功状态失败");
            } catch (Exception ex) {
                log.error("更新失败状态失败: {}", ex.getMessage());
            }


            return;
        }

        // 消息确认,手动执行ack
        channel.basicAck(deliveryTag, false);
    }

    private void handleChartUpdateError(long chartId, String execMessage) {
        try {
            chartService.updateChartStatus(chartId, "failed", execMessage);
        } catch (Exception e) {

            log.error("更新图表失败状态更新失败:{}{}", chartId, execMessage, e);
        }

    }

    /**
     * 构造输入
     *
     * @param chart
     * @return
     */
    private String buildUserInput(Chart chart) {
        // 构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");
        // 拼接分析目标
        String userGoal = chart.getGoal();
        if (StringUtils.isNotBlank(chart.getChartType())) {
            userGoal += "，请使用" + chart.getChartType();
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据：").append("\n");
        // 压缩后的数据
        String csvData = chart.getChartData();
        userInput.append(csvData).append("\n");
        return userInput.toString();

    }

}
