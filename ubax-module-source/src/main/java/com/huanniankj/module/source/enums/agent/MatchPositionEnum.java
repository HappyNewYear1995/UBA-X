package com.huanniankj.module.source.enums.agent;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 匹配位置枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum MatchPositionEnum implements ArrayValuable<String> {

    HTTP_BODY("http_body", "HTTP Body"),

    HTTP_HEADER("http_header", "HTTP Header"),

    HTTP_URL("http_url", "HTTP URL"),

    HTTP_METHOD("http_method", "HTTP Method"),

    HTTP_STATUS("http_status", "HTTP Status Code"),

    CUSTOM_FIELD("custom_field", "自定义字段");

    public static final String[] ARRAYS = Arrays.stream(values()).map(MatchPositionEnum::getPosition).toArray(String[]::new);

    /**
     * 位置值
     */
    private final String position;

    /**
     * 位置名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
