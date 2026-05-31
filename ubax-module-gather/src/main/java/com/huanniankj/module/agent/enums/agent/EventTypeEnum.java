package com.huanniankj.module.agent.enums.agent;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 事件类型枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum EventTypeEnum implements ArrayValuable<String> {

    MESSAGE("message", "消息事件"),

    CONNECTED("connected", "连接事件");

    public static final String[] ARRAYS = Arrays.stream(values()).map(EventTypeEnum::getEventType).toArray(String[]::new);

    /**
     * 事件类型值
     */
    private final String eventType;

    /**
     * 事件类型名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
