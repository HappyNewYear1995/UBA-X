package com.huanniankj.module.source.framework.web.config;

import com.huanniankj.framework.swagger.config.UbaxSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * source 模块的 web 组件的 Configuration
 *
 * @author zhaoff
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class SourceWebConfiguration {

    /**
     * source 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi sourceGroupedOpenApi() {
        return UbaxSwaggerAutoConfiguration.buildGroupedOpenApi("source");
    }

}
