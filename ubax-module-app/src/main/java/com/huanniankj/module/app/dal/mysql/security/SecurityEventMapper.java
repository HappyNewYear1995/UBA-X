package com.huanniankj.module.app.dal.mysql.security;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.app.dal.dataobject.security.SecurityEventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 安全检测事件 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface SecurityEventMapper extends BaseMapperX<SecurityEventDO> {

    default PageResult<SecurityEventDO> selectPage(com.huanniankj.framework.common.pojo.PageParam pageParam,
                                                    Long ruleId, Integer detectionType, Integer severity,
                                                    Boolean handled, String sourceIp) {
        return selectPage(pageParam, new LambdaQueryWrapperX<SecurityEventDO>()
                .eqIfPresent(SecurityEventDO::getRuleId, ruleId)
                .eqIfPresent(SecurityEventDO::getDetectionType, detectionType)
                .eqIfPresent(SecurityEventDO::getSeverity, severity)
                .eqIfPresent(SecurityEventDO::getHandled, handled)
                .likeIfPresent(SecurityEventDO::getSourceIp, sourceIp)
                .orderByDesc(SecurityEventDO::getId));
    }

    default List<SecurityEventDO> selectByRuleId(Long ruleId) {
        return selectList(SecurityEventDO::getRuleId, ruleId);
    }

    default List<SecurityEventDO> selectByHandled(Boolean handled) {
        return selectList(SecurityEventDO::getHandled, handled);
    }

}
