package com.huanniankj.module.analysis.controller.path.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

/**
 * @author zhaoff
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "路径分析配置响应")
public class PathConfigRespVO {

    @Schema(description = "配置 ID", example = "1")
    private Long id;

    @Schema(description = "配置名称")
    private String name;

    @Schema(description = "起始事件")
    private String startEvent;

    @Schema(description = "最大路径深度")
    private Integer maxDepth;

    @Schema(description = "路径窗口时间（秒）")
    private Integer windowTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

}
