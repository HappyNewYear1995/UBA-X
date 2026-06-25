package com.huanniankj.module.app.service.monitor;

import com.huanniankj.module.app.controller.monitor.vo.MetricsTrendReqVO;
import com.huanniankj.module.app.controller.monitor.vo.MetricsTrendRespVO;
import com.huanniankj.module.app.controller.monitor.vo.RealtimeMetricsRespVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 实时监控 Service 实现类
 *
 * 当前返回默认数据。实际数据聚合需在 ubax-server 装配层通过注入
 * source/processing 模块的 Mapper 来实现，或通过定时任务预计算。
 */
@Service
@Validated
@Slf4j
public class MonitorServiceImpl implements MonitorService {

    @Override
    public RealtimeMetricsRespVO getRealtimeMetrics() {
        // TODO 对接 source/processing 模块数据后，替换为真实聚合查询
        log.debug("获取实时监控指标 - 当前返回默认数据");
        return RealtimeMetricsRespVO.builder()
                .activeUsers(0L)
                .pageViews(0L)
                .conversionRate(0.0)
                .anomalyEvents(0L)
                .build();
    }

    @Override
    public List<MetricsTrendRespVO> getMetricsTrend(MetricsTrendReqVO reqVO) {
        // TODO 对接 source/processing 模块数据后，替换为真实趋势查询
        log.debug("获取指标趋势 - 当前返回空数据, metricName={}", reqVO.getMetricName());
        return new ArrayList<>();
    }
}
