package com.huanniankj.module.analysis.dal.mysql.funnel;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.analysis.controller.funnel.vo.FunnelConfigPageReqVO;
import com.huanniankj.module.analysis.dal.dataobject.funnel.FunnelConfigDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 漏斗分析配置 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface FunnelConfigMapper extends BaseMapperX<FunnelConfigDO> {

    default PageResult<FunnelConfigDO> selectPage(FunnelConfigPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<FunnelConfigDO>()
                .likeIfPresent(FunnelConfigDO::getName, reqVO.getName())
                .orderByDesc(FunnelConfigDO::getId));
    }

}
