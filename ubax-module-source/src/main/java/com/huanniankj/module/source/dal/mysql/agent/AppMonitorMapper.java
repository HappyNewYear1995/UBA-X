package com.huanniankj.module.source.dal.mysql.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.framework.mybatis.core.mapper.BaseMapperX;
import com.huanniankj.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.huanniankj.module.source.controller.agent.vo.AgentMonitorPageReqVO;
import com.huanniankj.module.source.dal.dataobject.agent.AgentMonitorMetricDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运行监控指标 Mapper
 *
 * @author zhaoff
 */
@Mapper
public interface AppMonitorMapper extends BaseMapperX<AgentMonitorMetricDO> {

    default PageResult<AgentMonitorMetricDO> selectPage(AgentMonitorPageReqVO reqVO) {
        LambdaQueryWrapperX<AgentMonitorMetricDO> query = new LambdaQueryWrapperX<AgentMonitorMetricDO>()
                .eqIfPresent(AgentMonitorMetricDO::getAgentId, reqVO.getAgentUuid())
                .eqIfPresent(AgentMonitorMetricDO::getMetricType, reqVO.getMetricType())
                .eqIfPresent(AgentMonitorMetricDO::getMetricName, reqVO.getMetricName())
                .eqIfPresent(AgentMonitorMetricDO::getTimeGranularity, reqVO.getTimeGranularity())
                .betweenIfPresent(AgentMonitorMetricDO::getMetricTime, reqVO.getMetricTime())
                .betweenIfPresent(AgentMonitorMetricDO::getCreateTime, reqVO.getCreateTime());
        query.orderByDesc(AgentMonitorMetricDO::getMetricTime);
        return selectPage(reqVO, query);
    }

    default List<AgentMonitorMetricDO> selectListByAgentUuidAndMetricType(String agentUuid, String metricType) {
        return selectList(new LambdaQueryWrapperX<AgentMonitorMetricDO>()
                .eq(AgentMonitorMetricDO::getAgentId, agentUuid)
                .eq(AgentMonitorMetricDO::getMetricType, metricType)
                .orderByDesc(AgentMonitorMetricDO::getMetricTime));
    }

    default List<AgentMonitorMetricDO> selectListByAgentUuidAndTimeRange(String agentUuid, LocalDateTime startTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<AgentMonitorMetricDO>()
                .eq(AgentMonitorMetricDO::getAgentId, agentUuid)
                .between(AgentMonitorMetricDO::getMetricTime, startTime, endTime)
                .orderByAsc(AgentMonitorMetricDO::getMetricTime));
    }

    default Double selectAvgValueByAgentUuidAndMetricType(String agentUuid, String metricType, LocalDateTime startTime, LocalDateTime endTime) {
        return selectObjs(new LambdaQueryWrapperX<AgentMonitorMetricDO>()
                        .select(AgentMonitorMetricDO::getMetricValue)
                        .eq(AgentMonitorMetricDO::getAgentId, agentUuid)
                        .eq(AgentMonitorMetricDO::getMetricType, metricType)
                        .between(AgentMonitorMetricDO::getMetricTime, startTime, endTime))
                .stream()
                .mapToDouble(obj -> ((Number) obj).doubleValue())
                .average()
                .orElse(0.0);
    }

}
