package com.huanniankj.module.gather.enums.agent;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 命令动作枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum CommandActionEnum implements ArrayValuable<String> {

    RESTART("restart", "重启"),

    STOP("stop", "停止");

    public static final String[] ARRAYS = Arrays.stream(values()).map(CommandActionEnum::getAction).toArray(String[]::new);

    /**
     * 动作值
     */
    private final String action;

    /**
     * 动作名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
