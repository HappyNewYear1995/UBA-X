package com.huanniankj.module.processing.controller.rule.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 清洗管道创建/修改 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "清洗管道创建/修改 Request VO")
@Data
public class CleanPipelineSaveReqVO {

    @Schema(description = "管道 ID", example = "1024")
    private Long id;

    @Schema(description = "管道名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "数据格式转换")
    @NotBlank(message = "管道名称不能为空")
    @Size(max = 100, message = "管道名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "管道类型（format_convert/data_clean/data_map/data_filter）", example = "data_clean")
    private String type;

    @Schema(description = "管道描述", example = "对采集数据进行格式转换处理")
    private String description;

    @Schema(description = "管道配置（JSON格式，定义清洗规则）", example = "{\"rules\":[{\"field\":\"name\",\"action\":\"trim\"}]}")
    private String config;

    @Schema(description = "关联事件ID", example = "2048")
    private Long eventId;

    @Schema(description = "状态（0=禁用 1=启用）", example = "1")
    private Integer status;

    @Schema(description = "排序", example = "1")
    private Integer sort;

    @Schema(description = "备注", example = "数据清洗管道")
    private String remark;

}
