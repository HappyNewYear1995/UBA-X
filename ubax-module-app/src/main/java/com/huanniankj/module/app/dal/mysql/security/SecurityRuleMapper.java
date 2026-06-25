package com.huanniankj.module.app.dal.mysql.security;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.app.dal.dataobject.security.SecurityRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 安全检测规则 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface SecurityRuleMapper extends BaseMapperX<SecurityRuleDO> {

    default PageResult<SecurityRuleDO> selectPage(com.huanniankj.framework.common.pojo.PageParam pageParam,
                                                   String name, Integer detectionType, Integer severity,
                                                   String action, Boolean enabled) {
        return selectPage(pageParam, new LambdaQueryWrapperX<SecurityRuleDO>()
                .likeIfPresent(SecurityRuleDO::getName, name)
                .eqIfPresent(SecurityRuleDO::getDetectionType, detectionType)
                .eqIfPresent(SecurityRuleDO::getSeverity, severity)
                .eqIfPresent(SecurityRuleDO::getAction, action)
                .eqIfPresent(SecurityRuleDO::getEnabled, enabled)
                .orderByDesc(SecurityRuleDO::getId));
    }

    default List<SecurityRuleDO> selectByEnabled(Boolean enabled) {
        return selectList(SecurityRuleDO::getEnabled, enabled);
    }

    default List<SecurityRuleDO> selectByName(String name) {
        return selectList(new LambdaQueryWrapperX<SecurityRuleDO>()
                .like(SecurityRuleDO::getName, name));
    }

}
