package com.huanniankj.module.processing.controller.event.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 事件分页 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "事件分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class EventPageReqVO extends PageParam {

    @Schema(description = "事件类型编码", example = "auth.login.success")
    private String eventType;

    @Schema(description = "事件大类（authentication/access/network/admin/system）", example = "authentication")
    private String eventCategory;

    @Schema(description = "主体类型（user/service_account/device/application）", example = "user")
    private String actorType;

    @Schema(description = "行为主体唯一标识，模糊匹配", example = "user_1001")
    private String actorId;

    @Schema(description = "具体动作，模糊匹配", example = "login")
    private String action;

    @Schema(description = "事件结果（success/failure/denied）", example = "success")
    private String result;

    @Schema(description = "事件严重等级（info/low/medium/high/critical）", example = "info")
    private String severity;

    @Schema(description = "产生该事件的数据源标识", example = "src_001")
    private String sourceId;

}
