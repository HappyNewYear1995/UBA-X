package com.huanniankj.module.processing.controller.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 事件配置信息 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "事件配置信息 Response VO")
@Data
public class EventConfigRespVO {

    @Schema(description = "事件配置 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "事件配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "页面浏览")
    private String name;

    @Schema(description = "事件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "page_view")
    private String code;

    @Schema(description = "数据源 ID 列表", example = "[1, 2, 3]")
    private List<Long> dataSourceIds;

    @Schema(description = "数据源类型", example = "database")
    private String dataSourceType;

    @Schema(description = "事件类型", example = "page_view")
    private String eventType;

    @Schema(description = "事件属性定义（JSON 格式）")
    private String properties;

    @Schema(description = "过滤条件（JSON 格式）")
    private String filterCondition;

    @Schema(description = "字段映射规则（JSON 格式）")
    private String fieldMapping;

    @Schema(description = "状态（0=禁用 1=启用）", example = "1")
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
