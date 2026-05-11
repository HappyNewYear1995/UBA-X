package com.huanniankj.module.ai.controller.admin.model.vo.apikey;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI API 密钥分页 Request VO")
@Data
public class AiApiKeyPageReqVO extends PageParam {

    @Schema(description = "名称", example = "ubax")
    private String name;

    @Schema(description = "平台", example = "ubax")
    private String platform;

    @Schema(description = "状态", example = "1")
    private Integer status;

}
