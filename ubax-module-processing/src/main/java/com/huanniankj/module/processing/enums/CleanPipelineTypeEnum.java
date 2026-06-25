package com.huanniankj.module.processing.enums;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * 通用状态枚举
 *
 * @author zhaoff
 */
@Getter
@AllArgsConstructor
public enum CleanPipelineTypeEnum implements ArrayValuable<String> {

    FORMAT_CONVERT("format_convert", "格式转换"),

    DATA_CLEAN("data_clean", "数据清洗"),

    DATA_MAP("data_map", "数据映射"),

    DATA_FILTER("data_filter", "数据过滤");

    public static final String[] ARRAYS = Arrays.stream(values()).map(CleanPipelineTypeEnum::getCode).toArray(String[]::new);

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
