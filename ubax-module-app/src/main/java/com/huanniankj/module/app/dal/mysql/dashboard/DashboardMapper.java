package com.huanniankj.module.app.dal.mysql.dashboard;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.app.dal.dataobject.dashboard.DashboardDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 数据看板 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface DashboardMapper extends BaseMapperX<DashboardDO> {

    default PageResult<DashboardDO> selectPage(com.huanniankj.framework.common.pojo.PageParam pageParam,
                                                String name, Boolean enabled) {
        return selectPage(pageParam, new LambdaQueryWrapperX<DashboardDO>()
                .likeIfPresent(DashboardDO::getName, name)
                .eqIfPresent(DashboardDO::getEnabled, enabled)
                .orderByDesc(DashboardDO::getId));
    }

    default List<DashboardDO> selectByEnabled(Boolean enabled) {
        return selectList(DashboardDO::getEnabled, enabled);
    }

    default List<DashboardDO> selectByName(String name) {
        return selectList(new LambdaQueryWrapperX<DashboardDO>()
                .like(DashboardDO::getName, name));
    }

}
