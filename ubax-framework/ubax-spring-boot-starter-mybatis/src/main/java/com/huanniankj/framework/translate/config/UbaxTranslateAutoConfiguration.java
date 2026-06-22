package com.huanniankj.framework.translate.config;

import com.fhs.trans.service.impl.TransService;
import com.huanniankj.framework.translate.core.TranslateUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 数据库翻译自动配置类
 *
 * @author zhaoff
 */
@AutoConfiguration
public class UbaxTranslateAutoConfiguration {

    @Bean
    @SuppressWarnings({"InstantiationOfUtilityClass", "SpringJavaInjectionPointsAutowiringInspection"})
    public TranslateUtils translateUtils(TransService transService) {
        TranslateUtils.init(transService);
        return new TranslateUtils();
    }

}
