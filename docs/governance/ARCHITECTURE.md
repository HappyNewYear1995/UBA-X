# 采集治理子系统 · 架构设计

> 关联文档：[PRD.md](./PRD.md) · [TECHNICAL.md](./TECHNICAL.md)
> 参考：现有 `docs/ARCHITECTURE.md`（总体分层与采集管线）

---

## 1. 设计原则

1. **沿用既有分层范式**：新子系统 `com.ubax.governance` 与 `com.ubax.system` 平级，内部遵循 `controller / service / domain(实体) / repo` 结构，复用 `ApiResponse` / `BusinessException` / `TenantContext` / `auditService`。
2. **单点解析改造，不新增链路**：所有治理能力都挂在既有唯一解析点 `CollectorService.ingest()` 上，采集管线（StreamEngine → StreamWriter）保持不变。
3. **配置读多写少 → 内存缓存**：解析路径是热路径（每条事件都走），配置查询必须缓存，禁止 per-event 打 DB。
4. **默认放行，向后兼容**：任何"未配置"状态都等价于当前行为，绝不因治理缺失而丢数据。

## 2. 在总体架构中的位置

```
SDK / API / 探针
      │  POST /api/collect/{event|batch|beacon|identify}
      ▼
CollectorController  ── clientIp + User-Agent 解析
      ▼
┌──────────────────────────────────────────────────────────┐
│ CollectorService.ingest(payload, ip, ua)                   │
│   ① resolveApp(appKey)                       [既有, 缓存]   │
│   ② GovernanceConfig: collect.* 开关/采样/脱敏  [新增, 缓存] │
│   ③ ParsingRuleService.apply(...)   字段映射/抽取/过滤/路由 │
│   ④ EventDefinitionService.resolve(eventKey)  字典/schema校验│
│   ⑤ IdentityService.resolve(distinctId,userId) alias 归并   │
│   ▼                                                         │
│   streamEngine.submit(Event)                  [既有]        │
└──────────────────────────────────────────────────────────┘
      ▼
StreamEngine → StreamWriter → RealtimeMinute / UserProfile / Event
      ▲
      │  查询
Analytics / Ext / Profile
```

治理三服务（②③④⑤）以**旁路增强**方式插入 ingest，输出仍是标准 `Event`，对下游完全透明。

## 3. 领域模型

新建包 `com.ubax.governance`，四张表：

```
EventDefinition   (uba_event_def)
  id, tenant_id, app_id, event_key, event_name, type,
  properties_schema(JSON/TEXT), validate(JSON/TEXT),
  on_miss(枚举: MARK/DROP/UNKNOWN), enabled, version, created_at, updated_at

ParsingRule       (uba_parsing_rule)
  id, tenant_id, app_id, priority, name,
  match(JSON/TEXT), action(JSON/TEXT), enabled, created_at, updated_at

IdentityRule      (uba_identity_rule)
  id, tenant_id, app_id, strategy(DISTINCT_FIRST/USER_FIRST/EXPLICIT),
  enabled, created_at, updated_at

UserIdentityAlias (uba_user_alias)
  id, tenant_id, app_id, distinct_id, unified_id, source, merged_at
  unique(tenant_id, app_id, distinct_id)
```

**租户作用域**：四表均带 `tenant_id` + `app_id`（`app_id=0` = 该租户全 App 生效，与 `Event.appId=0` 语义一致）。超管（tenantId=null）跨租户只读。此为多租户模型的自然延伸，区别于 `system` 模块的平台全局表。

### 实体关系

```
App(1) ──< EventDefinition(N)
App(1) ──< ParsingRule(N)          按 priority 顺序应用
App(1) ──< IdentityRule(1..N)
App(1) ──< UserIdentityAlias(N)     distinct_id → unified_id
UserProfile 仍以 distinct_id/unified_id 为键（不改主键，仅解析时折叠）
```

## 4. 服务设计

### 4.1 GovernanceConfigService（②，激活死开关）
- 读取 `SystemConfig` 的 `collect.enabled` / `collect.sampling` / `collect.ip.anonymize`。
- 内存缓存 + TTL/变更失效。
- 在 ingest 最前置：`enabled=false` → 直接拒收；`sampling<1` → 按比例随机放行；`ip.anonymize=true` → IP 末段置 0。

