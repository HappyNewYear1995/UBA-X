package com.huanniankj.module.ai.controller.admin.chat.vo.message;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Schema(description = "管理后台 - AI 聊天消息发送 Request VO")
@Data
public class AiChatMessageSendReqVO {

    @Schema(description = "聊天对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    @NotNull(message = "聊天对话编号不能为空")
    private Long conversationId;

    @Schema(description = "聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotEmpty(message = "聊天内容不能为空")
    private String content;

    @Schema(description = "是否携带上下文", example = "true")
    private Boolean useContext;

    @Schema(description = "是否联网搜索", example = "true")
    private Boolean useSearch;

    @Schema(description = "附件 URL 数组", example = "ubax")
    private List<String> attachmentUrls;

}
