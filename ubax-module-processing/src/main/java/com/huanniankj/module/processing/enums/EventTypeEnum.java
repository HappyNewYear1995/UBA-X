package com.huanniankj.module.processing.enums;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 事件类型枚举
 *
 * @author zhaoff
 */
@Getter
@AllArgsConstructor
public enum EventTypeEnum implements ArrayValuable<String> {

    PAGE_VIEW("page_view", "页面浏览"),

    CLICK("click", "点击事件"),

    CUSTOM("custom", "自定义事件");

    public static final String[] ARRAYS = Arrays.stream(values()).map(EventTypeEnum::getCode).toArray(String[]::new);

    /**
     * 编码
     */
    private final String code;

    /**
     * 描述
     */
    private final String desc;

    @Override
    public String[] array() {
        return ARRAYS;
    }

}
