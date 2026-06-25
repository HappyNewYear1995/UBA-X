package com.huanniankj.module.analysis.controller.path.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * @author zhaoff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "路径分析配置保存请求")
public class PathConfigSaveReqVO {

    @Schema(description = "配置 ID", example = "1")
    private Long id;

    @Schema(description = "配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "用户行为路径分析")
    @NotBlank(message = "配置名称不能为空")
    private String name;

    @Schema(description = "起始事件", example = "app_open")
    private String startEvent;

    @Schema(description = "最大路径深度", example = "7")
    private Integer maxDepth;

    @Schema(description = "路径窗口时间（秒）", example = "1800")
    private Integer windowTime;

    @Schema(description = "备注")
    private String remark;

}
