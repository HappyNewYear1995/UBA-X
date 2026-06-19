package com.huanniankj.module.gather.framework.datasource.core;

import com.huanniankj.module.gather.dal.dataobject.database.DatabaseDO;
import com.huanniankj.module.gather.enums.datasource.DatabaseTypeEnum;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源管理器
 * <p>
 * 负责在应用运行时动态创建、缓存、销毁数据库连接池，支持热插拔。
 * 使用线程安全的 ConcurrentHashMap 缓存已创建的数据源实例。
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class DataSourceManager {

    /**
     * 数据源缓存，Key 为数据源 ID
     */
    private final Map<Long, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    /**
     * 获取或创建数据源
     *
     * @param config 数据源配置
     * @return 数据源实例
     */
    public DataSource getOrCreateDataSource(DatabaseDO config) {
        return dataSourceCache.computeIfAbsent(config.getId(), key -> createDataSource(config));
    }

    /**
     * 创建数据源连接池
     *
     * @param config 数据源配置
     * @return HikariCP 数据源实例
     */
    public DataSource createDataSource(DatabaseDO config) {
        DatabaseTypeEnum dbType = DatabaseTypeEnum.getByCode(config.getDbType());
        if (dbType == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + config.getDbType());
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(dbType.getDriverClass());

        // 构建 JDBC URL
        String jdbcUrl = config.getUrl();
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            jdbcUrl = buildJdbcUrl(dbType, config);
        }
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(config.getUsername());
        hikariConfig.setPassword(config.getPassword());

        // 配置 SSL 和连接参数
        configureSslAndParams(hikariConfig, dbType, config);

        // 连接池配置
        hikariConfig.setMaximumPoolSize(config.getMaxPoolSize() != null ? config.getMaxPoolSize() : 20);
        hikariConfig.setConnectionTimeout(config.getConnectionTimeout() != null ? config.getConnectionTimeout() : 30000L);
        hikariConfig.setIdleTimeout(600000L); // 10 分钟
        hikariConfig.setMaxLifetime(1800000L); // 30 分钟
        hikariConfig.setPoolName("DynamicPool-" + config.getId());

        log.info("创建数据源连接池: id={}, name={}, url={}", config.getId(), config.getName(), jdbcUrl);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 根据数据库类型和配置构建 JDBC URL
     *
     * @param dbType 数据库类型枚举
     * @param config 数据源配置
     * @return JDBC URL
     */
    private String buildJdbcUrl(DatabaseTypeEnum dbType, DatabaseDO config) {
        String urlTemplate = dbType.getUrlTemplate()
                .replace("${host}", config.getHost())
                .replace("${port}", String.valueOf(config.getPort()))
                .replace("${database}", config.getDatabase());

        // 根据协议类型追加 SSL 参数
        String protocol = config.getProtocol();
        if (protocol != null && !protocol.isEmpty()) {
            urlTemplate = appendProtocolParams(urlTemplate, dbType, protocol, config);
        }

        // 追加额外连接参数
        String connectionParams = config.getConnectionParams();
        if (connectionParams != null && !connectionParams.isEmpty()) {
            urlTemplate = appendConnectionParams(urlTemplate, connectionParams);
        }

        return urlTemplate;
    }

    /**
     * 根据协议类型追加 JDBC URL 参数
     *
     * @param urlTemplate 基础 URL
     * @param dbType      数据库类型
     * @param protocol    协议类型
     * @param config      数据源配置
     * @return 追加参数后的 URL
     */
    private String appendProtocolParams(String urlTemplate, DatabaseTypeEnum dbType, String protocol, DatabaseDO config) {
        return switch (protocol.toLowerCase()) {
            case "ssl" -> switch (dbType) {
                case MYSQL -> urlTemplate.contains("?")
                        ? urlTemplate + "&useSSL=true&requireSSL=true"
                        : urlTemplate + "?useSSL=true&requireSSL=true";
                case POSTGRESQL -> urlTemplate.contains("?")
                        ? urlTemplate + "&ssl=true"
                        : urlTemplate + "?ssl=true";
                case ORACLE -> urlTemplate; // Oracle SSL 通过 TNS 配置，URL 不变
                case SQLSERVER -> urlTemplate + ";encrypt=true";
            };
            case "ssl-verify" -> switch (dbType) {
                case MYSQL -> urlTemplate.contains("?")
                        ? urlTemplate + "&useSSL=true&verifyServerCertificate=true"
                        : urlTemplate + "?useSSL=true&verifyServerCertificate=true";
                case POSTGRESQL -> urlTemplate.contains("?")
                        ? urlTemplate + "&ssl=true&sslmode=verify-full"
                        : urlTemplate + "?ssl=true&sslmode=verify-full";
                case ORACLE -> urlTemplate;
                case SQLSERVER -> urlTemplate + ";encrypt=true;trustServerCertificate=false";
            };
            default -> urlTemplate; // tcp 默认不追加
        };
    }

    /**
     * 追加额外连接参数 (JSON 格式)
     *
     * @param urlTemplate      基础 URL
     * @param connectionParams JSON 格式的连接参数
     * @return 追加参数后的 URL
     */
    private String appendConnectionParams(String urlTemplate, String connectionParams) {
        // 简单解析 JSON 格式的连接参数，如 {"key1":"value1","key2":"value2"}
        String cleaned = connectionParams.trim();
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

        // 解析键值对
        String[] pairs = cleaned.split(",");
        StringBuilder params = new StringBuilder();
        boolean hasSeparator = urlTemplate.contains("?") || urlTemplate.contains(";");

        for (String pair : pairs) {
            String[] kv = pair.split(":", 2);
            if (kv.length == 2) {
                String key = kv[0].trim().replace("\"", "");
                String value = kv[1].trim().replace("\"", "");
                if (params.isEmpty()) {
                    params.append(hasSeparator ? "&" : "?");
                } else {
                    params.append("&");
                }
                params.append(key).append("=").append(value);
            }
        }

        return urlTemplate + params;
    }

    /**
     * 配置 SSL 和连接参数到 HikariCP
     *
     * @param hikariConfig HikariCP 配置
     * @param dbType       数据库类型
     * @param config       数据源配置
     */
    private void configureSslAndParams(HikariConfig hikariConfig, DatabaseTypeEnum dbType, DatabaseDO config) {
        Properties dsProps = new Properties();

        String protocol = config.getProtocol();
        if (protocol != null && !protocol.isEmpty()) {
            // 配置 SSL 证书
            String sslCertPath = config.getSslCertPath();
            String sslKeyPath = config.getSslKeyPath();
            String sslCaPath = config.getSslCaPath();

            switch (dbType) {
                case MYSQL -> {
                    if (sslCertPath != null) {
                        dsProps.setProperty("clientCertificateKeyStoreUrl", "file:" + sslCertPath);
                    }
                    if (sslCaPath != null) {
                        dsProps.setProperty("trustCertificateKeyStoreUrl", "file:" + sslCaPath);
                    }
                }
                case POSTGRESQL -> {
                    if (sslCertPath != null) {
                        dsProps.setProperty("sslcert", sslCertPath);
                    }
                    if (sslKeyPath != null) {
                        dsProps.setProperty("sslkey", sslKeyPath);
                    }
                    if (sslCaPath != null) {
                        dsProps.setProperty("sslrootcert", sslCaPath);
                    }
                    if ("ssl-verify".equals(protocol)) {
                        dsProps.setProperty("sslmode", "verify-full");
                    }
                }
                case ORACLE -> {
                    if (sslCaPath != null) {
                        dsProps.setProperty("oracle.net.ssl_server_dn_match", "true");
                        dsProps.setProperty("javax.net.ssl.trustStore", sslCaPath);
                    }
                }
                case SQLSERVER -> {
                    if ("ssl-verify".equals(protocol)) {
                        dsProps.setProperty("trustServerCertificate", "false");
                    }
                    if (sslCaPath != null) {
                        dsProps.setProperty("trustStore", sslCaPath);
                    }
                }
            }
        }

        if (!dsProps.isEmpty()) {
            hikariConfig.setDataSourceProperties(dsProps);
        }
    }

    /**
     * 移除并关闭数据源
     *
     * @param databaseId 数据源 ID
     */
    public void removeDataSource(Long databaseId) {
        DataSource dataSource = dataSourceCache.remove(databaseId);
        if (dataSource instanceof HikariDataSource hikariDataSource) {
            hikariDataSource.close();
            log.info("关闭并移除数据源连接池: id={}", databaseId);
        }
    }

    /**
     * 刷新数据源 (先关闭旧连接池，再创建新的)
     *
     * @param config 新的数据源配置
     */
    public void refreshDataSource(DatabaseDO config) {
        removeDataSource(config.getId());
        createDataSource(config);
        log.info("刷新数据源连接池: id={}", config.getId());
    }

    /**
     * 测试数据源连接
     *
     * @param config 数据源配置
     * @return 连接是否成功
     */
    public boolean testConnection(DatabaseDO config) {
        try (HikariDataSource testDataSource = (HikariDataSource) createDataSource(config)) {
            try (Connection conn = testDataSource.getConnection()) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            log.error("数据源连接测试失败: id={}, error={}", config.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * 获取缓存的数据源
     *
     * @param databaseId 数据源 ID
     * @return 数据源实例，若不存在返回 null
     */
    public DataSource getDataSource(Long databaseId) {
        return dataSourceCache.get(databaseId);
    }

    /**
     * 清空所有数据源缓存并关闭连接池
     */
    public void destroyAll() {
        dataSourceCache.forEach((id, dataSource) -> {
            if (dataSource instanceof HikariDataSource hikariDataSource) {
                hikariDataSource.close();
            }
        });
        dataSourceCache.clear();
        log.info("清空所有数据源连接池");
    }

}
