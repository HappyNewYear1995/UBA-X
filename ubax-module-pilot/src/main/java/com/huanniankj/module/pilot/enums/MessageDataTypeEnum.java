package com.huanniankj.module.pilot.enums;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 消息数据类型枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum MessageDataTypeEnum implements ArrayValuable<String> {

    CONFIG("config", "配置推送"),

    COMMAND("command", "远程命令");

    public static final String[] ARRAYS = Arrays.stream(values()).map(MessageDataTypeEnum::getType).toArray(String[]::new);

    /**
     * 数据类型值
     */
    private final String type;

    /**
     * 数据类型名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
