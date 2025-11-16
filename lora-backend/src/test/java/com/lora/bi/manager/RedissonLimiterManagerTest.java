package com.lora.bi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class RedissonLimiterManagerTest {


    @Resource
    private RedissonLimiterManager redissonLimiterManager;
    @Test
    void doRateLimit() throws InterruptedException {
        String userId = "1";
        for(int i = 0; i < 2; i++) {
            redissonLimiterManager.doRateLimit(userId);
            System.out.println("成功");
        }
        Thread.sleep(1000);
        for(int i = 0; i < 10; i++) {
            redissonLimiterManager.doRateLimit(userId);
            System.out.println("成功");
        }

    }
}