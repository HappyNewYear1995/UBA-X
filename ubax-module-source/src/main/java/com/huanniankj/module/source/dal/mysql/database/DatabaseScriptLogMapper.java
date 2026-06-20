package com.huanniankj.module.source.dal.mysql.database;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.database.vo.DatabaseScriptLogPageReqVO;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseScriptLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据据脚本执行日志 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface DatabaseScriptLogMapper extends BaseMapperX<DatabaseScriptLogDO> {

    /**
     * 分页查询执行日志
     *
     * @param pageReqVO 分页请求
     * @return 执行日志分页结果
     */
    default PageResult<DatabaseScriptLogDO> selectPage(DatabaseScriptLogPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<DatabaseScriptLogDO>()
                .eqIfPresent(DatabaseScriptLogDO::getScriptId, pageReqVO.getScriptId())
                .eqIfPresent(DatabaseScriptLogDO::getDatabaseId, pageReqVO.getDatabaseId())
                .eqIfPresent(DatabaseScriptLogDO::getStatus, pageReqVO.getStatus())
                .eqIfPresent(DatabaseScriptLogDO::getExecuteType, pageReqVO.getExecuteType())
                .betweenIfPresent(DatabaseScriptLogDO::getCreateTime, pageReqVO.getBeginTime(), pageReqVO.getEndTime())
                .orderByDesc(DatabaseScriptLogDO::getId));
    }

}
