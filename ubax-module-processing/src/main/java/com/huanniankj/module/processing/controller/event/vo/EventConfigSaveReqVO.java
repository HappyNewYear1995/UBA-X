package com.huanniankj.module.processing.controller.event.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 事件配置创建/修改 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "事件配置创建/修改 Request VO")
@Data
public class EventConfigSaveReqVO {

    @Schema(description = "事件配置 ID", example = "1024")
    private Long id;

    @Schema(description = "事件配置名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "页面浏览")
    @NotBlank(message = "事件配置名称不能为空")
    @Size(max = 100, message = "事件配置名称长度不能超过 100 个字符")
    private String name;

    @Schema(description = "事件编码", requiredMode = Schema.RequiredMode.REQUIRED, example = "page_view")
    @NotBlank(message = "事件编码不能为空")
    @Size(max = 64, message = "事件编码长度不能超过 64 个字符")
    private String code;

    @Schema(description = "数据源 ID 列表", example = "[1, 2, 3]")
    private List<Long> dataSourceIds;

    @Schema(description = "数据源类型（database/webservice/sdk）", example = "database")
    private String dataSourceType;

    @Schema(description = "事件类型（page_view/click/custom）", example = "page_view")
    private String eventType;

    @Schema(description = "事件属性定义（JSON 格式）", example = "{\"page_url\":\"string\",\"page_title\":\"string\"}")
    private String properties;

    @Schema(description = "过滤条件（JSON 格式）", example = "{\"status\":1,\"type\":\"active\"}")
    private String filterCondition;

    @Schema(description = "字段映射规则（JSON 格式）", example = "{\"page_url\":\"url\",\"page_title\":\"title\"}")
    private String fieldMapping;

    @Schema(description = "状态（0=禁用 1=启用）", example = "1")
    private Integer status;

    @Schema(description = "备注", example = "用户浏览页面事件")
    private String remark;

}
