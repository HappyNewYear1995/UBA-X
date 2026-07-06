package com.huanniankj.module.source.dal.mysql.processing;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptPageReqVO;
import com.huanniankj.module.source.dal.dataobject.processing.ProcessingScriptDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessingScriptMapper extends BaseMapperX<ProcessingScriptDO> {

    default PageResult<ProcessingScriptDO> selectPage(ProcessingScriptPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<ProcessingScriptDO>()
                .likeIfPresent(ProcessingScriptDO::getName, reqVO.getName())
                .likeIfPresent(ProcessingScriptDO::getCode, reqVO.getCode())
                .eqIfPresent(ProcessingScriptDO::getStatus, reqVO.getStatus())
                .orderByDesc(ProcessingScriptDO::getId));
    }

    default ProcessingScriptDO selectByCode(String code) {
        return selectOne(ProcessingScriptDO::getCode, code);
    }

}
