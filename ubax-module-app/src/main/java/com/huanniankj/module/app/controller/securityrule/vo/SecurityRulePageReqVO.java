package com.huanniankj.module.app.controller.securityrule.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 安全检测规则分页 ReqVO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "安全检测规则分页请求")
public class SecurityRulePageReqVO extends PageParam {

    @Schema(description = "规则名称", example = "SQL注入检测")
    private String name;

    @Schema(description = "检测类型", example = "1")
    private Integer detectionType;

    @Schema(description = "严重级别", example = "2")
    private Integer severity;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

}
