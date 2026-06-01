package com.huanniankj.module.gather.controller.admin.agent.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

import static com.huanniankj.framework.common.util.date.DateUtils.FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND;

/**
 * Agent 分页查询 ReqVO
 *
 * @author zhaoff
 */
@Schema(description = "Agent 分页查询 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class AgentPageReqVO extends PageParam {

    @Schema(description = "UUID", example = "7e9e362a-5d29-4a23-ab90-3ad02ea54e9e")
    private String uuid;

    @Schema(description = "主机名", example = "server-01")
    private String hostname;

    @Schema(description = "IP", example = "192.168.1.2")
    private String ip;

    @Schema(description = "终端类型", example = "10")
    private Integer terminal;

    @Schema(description = "平台类型", example = "10")
    private Integer platform;

    @Schema(description = "状态", example = "0")
    private Integer status;

    @Schema(description = "创建时间")
    @DateTimeFormat(pattern = FORMAT_YEAR_MONTH_DAY_HOUR_MINUTE_SECOND)
    private LocalDateTime[] createTime;

}
