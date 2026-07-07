package com.huanniankj.module.source.dal.dataobject.webservice;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * WebService 数据源 DO
 *
 * @author zhaoff
 */
@TableName("source_webservice")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebServiceDO extends BaseDO {

    /**
     * 数据源 ID
     */
    @TableId
    private Long id;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 服务地址
     */
    private String url;

    /**
     * 请求方法 (GET/POST/PUT/DELETE)
     */
    private String method;

    /**
     * 请求头 (JSON 格式，如 {"Content-Type":"application/json"})
     */
    private String headers;

    /**
     * 请求体 (JSON 格式，POST/PUT 请求使用)
     */
    private String body;

    /**
     * 认证类型 (none/basic/bearer/apikey)
     *
     * @see com.huanniankj.module.source.enums.webservice.AuthTypeEnum
     */
    private String authType;

    /**
     * 认证凭据（Basic 的 Base64 值、Bearer 的 Token、API Key 等）
     */
    private String authToken;

    /**
     * SOAP 命名空间（SOAP 协议时使用）
     */
    private String soapNamespace;

    /**
     * SOAP 操作名（SOAP 协议时使用）
     */
    private String soapAction;

    /**
     * 响应数据路径（JSONPath 表达式，用于提取数据，如 $.data.list）
     */
    private String responsePath;

    /**
     * 同步间隔（秒，0 表示不自动同步）
     */
    private Integer syncInterval;

    /**
     * 状态 (0-正常 1-异常)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
