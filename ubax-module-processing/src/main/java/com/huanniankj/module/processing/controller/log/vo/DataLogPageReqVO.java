package com.huanniankj.module.processing.controller.log.vo;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据日志分页 Request VO
 *
 * @author zhaoff
 */
@Schema(description = "数据日志分页 Request VO")
@Data
@EqualsAndHashCode(callSuper = true)
public class DataLogPageReqVO extends PageParam {

    @Schema(description = "事件类型", example = "page_view")
    private String eventType;

    @Schema(description = "来源应用标识", example = "app-001")
    private String appId;

    @Schema(description = "设备ID，模糊匹配", example = "device-001")
    private String deviceId;

    @Schema(description = "用户ID，模糊匹配", example = "user-001")
    private String userId;

}
