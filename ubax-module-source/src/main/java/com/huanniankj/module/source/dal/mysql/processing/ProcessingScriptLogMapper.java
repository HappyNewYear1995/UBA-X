package com.huanniankj.module.source.dal.mysql.processing;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.processing.vo.ProcessingScriptLogPageReqVO;
import com.huanniankj.module.source.dal.dataobject.processing.ProcessingScriptLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProcessingScriptLogMapper extends BaseMapperX<ProcessingScriptLogDO> {

    default PageResult<ProcessingScriptLogDO> selectPage(ProcessingScriptLogPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<ProcessingScriptLogDO>()
                .eqIfPresent(ProcessingScriptLogDO::getScriptId, pageReqVO.getScriptId())
                .eqIfPresent(ProcessingScriptLogDO::getStatus, pageReqVO.getStatus())
                .eqIfPresent(ProcessingScriptLogDO::getExecuteType, pageReqVO.getExecuteType())
                .betweenIfPresent(ProcessingScriptLogDO::getCreateTime, pageReqVO.getBeginTime(), pageReqVO.getEndTime())
                .orderByDesc(ProcessingScriptLogDO::getId));
    }
}
