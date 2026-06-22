package com.huanniankj.module.source.controller.webservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * WebService 数据源 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "WebService 数据源响应")
public class WebServiceRespVO {

    @Schema(description = "数据源 ID", example = "1")
    private Long id;

    @Schema(description = "数据源名称", example = "用户数据接口")
    private String name;

    @Schema(description = "服务地址", example = "https://api.example.com/data")
    private String url;

    @Schema(description = "请求方法", example = "POST")
    private String method;

    @Schema(description = "请求头 (JSON 格式)")
    private String headers;

    @Schema(description = "请求体 (JSON 格式)")
    private String body;

    @Schema(description = "认证类型", example = "bearer")
    private String authType;

    @Schema(description = "认证凭据（脱敏）")
    private String authToken;

    @Schema(description = "SOAP 命名空间")
    private String soapNamespace;

    @Schema(description = "SOAP 操作名")
    private String soapAction;

    @Schema(description = "响应数据路径 (JSONPath)")
    private String responsePath;

    @Schema(description = "同步间隔（秒）")
    private Integer syncInterval;

    @Schema(description = "状态 (0-正常 1-异常)", example = "0")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
