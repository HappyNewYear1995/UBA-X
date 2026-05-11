package com.huanniankj.module.ai.controller.admin.write.vo;

import com.huanniankj.framework.common.validation.InEnum;
import com.huanniankj.module.ai.enums.write.AiWriteTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Schema(description = "管理后台 - AI 写作生成 Request VO")
@Data
public class AiWriteGenerateReqVO {

    @Schema(description = "写作类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @InEnum(value = AiWriteTypeEnum.class, message = "写作类型必须是 {value}")
    private Integer type;

    @Schema(description = "写作内容提示", example = "xxx")
    private String prompt;

    @Schema(description = "原文", example = "xxx")
    private String originalContent;

    @Schema(description = "长度", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "长度不能为空")
    private Integer length;

    @Schema(description = "格式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "格式不能为空")
    private Integer format;

    @Schema(description = "语气", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "语气不能为空")
    private Integer tone;

    @Schema(description = "语言", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "语言不能为空")
    private Integer language;

}
