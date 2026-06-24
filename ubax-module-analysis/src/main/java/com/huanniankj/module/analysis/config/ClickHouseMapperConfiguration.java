package com.huanniankj.module.analysis.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * ClickHouse Mapper 条件化配置
 *
 * 仅在配置了 ClickHouse 数据源时（spring.datasource.dynamic.datasource.clickhouse.url 存在），
 * 才扫描并注册 EventAnalysisMapper。
 *
 * 当 ClickHouse 数据源未配置时，EventAnalysisMapper 不会被创建，
 * 分析服务中的 @Autowired(required = false) 注入会得到 null，并优雅降级。
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.datasource.dynamic.datasource.clickhouse", name = "url")
@MapperScan(basePackages = "com.huanniankj.module.analysis.dal.clickhouse")
public class ClickHouseMapperConfiguration {
}
