package com.huanniankj.module.gather.dal.dataobject.database;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.huanniankj.framework.mybatis.core.dataobject.BaseDO;
import com.huanniankj.module.gather.enums.datasource.DatabaseTypeEnum;
import lombok.*;

/**
 * 数据库数据源 DO
 *
 * @author zhaoff
 */
@TableName("source_database")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseDO extends BaseDO {

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
     * 数据库类型编码
     *
     * @see DatabaseTypeEnum
     */
    private String dbType;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 数据库名
     */
    @TableField("`database`")
    private String database;

    /**
     * 用户名
     */
    @TableField("`username`")
    private String username;

    /**
     * 密码 (加密存储)
     */
    @TableField("`password`")
    private String password;

    /**
     * JDBC URL (可选，为空时根据模板自动生成)
     */
    private String url;

    /**
     * 连接协议 (tcp/ssl/ssl-verify)
     */
    private String protocol;

    /**
     * SSL 证书路径 (客户端证书)
     */
    private String sslCertPath;

    /**
     * SSL 私钥路径
     */
    private String sslKeyPath;

    /**
     * SSL CA 证书路径
     */
    private String sslCaPath;

    /**
     * 密码加密方式 (plain/aes/rsa)
     */
    private String passwordEncryptType;

    /**
     * 额外连接参数 (JSON 格式，如 {"useSSL":"true","serverTimezone":"Asia/Shanghai"})
     */
    private String connectionParams;

    /**
     * 连接池最大连接数
     */
    private Integer maxPoolSize;

    /**
     * 连接超时时间 (毫秒)
     */
    private Long connectionTimeout;

    /**
     * 状态 (0-正常 1-异常)
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

}
