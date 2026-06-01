package com.huanniankj.module.gather.controller.admin.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 事件配置保存请求 VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - 事件配置保存请求 VO")
@Data
public class EventConfigSaveReqVO {

    @Schema(description = "配置 ID，更新时必填", example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "HTTP Body 包含错误关键字")
    @NotBlank(message = "配置名称不能为空")
    private String configName;

    @Schema(description = "配置描述", example = "匹配 HTTP Body 中包含错误关键字的事件")
    private String configDesc;

    @Schema(description = "匹配位置", requiredMode = Schema.RequiredMode.REQUIRED, example = "http_body")
    @NotBlank(message = "匹配位置不能为空")
    private String matchPosition;

    @Schema(description = "匹配类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "contains")
    @NotBlank(message = "匹配类型不能为空")
    private String matchType;

    @Schema(description = "匹配规则值", requiredMode = Schema.RequiredMode.REQUIRED, example = "error")
    @NotBlank(message = "匹配规则值不能为空")
    private String matchValue;

    @Schema(description = "匹配逻辑", example = "and")
    private String matchLogic;

    @Schema(description = "匹配后事件类型", example = "http_error")
    private String eventType;

    @Schema(description = "匹配后事件级别", example = "3")
    private Integer eventLevel;

    @Schema(description = "匹配后事件标题模板", example = "检测到 HTTP 错误: {matchValue}")
    private String eventTitleTemplate;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "用于匹配 HTTP Body 中的错误关键字")
    private String remark;

}
