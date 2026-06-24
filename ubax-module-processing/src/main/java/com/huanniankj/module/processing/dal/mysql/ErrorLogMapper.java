package com.huanniankj.module.processing.dal.mysql;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.processing.controller.vo.ErrorLogPageReqVO;
import com.huanniankj.module.processing.dal.dataobject.ErrorLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异常日志 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface ErrorLogMapper extends BaseMapperX<ErrorLogDO> {

    default PageResult<ErrorLogDO> selectPage(ErrorLogPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ErrorLogDO>()
                .eqIfPresent(ErrorLogDO::getErrorType, reqVO.getErrorType())
                .likeIfPresent(ErrorLogDO::getSource, reqVO.getSource())
                .eqIfPresent(ErrorLogDO::getStatus, reqVO.getStatus())
                .eqIfPresent(ErrorLogDO::getPipelineId, reqVO.getPipelineId())
                .eqIfPresent(ErrorLogDO::getEventId, reqVO.getEventId())
                .orderByDesc(ErrorLogDO::getId));
    }

}
