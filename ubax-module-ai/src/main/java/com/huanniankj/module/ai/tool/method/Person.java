package com.huanniankj.module.ai.tool.method;

/**
 * 来自 Spring AI 官方文档
 * <p>
 * Represents a person with basic information.
 * This is an immutable record.
 */
public record Person(
        int id,
        String firstName,
        String lastName,
        String email,
        String sex,
        String ipAddress,
        String jobTitle,
        int age
) {
}
