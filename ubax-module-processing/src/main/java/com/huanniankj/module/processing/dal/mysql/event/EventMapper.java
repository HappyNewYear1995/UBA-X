package com.huanniankj.module.processing.dal.mysql.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.processing.controller.event.vo.EventPageReqVO;
import com.huanniankj.module.processing.dal.dataobject.event.EventDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事件 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface EventMapper extends BaseMapperX<EventDO> {

    default PageResult<EventDO> selectPage(EventPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EventDO>()
                .eqIfPresent(EventDO::getEventType, reqVO.getEventType())
                .eqIfPresent(EventDO::getEventCategory, reqVO.getEventCategory())
                .eqIfPresent(EventDO::getActorType, reqVO.getActorType())
                .eqIfPresent(EventDO::getResult, reqVO.getResult())
                .eqIfPresent(EventDO::getSeverity, reqVO.getSeverity())
                .eqIfPresent(EventDO::getSourceId, reqVO.getSourceId())
                .likeIfPresent(EventDO::getActorId, reqVO.getActorId())
                .likeIfPresent(EventDO::getAction, reqVO.getAction())
                .orderByDesc(EventDO::getId));
    }

}
