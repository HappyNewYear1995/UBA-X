package com.huanniankj.module.pilot.framework.web.config;

import com.huanniankj.framework.swagger.config.UbaxSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * pilot 模块的 web 组件的 Configuration
 *
 * @author zhaoff
 */
@Configuration(proxyBeanMethods = false)
public class PilotWebConfiguration {

    /**
     * pilot 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi pilotGroupedOpenApi() {
        return UbaxSwaggerAutoConfiguration.buildGroupedOpenApi("pilot");
    }

}
