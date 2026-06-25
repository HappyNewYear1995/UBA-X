package com.huanniankj.module.app.controller.alertrecord.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 告警记录分页 ReqVO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "告警记录分页请求")
public class AlertRecordPageReqVO extends PageParam {

    @Schema(description = "规则 ID", example = "1")
    private Long ruleId;

    @Schema(description = "告警类型", example = "1")
    private Integer alertType;

    @Schema(description = "告警级别", example = "1")
    private Integer alertLevel;

    @Schema(description = "通知状态", example = "0")
    private Integer notificationStatus;

    @Schema(description = "是否已确认", example = "false")
    private Boolean acknowledged;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
