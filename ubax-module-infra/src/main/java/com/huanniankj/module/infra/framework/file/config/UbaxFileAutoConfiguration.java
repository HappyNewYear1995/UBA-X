package com.huanniankj.module.infra.framework.file.config;

import com.huanniankj.module.infra.framework.file.core.client.FileClientFactory;
import com.huanniankj.module.infra.framework.file.core.client.FileClientFactoryImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 文件配置类
 *
 * @author zhaoff
 */
@Configuration(proxyBeanMethods = false)
public class UbaxFileAutoConfiguration {

    @Bean
    public FileClientFactory fileClientFactory() {
        return new FileClientFactoryImpl();
    }

}