### 4.2 ParsingRuleService（③）
- 按 `tenant_id + app_id`（含 app_id=0）加载启用规则，按 `priority` 升序。
- `match`：条件匹配（event/type/path/app 等，AND 语义）。
- `action`：`set` / `rename` / `extract`（从 properties 或 query 提升字段）/ `drop` / `route` / `anonymize`。
- 命中 `drop` → 短路返回 null，ingest 丢弃。

### 4.3 EventDefinitionService（④）
- 按 event_key 查字典（缓存）。
- 命中 → 用标准 event_name/type，按 properties_schema 校验属性（缺失/类型错按 validate 策略处理）。
- 未命中 → 按 `on_miss` 策略：MARK（放行打标 `_undef=true`）/ DROP / UNKNOWN（降级为 `$unknown`）。默认 MARK。

### 4.4 IdentityService（⑤）
- 查 `uba_user_alias`：distinct_id → unified_id。
- 命中 → 用 unified_id 作为 UserProfile 键，实现登录前后折叠。
- 未命中 → 按 IdentityRule.strategy 决定用 distinctId 还是 userId。
- 提供 `identify(appKey, distinctId, userId)` 写入 alias（幂等 upsert）。

### 4.5 缓存与失效
- 三类配置各自 `ConcurrentHashMap` 缓存，key = `tenantId:appId`。
- Service 的写方法（create/update/toggle/delete）末尾调用 `invalidate(tenantId, appId)`。
- 兜底 TTL（默认 60s），防止多实例下失效遗漏（当前单实例可仅靠主动失效）。

## 5. 控制器与 API

统一 `@RequestMapping("/api/governance")`，每个写方法首行 `requireManage()`，写操作 `auditService.record(...)`，返回 `ApiResponse.ok(...)`。

```
GET    /api/governance/events            分页列表 {rows,total,page,size}
POST   /api/governance/events            新增
PUT    /api/governance/events/{id}       编辑
PATCH  /api/governance/events/{id}/toggle 启停
DELETE /api/governance/events/{id}       删除

GET/POST/PUT/PATCH/DELETE  /api/governance/rules[/{id}][/toggle]   同构

GET    /api/governance/identity          策略查询
PUT    /api/governance/identity          策略保存
GET    /api/governance/alias             alias 关系分页查询

# 开放端点（无鉴权，与 /event 同级，纳入 SecurityConfig 白名单）
POST   /api/collect/identify             {appKey, distinctId, userId} → 写 alias
```

## 6. 权限模型

- `PermissionCatalog.build()` 在 `collect` 分组新增 `meta:manage`（采集治理）。
- `DataSeeder.seedRoles()` 给 SUPER_ADMIN / TENANT_ADMIN 追加 `meta:manage`。
- 前端路由 meta 挂 `perm:'meta:manage'`，`auth.has()` 校验；后端 `requireManage()` 兜底（角色级，与现有 system 一致）。

## 7. 安全边界

- `/api/collect/identify` 为开放端点，须做限流与 appKey 校验，防止恶意刷 alias 污染身份图谱。
- alias 写入仅在同一 `tenant_id + app_id` 内生效，跨租户隔离。
- 配置类接口全部 `requireManage()`，普通用户不可访问。

## 8. 与既有模块的耦合点（改动清单）

| 文件 | 改动 |
| --- | --- |
| `collector/CollectorService.java` | ingest 插入 ②③④⑤ 治理钩子（核心改动） |
| `collector/CollectorController.java` | 新增 `/identify` 端点 |
| `common/SecurityConfig.java` | `/api/collect/**` 已在白名单，`/identify` 自动覆盖 |
| `tenant/PermissionCatalog.java` | 新增 `meta:manage` |
| `bootstrap/DataSeeder.java` | seedRoles 追加权限；新增治理种子数据 |
| `resources/sdk/ubax.js` | identify() 上报服务端 alias |
| `frontend/src/router/index.js` | 新增 governance 路由组 |
| `frontend/src/api/index.js` | 新增 governanceApi |
| 新增 `frontend/src/views/governance/*.vue` | 三个配置页 |

**不改动**：StreamEngine、StreamWriter、Event/UserProfile 主键、Analytics/Ext/Profile 查询层——治理对下游透明。
