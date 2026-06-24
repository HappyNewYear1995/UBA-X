package com.huanniankj.module.analysis.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "留存分析配置响应")
public class RetentionConfigRespVO {

    @Schema(description = "配置 ID", example = "1")
    private Long id;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "留存类型")
    private String retentionType;

    @Schema(description = "统计维度")
    private String dimension;

    @Schema(description = "起始事件")
    private String startEvent;

    @Schema(description = "回访事件")
    private String returnEvent;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
