package com.huanniankj.module.collect.controller.app.vo;

import lombok.Data;
import java.util.Map;

/**
 * 客户端精简版事件上报实体
 * 前端 SDK 或 App 只需上报最核心的业务字段，其它几十个字段由后端/Flink自动补全
 */
@Data
public class ClientEventTrackReqVO {

    /**
     * 来源应用标识
     */
    private String appId;

    /**
     * 设备指纹ID
     */
    private String deviceId;

    /**
     * 用户ID (如果已登录)
     */
    private String userId;

    /**
     * 业务会话ID
     */
    private String sessionId;

    /**
     * 动作名称 (如: $pageview, $click, register)
     */
    private String eventName;

    /**
     * 前端触发的精确毫秒时间戳
     */
    private Long timestamp;

    /**
     * 来源渠道/上一跳 (可选，若不传则后端从 HTTP Header 的 Referer 中提取)
     */
    private String referrer;

    /**
     * 当前页面地址 (可选)
     */
    private String url;

    /**
     * 屏幕分辨率 (可选)
     */
    private String screenResolution;

    /**
     * 自定义业务属性字典
     * 例如: {"button_color":"red", "price":100}
     */
    private Map<String, Object> properties;
}
