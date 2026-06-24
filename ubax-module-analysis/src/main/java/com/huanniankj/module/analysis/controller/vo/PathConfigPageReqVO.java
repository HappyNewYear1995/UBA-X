package com.huanniankj.module.analysis.controller.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "路径分析配置分页查询请求")
public class PathConfigPageReqVO extends PageParam {

    @Schema(description = "配置名称", example = "路径")
    private String name;

}
