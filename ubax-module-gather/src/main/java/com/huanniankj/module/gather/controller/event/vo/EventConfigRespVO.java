package com.huanniankj.module.gather.controller.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件配置响应 VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - 事件配置响应 VO")
@Data
public class EventConfigRespVO {

    @Schema(description = "配置 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "HTTP Body 包含错误关键字")
    private String configName;

    @Schema(description = "配置描述", example = "匹配 HTTP Body 中包含错误关键字的事件")
    private String configDesc;

    @Schema(description = "匹配位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "http_body")
    private String matchPosition;

    @Schema(description = "匹配位置名", example = "HTTP Body")
    private String matchPositionName;

    @Schema(description = "匹配类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "contains")
    private String matchType;

    @Schema(description = "匹配类型名", example = "包含")
    private String matchTypeName;

    @Schema(description = "匹配规则值", requiredMode = Schema.RequiredMode.REQUIRED, example = "error")
    private String matchValue;

    @Schema(description = "匹配逻辑", example = "and")
    private String matchLogic;

    @Schema(description = "匹配逻辑名", example = "且（所有规则都需满足）")
    private String matchLogicName;

    @Schema(description = "匹配后事件类型", example = "http_error")
    private String eventType;

    @Schema(description = "匹配后事件级别", example = "3")
    private Integer eventLevel;

    @Schema(description = "匹配后事件级别名", example = "错误")
    private String eventLevelName;

    @Schema(description = "匹配后事件标题模板", example = "检测到 HTTP 错误: {matchValue}")
    private String eventTitleTemplate;

    @Schema(description = "是否启用", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean enabled;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "用于匹配 HTTP Body 中的错误关键字")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
