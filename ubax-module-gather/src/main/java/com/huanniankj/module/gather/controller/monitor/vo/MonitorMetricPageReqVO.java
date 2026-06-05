package com.huanniankj.module.gather.controller.monitor.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 运行监控指标分页请求 VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - 运行监控指标分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MonitorMetricPageReqVO extends PageParam {

    @Schema(description = "Agent UUID", example = "agent-uuid-001")
    private String agentUuid;

    @Schema(description = "指标类型", example = "event_count")
    private String metricType;

    @Schema(description = "指标名称", example = "events_per_minute")
    private String metricName;

    @Schema(description = "时间粒度", example = "minute")
    private String timeGranularity;

    @Schema(description = "指标数据时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] metricTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
