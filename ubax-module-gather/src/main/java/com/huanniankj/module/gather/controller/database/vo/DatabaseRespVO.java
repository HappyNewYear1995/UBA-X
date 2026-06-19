package com.huanniankj.module.gather.controller.database.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库数据源 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "数据库数据源响应")
public class DatabaseRespVO {

    @Schema(description = "数据源 ID", example = "1")
    private Long id;

    @Schema(description = "数据源名称", example = "生产库")
    private String name;

    @Schema(description = "数据库类型编码", example = "mysql")
    private String dbType;

    @Schema(description = "数据库类型名称", example = "MySQL")
    private String dbTypeName;

    @Schema(description = "主机地址", example = "127.0.0.1")
    private String host;

    @Schema(description = "端口号", example = "3306")
    private Integer port;

    @Schema(description = "数据库名", example = "ubax")
    private String database;

    @Schema(description = "用户名", example = "root")
    private String username;

    @Schema(description = "密码（脱敏）", example = "******")
    private String password;

    @Schema(description = "JDBC URL", example = "jdbc:mysql://127.0.0.1:3306/ubax")
    private String url;

    @Schema(description = "连接协议", example = "ssl")
    private String protocol;

    @Schema(description = "SSL 证书路径", example = "/path/to/client-cert.pem")
    private String sslCertPath;

    @Schema(description = "SSL 私钥路径", example = "/path/to/client-key.pem")
    private String sslKeyPath;

    @Schema(description = "SSL CA 证书路径", example = "/path/to/ca.pem")
    private String sslCaPath;

    @Schema(description = "密码加密方式", example = "aes")
    private String passwordEncryptType;

    @Schema(description = "额外连接参数 (JSON 格式)", example = "{\"useSSL\":\"true\"}")
    private String connectionParams;

    @Schema(description = "连接池最大连接数", example = "20")
    private Integer maxPoolSize;

    @Schema(description = "连接超时时间 (毫秒)", example = "30000")
    private Long connectionTimeout;

    @Schema(description = "状态 (0-正常 1-禁用)", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "生产环境数据库")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
