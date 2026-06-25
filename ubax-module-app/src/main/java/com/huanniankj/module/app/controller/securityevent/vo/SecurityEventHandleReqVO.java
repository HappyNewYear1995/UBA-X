package com.huanniankj.module.app.controller.securityevent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 安全检测事件处理 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "安全检测事件处理请求")
public class SecurityEventHandleReqVO {

    @Schema(description = "安全检测事件 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "安全检测事件 ID 不能为空")
    private Long id;

    @Schema(description = "处理备注", example = "已封禁IP")
    @Size(max = 500, message = "处理备注长度不能超过500个字符")
    private String handleRemark;

}
