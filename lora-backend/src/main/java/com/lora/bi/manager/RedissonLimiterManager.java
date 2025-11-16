package com.lora.bi.manager;

import com.lora.bi.common.ErrorCode;
import com.lora.bi.exception.BusinessException;
import com.lora.bi.exception.ThrowUtils;
import net.bytebuddy.implementation.bytecode.Throw;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 针对redissonlimiter  基础服务 ( 提供服务)
 */
@Service
public class RedissonLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 【步骤 1：初始化】设置限流器的速率和容量，这是使用限流器的前提
     *
     * @param key
     */
    public void doRateLimit(String key) {
        RRateLimiter ratelimiter = redissonClient.getRateLimiter(key);
        // RateType.OVERALL 表示全局限流 intervalUnit表示时间单位
        // 尝试设置速率：在 5 时间间隔 时间内，允许 rate 个请求通过 每秒允许2个请求通过，
        ratelimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);

        // 一个请求来了后，获取一个令牌，
        boolean canOp = ratelimiter.tryAcquire(1);
        if (!canOp) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST, "请求太多");

        }

    }


}
