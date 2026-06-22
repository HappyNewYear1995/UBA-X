package com.huanniankj.module.analysis.dal.mysql;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.analysis.controller.admin.vo.PathConfigPageReqVO;
import com.huanniankj.module.analysis.dal.dataobject.PathConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 路径分析配置 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface PathConfigMapper extends BaseMapperX<PathConfigDO> {

    default PageResult<PathConfigDO> selectPage(PathConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<PathConfigDO>()
                .likeIfPresent(PathConfigDO::getName, reqVO.getName())
                .orderByDesc(PathConfigDO::getId));
    }

}
