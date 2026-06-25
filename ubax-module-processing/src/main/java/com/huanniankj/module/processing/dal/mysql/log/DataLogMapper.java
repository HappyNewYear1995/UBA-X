package com.huanniankj.module.processing.dal.mysql.log;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.processing.controller.log.vo.DataLogPageReqVO;
import com.huanniankj.module.processing.dal.dataobject.log.DataLogDO;
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
