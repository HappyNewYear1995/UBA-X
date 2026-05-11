package com.huanniankj.framework.idempotent.config;

import com.huanniankj.framework.idempotent.core.aop.IdempotentAspect;
import com.huanniankj.framework.idempotent.core.keyresolver.IdempotentKeyResolver;
import com.huanniankj.framework.idempotent.core.keyresolver.impl.DefaultIdempotentKeyResolver;
import com.huanniankj.framework.idempotent.core.keyresolver.impl.ExpressionIdempotentKeyResolver;
import com.huanniankj.framework.idempotent.core.keyresolver.impl.UserIdempotentKeyResolver;
import com.huanniankj.framework.idempotent.core.redis.IdempotentRedisDAO;
import com.huanniankj.framework.redis.config.UbaxRedisAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@AutoConfiguration(after = UbaxRedisAutoConfiguration.class)
public class UbaxIdempotentConfiguration {

    @Bean
    public IdempotentAspect idempotentAspect(List<IdempotentKeyResolver> keyResolvers, IdempotentRedisDAO idempotentRedisDAO) {
        return new IdempotentAspect(keyResolvers, idempotentRedisDAO);
    }

    @Bean
    public IdempotentRedisDAO idempotentRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new IdempotentRedisDAO(stringRedisTemplate);
    }

    // ========== 各种 IdempotentKeyResolver Bean ==========

    @Bean
    public DefaultIdempotentKeyResolver defaultIdempotentKeyResolver() {
        return new DefaultIdempotentKeyResolver();
    }

    @Bean
    public UserIdempotentKeyResolver userIdempotentKeyResolver() {
        return new UserIdempotentKeyResolver();
    }

    @Bean
    public ExpressionIdempotentKeyResolver expressionIdempotentKeyResolver() {
        return new ExpressionIdempotentKeyResolver();
    }

}
