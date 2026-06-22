package com.huanniankj.module.analysis.dal.mysql;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.analysis.controller.admin.vo.RetentionConfigPageReqVO;
import com.huanniankj.module.analysis.dal.dataobject.RetentionConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 留存分析配置 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface RetentionConfigMapper extends BaseMapperX<RetentionConfigDO> {

    default PageResult<RetentionConfigDO> selectPage(RetentionConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<RetentionConfigDO>()
                .likeIfPresent(RetentionConfigDO::getName, reqVO.getName())
                .eqIfPresent(RetentionConfigDO::getRetentionType, reqVO.getRetentionType())
                .orderByDesc(RetentionConfigDO::getId));
    }

}
