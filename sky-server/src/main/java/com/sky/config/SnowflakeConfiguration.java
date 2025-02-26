package com.sky.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SnowflakeConfiguration {
    @Bean
    public SnowflakeIdGenerator snowflakeIdGenerator() {
        // 假设机器ID为 1（可以根据实际部署环境配置）
        return new SnowflakeIdGenerator(1);
    }
}
