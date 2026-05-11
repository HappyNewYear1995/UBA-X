package com.huanniankj.framework.signature.config;

import com.huanniankj.framework.redis.config.UbaxRedisAutoConfiguration;
import com.huanniankj.framework.signature.core.aop.ApiSignatureAspect;
import com.huanniankj.framework.signature.core.redis.ApiSignatureRedisDAO;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * HTTP API 签名的自动配置类
 *
 * @author zhaoff
 */
@AutoConfiguration(after = UbaxRedisAutoConfiguration.class)
public class UbaxApiSignatureAutoConfiguration {

    @Bean
    public ApiSignatureAspect signatureAspect(ApiSignatureRedisDAO signatureRedisDAO) {
        return new ApiSignatureAspect(signatureRedisDAO);
    }

    @Bean
    public ApiSignatureRedisDAO signatureRedisDAO(StringRedisTemplate stringRedisTemplate) {
        return new ApiSignatureRedisDAO(stringRedisTemplate);
    }

}
