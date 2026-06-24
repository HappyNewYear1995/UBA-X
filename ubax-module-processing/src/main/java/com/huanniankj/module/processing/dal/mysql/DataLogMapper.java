package com.huanniankj.module.processing.dal.mysql;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.processing.controller.vo.DataLogPageReqVO;
import com.huanniankj.module.processing.dal.dataobject.DataLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据日志 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface DataLogMapper extends BaseMapperX<DataLogDO> {

    default PageResult<DataLogDO> selectPage(DataLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<DataLogDO>()
                .eqIfPresent(DataLogDO::getEventType, reqVO.getEventType())
                .eqIfPresent(DataLogDO::getAppId, reqVO.getAppId())
                .likeIfPresent(DataLogDO::getDeviceId, reqVO.getDeviceId())
                .likeIfPresent(DataLogDO::getUserId, reqVO.getUserId())
                .orderByDesc(DataLogDO::getId));
    }

}
