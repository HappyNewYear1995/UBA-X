package com.huanniankj.module.processing.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 清洗管道信息 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "清洗管道信息 Response VO")
@Data
public class CleanPipelineRespVO {

    @Schema(description = "管道 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "管道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "数据格式转换")
    private String name;

    @Schema(description = "管道类型（format_convert/data_clean/data_map/data_filter）", example = "data_clean")
    private String type;

    @Schema(description = "管道描述", example = "对采集数据进行格式转换处理")
    private String description;

    @Schema(description = "管道配置（JSON格式，定义清洗规则）")
    private String config;

    @Schema(description = "关联事件ID", example = "2048")
    private Long eventId;

    @Schema(description = "状态（0=禁用 1=启用）", example = "1")
    private Integer status;

    @Schema(description = "已处理数据量", example = "1000")
    private Long processedCount;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
