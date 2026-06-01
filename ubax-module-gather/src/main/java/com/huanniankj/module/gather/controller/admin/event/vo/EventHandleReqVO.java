package com.huanniankj.module.gather.controller.admin.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 事件处理请求 VO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - 事件处理请求 VO")
@Data
public class EventHandleReqVO {

    @Schema(description = "事件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "事件 ID 不能为空")
    private Long id;

    @Schema(description = "处理人", example = "admin")
    private String handler;

    @Schema(description = "处理备注", example = "已处理")
    private String handleRemark;

}
