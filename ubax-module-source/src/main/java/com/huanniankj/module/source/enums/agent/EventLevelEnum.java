package com.huanniankj.module.source.enums.agent;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 事件级别枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum EventLevelEnum implements ArrayValuable<Integer> {

    INFO(1, "信息"),

    WARNING(2, "警告"),

    ERROR(3, "错误"),

    CRITICAL(4, "严重");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(EventLevelEnum::getLevel).toArray(Integer[]::new);

    /**
     * 级别值
     */
    private final Integer level;

    /**
     * 级别名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
