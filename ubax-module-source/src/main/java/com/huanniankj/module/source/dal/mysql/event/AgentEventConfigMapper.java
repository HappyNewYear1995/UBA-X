package com.huanniankj.module.source.dal.mysql.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.agent.vo.EventConfigPageReqVO;
import com.huanniankj.module.source.dal.dataobject.agent.AgentRuleConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Agent 事件配置 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AgentEventConfigMapper extends BaseMapperX<AgentRuleConfigDO> {

    default PageResult<AgentRuleConfigDO> selectPage(EventConfigPageReqVO reqVO) {
        LambdaQueryWrapperX<AgentRuleConfigDO> query = new LambdaQueryWrapperX<AgentRuleConfigDO>()
                .likeIfPresent(AgentRuleConfigDO::getConfigName, reqVO.getConfigName())
                .eqIfPresent(AgentRuleConfigDO::getMatchPosition, reqVO.getMatchPosition())
                .eqIfPresent(AgentRuleConfigDO::getMatchType, reqVO.getMatchType())
                .eqIfPresent(AgentRuleConfigDO::getEventType, reqVO.getEventType())
                .eqIfPresent(AgentRuleConfigDO::getEventLevel, reqVO.getEventLevel())
                .eqIfPresent(AgentRuleConfigDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(AgentRuleConfigDO::getCreateTime, reqVO.getCreateTime());
        query.orderByAsc(AgentRuleConfigDO::getSort).orderByDesc(AgentRuleConfigDO::getCreateTime);
        return selectPage(reqVO, query);
    }

    default List<AgentRuleConfigDO> selectListByEnabled(Boolean enabled) {
        return selectList(new LambdaQueryWrapperX<AgentRuleConfigDO>()
                .eq(AgentRuleConfigDO::getEnabled, enabled)
                .orderByAsc(AgentRuleConfigDO::getSort));
    }

    default List<AgentRuleConfigDO> selectListByMatchPosition(String matchPosition) {
        return selectList(new LambdaQueryWrapperX<AgentRuleConfigDO>()
                .eq(AgentRuleConfigDO::getMatchPosition, matchPosition)
                .eq(AgentRuleConfigDO::getEnabled, true)
                .orderByAsc(AgentRuleConfigDO::getSort));
    }

}
