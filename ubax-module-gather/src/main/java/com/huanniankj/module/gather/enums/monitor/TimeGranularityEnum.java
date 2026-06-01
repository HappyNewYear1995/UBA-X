package com.huanniankj.module.gather.enums.monitor;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 时间粒度枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum TimeGranularityEnum implements ArrayValuable<String> {

    MINUTE("minute", "分钟"),

    HOUR("hour", "小时"),

    DAY("day", "天");

    public static final String[] ARRAYS = Arrays.stream(values()).map(TimeGranularityEnum::getGranularity).toArray(String[]::new);

    /**
     * 粒度值
     */
    private final String granularity;

    /**
     * 粒度名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
