package com.huanniankj.module.analysis.dal.mysql;

import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.analysis.dal.dataobject.FunnelResultDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 漏斗分析结果 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface FunnelResultMapper extends BaseMapperX<FunnelResultDO> {

    default List<FunnelResultDO> selectByConfigIdAndDateRange(Long configId, LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<FunnelResultDO>()
                .eq(FunnelResultDO::getConfigId, configId)
                .ge(FunnelResultDO::getStatDate, startDate)
                .le(FunnelResultDO::getStatDate, endDate)
                .orderByAsc(FunnelResultDO::getStatDate));
    }

}
