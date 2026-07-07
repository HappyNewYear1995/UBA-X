package com.huanniankj.module.source.enums.webservice;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 认证类型枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum AuthTypeEnum implements ArrayValuable<String> {

    NONE("none", "无认证"),

    /**
     * Authorization: Basic base64(username:password)
     */
    BASIC("basic", "基本认证"),

    /**
     * Authorization: Bearer <your_access_token>
     */
    BEARER("bearer", "令牌认证"),

    /**
     * 请求头：X-API-Key: your_api_key
     * 查询参数：?api_key=your_api_key
     * 自定义头：X-Custom-API-Key: your_api_key
     */
    API_KEY("apikey", "密钥认证");

    public static final String[] ARRAYS = Arrays.stream(values()).map(AuthTypeEnum::getAuthType).toArray(String[]::new);

    /**
     * 认证类型值
     */
    private final String authType;

    /**
     * 认证类型名
     */
    private final String name;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
