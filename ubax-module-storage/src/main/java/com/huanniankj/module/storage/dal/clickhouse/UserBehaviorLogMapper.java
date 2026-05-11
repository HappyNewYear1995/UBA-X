package com.huanniankj.module.storage.dal.clickhouse;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.huanniankj.framework.datasource.core.enums.DataSourceEnum;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.module.storage.dal.dataobject.UserBehaviorLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserBehaviorLog Mapper 接口
 * 负责 ClickHouse ubax_user_behavior_log 表的持久化操作
 */
@Mapper
@DS(DataSourceEnum.CLICKHOUSE) // 指定强制走 clickhouse 数据源
public interface UserBehaviorLogMapper extends BaseMapperX<UserBehaviorLogDO> {
}
