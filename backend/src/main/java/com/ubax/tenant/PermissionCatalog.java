package com.ubax.tenant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Central catalog of all permission codes recognized by the platform.
 * Each permission belongs to a group so the role editor UI can render a
 * grouped checkbox matrix. Codes are the single source of truth shared with
 * {@code DataSeeder.seedRoles()} and the frontend {@code auth.has()}.
 */
public final class PermissionCatalog {

    public static final class Permission {
        private final String code;
        private final String label;
        private final String description;

        Permission(String code, String label, String description) {
            this.code = code;
            this.label = label;
            this.description = description;
        }
        public String getCode() { return code; }
        public String getLabel() { return label; }
        public String getDescription() { return description; }
    }

    public static final class Group {
        private final String key;
        private final String title;
        private final List<Permission> permissions = new ArrayList<>();

        Group(String key, String title) {
            this.key = key;
            this.title = title;
        }
        Group add(String code, String label, String description) {
            permissions.add(new Permission(code, label, description));
            return this;
        }
        public String getKey() { return key; }
        public String getTitle() { return title; }
        public List<Permission> getPermissions() { return permissions; }
    }

    private static final List<Group> GROUPS = build();

    private static List<Group> build() {
        List<Group> list = new ArrayList<>();

        list.add(new Group("analytics", "数据分析")
                .add("analytics:view", "查看分析", "访问概览、趋势、分布、漏斗、留存等分析报表")
                .add("data:view", "查看数据", "查看原始事件、页面、会话与用户画像数据")
                .add("assistant:use", "AI 助手", "使用 AI 数据助手进行自然语言查询与报表生成"));

        list.add(new Group("collect", "数据采集")
                .add("app:manage", "应用管理", "创建、编辑、删除应用及采集配置")
                .add("agent:manage", "采集节点管理", "管理采集探针 / 节点"));

        list.add(new Group("system", "系统管理")
                .add("user:manage", "用户管理", "创建、编辑、删除本租户用户与重置密码")
                .add("role:manage", "角色管理", "创建、编辑角色并分配权限")
                .add("tenant:manage", "租户管理", "跨租户管理平台、开通与停用租户")
                .add("system:manage", "全局配置", "平台级全局配置与运维操作"));

        return list;
    }

    /** All permission groups with their permissions (for the role editor UI). */
    public static List<Group> groups() {
        return GROUPS;
    }

    /** Flat list of every known permission code. */
    public static List<String> allCodes() {
        List<String> codes = new ArrayList<>();
        for (Group g : GROUPS) {
            for (Permission p : g.getPermissions()) {
                codes.add(p.getCode());
            }
        }
        return codes;
    }

    /** Serialize groups to the map shape the frontend expects. */
    public static List<Map<String, Object>> asView() {
        List<Map<String, Object>> out = new ArrayList<>();
        for (Group g : GROUPS) {
            Map<String, Object> gm = new LinkedHashMap<>();
            gm.put("key", g.getKey());
            gm.put("title", g.getTitle());
            List<Map<String, Object>> perms = new ArrayList<>();
            for (Permission p : g.getPermissions()) {
                Map<String, Object> pm = new LinkedHashMap<>();
                pm.put("code", p.getCode());
                pm.put("label", p.getLabel());
                pm.put("description", p.getDescription());
                perms.add(pm);
            }
            gm.put("permissions", perms);
            out.add(gm);
        }
        return out;
    }

    /** Whether the given code exists in the catalog. */
    public static boolean isValid(String code) {
        return allCodes().contains(code);
    }

    private PermissionCatalog() {
    }
}
