package com.huanniankj.module.gather.framework.datasource.core;

/**
 * 数据源上下文持有者
 * <p>
 * 使用 ThreadLocal 存储当前线程需要使用的数据源 Key，实现线程隔离的数据源切换。
 * 每次请求或任务执行完毕后，必须调用 {@link #clear()} 防止数据源污染。
 *
 * @author zhaoff
 */
public class DataSourceContextHolder {

    /**
     * 存储当前线程使用的数据源 Key
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的数据源 Key
     *
     * @param dataSourceKey 数据源唯一标识
     */
    public static void setDataSourceKey(String dataSourceKey) {
        CONTEXT_HOLDER.set(dataSourceKey);
    }

    /**
     * 获取当前线程的数据源 Key
     *
     * @return 数据源 Key
     */
    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的数据源 Key
     * <p>
     * 必须在 finally 块中调用，防止在线程池复用场景下发生数据源污染或内存泄漏。
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

}
