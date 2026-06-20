package com.huanniankj.module.source.controller.database.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据库脚本分页请求 VO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "数据库脚本分页请求")
public class DatabaseScriptPageReqVO extends PageParam {

    @Schema(description = "脚本名称", example = "用户数据")
    private String name;

    @Schema(description = "脚本编码", example = "user_data")
    private String code;

    @Schema(description = "数据源ID", example = "1")
    private Long databaseId;

    @Schema(description = "脚本类型", example = "sql")
    private String scriptType;

    @Schema(description = "状态 (0-启用 1-禁用)", example = "0")
    private Integer status;

}
