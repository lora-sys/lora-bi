package com.lora.bi.mq;

import com.lora.bi.bimq.MyMessageProductor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
@SpringBootTest
class DirectProductorTest {

    @Resource
    MyMessageProductor myMessageProductor;


    @Test
    void sendMessage() throws Exception {
        myMessageProductor.sendMessage("test_exchange","你好啊","myRoutingKey");
    }
}