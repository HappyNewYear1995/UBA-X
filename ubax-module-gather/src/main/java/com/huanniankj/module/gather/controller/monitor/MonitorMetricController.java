package com.huanniankj.module.gather.controller.monitor;

import com.huanniankj.framework.common.pojo.CommonResult;
import com.huanniankj.framework.common.pojo.PageResult;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricPageReqVO;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricRespVO;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorMetricSaveReqVO;
import com.huanniankj.module.gather.controller.monitor.vo.MonitorStatisticsRespVO;
import com.huanniankj.module.gather.service.monitor.MonitorMetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import static com.huanniankj.framework.common.pojo.CommonResult.success;
import static com.huanniankj.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 运行监控控制层
 *
 * @author zhaoff
 */
@Tag(name = "管理后台 - 运行监控", description = "Agent 运行监控接口")
@RestController
@RequestMapping("/gather/monitor")
@Validated
public class MonitorMetricController {

    @Resource
    private MonitorMetricService monitorMetricService;

    @PostMapping("/metric/create")
    @Operation(summary = "创建监控指标", description = "用于 Agent 上报监控指标")
    @PermitAll
    public CommonResult<Long> createMetric(@Valid @RequestBody MonitorMetricSaveReqVO saveReqVO) {
        return success(monitorMetricService.createMetric(saveReqVO));
    }

    @GetMapping("/metric/get")
    @Operation(summary = "获得监控指标详情", description = "用于管理后台查看监控指标详情")
    @Parameter(name = "id", description = "指标 ID", required = true, example = "1")
    @PermitAll
    public CommonResult<MonitorMetricRespVO> getMetric(@RequestParam("id") Long id) {
        return success(monitorMetricService.getMetric(id));
    }

    @GetMapping("/metric/page")
    @Operation(summary = "获得监控指标分页列表", description = "用于管理后台查看监控指标列表")
    @PermitAll
    public CommonResult<PageResult<MonitorMetricRespVO>> getMetricPage(@Valid MonitorMetricPageReqVO pageReqVO) {
        return success(monitorMetricService.getMetricPage(pageReqVO));
    }

    @GetMapping("/metric/list-by-agent-and-type")
    @Operation(summary = "根据 Agent UUID 和指标类型获得监控指标列表", description = "用于查看指定 Agent 指定类型的监控指标")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @Parameter(name = "metricType", description = "指标类型", required = true, example = "event_count")
    @PermitAll
    public CommonResult<List<MonitorMetricRespVO>> getMetricListByAgentUuidAndType(
            @RequestParam("agentUuid") String agentUuid,
            @RequestParam("metricType") String metricType) {
        return success(monitorMetricService.getMetricListByAgentUuidAndType(agentUuid, metricType));
    }

    @GetMapping("/metric/list-by-agent-and-time")
    @Operation(summary = "根据 Agent UUID 和时间范围获得监控指标列表", description = "用于查看指定 Agent 指定时间范围的监控指标")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @Parameter(name = "startTime", description = "开始时间", required = true)
    @Parameter(name = "endTime", description = "结束时间", required = true)
    @PermitAll
    public CommonResult<List<MonitorMetricRespVO>> getMetricListByAgentUuidAndTimeRange(
            @RequestParam("agentUuid") String agentUuid,
            @RequestParam("startTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        return success(monitorMetricService.getMetricListByAgentUuidAndTimeRange(agentUuid, startTime, endTime));
    }

    @GetMapping("/statistics")
    @Operation(summary = "获得 Agent 监控统计信息", description = "用于查看指定 Agent 的监控统计信息，包括事件数、成功率、流量等")
    @Parameter(name = "agentUuid", description = "Agent UUID", required = true, example = "agent-uuid-001")
    @Parameter(name = "startTime", description = "开始时间", required = true)
    @Parameter(name = "endTime", description = "结束时间", required = true)
    @PermitAll
    public CommonResult<MonitorStatisticsRespVO> getAgentStatistics(
            @RequestParam("agentUuid") String agentUuid,
            @RequestParam("startTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime startTime,
            @RequestParam("endTime") @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND) LocalDateTime endTime) {
        return success(monitorMetricService.getAgentStatistics(agentUuid, startTime, endTime));
    }

}
