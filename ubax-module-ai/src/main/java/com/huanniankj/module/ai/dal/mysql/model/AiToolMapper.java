package com.huanniankj.module.ai.dal.mysql.model;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.ai.controller.admin.model.vo.tool.AiToolPageReqVO;
import com.huanniankj.module.ai.dal.dataobject.model.AiToolDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * AI 工具 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AiToolMapper extends BaseMapperX<AiToolDO> {

    default PageResult<AiToolDO> selectPage(AiToolPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<AiToolDO>()
                .likeIfPresent(AiToolDO::getName, reqVO.getName())
                .eqIfPresent(AiToolDO::getDescription, reqVO.getDescription())
                .eqIfPresent(AiToolDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(AiToolDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(AiToolDO::getId));
    }

    default List<AiToolDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<AiToolDO>()
                .eq(AiToolDO::getStatus, status)
                .orderByDesc(AiToolDO::getId));
    }

}
