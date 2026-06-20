package com.huanniankj.module.source.enums.agent;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 平台类型枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum PlatformEnum implements ArrayValuable<Integer> {

    AUTO(1, "自动"),

    UNKNOWN(0, "未知"),

    WEB(10, "WEB"),

    H5(20, "H5"),

    APP(30, "App"),

    WECHAT_MINI_PROGRAM(40, "微信小程序"),
    ;

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(PlatformEnum::getPlatform).toArray(Integer[]::new);

    /**
     * 平台
     */
    private final Integer platform;

    /**
     * 平台名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }
}
