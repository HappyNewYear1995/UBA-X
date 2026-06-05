package com.huanniankj.module.system.controller.oauth2.vo.open;

import com.huanniankj.framework.common.core.KeyValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "管理后台 - 授权页的信息 Response VO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2OpenAuthorizeInfoRespVO {

    /**
     * 客户端
     */
    private Client client;

    @Schema(description = "scope 的选中信息,使用 List 保证有序性，Key 是 scope，Value 为是否选中", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<KeyValue<String, Boolean>> scopes;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Client {

        @Schema(description = "应用名", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
        private String name;

        @Schema(description = "应用图标", requiredMode = Schema.RequiredMode.REQUIRED, example = "ubax")
        private String logo;

    }

}
