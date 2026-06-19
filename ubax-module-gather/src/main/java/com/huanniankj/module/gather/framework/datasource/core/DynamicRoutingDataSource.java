package com.huanniankj.module.gather.framework.datasource.core;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态路由数据源
 * <p>
 * 继承 Spring 的 {@link AbstractRoutingDataSource}，通过重写 {@link #determineCurrentLookupKey()} 方法，
 * 实现基于 ThreadLocal 的数据源动态切换。
 *
 * @author zhaoff
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    /**
     * 维护目标数据源 Map，用于动态增删数据源
     */
    private final Map<Object, Object> targetDataSources = new HashMap<>();

    /**
     * 构造方法
     *
     * @param defaultTargetDataSource 默认数据源
     * @param targetDataSources       目标数据源 Map
     */
    public DynamicRoutingDataSource(Object defaultTargetDataSource, Map<Object, Object> targetDataSources) {
        this.targetDataSources.putAll(targetDataSources);
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        super.setTargetDataSources(this.targetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * 决定当前使用的数据源 Key
     * <p>
     * Spring 在每次获取数据库连接前会自动调用该方法，
     * 从 {@link DataSourceContextHolder} 中获取当前线程的数据源 Key。
     *
     * @return 数据源 Key，若未设置则返回 null (使用默认数据源)
     */
    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceContextHolder.getDataSourceKey();
    }

    /**
     * 动态添加数据源
     * <p>
     * 在运行时新增数据源后调用此方法，使路由感知到新数据源。
     *
     * @param key        数据源 Key
     * @param dataSource 数据源实例
     */
    public void addDataSource(Object key, DataSource dataSource) {
        targetDataSources.put(key, dataSource);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

    /**
     * 动态移除数据源
     * <p>
     * 在运行时删除数据源配置后调用此方法，从路由中移除对应数据源。
     *
     * @param key 数据源 Key
     */
    public void removeDataSource(Object key) {
        targetDataSources.remove(key);
        super.setTargetDataSources(targetDataSources);
        super.afterPropertiesSet();
    }

}
