package com.huanniankj.module.source.service.agent;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.source.controller.agent.vo.AgentMonitorPageReqVO;
import com.huanniankj.module.source.controller.agent.vo.AgentMonitorRespVO;
import com.huanniankj.module.source.controller.agent.vo.AgentMonitorSaveReqVO;
import com.huanniankj.module.source.controller.agent.vo.AgentMonitorStatisticsRespVO;
import com.huanniankj.module.source.convert.agent.MonitorMetricConvert;
import com.huanniankj.module.source.dal.dataobject.agent.AgentMonitorMetricDO;
import com.huanniankj.module.source.dal.mysql.agent.AgentLogMapper;
import com.huanniankj.module.source.dal.mysql.agent.AppMonitorMapper;
import com.huanniankj.module.source.enums.agent.EventLevelEnum;
import com.huanniankj.module.source.enums.agent.MetricTypeEnum;
import com.huanniankj.module.source.enums.agent.TimeGranularityEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.huanniankj.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.huanniankj.module.source.enums.ErrorCodeConstants.MONITOR_METRIC_NOT_EXISTS;

/**
 * 运行监控服务实现
 *
 * @author zhaoff
 */
@Service
@Slf4j
public class AgentMonitorServiceImpl implements AgentMonitorService {

    @Resource
    private AppMonitorMapper appMonitorMapper;

    @Resource
    private AgentLogMapper agentLogMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMetric(AgentMonitorSaveReqVO saveReqVO) {
        AgentMonitorMetricDO metric = MonitorMetricConvert.INSTANCE.convert(saveReqVO);
        if (metric.getMetricTime() == null) {
            metric.setMetricTime(LocalDateTime.now());
        }
        appMonitorMapper.insert(metric);
        log.info("监控指标已创建: id={}, type={}, name={}, value={}",
                metric.getId(), metric.getMetricType(), metric.getMetricName(), metric.getMetricValue());
        return metric.getId();
    }

    @Override
    public AgentMonitorRespVO getMetric(Long id) {
        AgentMonitorMetricDO metric = appMonitorMapper.selectById(id);
        if (metric == null) {
            throw exception(MONITOR_METRIC_NOT_EXISTS);
        }
        return convertToRespVO(metric);
    }

    @Override
    public PageResult<AgentMonitorRespVO> getMetricPage(AgentMonitorPageReqVO pageReqVO) {
        PageResult<AgentMonitorMetricDO> pageResult = appMonitorMapper.selectPage(pageReqVO);
        return MonitorMetricConvert.INSTANCE.convertPage(pageResult);
    }

    @Override
    public List<AgentMonitorRespVO> getMetricListByAgentUuidAndType(String agentUuid, String metricType) {
        List<AgentMonitorMetricDO> metrics = appMonitorMapper.selectListByAgentUuidAndMetricType(agentUuid, metricType);
        return metrics.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<AgentMonitorRespVO> getMetricListByAgentUuidAndTimeRange(String agentUuid, LocalDateTime startTime, LocalDateTime endTime) {
        List<AgentMonitorMetricDO> metrics = appMonitorMapper.selectListByAgentUuidAndTimeRange(agentUuid, startTime, endTime);
        return metrics.stream()
                .map(this::convertToRespVO)
                .collect(Collectors.toList());
    }

    @Override
    public AgentMonitorStatisticsRespVO getAgentStatistics(String agentUuid, LocalDateTime startTime, LocalDateTime endTime) {
        AgentMonitorStatisticsRespVO statistics = new AgentMonitorStatisticsRespVO();
        statistics.setAgentUuid(agentUuid);

        // 统计各事件级别数量
        Map<Integer, Long> eventLevelCountMap = new HashMap<>();
        for (EventLevelEnum level : EventLevelEnum.values()) {
            Long count = agentLogMapper.selectCountByAgentUuidAndLevel(agentUuid, level.getLevel());
            eventLevelCountMap.put(level.getLevel(), count);
        }
        statistics.setEventLevelCountMap(eventLevelCountMap);

        // 事件总数
        Long totalEventCount = eventLevelCountMap.values().stream().mapToLong(Long::longValue).sum();
        statistics.setTotalEventCount(totalEventCount);

        // 成功事件数（INFO 级别视为成功）
        Long successEventCount = eventLevelCountMap.getOrDefault(EventLevelEnum.INFO.getLevel(), 0L);
        statistics.setSuccessEventCount(successEventCount);

        // 失败事件数（WARNING、ERROR、CRITICAL 视为失败）
        Long failedEventCount = totalEventCount - successEventCount;
        statistics.setFailedEventCount(failedEventCount);

        // 成功率
        if (totalEventCount > 0) {
            statistics.setSuccessRate((double) successEventCount / totalEventCount * 100);
            statistics.setErrorRate((double) failedEventCount / totalEventCount * 100);
        } else {
            statistics.setSuccessRate(100.0);
            statistics.setErrorRate(0.0);
        }

        // 获取平均延迟
        Double avgLatency = appMonitorMapper.selectAvgValueByAgentUuidAndMetricType(
                agentUuid, MetricTypeEnum.LATENCY.getType(), startTime, endTime);
        statistics.setAvgLatency(avgLatency);

        // 获取吞吐量
        Double throughput = appMonitorMapper.selectAvgValueByAgentUuidAndMetricType(
                agentUuid, MetricTypeEnum.THROUGHPUT.getType(), startTime, endTime);
        statistics.setThroughput(throughput);

        // 获取带宽使用
        Double bandwidth = appMonitorMapper.selectAvgValueByAgentUuidAndMetricType(
                agentUuid, MetricTypeEnum.BANDWIDTH.getType(), startTime, endTime);
        statistics.setBandwidthUsage(bandwidth);

        log.info("Agent 监控统计信息: uuid={}, totalEvents={}, successRate={}%, errorRate={}%",
                agentUuid, totalEventCount, statistics.getSuccessRate(), statistics.getErrorRate());

        return statistics;
    }

    /**
     * 将 MonitorMetricDO 转换为 MonitorMetricRespVO，并填充枚举名称
     */
    private AgentMonitorRespVO convertToRespVO(AgentMonitorMetricDO metric) {
        AgentMonitorRespVO respVO = MonitorMetricConvert.INSTANCE.convert(metric);
        respVO.setMetricTypeName(getMetricTypeName(metric.getMetricType()));
        respVO.setTimeGranularityName(getTimeGranularityName(metric.getTimeGranularity()));
        return respVO;
    }

    /**
     * 获取指标类型名称
     */
    private String getMetricTypeName(String type) {
        if (type == null) {
            return null;
        }
        for (MetricTypeEnum value : MetricTypeEnum.values()) {
            if (value.getType().equals(type)) {
                return value.getName();
            }
        }
        return null;
    }

    /**
     * 获取时间粒度名称
     */
    private String getTimeGranularityName(String granularity) {
        if (granularity == null) {
            return null;
        }
        for (TimeGranularityEnum value : TimeGranularityEnum.values()) {
            if (value.getGranularity().equals(granularity)) {
                return value.getName();
            }
        }
        return null;
    }

}
