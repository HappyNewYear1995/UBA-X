package com.huanniankj.module.app.controller.securityevent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 安全检测事件 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "安全检测事件响应")
public class SecurityEventRespVO {

    @Schema(description = "安全检测事件 ID", example = "1")
    private Long id;

    @Schema(description = "规则 ID", example = "1")
    private Long ruleId;

    @Schema(description = "规则名称", example = "SQL注入检测")
    private String ruleName;

    @Schema(description = "检测类型", example = "1")
    private Integer detectionType;

    @Schema(description = "来源 IP", example = "192.168.1.100")
    private String sourceIp;

    @Schema(description = "目标资源", example = "/api/query")
    private String targetResource;

    @Schema(description = "事件详情", example = "检测到SQL注入攻击")
    private String eventDetail;

    @Schema(description = "严重级别", example = "2")
    private Integer severity;

    @Schema(description = "已采取动作", example = "block")
    private String actionTaken;

    @Schema(description = "是否已处理", example = "false")
    private Boolean handled;

    @Schema(description = "处理人", example = "admin")
    private String handler;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理备注", example = "已封禁IP")
    private String handleRemark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
