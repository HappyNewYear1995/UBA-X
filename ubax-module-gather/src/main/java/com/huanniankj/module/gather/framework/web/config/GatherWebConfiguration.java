package com.huanniankj.module.gather.framework.web.config;

import com.huanniankj.framework.swagger.config.UbaxSwaggerAutoConfiguration;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * gather 模块的 web 组件的 Configuration
 *
 * @author zhaoff
 */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
public class GatherWebConfiguration {

    /**
     * gather 模块的 API 分组
     */
    @Bean
    public GroupedOpenApi gatherGroupedOpenApi() {
        return UbaxSwaggerAutoConfiguration.buildGroupedOpenApi("gather");
    }

}
