package com.huanniankj.module.processing.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 事件信息 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "事件信息 Response VO")
@Data
public class EventRespVO {

    @Schema(description = "全局唯一标识", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "事件唯一标识（Snowflake/UUID v7）", requiredMode = Schema.RequiredMode.REQUIRED, example = "evt_1234567890")
    private String eventId;

    @Schema(description = "事件类型编码（分层命名空间，如 auth.login.success）", requiredMode = Schema.RequiredMode.REQUIRED, example = "auth.login.success")
    private String eventType;

    @Schema(description = "事件大类（authentication/access/network/admin/system）", example = "authentication")
    private String eventCategory;

    @Schema(description = "产生该事件的数据源标识", example = "src_001")
    private String sourceId;

    @Schema(description = "数据清洗管道标识", example = "2048")
    private Long pipelineId;

    @Schema(description = "行为主体唯一标识", example = "user_1001")
    private String actorId;

    @Schema(description = "主体类型（user/service_account/device/application）", example = "user")
    private String actorType;

    @Schema(description = "主体附加属性快照（JSON）", example = "{\"department\":\"IT\",\"role\":\"admin\"}")
    private String actorAttributes;

    @Schema(description = "事件发生的原始时间", example = "2024-01-01T12:00:00")
    private LocalDateTime eventTime;

    @Schema(description = "事件被系统摄入的时间", example = "2024-01-01T12:00:01")
    private LocalDateTime ingestTime;

    @Schema(description = "事件经清洗管道处理完成的时间", example = "2024-01-01T12:00:02")
    private LocalDateTime processTime;

    @Schema(description = "原始时区信息", example = "Asia/Shanghai")
    private String eventTimeZone;

    @Schema(description = "地理位置信息", example = "北京市朝阳区")
    private String location;

    @Schema(description = "具体动作（login/file_download/api_call等）", example = "login")
    private String action;

    @Schema(description = "操作的目标资源", example = "/api/v1/auth/login")
    private String targetResource;

    @Schema(description = "目标资源类型", example = "api_endpoint")
    private String targetResourceType;

    @Schema(description = "事件结果（success/failure/denied）", example = "success")
    private String result;

    @Schema(description = "事件严重等级（info/low/medium/high/critical）", example = "info")
    private String severity;

    @Schema(description = "原始事件数据完整保留（JSON）")
    private String rawEvent;

    @Schema(description = "清洗管道补充的上下文信息（JSON）", example = "{\"ip_location\":\"北京\",\"risk_score\":0.1}")
    private String enrichments;

    @Schema(description = "标签数组（JSON array）", example = "[\"login\",\"auth\"]")
    private String tags;

    @Schema(description = "关联的会话标识", example = "sess_abc123")
    private String sessionId;

    @Schema(description = "关联标识（串联相关事件）", example = "corr_xyz789")
    private String correlationId;

    @Schema(description = "备注", example = "用户正常登录")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
