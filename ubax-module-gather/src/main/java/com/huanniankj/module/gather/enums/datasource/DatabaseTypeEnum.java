package com.huanniankj.module.gather.enums.datasource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据库类型枚举
 *
 * @author zhaoff
 */
@Getter
@AllArgsConstructor
public enum DatabaseTypeEnum {

    MYSQL("mysql", "MySQL", "com.mysql.cj.jdbc.Driver", "jdbc:mysql://${host}:${port}/${database}?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai"),
    POSTGRESQL("postgresql", "PostgreSQL", "org.postgresql.Driver", "jdbc:postgresql://${host}:${port}/${database}"),
    ORACLE("oracle", "Oracle", "oracle.jdbc.OracleDriver", "jdbc:oracle:thin:@${host}:${port}:${database}"),
    SQLSERVER("sqlserver", "SQL Server", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "jdbc:sqlserver://${host}:${port};databaseName=${database}"),
    ;

    /**
     * 类型编码
     */
    private final String code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * JDBC 驱动类
     */
    private final String driverClass;

    /**
     * JDBC URL 模板
     */
    private final String urlTemplate;

    /**
     * 根据 code 获取枚举
     *
     * @param code 类型编码
     * @return 对应的枚举值，未找到返回 null
     */
    public static DatabaseTypeEnum getByCode(String code) {
        for (DatabaseTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

}
