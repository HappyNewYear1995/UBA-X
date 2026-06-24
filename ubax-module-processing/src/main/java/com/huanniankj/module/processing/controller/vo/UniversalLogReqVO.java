package com.huanniankj.module.processing.controller.vo;

import lombok.Data;
import java.util.Map;

/**
 * 统一日志采集请求实体类
 * 参考大宽表 uba_log 设计，涵盖访问日志(AccessLog)、点击日志(ClickLog)、机器日志等
 */
@Data
public class UniversalLogReqVO {

    private String logId;
    private String uuid;
    private String userId;
    private String pid;
    private String requestId;
    private String msec;
    private String requestLength;
    private String remoteAddr;
    private String httpPort;
    private String request;
    private String requestUri;
    private String uri;
    private String args;
    private String status;
    private String bodyBytesSent;
    private String bytesSent;
    private String httpReferer;
    private String httpUserAgent;
    private String httpHost;
    private String serverName;
    private Long startTime; // 毫秒时间戳
    private Long endTime;   // 毫秒时间戳
    private Double requestTime;
    private String scheme;
    private String requestMethod;
    private String platformType;
    private String authorization;
    private String cookie;
    private String httpXTrueIp;
    private String logType; // ClickLog, AccessLog, MachineLog, AILog 等
    private String country;
    private String province;
    private String city;
    private String browser;
    private String model;
    private String brand;
    private String manufacturer;
    private String webviewAppName;
    private String agentClass;
    private String browserVersion;
    private String httpVersion;
    private Long statDate;
    private Long statHour;
    private Long statMin;
    private String deviceId;
    private Long whetherIgnore;

    /**
     * 扩展字段：用于存放一些特定业务场景的自定义 JSON 属性
     */
    private Map<String, Object> properties;
}
