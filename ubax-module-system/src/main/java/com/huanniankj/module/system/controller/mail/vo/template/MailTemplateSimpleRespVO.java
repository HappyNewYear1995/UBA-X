package com.huanniankj.module.system.controller.mail.vo.template;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邮件模版的精简 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "邮件模版的精简 Response VO")
@Data
public class MailTemplateSimpleRespVO {

    @Schema(description = "模版编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "模版名字", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String name;

}
