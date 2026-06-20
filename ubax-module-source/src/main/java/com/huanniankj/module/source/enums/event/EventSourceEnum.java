package com.huanniankj.module.source.enums.event;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 事件来源枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum EventSourceEnum implements ArrayValuable<String> {

    AGENT("agent", "Agent 上报"),

    SYSTEM("system", "系统生成"),

    USER("user", "用户创建");

    public static final String[] ARRAYS = Arrays.stream(values()).map(EventSourceEnum::getSource).toArray(String[]::new);

    /**
     * 来源值
     */
    private final String source;

    /**
     * 来源名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
