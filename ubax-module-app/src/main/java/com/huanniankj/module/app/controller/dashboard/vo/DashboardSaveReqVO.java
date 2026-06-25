package com.huanniankj.module.app.controller.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 数据看板保存 ReqVO
 *
 * @author zhaoff
 */
@Data
@Schema(description = "数据看板保存请求")
public class DashboardSaveReqVO {

    @Schema(description = "数据看板 ID", example = "1")
    private Long id;

    @Schema(description = "看板名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "运维监控看板")
    @NotBlank(message = "看板名称不能为空")
    @Size(max = 100, message = "看板名称长度不能超过100个字符")
    private String name;

    @Schema(description = "描述", example = "生产环境运维监控看板")
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    @Schema(description = "布局配置 (JSON 格式)", example = "{\"layout\":[{\"x\":0,\"y\":0,\"w\":6,\"h\":4}]}")
    private String layoutConfig;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "备注", example = "主看板")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}
