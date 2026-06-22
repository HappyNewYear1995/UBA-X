package com.huanniankj.module.analysis.controller.admin.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "漏斗分析配置分页查询请求")
public class FunnelConfigPageReqVO extends PageParam {

    @Schema(description = "漏斗名称", example = "注册")
    private String name;

}
