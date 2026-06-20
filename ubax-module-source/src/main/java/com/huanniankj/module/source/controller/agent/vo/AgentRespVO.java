package com.huanniankj.module.source.controller.agent.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Agent 响应 RespVO
 *
 * @author zhaoff
 */
@Schema(description = "管理后台 - Agent 响应 Response VO")
@Data
public class AgentRespVO {

    @Schema(description = "Agent ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "UUID", requiredMode = Schema.RequiredMode.REQUIRED)
    private String uuid;

    @Schema(description = "主机名", requiredMode = Schema.RequiredMode.REQUIRED)
    private String hostname;

    @Schema(description = "Agent 版本")
    private String version;

    @Schema(description = "终端类型")
    private Integer terminal;

    @Schema(description = "平台类型")
    private Integer platform;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "采集器状态")
    private String collectorStatus;

    @Schema(description = "在线状态")
    private Boolean online;

    @Schema(description = "最后心跳时间")
    private LocalDateTime lastHeartbeat;

    @Schema(description = "IP 地址")
    private String ip;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "配置信息")
    private String config;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
