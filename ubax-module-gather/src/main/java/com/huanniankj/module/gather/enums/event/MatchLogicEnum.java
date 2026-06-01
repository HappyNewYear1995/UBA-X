package com.huanniankj.module.gather.enums.event;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 匹配逻辑枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum MatchLogicEnum implements ArrayValuable<String> {

    AND("and", "且（所有规则都需满足）"),

    OR("or", "或（任一规则满足即可）");

    public static final String[] ARRAYS = Arrays.stream(values()).map(MatchLogicEnum::getLogic).toArray(String[]::new);

    /**
     * 逻辑值
     */
    private final String logic;

    /**
     * 逻辑名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
