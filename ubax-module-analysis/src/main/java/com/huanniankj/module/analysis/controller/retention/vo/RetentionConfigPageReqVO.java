package com.huanniankj.module.analysis.controller.retention.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

/**
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "留存分析配置分页查询请求")
public class RetentionConfigPageReqVO extends PageParam {

    @Schema(description = "配置名称", example = "留存")
    private String name;

    @Schema(description = "留存类型", example = "next_day")
    private String retentionType;

}
