package com.huanniankj.module.analysis.dal.clickhouse;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.huanniankj.framework.datasource.core.enums.DataSourceEnum;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.module.analysis.dal.dataobject.EventAnalysisDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * EventAnalysis Mapper 接口
 * 负责查询 ClickHouse 中的用户行为宽表
 */
@Mapper
@DS(DataSourceEnum.CLICKHOUSE)
public interface EventAnalysisMapper extends BaseMapperX<EventAnalysisDO> {
}
