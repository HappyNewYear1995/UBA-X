package com.huanniankj.module.infra.dal.mysql.db;

import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.module.infra.dal.dataobject.db.DataSourceConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据源配置 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface DataSourceConfigMapper extends BaseMapperX<DataSourceConfigDO> {
}
