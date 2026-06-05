package com.huanniankj.module.gather.dal.mysql.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.gather.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.gather.dal.dataobject.event.EventDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 事件管理 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface EventMapper extends BaseMapperX<EventDO> {

    default PageResult<EventDO> selectPage(EventPageReqVO reqVO) {
        LambdaQueryWrapperX<EventDO> query = new LambdaQueryWrapperX<EventDO>()
                .eqIfPresent(EventDO::getAgentUuid, reqVO.getAgentUuid())
                .eqIfPresent(EventDO::getEventType, reqVO.getEventType())
                .eqIfPresent(EventDO::getEventLevel, reqVO.getEventLevel())
                .eqIfPresent(EventDO::getEventSource, reqVO.getEventSource())
                .likeIfPresent(EventDO::getTitle, reqVO.getTitle())
                .eqIfPresent(EventDO::getHandled, reqVO.getHandled())
                .betweenIfPresent(EventDO::getEventTime, reqVO.getEventTime())
                .betweenIfPresent(EventDO::getCreateTime, reqVO.getCreateTime());
        query.orderByDesc(EventDO::getEventTime);
        return selectPage(reqVO, query);
    }

    default List<EventDO> selectListByAgentUuid(String agentUuid) {
        return selectList(EventDO::getAgentUuid, agentUuid);
    }

    default Long selectCountByAgentUuidAndLevel(String agentUuid, Integer eventLevel) {
        return selectCount(new LambdaQueryWrapperX<EventDO>()
                .eq(EventDO::getAgentUuid, agentUuid)
                .eq(EventDO::getEventLevel, eventLevel));
    }

    default Long selectCountByAgentUuidAndHandled(String agentUuid, Boolean handled) {
        return selectCount(new LambdaQueryWrapperX<EventDO>()
                .eq(EventDO::getAgentUuid, agentUuid)
                .eq(EventDO::getHandled, handled));
    }

}
