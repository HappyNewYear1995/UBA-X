package com.huanniankj.module.pilot.enums;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 采集器状态枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum CollectorStatusEnum implements ArrayValuable<String> {

    UNKNOWN("unknown", "未知"),

    RUNNING("running", "运行"),

    STOPPED("stopped", "停止");


    public static final String[] ARRAYS = Arrays.stream(values()).map(CollectorStatusEnum::getStatus).toArray(String[]::new);

    /**
     * 状态
     */
    private final String status;

    /**
     * 状态名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
