package com.huanniankj.module.app.service.monitor;

import com.huanniankj.module.app.controller.monitor.vo.MetricsTrendReqVO;
import com.huanniankj.module.app.controller.monitor.vo.MetricsTrendRespVO;
import com.huanniankj.module.app.controller.monitor.vo.RealtimeMetricsRespVO;

import java.util.List;

/**
 * 实时监控 Service 接口
 *
 * @author zhaoff
 */
public interface MonitorService {

    /**
     * 获取实时监控指标
     *
     * @return 实时监控指标
     */
    RealtimeMetricsRespVO getRealtimeMetrics();

    /**
     * 获取指标趋势数据
     *
     * @param reqVO 查询条件
     * @return 指标趋势列表
     */
    List<MetricsTrendRespVO> getMetricsTrend(MetricsTrendReqVO reqVO);

}
