package com.huanniankj.module.source.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * WebService 执行请求 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "WebService 执行请求")
public class WebServiceExecuteReqVO {

    @Schema(description = "数据源 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "数据源 ID 不能为空")
    private Long databaseId;

    @Schema(description = "请求 URL（覆盖数据源默认 URL）", example = "https://api.example.com/data")
    private String url;

    @Schema(description = "请求方法 (GET/POST/PUT/DELETE)", example = "POST")
    private String method;

    @Schema(description = "请求头（覆盖数据源默认请求头）", example = "{\"Content-Type\":\"application/json\"}")
    private String headers;

    @Schema(description = "请求体", example = "{\"query\":\"select * from table\"}")
    private String body;

    @Schema(description = "认证类型 (none/basic/bearer/apikey，覆盖数据源默认)", example = "bearer")
    private String authType;

    @Schema(description = "认证凭据（覆盖数据源默认）", example = "Bearer token")
    private String authToken;

    @Schema(description = "SOAP 命名空间", example = "http://example.com/namespace")
    private String soapNamespace;

    @Schema(description = "SOAP 操作名", example = "GetData")
    private String soapAction;

    @Schema(description = "响应数据路径 (JSONPath，覆盖数据源默认)", example = "$.data.list")
    private String responsePath;

    @Schema(description = "请求参数（用于 URL 查询参数或模板替换）")
    private Map<String, String> params;

}
