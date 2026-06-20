package com.huanniankj.module.source.dal.mysql.database;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.database.vo.DatabasePageReqVO;
import com.huanniankj.module.source.dal.dataobject.database.DatabaseDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 数据库数据源 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface DatabaseMapper extends BaseMapperX<DatabaseDO> {

    /**
     * 分页查询数据库数据源
     *
     * @param pageReqVO 分页查询条件
     * @return 分页结果
     */
    default PageResult<DatabaseDO> selectPage(DatabasePageReqVO pageReqVO) {
        return selectPage(pageReqVO, new LambdaQueryWrapperX<DatabaseDO>()
                .likeIfPresent(DatabaseDO::getName, pageReqVO.getName())
                .eqIfPresent(DatabaseDO::getDbType, pageReqVO.getDbType())
                .eqIfPresent(DatabaseDO::getStatus, pageReqVO.getStatus())
                .orderByDesc(DatabaseDO::getId));
    }

    /**
     * 根据数据源名称查询
     *
     * @param name 数据源名称
     * @return 数据源配置
     */
    default DatabaseDO selectByName(String name) {
        return selectOne(DatabaseDO::getName, name);
    }

}
