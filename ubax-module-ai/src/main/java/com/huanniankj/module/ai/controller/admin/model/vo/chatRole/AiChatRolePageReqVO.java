package com.huanniankj.module.ai.controller.admin.model.vo.chatRole;

import com.huanniankj.framework.common.pojo.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "管理后台 - AI 聊天角色分页 Request VO")
@Data
public class AiChatRolePageReqVO extends PageParam {

    @Schema(description = "角色名称", example = "ubax")
    private String name;

    @Schema(description = "角色类别", example = "ubax")
    private String category;

    @Schema(description = "是否公开", example = "1")
    private Boolean publicStatus;

}
