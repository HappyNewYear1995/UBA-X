package com.huanniankj.module.processing.dal.mysql.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.processing.controller.event.vo.EventConfigPageReqVO;
import com.huanniankj.module.processing.dal.dataobject.event.EventConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 事件配置 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface EventConfigMapper extends BaseMapperX<EventConfigDO> {

    default PageResult<EventConfigDO> selectPage(EventConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<EventConfigDO>()
                .likeIfPresent(EventConfigDO::getName, reqVO.getName())
                .likeIfPresent(EventConfigDO::getCode, reqVO.getCode())
                .eqIfPresent(EventConfigDO::getDataSourceType, reqVO.getDataSourceType())
                .eqIfPresent(EventConfigDO::getEventType, reqVO.getEventType())
                .eqIfPresent(EventConfigDO::getStatus, reqVO.getStatus())
                .orderByDesc(EventConfigDO::getId));
    }

    default EventConfigDO selectByCode(String code) {
        return selectOne(EventConfigDO::getCode, code);
    }

}
