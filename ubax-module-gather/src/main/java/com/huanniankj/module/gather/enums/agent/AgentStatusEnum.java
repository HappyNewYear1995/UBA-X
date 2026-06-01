package com.huanniankj.module.gather.enums.agent;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Agent状态枚举
 *
 * @author zhaoff
 */
@Getter
@AllArgsConstructor
public enum AgentStatusEnum implements ArrayValuable<Integer> {

    ON(0, "开启"),

    OFF(1, "关闭");

    public static final Integer[] ARRAYS = Arrays.stream(values()).map(AgentStatusEnum::getStatus).toArray(Integer[]::new);

    /**
     * 状态值
     */
    private final Integer status;

    /**
     * 状态名
     */
    private final String name;

    @Override
    public Integer[] array() {
        return ARRAYS;
    }

}
