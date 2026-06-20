package com.huanniankj.module.source.controller.event.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 事件分页请求 VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - 事件分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EventPageReqVO extends PageParam {

    @Schema(description = "Agent UUID", example = "agent-uuid-001")
    private String agentUuid;

    @Schema(description = "事件类型", example = "custom_event")
    private String eventType;

    @Schema(description = "事件级别", example = "1")
    private Integer eventLevel;

    @Schema(description = "事件来源", example = "agent")
    private String eventSource;

    @Schema(description = "事件标题", example = "CPU 使用率过高")
    private String title;

    @Schema(description = "是否已处理", example = "true")
    private Boolean handled;

    @Schema(description = "事件发生时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] eventTime;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
