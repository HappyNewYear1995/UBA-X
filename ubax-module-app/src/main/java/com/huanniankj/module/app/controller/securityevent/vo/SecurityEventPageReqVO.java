package com.huanniankj.module.app.controller.securityevent.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 安全检测事件分页 ReqVO
 *
 * @author zhaoff
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "安全检测事件分页请求")
public class SecurityEventPageReqVO extends PageParam {

    @Schema(description = "规则 ID", example = "1")
    private Long ruleId;

    @Schema(description = "检测类型", example = "1")
    private Integer detectionType;

    @Schema(description = "严重级别", example = "2")
    private Integer severity;

    @Schema(description = "是否已处理", example = "false")
    private Boolean handled;

    @Schema(description = "来源 IP", example = "192.168.1.100")
    private String sourceIp;

    @Schema(description = "创建时间")
    private LocalDateTime[] createTime;

}
