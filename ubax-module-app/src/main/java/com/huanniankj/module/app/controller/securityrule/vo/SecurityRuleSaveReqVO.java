package com.huanniankj.module.app.controller.securityrule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 安全检测规则保存 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "安全检测规则保存请求")
public class SecurityRuleSaveReqVO {

    @Schema(description = "安全检测规则 ID", example = "1")
    private Long id;

    @Schema(description = "规则名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "SQL注入检测")
    @NotBlank(message = "规则名称不能为空")
    @Size(max = 100, message = "规则名称长度不能超过100个字符")
    private String name;

    @Schema(description = "检测类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "检测类型不能为空")
    private Integer detectionType;

    @Schema(description = "匹配模式", example = ".*SELECT.*FROM.*")
    private String pattern;

    @Schema(description = "严重级别", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "严重级别不能为空")
    private Integer severity;

    @Schema(description = "处理动作", example = "block")
    @Size(max = 20, message = "处理动作长度不能超过20个字符")
    private String action;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "备注", example = "SQL注入检测规则")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}
