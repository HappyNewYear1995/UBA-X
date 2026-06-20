package com.huanniankj.module.source.dal.mysql.database;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.database.vo.DatabaseScriptPageReqVO;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseScriptDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据库脚本 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface DatabaseScriptMapper extends BaseMapperX<DatabaseScriptDO> {

    /**
     * 分页查询数据库脚本
     *
     * @param pageReqVO 分页参数
     * @return 数据库脚本
     */
    default PageResult<DatabaseScriptDO> selectPage(DatabaseScriptPageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<DatabaseScriptDO>()
                .likeIfPresent(DatabaseScriptDO::getName, pageReqVO.getName())
                .likeIfPresent(DatabaseScriptDO::getCode, pageReqVO.getCode())
                .eqIfPresent(DatabaseScriptDO::getDatabaseId, pageReqVO.getDatabaseId())
                .eqIfPresent(DatabaseScriptDO::getScriptType, pageReqVO.getScriptType())
                .eqIfPresent(DatabaseScriptDO::getStatus, pageReqVO.getStatus())
                .orderByDesc(DatabaseScriptDO::getId));
    }

    /**
     * 根据数据库脚本编码查询
     *
     * @param code 数据库脚本编码
     * @return 数据库脚本
     */
    default DatabaseScriptDO selectByCode(String code) {
        return selectOne(DatabaseScriptDO::getCode, code);
    }

}
