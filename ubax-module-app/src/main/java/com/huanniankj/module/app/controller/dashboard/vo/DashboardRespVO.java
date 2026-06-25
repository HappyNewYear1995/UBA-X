package com.huanniankj.module.app.controller.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据看板 RespVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "数据看板响应")
public class DashboardRespVO {

    @Schema(description = "数据看板 ID", example = "1")
    private Long id;

    @Schema(description = "看板名称", example = "运维监控看板")
    private String name;

    @Schema(description = "描述", example = "生产环境运维监控看板")
    private String description;

    @Schema(description = "布局配置 (JSON 格式)", example = "{\"layout\":[{\"x\":0,\"y\":0,\"w\":6,\"h\":4}]}")
    private String layoutConfig;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "备注", example = "主看板")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
