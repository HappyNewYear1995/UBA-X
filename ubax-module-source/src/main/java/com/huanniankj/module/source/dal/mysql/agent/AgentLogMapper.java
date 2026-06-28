package com.huanniankj.module.source.dal.mysql.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.agent.vo.AgentLogPageReqVO;
import com.huanniankj.module.source.dal.dataobject.agent.AgentLogDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * Agent 事件管理 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AgentLogMapper extends BaseMapperX<AgentLogDO> {

    default PageResult<AgentLogDO> selectPage(AgentLogPageReqVO reqVO) {
        LambdaQueryWrapperX<AgentLogDO> query = new LambdaQueryWrapperX<AgentLogDO>()
                .eqIfPresent(AgentLogDO::getAgentUuid, reqVO.getAgentUuid())
                .eqIfPresent(AgentLogDO::getEventType, reqVO.getEventType())
                .eqIfPresent(AgentLogDO::getEventLevel, reqVO.getEventLevel())
                .eqIfPresent(AgentLogDO::getEventSource, reqVO.getEventSource())
                .likeIfPresent(AgentLogDO::getTitle, reqVO.getTitle())
                .eqIfPresent(AgentLogDO::getHandled, reqVO.getHandled())
                .betweenIfPresent(AgentLogDO::getEventTime, reqVO.getEventTime())
                .betweenIfPresent(AgentLogDO::getCreateTime, reqVO.getCreateTime());
        query.orderByDesc(AgentLogDO::getEventTime);
        return selectPage(reqVO, query);
    }

    default List<AgentLogDO> selectListByAgentUuid(String agentUuid) {
        return selectList(AgentLogDO::getAgentUuid, agentUuid);
    }

    default Long selectCountByAgentUuidAndLevel(String agentUuid, Integer eventLevel) {
        return selectCount(new LambdaQueryWrapperX<AgentLogDO>()
                .eq(AgentLogDO::getAgentUuid, agentUuid)
                .eq(AgentLogDO::getEventLevel, eventLevel));
    }

    default Long selectCountByAgentUuidAndHandled(String agentUuid, Boolean handled) {
        return selectCount(new LambdaQueryWrapperX<AgentLogDO>()
                .eq(AgentLogDO::getAgentUuid, agentUuid)
                .eq(AgentLogDO::getHandled, handled));
    }

}
