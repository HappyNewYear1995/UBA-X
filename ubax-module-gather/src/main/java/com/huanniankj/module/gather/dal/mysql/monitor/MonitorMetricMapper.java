package com.huanniankj.module.gather.dal.mysql.monitor;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricPageReqVO;
import com.huanniankj.module.gather.dal.dataobject.monitor.MonitorMetricDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运行监控指标 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface MonitorMetricMapper extends BaseMapperX<MonitorMetricDO> {

    default PageResult<MonitorMetricDO> selectPage(MonitorMetricPageReqVO reqVO) {
        LambdaQueryWrapperX<MonitorMetricDO> query = new LambdaQueryWrapperX<MonitorMetricDO>()
                .eqIfPresent(MonitorMetricDO::getAgentId, reqVO.getAgentUuid())
                .eqIfPresent(MonitorMetricDO::getMetricType, reqVO.getMetricType())
                .eqIfPresent(MonitorMetricDO::getMetricName, reqVO.getMetricName())
                .eqIfPresent(MonitorMetricDO::getTimeGranularity, reqVO.getTimeGranularity())
                .betweenIfPresent(MonitorMetricDO::getMetricTime, reqVO.getMetricTime())
                .betweenIfPresent(MonitorMetricDO::getCreateTime, reqVO.getCreateTime());
        query.orderByDesc(MonitorMetricDO::getMetricTime);
        return selectPage(reqVO, query);
    }

    default List<MonitorMetricDO> selectListByAgentUuidAndMetricType(String agentUuid, String metricType) {
        return selectList(new LambdaQueryWrapperX<MonitorMetricDO>()
                .eq(MonitorMetricDO::getAgentId, agentUuid)
                .eq(MonitorMetricDO::getMetricType, metricType)
                .orderByDesc(MonitorMetricDO::getMetricTime));
    }

    default List<MonitorMetricDO> selectListByAgentUuidAndTimeRange(String agentUuid, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<MonitorMetricDO>()
                .eq(MonitorMetricDO::getAgentId, agentUuid)
                .between(MonitorMetricDO::getMetricTime, startTime, endTime)
                .orderByAsc(MonitorMetricDO::getMetricTime));
    }

    default Double selectAvgValueByAgentUuidAndMetricType(String agentUuid, String metricType, LocalDateTime startTime, LocalDateTime endTime) {
        return selectObjs(new LambdaQueryWrapperX<MonitorMetricDO>()
                        .select(MonitorMetricDO::getMetricValue)
                        .eq(MonitorMetricDO::getAgentId, agentUuid)
                        .eq(MonitorMetricDO::getMetricType, metricType)
                        .between(MonitorMetricDO::getMetricTime, startTime, endTime))
                .stream()
                .mapToDouble(obj -> ((Number) obj).doubleValue())
                .average()
                .orElse(0.0);
    }

}
