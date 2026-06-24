package com.huanniankj.module.infra.enums.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 配置类型枚举
 *
 * @author zhaoff
 */
@Getter
@AllArgsConstructor
public enum ConfigTypeEnum {

    /**
     * 系统配置
     */
    SYSTEM(1),
    /**
     * 自定义配置
     */
    CUSTOM(2);

    private final Integer type;

}
