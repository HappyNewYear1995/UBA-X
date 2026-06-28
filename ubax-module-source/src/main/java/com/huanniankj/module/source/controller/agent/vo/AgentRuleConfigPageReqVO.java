package com.huanniankj.module.source.controller.agent.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * 事件配置分页请求 VO
 *
 * @author zhaoff
 */
@Schema(description = "事件配置分页请求 VO")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AgentRuleConfigPageReqVO extends PageParam {

    @Schema(description = "配置名称", example = "HTTP Body 包含错误关键字")
    private String configName;

    @Schema(description = "匹配位置", example = "http_body")
    private String matchPosition;

    @Schema(description = "匹配类型", example = "contains")
    private String matchType;

    @Schema(description = "匹配后事件类型", example = "http_error")
    private String eventType;

    @Schema(description = "匹配后事件级别", example = "3")
    private Integer eventLevel;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
