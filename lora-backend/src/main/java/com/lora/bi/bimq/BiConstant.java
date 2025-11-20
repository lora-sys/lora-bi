package com.lora.bi.bimq;

public interface BiConstant {
    String BI_QUEUE_NAME = "bi_queue";
    String EXCHANGE_NAME = "bi_exchange";
    String BI_ROUTING_KEY = "bi_routingkey";

    // 死信队列
    String DLX_QUEUE_NAME = "bi_dlx_queue";
    String DLX_EXCHANGE_NAME = "bi_dlx_exchange";
    String DLX_ROUTING_KEY = "bi_dlx_routingkey";
}
