package com.ubax.assistant;

import com.ubax.common.ApiResponse;
import com.ubax.common.TenantContext;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    private Long tid() { return TenantContext.requireTenantId(); }

    @PostMapping("/chat")
    public ApiResponse<Map<String, Object>> chat(@RequestBody ChatRequest req) {
        String message = req.getMessage() == null ? "" : req.getMessage();
        return ApiResponse.ok(assistantService.chat(tid(), req.getAppId(), message));
    }

    @GetMapping("/suggest")
    public ApiResponse<Object> suggest() {
        return ApiResponse.ok(java.util.Arrays.asList(
                "今日的 PV 和 UV 是多少？",
                "生成近一周的访问数据报表",
                "近一周访问方式的饼图",
                "最近14天的访问趋势",
                "热门页面 TOP10"
        ));
    }

    public static class ChatRequest {
        private String message;
        private Long appId;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public Long getAppId() { return appId; }
        public void setAppId(Long appId) { this.appId = appId; }
    }
}
