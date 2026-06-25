package com.huanniankj.module.analysis.dal.clickhouse;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.huanniankj.framework.datasource.core.enums.DataSourceEnum;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.module.analysis.dal.dataobject.base.EventAnalysisDO;

/**
 * EventAnalysis Mapper 接口
 * 负责查询 ClickHouse 中的用户行为宽表
 * <p>
 * 注意：不使用 @Mapper 注解，避免被全局 @MapperScan 扫描到。
 * 改由 ClickHouseMapperConfiguration 条件化注册，仅在 ClickHouse 数据源配置时才创建此 Mapper。
 *
 * @author zhaoff
 */
@DS(DataSourceEnum.CLICKHOUSE)
public interface EventAnalysisMapper extends BaseMapperX<EventAnalysisDO> {
}
