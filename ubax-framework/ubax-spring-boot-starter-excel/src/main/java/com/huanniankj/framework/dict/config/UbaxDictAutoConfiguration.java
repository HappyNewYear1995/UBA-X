package com.huanniankj.framework.dict.config;

import com.huanniankj.framework.common.biz.system.dict.DictDataCommonApi;
import com.huanniankj.framework.dict.core.DictFrameworkUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * 字典自动配置类
 *
 * @author zhaofgf
 */
@AutoConfiguration
public class UbaxDictAutoConfiguration {

    @Bean
    @SuppressWarnings("InstantiationOfUtilityClass")
    public DictFrameworkUtils dictUtils(DictDataCommonApi dictDataApi) {
        DictFrameworkUtils.init(dictDataApi);
        return new DictFrameworkUtils();
    }

}
