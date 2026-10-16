package com.ubax.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping({"/api/health", "/health"})
    public Map<String, Object> health() {
        return Map.of("status", "UP", "app", "ubax-server", "ts", System.currentTimeMillis());
    }
}
