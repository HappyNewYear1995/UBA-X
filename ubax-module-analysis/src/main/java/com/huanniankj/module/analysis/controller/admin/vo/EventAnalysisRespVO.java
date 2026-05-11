package com.huanniankj.module.analysis.controller.admin.vo;

import lombok.Data;

@Data
public class EventAnalysisRespVO {

    /**
     * 聚合维度的 Key (例如： "12点", "Chrome", "北京")
     */
    private String dimensionKey;

    /**
     * 页面浏览量 / 事件触发总次数
     */
    private Long pv;

    /**
     * 独立访客数 (去重 device_id)
     */
    private Long uv;

    /**
     * 平均处理耗时 (毫秒)
     */
    private Double avgRequestTime;
}
