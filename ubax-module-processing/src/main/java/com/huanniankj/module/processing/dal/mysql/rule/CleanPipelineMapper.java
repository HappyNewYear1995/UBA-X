package com.huanniankj.module.processing.dal.mysql.rule;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.processing.controller.rule.vo.CleanPipelinePageReqVO;
import com.huanniankj.module.processing.dal.dataobject.rule.CleanPipelineDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 清洗管道 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface CleanPipelineMapper extends BaseMapperX<CleanPipelineDO> {

    default PageResult<CleanPipelineDO> selectPage(CleanPipelinePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<CleanPipelineDO>()
                .likeIfPresent(CleanPipelineDO::getName, reqVO.getName())
                .eqIfPresent(CleanPipelineDO::getType, reqVO.getType())
                .eqIfPresent(CleanPipelineDO::getStatus, reqVO.getStatus())
                .eqIfPresent(CleanPipelineDO::getEventId, reqVO.getEventId())
                .orderByDesc(CleanPipelineDO::getId));
    }

    default CleanPipelineDO selectByName(String name) {
        return selectOne(CleanPipelineDO::getName, name);
    }

}
