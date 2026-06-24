package com.huanniankj.module.app.framework.web.config;

import com.huanniankj.framework.swagger.config.UbaxSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * app 模块的 web 组件的 Configuration
 *
 * @author zhaoff
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class AppWebConfiguration {

    /**
     * app 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi appGroupedOpenApi() {
        return UbaxSwaggerAutoConfiguration.buildGroupedOpenApi("app");
    }

}
