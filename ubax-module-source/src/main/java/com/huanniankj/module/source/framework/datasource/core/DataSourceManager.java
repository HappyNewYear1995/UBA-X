package com.huanniankj.module.source.framework.datasource.core;

import com.huanniankj.module.source.dal.dataobject.database.DatabaseDO;
import com.huanniankj.module.source.dal.mysql.database.DatabaseMapper;
import com.huanniankj.module.source.enums.datasource.DatabaseTypeEnum;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 动态数据源管理器
 * <p>
 * 负责在应用运行时动态创建、缓存、销毁数据库连接池，支持热插拔。
 * 使用线程安全的 ConcurrentHashMap 缓存已创建的数据源实例。
 * 应用启动时自动加载已配置的数据源，关闭时自动销毁所有连接池。
 *
 * @author zhaoff
 */
@Slf4j
@Component
public class DataSourceManager {

    private final DatabaseMapper databaseMapper;

    /**
     * 数据源缓存，Key 为数据源 ID
     */
    private final Map<Long, DataSource> dataSourceCache = new ConcurrentHashMap<>();

    public DataSourceManager(DatabaseMapper databaseMapper) {
        this.databaseMapper = databaseMapper;
    }

    /**
     * 应用启动时，加载所有已配置的数据源到内存
     */
    @PostConstruct
    public void init() {
        List<DatabaseDO> configs = databaseMapper.selectList();
        for (DatabaseDO config : configs) {
            try {
                dataSourceCache.put(config.getId(), createDataSource(config));
                log.info("初始化加载数据源: id={}, name={}", config.getId(), config.getName());
            } catch (Exception e) {
                log.warn("初始化加载数据源失败，将在首次使用时重试: id={}, name={}, error={}",
                        config.getId(), config.getName(), e.getMessage());
            }
        }
        log.info("数据源初始化完成，已加载 {} 个数据源", dataSourceCache.size());
    }

    /**
     * 应用关闭时，销毁所有数据源连接池
     */
    @PreDestroy
    public void destroy() {
        destroyAll();
    }

    /**
     * 获取或创建数据源
     *
     * @param databaseSource 数据源配置
     * @return 数据源实例
     */
    public DataSource getOrCreateDataSource(DatabaseDO databaseSource) {
        return dataSourceCache.computeIfAbsent(databaseSource.getId(), key -> createDataSource(databaseSource));
    }

    /**
     * 创建并缓存数据源连接池
     *
     * @param databaseSource 数据源配置
     */
    public void createAndCacheDataSource(DatabaseDO databaseSource) {
        DataSource dataSource = createDataSource(databaseSource);
        dataSourceCache.put(databaseSource.getId(), dataSource);
    }

