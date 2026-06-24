package com.huanniankj.module.system.controller.permission.vo.role;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 角色精简信息 Response VO
 *
 * @author zhaoff
 */
@Schema(description = "角色精简信息 Response VO")
@Data
public class RoleSimpleRespVO {

    @Schema(description = "角色编号", requiredMode = Schema.RequiredMode.REQUIRED, example = "1024")
    private Long id;

    @Schema(description = "角色名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
    private String name;

}
