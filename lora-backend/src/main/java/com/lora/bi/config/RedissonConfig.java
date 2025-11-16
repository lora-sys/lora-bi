package com.lora.bi.config;

import io.swagger.models.auth.In;
import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String password;
    private String host;
    private String port;
    private Integer database;

    @Bean
    public RedissonClient getRedissonClient() {
        //  1. 创建配置对象
        Config config = new Config();
        // 2. 配置单机模式 (SingleServer)
        config.useSingleServer()
                .setDatabase(database)
                // Redis 服务器地址，格式必须是 "redis://host:port"
                .setAddress("redis://127.0.0.1:6379")
                // 可选：设置密码
                // .setPassword("your-password")
                // 可选：设置连接池大小
                .setConnectionPoolSize(50);
        // 3. 创建 RedissonClient 实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