    /**
     * 创建数据源连接池（不缓存）
     *
     * @param databaseSource 数据源配置
     * @return HikariCP 数据源实例
     */
    public DataSource createDataSource(DatabaseDO databaseSource) {
        DatabaseTypeEnum dbType = DatabaseTypeEnum.getByCode(databaseSource.getDbType());
        if (dbType == null) {
            throw new IllegalArgumentException("不支持的数据库类型: " + databaseSource.getDbType());
        }

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(dbType.getDriverClass());

        // 构建 JDBC URL
        String jdbcUrl = databaseSource.getUrl();
        if (jdbcUrl == null || jdbcUrl.isEmpty()) {
            jdbcUrl = buildJdbcUrl(dbType, databaseSource);
        }
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(databaseSource.getUsername());
        hikariConfig.setPassword(databaseSource.getPassword());

        // 配置 SSL 和连接参数
        configureSslAndParams(hikariConfig, dbType, databaseSource);

        // 连接池配置
        hikariConfig.setMaximumPoolSize(databaseSource.getMaxPoolSize() != null ? databaseSource.getMaxPoolSize() : 20);
        hikariConfig.setConnectionTimeout(databaseSource.getConnectionTimeout() != null ? databaseSource.getConnectionTimeout() : 30000L);
        hikariConfig.setIdleTimeout(600000L);
        hikariConfig.setMaxLifetime(1800000L);
        hikariConfig.setPoolName("DynamicPool-" + databaseSource.getId());

        log.info("创建数据源连接池: id={}, name={}, url={}", databaseSource.getId(), databaseSource.getName(), jdbcUrl);
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 根据数据库类型和配置构建 JDBC URL
     *
     * @param dbType         数据库类型
     * @param databaseSource 数据库数据源
     * @return JDBC URL
     */
    private String buildJdbcUrl(DatabaseTypeEnum dbType, DatabaseDO databaseSource) {
        String urlTemplate = dbType.getUrlTemplate()
                .replace("${host}", databaseSource.getHost())
                .replace("${port}", String.valueOf(databaseSource.getPort()))
                .replace("${database}", databaseSource.getDatabase());

        String protocol = databaseSource.getProtocol();
        if (protocol != null && !protocol.isEmpty()) {
            urlTemplate = appendProtocolParams(urlTemplate, dbType, protocol);
        }

        String connectionParams = databaseSource.getConnectionParams();
        if (connectionParams != null && !connectionParams.isEmpty()) {
            urlTemplate = appendConnectionParams(urlTemplate, connectionParams);
        }

        return urlTemplate;
    }

    /**
     * 根据协议类型追加 JDBC URL 参数
     *
     * @param urlTemplate URL 模板
     * @param dbType      数据库类型
     * @param protocol    协议
     * @return JDBC URL
     */
    private String appendProtocolParams(String urlTemplate, DatabaseTypeEnum dbType, String protocol) {
        return switch (protocol.toLowerCase()) {
            case "ssl" -> switch (dbType) {
                case MYSQL -> urlTemplate.contains("?")
                        ? urlTemplate + "&useSSL=true&requireSSL=true"
                        : urlTemplate + "?useSSL=true&requireSSL=true";
                case POSTGRESQL -> urlTemplate.contains("?")
                        ? urlTemplate + "&ssl=true"
                        : urlTemplate + "?ssl=true";
                case ORACLE -> urlTemplate;
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
            default -> urlTemplate;
        };
    }

    /**
     * 追加额外连接参数 (JSON 格式)
     *
     * @param urlTemplate      URL模板
     * @param connectionParams 连接参数
     * @return JDBC URL
     */
    private String appendConnectionParams(String urlTemplate, String connectionParams) {
        String cleaned = connectionParams.trim();
        if (cleaned.startsWith("{") && cleaned.endsWith("}")) {
            cleaned = cleaned.substring(1, cleaned.length() - 1);
        }

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
     * @param hikariConfig   hikari 配置
     * @param dbType         数据库类型
     * @param databaseSource 数据库数据源
     */
    private void configureSslAndParams(HikariConfig hikariConfig, DatabaseTypeEnum dbType, DatabaseDO databaseSource) {
        Properties dsProps = new Properties();

        String protocol = databaseSource.getProtocol();
        if (protocol != null && !protocol.isEmpty()) {
            String sslCertPath = databaseSource.getSslCertPath();
            String sslKeyPath = databaseSource.getSslKeyPath();
            String sslCaPath = databaseSource.getSslCaPath();

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
     * 刷新数据源 (先关闭旧连接池，再创建新的并放入缓存)
     *
     * @param databaseSource 新的数据源配置
     */
    public void refreshDataSource(DatabaseDO databaseSource) {
        removeDataSource(databaseSource.getId());
        dataSourceCache.put(databaseSource.getId(), createDataSource(databaseSource));
        log.info("刷新数据源连接池: id={}", databaseSource.getId());
    }

    /**
     * 测试数据源连接（创建临时连接池，测试后立即关闭）
     *
     * @param databaseSource 数据源配置
     * @return 连接是否成功
     */
    public boolean testConnection(DatabaseDO databaseSource) {
        try (HikariDataSource testDataSource = (HikariDataSource) createDataSource(databaseSource)) {
            try (Connection conn = testDataSource.getConnection()) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            log.error("数据源连接测试失败: id={}, error={}", databaseSource.getId(), e.getMessage());
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
