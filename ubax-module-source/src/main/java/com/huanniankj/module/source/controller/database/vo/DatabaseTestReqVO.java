package com.huanniankj.module.source.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 数据库数据源连接测试请求 VO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "数据库数据源连接测试请求")
public class DatabaseTestReqVO {

    @Schema(description = "数据源 ID（已保存数据源测试时传入）", example = "1")
    private Long id;

    @Schema(description = "数据库类型", example = "mysql")
    private String dbType;

    @Schema(description = "主机地址", example = "127.0.0.1")
    private String host;

    @Schema(description = "端口号", example = "3306")
    private Integer port;

    @Schema(description = "数据库名", example = "ubax")
    private String database;

    @Schema(description = "用户名", example = "root")
    private String username;

    @Schema(description = "密码", example = "123456")
    private String password;

    @Schema(description = "连接协议", example = "tcp")
    private String protocol;

    @Schema(description = "SSL 证书路径", example = "/path/to/client-cert.pem")
    private String sslCertPath;

    @Schema(description = "SSL 私钥路径", example = "/path/to/client-key.pem")
    private String sslKeyPath;

    @Schema(description = "SSL CA 证书路径", example = "/path/to/ca.pem")
    private String sslCaPath;

    @Schema(description = "额外连接参数 (JSON 格式)", example = "{\"useSSL\":\"true\"}")
    private String connectionParams;

}
