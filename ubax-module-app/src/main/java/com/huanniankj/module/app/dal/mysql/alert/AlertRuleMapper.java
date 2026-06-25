package com.huanniankj.module.app.dal.mysql.alert;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.app.dal.dataobject.alert.AlertRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 告警规则 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AlertRuleMapper extends BaseMapperX<AlertRuleDO> {

    default PageResult<AlertRuleDO> selectPage(com.huanniankj.framework.common.pojo.PageParam pageParam,
                                                String name, Integer alertType, String metricName, Boolean enabled) {
        return selectPage(pageParam, new LambdaQueryWrapperX<AlertRuleDO>()
                .likeIfPresent(AlertRuleDO::getName, name)
                .eqIfPresent(AlertRuleDO::getAlertType, alertType)
                .likeIfPresent(AlertRuleDO::getMetricName, metricName)
                .eqIfPresent(AlertRuleDO::getEnabled, enabled)
                .orderByDesc(AlertRuleDO::getId));
    }

    default List<AlertRuleDO> selectByEnabled(Boolean enabled) {
        return selectList(AlertRuleDO::getEnabled, enabled);
    }

    default List<AlertRuleDO> selectByName(String name) {
        return selectList(new LambdaQueryWrapperX<AlertRuleDO>()
                .like(AlertRuleDO::getName, name));
    }

}
