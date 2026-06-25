package com.huanniankj.module.app.controller.monitor;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.module.app.controller.monitor.vo.MetricsTrendReqVO;
import com.huanniankj.module.app.controller.monitor.vo.MetricsTrendRespVO;
import com.huanniankj.module.app.controller.monitor.vo.RealtimeMetricsRespVO;
import com.huanniankj.module.app.service.monitor.MonitorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;

@Tag(name = "实时监控")
@RestController
@RequestMapping("/app/monitor")
@Validated
public class MonitorController {

    @Resource
    private MonitorService monitorService;

    @GetMapping("/realtime-metrics")
    @Operation(summary = "获得实时监控指标")
    @PreAuthorize("@ss.hasPermission('app:monitor:query')")
    public CommonResult<RealtimeMetricsRespVO> getRealtimeMetrics() {
        return success(monitorService.getRealtimeMetrics());
    }

    @GetMapping("/trend")
    @Operation(summary = "获得指标趋势")
    @PreAuthorize("@ss.hasPermission('app:monitor:query')")
    public CommonResult<List<MetricsTrendRespVO>> getMetricsTrend(@Valid MetricsTrendReqVO reqVO) {
        return success(monitorService.getMetricsTrend(reqVO));
    }

}
