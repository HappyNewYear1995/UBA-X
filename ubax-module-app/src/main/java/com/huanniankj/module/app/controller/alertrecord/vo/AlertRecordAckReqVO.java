package com.huanniankj.module.app.controller.alertrecord.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 告警记录确认 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "告警记录确认请求")
public class AlertRecordAckReqVO {

    @Schema(description = "告警记录 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "告警记录 ID 不能为空")
    private Long id;

    @Schema(description = "备注", example = "已确认处理")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}
