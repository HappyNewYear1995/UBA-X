package com.huanniankj.module.ai.controller.admin.chat.vo.message;

import com.huanniankj.module.ai.framework.ai.core.webserch.AiWebSearchResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 聊天消息 Response VO")
@Data
public class AiChatMessageRespVO {

    @Schema(description = "编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "对话编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "2048")
    private Long conversationId;

    @Schema(description = "回复消息编号", example = "1024")
    private Long replyId;

    @Schema(description = "消息类型", requiredMode = Schema.RequiredMode.REQUIRED, example = "role")
    private String type; // 参见 MessageType 枚举类

    @Schema(description = "用户编号", example = "1024")
    private Long userId;

    @Schema(description = "角色编号", example = "888")
    private Long roleId;

    @Schema(description = "模型标志", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String model;

    @Schema(description = "模型编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Long modelId;

    @Schema(description = "聊天内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String content;

    @Schema(description = "推理内容", example = "ubax")
    private String reasoningContent;

    @Schema(description = "是否携带上下文", requiredMode = Schema.RequiredMode.REQUIRED, example = "true")
    private Boolean useContext;

    @Schema(description = "知识库段落编号数组", example = "[1,2,3]")
    private List<Long> segmentIds;

    @Schema(description = "知识库段落数组")
    private List<KnowledgeSegment> segments;

    @Schema(description = "联网搜索的网页内容数组")
    private List<AiWebSearchResponse.WebPage> webSearchPages;

    @Schema(description = "附件 URL 数组", example = "ubax")
    private List<String> attachmentUrls;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED, example = "2024-05-12 12:51")
    private LocalDateTime createTime;

    // ========== 仅在【对话管理】时加载 ==========

    @Schema(description = "角色名字", example = "ubax")
    private String roleName;

    @Schema(description = "知识库段落", example = "ubax")
    @Data
    public static class KnowledgeSegment {

        @Schema(description = "段落编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long id;

        @Schema(description = "切片内容", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
        private String content;

        @Schema(description = "文档编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
        private Long documentId;

        @Schema(description = "文档名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
        private String documentName;

    }

}
