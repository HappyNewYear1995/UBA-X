package com.huanniankj.module.analysis.controller.admin.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "漏斗分析配置响应")
public class FunnelConfigRespVO {

    @Schema(description = "配置 ID", example = "1")
    private Long id;

    @Schema(description = "漏斗名称", example = "注册到支付漏斗")
    private String name;

    @Schema(description = "漏斗步骤事件列表（JSON 数组格式）")
    private String steps;

    @Schema(description = "窗口时间（秒）", example = "86400")
    private Integer windowTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
