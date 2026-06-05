package com.huanniankj.module.gather.service.monitor;

import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricPageReqVO;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricRespVO;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricSaveReqVO;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorStatisticsRespVO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 运行监控服务接口
 *
 * @author zhaoff
 */
public interface MonitorMetricService {

    /**
     * 创建监控指标
     *
     * @param saveReqVO 创建监控指标请求
     * @return 指标 ID
     */
    Long createMetric(MonitorMetricSaveReqVO saveReqVO);

    /**
     * 获取监控指标详情
     *
     * @param id 指标 ID
     * @return 指标详情
     */
    MonitorMetricRespVO getMetric(Long id);

    /**
     * 获取监控指标分页列表
     *
     * @param pageReqVO 分页查询
     * @return 指标分页结果
     */
    PageResult<MonitorMetricRespVO> getMetricPage(MonitorMetricPageReqVO pageReqVO);

    /**
     * 根据 Agent UUID 和指标类型获取指标列表
     *
     * @param agentUuid  Agent UUID
     * @param metricType 指标类型
     * @return 指标列表
     */
    List<MonitorMetricRespVO> getMetricListByAgentUuidAndType(String agentUuid, String metricType);

    /**
     * 根据 Agent UUID 和时间范围获取指标列表
     *
     * @param agentUuid Agent UUID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 指标列表
     */
    List<MonitorMetricRespVO> getMetricListByAgentUuidAndTimeRange(String agentUuid, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 获取 Agent 监控统计信息
     *
     * @param agentUuid Agent UUID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 监控统计信息
     */
    MonitorStatisticsRespVO getAgentStatistics(String agentUuid, LocalDateTime startTime, LocalDateTime endTime);

}
