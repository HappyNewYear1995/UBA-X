package com.huanniankj.module.gather.controller.database.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 脚本执行日志分页请求 VO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "脚本执行日志分页请求")
public class DatabaseScriptLogPageReqVO extends PageParam {

    @Schema(description = "脚本ID", example = "1")
    private Long scriptId;

    @Schema(description = "数据源ID", example = "1")
    private Long databaseId;

    @Schema(description = "执行状态 (0-成功 1-失败)", example = "0")
    private Integer status;

    @Schema(description = "执行类型 (manual/scheduled)", example = "manual")
    private String executeType;

    @Schema(description = "开始时间")
    private LocalDateTime beginTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

}
