package com.huanniankj.module.app.controller.dashboard.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据看板分页 ReqVO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据看板分页请求")
public class DashboardPageReqVO extends PageParam {

    @Schema(description = "看板名称", example = "运维监控看板")
    private String name;

    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

}
