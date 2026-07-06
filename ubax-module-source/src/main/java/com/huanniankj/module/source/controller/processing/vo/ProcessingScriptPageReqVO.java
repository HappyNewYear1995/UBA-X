package com.huanniankj.module.source.controller.processing.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "处理脚本分页请求")
public class ProcessingScriptPageReqVO extends PageParam {

    @Schema(description = "脚本名称")
    private String name;

    @Schema(description = "脚本编码")
    private String code;

    @Schema(description = "状态 (0-启用 1-禁用)")
    private Integer status;

}
