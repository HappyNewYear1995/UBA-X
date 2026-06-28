package com.huanniankj.module.source.enums.agent;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 匹配类型枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum MatchTypeEnum implements ArrayValuable<String> {

    CONTAINS("contains", "包含"),

    EQUALS("equals", "等于"),

    STARTS_WITH("starts_with", "以...开头"),

    ENDS_WITH("ends_with", "以...结尾"),

    REGEX("regex", "正则表达式"),

    NOT_CONTAINS("not_contains", "不包含");

    public static final String[] ARRAYS = Arrays.stream(values()).map(MatchTypeEnum::getType).toArray(String[]::new);

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
