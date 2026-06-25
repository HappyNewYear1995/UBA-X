package com.huanniankj.module.app.controller.alertrule.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 告警规则分页 ReqVO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "告警规则分页请求")
public class AlertRulePageReqVO extends PageParam {

    @Schema(description = "规则名称", example = "CPU使用率告警")
    private String name;

    @Schema(description = "告警类型", example = "1")
    private Integer alertType;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "通知类型", example = "email")
    private String notificationType;

}
