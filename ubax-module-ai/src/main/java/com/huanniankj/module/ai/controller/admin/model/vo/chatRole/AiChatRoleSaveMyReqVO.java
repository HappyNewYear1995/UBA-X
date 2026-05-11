package com.huanniankj.module.ai.controller.admin.model.vo.chatRole;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

@Schema(description = "管理后台 - AI 聊天角色新增/修改【我的】 Request VO")
@Data
public class AiChatRoleSaveMyReqVO {

    @Schema(description = "角色编号", example = "1024")
    private Long id;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotEmpty(message = "角色名称不能为空")
    private String name;

    @Schema(description = "角色头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotEmpty(message = "角色头像不能为空")
    @URL(message = "角色头像必须是 URL 格式")
    private String avatar;

    @Schema(description = "角色描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotEmpty(message = "角色描述不能为空")
    private String description;

    @Schema(description = "角色设定", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    @NotEmpty(message = "角色设定不能为空")
    private String systemMessage;

    @Schema(description = "引用的知识库编号列表", example = "1,2,3")
    private List<Long> knowledgeIds;

    @Schema(description = "引用的工具编号列表", example = "1,2,3")
    private List<Long> toolIds;

    @Schema(description = "引用的 MCP Client 名字列表", example = "filesystem")
    private List<String> mcpClientNames;

}
