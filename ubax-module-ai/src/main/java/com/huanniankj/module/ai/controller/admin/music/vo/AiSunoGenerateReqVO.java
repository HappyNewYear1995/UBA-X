package com.huanniankj.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 音乐生成 Request VO")
@Data
public class AiSunoGenerateReqVO {

    @Schema(description = "平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "Suno")
    @NotBlank(message = "平台不能为空")
    private String platform; // 参见 AiPlatformEnum 枚举

    /**
     * 1. 描述模式：描述词 + 是否纯音乐 + 模型
     * 2. 歌词模式：歌词 + 音乐风格 + 标题 + 模型
     */
    @Schema(description = "生成模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "2")
    @NotNull(message = "生成模式不能为空")
    private Integer generateMode; // 参见 AiMusicGenerateModeEnum 枚举

    @Schema(description = "用于生成音乐音频的歌词提示",
            example = """
                    1.描述模式：xxx
                    2.歌词模式：
                    [Verse]
                    xxx
                    [Chorus]
                    xxx
                    """)
    private String prompt;

    @Schema(description = "是否纯音乐", example = "true")
    private Boolean makeInstrumental;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotEmpty(message = "模型不能为空")
    private String model;

    @Schema(description = "音乐风格", example = "[\"pop\",\"jazz\",\"punk\"]")
    private List<String> tags;

    @Schema(description = "音乐/歌曲名称", example = "ubax")
    private String title;

}
