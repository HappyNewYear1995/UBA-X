package com.huanniankj.module.gather.controller.database.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库数据源分页 ReqVO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据库数据源分页请求")
public class DatabasePageReqVO extends PageParam {

    @Schema(description = "数据源名称", example = "生产库")
    private String name;

    @Schema(description = "数据库类型编码", example = "mysql")
    private String dbType;

    @Schema(description = "状态 (0-正常 1-禁用)", example = "0")
    private Integer status;

}
