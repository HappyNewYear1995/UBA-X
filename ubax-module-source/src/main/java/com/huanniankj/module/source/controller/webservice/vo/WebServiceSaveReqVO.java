package com.huanniankj.module.source.controller.webservice.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * WebService 数据源保存 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "WebService 数据源保存请求")
public class WebServiceSaveReqVO {

    @Schema(description = "数据源 ID", example = "1")
    private Long id;

    @Schema(description = "数据源名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户数据接口")
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @Schema(description = "服务地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "https://api.example.com/data")
    @NotBlank(message = "服务地址不能为空")
    private String url;

    @Schema(description = "请求方法 (GET/POST/PUT/DELETE)", example = "POST")
    private String method;

    @Schema(description = "请求头 (JSON 格式)", example = "{\"Content-Type\":\"application/json\"}")
    private String headers;

    @Schema(description = "请求体 (JSON 格式)", example = "{\"query\":\"select * from table\"}")
    private String body;

    @Schema(description = "认证类型 (none/basic/bearer/apikey)", example = "bearer")
    private String authType;

    @Schema(description = "认证凭据", example = "Bearer token")
    private String authToken;

    @Schema(description = "SOAP 命名空间", example = "http://example.com/namespace")
    private String soapNamespace;

    @Schema(description = "SOAP 操作名", example = "GetData")
    private String soapAction;

    @Schema(description = "响应数据路径 (JSONPath)", example = "$.data.list")
    private String responsePath;

    @Schema(description = "同步间隔（秒，0 表示不自动同步）", example = "3600")
    private Integer syncInterval;

    @Schema(description = "备注", example = "生产环境接口")
    private String remark;

}
