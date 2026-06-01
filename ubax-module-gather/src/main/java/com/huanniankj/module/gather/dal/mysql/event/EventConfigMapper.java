package com.huanniankj.module.gather.dal.mysql.event;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.gather.controller.admin.event.vo.EventConfigPageReqVO;
import com.huanniankj.module.gather.dal.dataobject.event.EventConfigDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 事件配置 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface EventConfigMapper extends BaseMapperX<EventConfigDO> {

    default PageResult<EventConfigDO> selectPage(EventConfigPageReqVO reqVO) {
        LambdaQueryWrapperX<EventConfigDO> query = new LambdaQueryWrapperX<EventConfigDO>()
                .likeIfPresent(EventConfigDO::getConfigName, reqVO.getConfigName())
                .eqIfPresent(EventConfigDO::getMatchPosition, reqVO.getMatchPosition())
                .eqIfPresent(EventConfigDO::getMatchType, reqVO.getMatchType())
                .eqIfPresent(EventConfigDO::getEventType, reqVO.getEventType())
                .eqIfPresent(EventConfigDO::getEventLevel, reqVO.getEventLevel())
                .eqIfPresent(EventConfigDO::getEnabled, reqVO.getEnabled())
                .betweenIfPresent(EventConfigDO::getCreateTime, reqVO.getCreateTime());
        query.orderByAsc(EventConfigDO::getSort).orderByDesc(EventConfigDO::getCreateTime);
        return selectPage(reqVO, query);
    }

    default List<EventConfigDO> selectListByEnabled(Boolean enabled) {
        return selectList(new LambdaQueryWrapperX<EventConfigDO>()
                .eq(EventConfigDO::getEnabled, enabled)
                .orderByAsc(EventConfigDO::getSort));
    }

    default List<EventConfigDO> selectListByMatchPosition(String matchPosition) {
        return selectList(new LambdaQueryWrapperX<EventConfigDO>()
                .eq(EventConfigDO::getMatchPosition, matchPosition)
                .eq(EventConfigDO::getEnabled, true)
                .orderByAsc(EventConfigDO::getSort));
    }

}
