package com.huanniankj.module.pilot.enums;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 消息类型枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum MessageTypeEnum implements ArrayValuable<String> {

    COMMAND("command", "指令"),

    CONFIG("config", "配置");

    public static final String[] ARRAYS = Arrays.stream(values()).map(MessageTypeEnum::getType).toArray(String[]::new);

    /**
     * 类型值
     */
    private final String type;

    /**
     * 类型名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
