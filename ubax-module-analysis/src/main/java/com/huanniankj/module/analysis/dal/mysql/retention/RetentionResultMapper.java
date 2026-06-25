package com.huanniankj.module.analysis.dal.mysql.retention;

import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.analysis.dal.dataobject.retention.RetentionResultDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 留存分析结果 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface RetentionResultMapper extends BaseMapperX<RetentionResultDO> {

    default List<RetentionResultDO> selectByConfigIdAndDateRange(Long configId, LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<RetentionResultDO>()
                .eq(RetentionResultDO::getConfigId, configId)
                .ge(RetentionResultDO::getStatDate, startDate)
                .le(RetentionResultDO::getStatDate, endDate)
                .orderByAsc(RetentionResultDO::getStatDate));
    }

}
