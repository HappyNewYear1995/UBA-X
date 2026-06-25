package com.huanniankj.module.processing.controller.rule.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 清洗管道分页 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "清洗管道分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class CleanPipelinePageReqVO extends PageParam {

    @Schema(description = "管道名称，模糊匹配", example = "数据格式转换")
    private String name;

    @Schema(description = "管道类型", example = "data_clean")
    private String type;

    @Schema(description = "状态（0=禁用 1=启用）", example = "1")
    private Integer status;

    @Schema(description = "关联事件ID", example = "2048")
    private Long eventId;

}
