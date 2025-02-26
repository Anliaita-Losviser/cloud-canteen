package com.sky.service.impl;

import com.sky.config.SnowflakeIdGenerator;
import com.sky.service.IdGenerateService;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import javax.annotation.Resource;

public class IdGenerateServiceImpl implements IdGenerateService, IdentifierGenerator {
    @Resource(name = "snowflakeIdGenerator")
    private SnowflakeIdGenerator snowflakeIdGenerator;
    
    /**
     * 生成ID
     * @return
     */
    @Override
    public long generateUserId() {
        return snowflakeIdGenerator.nextId();
    }
    
    /**
     * hibernate自定义ID生成规则
     * @param sharedSessionContractImplementor
     * @param o
     * @return
     */
    @Override
    public Object generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) {
        return snowflakeIdGenerator.nextId();
    }
}
