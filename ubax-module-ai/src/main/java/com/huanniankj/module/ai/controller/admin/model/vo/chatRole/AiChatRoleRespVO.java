package com.huanniankj.module.ai.controller.admin.model.vo.chatRole;

import com.fhs.core.trans.anno.Trans;
import com.fhs.core.trans.constant.TransType;
import com.fhs.core.trans.vo.VO;
import com.huanniankj.module.ai.dal.dataobject.model.AiModelDO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "管理后台 - AI 聊天角色 Response VO")
@Data
public class AiChatRoleRespVO implements VO {

    @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "用户编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "9442")
    private Long userId;

    @Schema(description = "模型编号", example = "1024")
    @Trans(type = TransType.SIMPLE, target = AiModelDO.class, fields = {"name", "model"}, refs = {"modelName", "model"})
    private Long modelId;

    @Schema(description = "模型名字", example = "ubax")
    private String modelName;

    @Schema(description = "模型标识", example = "ubax")
    private String model;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String name;

    @Schema(description = "角色头像", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String avatar;

    @Schema(description = "角色类别", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String category;

    @Schema(description = "角色排序", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Integer sort;

    @Schema(description = "角色描述", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String description;

    @Schema(description = "角色设定", requiredMode = Schema.RequiredMode.REQUIRED)
    private String systemMessage;

    @Schema(description = "引用的知识库编号列表", example = "1,2,3")
    private List<Long> knowledgeIds;

    @Schema(description = "引用的工具编号列表", example = "1,2,3")
    private List<Long> toolIds;

    @Schema(description = "引用的 MCP Client 名字列表", example = "filesystem")
    private List<String> mcpClientNames;

    @Schema(description = "是否公开", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    private Boolean publicStatus;

    @Schema(description = "状态", example = "1")
    private Integer status;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createTime;

}
