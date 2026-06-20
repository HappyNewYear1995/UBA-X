package com.huanniankj.module.source.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 数据库数据源保存 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "数据库数据源保存请求")
public class DatabaseSaveReqVO {

    @Schema(description = "数据源 ID", example = "1")
    private Long id;

    @Schema(description = "数据源名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "生产库")
    @NotBlank(message = "数据源名称不能为空")
    private String name;

    @Schema(description = "数据库类型编码 (mysql/postgresql/oracle/sqlserver)", requiredMode = Schema.RequiredMode.REQUIRED, example = "mysql")
    @NotBlank(message = "数据库类型不能为空")
    private String dbType;

    @Schema(description = "主机地址", requiredMode = Schema.RequiredMode.REQUIRED, example = "127.0.0.1")
    @NotBlank(message = "主机地址不能为空")
    private String host;

    @Schema(description = "端口号", requiredMode = Schema.RequiredMode.REQUIRED, example = "3306")
    @NotNull(message = "端口号不能为空")
    private Integer port;

    @Schema(description = "数据库名", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotBlank(message = "数据库名不能为空")
    private String database;

    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "root")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码（编辑时不传则不修改）", example = "123456")
    private String password;

    @Schema(description = "JDBC URL (可选，为空时根据模板自动生成)", example = "jdbc:mysql://127.0.0.1:3306/ubax")
    private String url;

    @Schema(description = "连接协议 (tcp/ssl/ssl-verify)", example = "ssl")
    private String protocol;

    @Schema(description = "SSL 证书路径", example = "/path/to/client-cert.pem")
    private String sslCertPath;

    @Schema(description = "SSL 私钥路径", example = "/path/to/client-key.pem")
    private String sslKeyPath;

    @Schema(description = "SSL CA 证书路径", example = "/path/to/ca.pem")
    private String sslCaPath;

    @Schema(description = "密码加密方式 (plain/aes/rsa)", example = "aes")
    private String passwordEncryptType;

    @Schema(description = "额外连接参数 (JSON 格式)", example = "{\"useSSL\":\"true\",\"serverTimezone\":\"Asia/Shanghai\"}")
    private String connectionParams;

    @Schema(description = "连接池最大连接数", example = "20")
    private Integer maxPoolSize;

    @Schema(description = "连接超时时间 (毫秒)", example = "30000")
    private Long connectionTimeout;

    @Schema(description = "备注", example = "生产环境数据库")
    private String remark;

}
