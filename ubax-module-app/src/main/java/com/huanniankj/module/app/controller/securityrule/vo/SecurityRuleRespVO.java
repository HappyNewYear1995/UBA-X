package com.huanniankj.module.app.controller.securityrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全检测规则 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "安全检测规则响应")
public class SecurityRuleRespVO {

    @Schema(description = "安全检测规则 ID", example = "1")
    private Long id;

    @Schema(description = "规则名称", example = "SQL注入检测")
    private String name;

    @Schema(description = "检测类型", example = "1")
    private Integer detectionType;

    @Schema(description = "匹配模式", example = ".*SELECT.*FROM.*")
    private String pattern;

    @Schema(description = "严重级别", example = "2")
    private Integer severity;

    @Schema(description = "处理动作", example = "block")
    private String action;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "备注", example = "SQL注入检测规则")
    private String remark;

    @Schema(description = "触发次数", example = "5")
    private Integer triggerCount;

    @Schema(description = "最后触发时间")
    private LocalDateTime lastTriggeredTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
