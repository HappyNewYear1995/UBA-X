package com.huanniankj.module.ai.controller.admin.music.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 音乐 Response VO")
@Data
public class AiMusicRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long userId;

    @Schema(description = "音乐名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String title;

    @Schema(description = "歌词", example = "ubax")
    private String lyric;

    @Schema(description = "图片地址", example = "ubax")
    private String imageUrl;

    @Schema(description = "音频地址", example = "ubax")
    private String audioUrl;

    @Schema(description = "视频地址", example = "ubax")
    private String videoUrl;

    @Schema(description = "音乐状态", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    private Integer status;

    @Schema(description = "描述词", example = "一首轻快的歌曲")
    private String gptDescriptionPrompt;

    @Schema(description = "提示词", example = "uba")
    private String prompt;

    @Schema(description = "模型平台", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String platform;

    @Schema(description = "模型", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String model;

    @Schema(description = "生成模式", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer generateMode;

    @Schema(description = "音乐风格标签")
    private List<String> tags;

    @Schema(description = "音乐时长", example = "[\"pop\",\"jazz\",\"punk\"]")
    private Double duration;

    @Schema(description = "是否发布", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean publicStatus;

    @Schema(description = "任务编号", example = "1024")
    private String taskId;

    @Schema(description = "错误信息")
    private String errorMessage;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
