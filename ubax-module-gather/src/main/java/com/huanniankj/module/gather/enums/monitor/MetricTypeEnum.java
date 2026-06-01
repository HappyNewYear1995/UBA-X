package com.huanniankj.module.gather.enums.monitor;

import com.huanniankj.framework.common.core.ArrayValuable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

/**
 * 指标类型枚举
 *
 * @author zhaoff
 */
@RequiredArgsConstructor
@Getter
public enum MetricTypeEnum implements ArrayValuable<String> {

    EVENT_COUNT("event_count", "事件数量"),

    SUCCESS_RATE("success_rate", "成功率"),

    THROUGHPUT("throughput", "吞吐量"),

    LATENCY("latency", "延迟"),

    ERROR_RATE("error_rate", "错误率"),

    BANDWIDTH("bandwidth", "带宽使用");

    public static final String[] ARRAYS = Arrays.stream(values()).map(MetricTypeEnum::getType).toArray(String[]::new);

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
