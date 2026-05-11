package com.huanniankj.module.storage.dal.dataobject;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一日志实体类
 * 映射 ClickHouse 中的 ubax_user_behavior_log 表
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName("ubax_user_behavior_log")
public class UserBehaviorLogDO extends BaseDO {

    /**
     * 日志id
     */
    @TableId(type = com.baomidou.mybatisplus.annotation.IdType.ASSIGN_UUID)
    private String logId;

    /**
     * 唯一请求标识符
     */
    private String uuid;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 进程 PID
     */
    private String pid;

    /**
     * 唯一的请求 ID
     */
    private String requestId;

    /**
     * 请求的 Unix 时间，精确到毫秒
     */
    private String msec;

    /**
     * 请求长度（包括头部和正文）
     */
    private String requestLength;

    /**
     * 客户端 IP 地址
     */
    private String remoteAddr;

    /**
     * 客户端端口
     */
    private String httpPort;

    /**
     * 完整路径（无参数）的请求
     */
    private String request;

    /**
     * 完整路径和参数的请求
     */
    private String requestUri;

    /**
     * 去掉请求参数的请求地址
     */
    private String uri;

    /**
     * 请求参数
     */
    private String args;

    /**
     * 响应状态码
     */
    private String status;

    /**
     * 发送给客户端的正文字节数（不包括头部）
     */
    private String bodyBytesSent;

    /**
     * 发送给客户端的总字节数
     */
    private String bytesSent;

    /**
     * HTTP httpReferrer 头
     */
    private String httpReferer;

    /**
     * 用户代理（User-Agent）
     */
    private String httpUserAgent;

    /**
     * 请求的 Host 头
     */
    private String httpHost;

    /**
     * 提供请求的虚拟主机名称
     */
    private String serverName;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 请求处理时间（秒，带毫秒精度）
     */
    private Double requestTime;

    /**
     * 请求使用的协议（http 或 https）
     */
    private String scheme;

    /**
     * 请求方法（如 GET、POST 等）
     */
    private String requestMethod;

    /**
     * 平台类型
     */
    private String platformType;

    /**
     * 授权令牌
     */
    private String authorization;

    /**
     * Cookie 信息
     */
    private String cookie;

    /**
     * 客户端真实 IP
     */
    private String httpXTrueIp;

    /**
     * 日志类型：ClickLog、AccessLog
     */
    private String logType;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 手机
     */
    private String model;

    /**
     * 品牌
     */
    private String brand;

    /**
     * 制造商
     */
    public String manufacturer;

    /**
     * 应用程序名称
     */
    public String webviewAppName;

    /**
     * 代理分类
     */
    public String agentClass;

    /**
     * 浏览器版本
     */
    private String browserVersion;

    /**
     * http版本
     */
    private String httpVersion;

    /**
     * 统计日期
     */
    private Long statDate;

    /**
     * 统计小时
     */
    private Long statHour;

    /**
     * 统计分钟
     */
    private Long statMin;

    /**
     * 手机app设备id
     */
    private String deviceId;

    /**
     * 忽略
     */
    private Long whetherIgnore;

    /**
     * 原始数据存储 (存放 SDK 或 Nginx 传过来的未清洗原始 JSON 报文)
     * 在排查脏数据或回溯未知字段时非常有用
     */
    private String rawData;
}
