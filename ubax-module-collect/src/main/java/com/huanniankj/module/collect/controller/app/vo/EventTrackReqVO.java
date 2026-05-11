package com.huanniankj.module.collect.controller.app.vo;

import lombok.Data;
import java.util.Map;

/**
 * 事件追踪请求实体类
 */
@Data
public class EventTrackReqVO {

    /**
     * 应用ID，标识数据来源的应用（例如：Web, iOS, Android, 小程序）
     */
    private String appId;

    /**
     * 唯一设备ID
     */
    private String deviceId;

    /**
     * 用户ID（登录后关联的业务用户ID，可为空）
     */
    private String userId;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 事件名称（例如：$pageview, button_click）
     */
    private String eventName;

    /**
     * 客户端事件发生时间（毫秒级时间戳）
     */
    private Long timestamp;

    /**
     * 当前访问的完整 URL
     */
    private String url;

    /**
     * 当前访问的页面路径 (URI)
     */
    private String path;

    /**
     * 页面标题 (Title)
     */
    private String title;

    /**
     * 来源地址，上一跳地址 (Referrer)
     */
    private String referrer;

    /**
     * 屏幕分辨率 (例如：1920x1080)
     */
    private String screenResolution;

    /**
     * 浏览器语言 (例如：zh-CN)
     */
    private String language;

    /**
     * 事件属性字典，包含页面路径、元素位置、自定义业务参数等具体信息
     */
    private Map<String, Object> properties;
}
