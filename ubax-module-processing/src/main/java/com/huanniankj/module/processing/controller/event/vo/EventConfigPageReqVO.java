package com.huanniankj.module.processing.controller.event.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 事件配置分页 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "事件配置分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class EventConfigPageReqVO extends PageParam {

    @Schema(description = "事件配置名称，模糊匹配", example = "页面浏览")
    private String name;

    @Schema(description = "事件编码，模糊匹配", example = "page_view")
    private String code;

    @Schema(description = "数据源类型", example = "database")
    private String dataSourceType;

    @Schema(description = "事件类型", example = "page_view")
    private String eventType;

    @Schema(description = "状态（0=禁用 1=启用）", example = "1")
    private Integer status;

}
