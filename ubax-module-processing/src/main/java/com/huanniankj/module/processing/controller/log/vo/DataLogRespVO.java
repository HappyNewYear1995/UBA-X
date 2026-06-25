package com.huanniankj.module.processing.controller.log.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据日志信息 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "数据日志信息 Response VO")
@Data
public class DataLogRespVO {

    @Schema(description = "日志 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "日志唯一标识", example = "log-20240101-001")
    private String logId;

    @Schema(description = "来源应用标识", example = "app-001")
    private String appId;

    @Schema(description = "事件类型", example = "page_view")
    private String eventType;

    @Schema(description = "设备ID", example = "device-001")
    private String deviceId;

    @Schema(description = "用户ID", example = "user-001")
    private String userId;

    @Schema(description = "平台", example = "web")
    private String platform;

    @Schema(description = "IP地址", example = "192.168.1.1")
    private String ip;

    @Schema(description = "User Agent")
    private String userAgent;

    @Schema(description = "事件属性（JSON格式）")
    private String properties;

    @Schema(description = "关联事件ID", example = "3072")
    private Long eventId;

    @Schema(description = "关联清洗管道ID", example = "2048")
    private Long pipelineId;

    @Schema(description = "采集时间")
    private LocalDateTime collectTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
