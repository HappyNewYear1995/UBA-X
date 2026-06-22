package com.huanniankj.module.source.controller.webservice.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * WebService 数据源分页查询 ReqVO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "WebService 数据源分页查询请求")
public class WebServicePageReqVO extends PageParam {

    @Schema(description = "数据源名称", example = "用户数据")
    private String name;

    @Schema(description = "状态 (0-正常 1-异常)", example = "0")
    private Integer status;

}
