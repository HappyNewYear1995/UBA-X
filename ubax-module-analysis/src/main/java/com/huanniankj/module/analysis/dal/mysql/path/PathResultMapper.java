package com.huanniankj.module.analysis.dal.mysql.path;

import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.analysis.dal.dataobject.path.PathResultDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

/**
 * 路径分析结果 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface PathResultMapper extends BaseMapperX<PathResultDO> {

    default List<PathResultDO> selectByConfigIdAndDateRange(Long configId, LocalDate startDate, LocalDate endDate) {
        return selectList(new LambdaQueryWrapperX<PathResultDO>()
                .eq(PathResultDO::getConfigId, configId)
                .ge(PathResultDO::getStatDate, startDate)
                .le(PathResultDO::getStatDate, endDate)
                .orderByAsc(PathResultDO::getStatDate));
    }

}
