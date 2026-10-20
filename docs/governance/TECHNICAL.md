# 采集治理子系统 · 技术实现方案

> 关联文档：[PRD.md](./PRD.md) · [ARCHITECTURE.md](./ARCHITECTURE.md)
> 技术栈：Spring Boot 3.3.5 / Java 17 / Spring Data JPA / Vue 3.5 `<script setup>` / Element Plus 2.8

本文给出可直接照做的落地方案：包结构、实体、Repo/Service/Controller 骨架、`ingest()` 集成点（对齐真实代码行）、前端页面与 API、种子数据、验证步骤。

---

## 1. 包与目录结构

```
backend/src/main/java/com/ubax/governance/
  domain/   EventDefinition.java  ParsingRule.java  IdentityRule.java  UserIdentityAlias.java
  repo/     EventDefinitionRepository.java  ParsingRuleRepository.java
            IdentityRuleRepository.java  UserIdentityAliasRepository.java
  service/  GovernanceConfigService.java  ParsingRuleService.java
            EventDefinitionService.java  IdentityService.java
  controller/ GovernanceController.java          # /api/governance/**（鉴权）
  dto/      GovernanceDtos.java                  # 请求/响应体

frontend/src/views/governance/
  EventDef.vue  ParsingRule.vue  Identity.vue
frontend/src/api/index.js     # 新增 governanceApi
frontend/src/router/index.js  # 新增 governance 路由组
```

## 2. 实体（JPA）

沿用 `com.ubax.system.domain.Dict` 范式：`@Data @Entity @Table`、`@GeneratedValue(IDENTITY)`、JSON 字段用 `@Lob @Column(columnDefinition="TEXT")`。

```java
@Data @Entity @Table(name = "uba_event_def",
    uniqueConstraints = @UniqueConstraint(columnNames = {"tenant_id","app_id","event_key"}))
public class EventDefinition {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="tenant_id") private Long tenantId;
    @Column(name="app_id")    private Long appId;        // 0 = 全 App
    @Column(name="event_key") private String eventKey;
    @Column(name="event_name")private String eventName;
    private String type;                                  // pageview/click/leave/custom
    @Lob @Column(name="properties_schema", columnDefinition="TEXT") private String propertiesSchema; // JSON
    @Lob @Column(columnDefinition="TEXT") private String validate;  // JSON
    @Column(name="on_miss") private String onMiss = "MARK";         // MARK/DROP/UNKNOWN
    private boolean enabled = true;
    private int version = 1;
    @Column(name="created_at") private LocalDateTime createdAt;
    @Column(name="updated_at") private LocalDateTime updatedAt;
}
```

`ParsingRule`（priority/match/action）、`IdentityRule`（strategy）、`UserIdentityAlias`（distinct_id/unified_id/source，unique(tenant_id,app_id,distinct_id)）同构，字段见 ARCHITECTURE.md §3。

## 3. Repo

```java
public interface EventDefinitionRepository extends JpaRepository<EventDefinition, Long> {
    List<EventDefinition> findByTenantIdAndEnabledTrue(Long tenantId);
    Optional<EventDefinition> findByTenantIdAndAppIdAndEventKey(Long t, Long a, String k);
    Page<EventDefinition> findByTenantId(Long t, Pageable p);
}
// ParsingRuleRepository.findByTenantIdAndEnabledTrueOrderByPriorityAsc(...)
// UserIdentityAliasRepository.findByTenantIdAndAppIdAndDistinctId(...)
```

## 4. Service

### 4.1 缓存基类（关键，热路径）
```java
// 每个 Service 持有 ConcurrentHashMap<Long/*tenantId*/, Map<Long/*appId*/, ...>>
// 读：先查缓存，miss 再查 DB 回填；写方法末尾 invalidate(tenantId, appId)
```

### 4.2 GovernanceConfigService（激活 collect.* 死开关）
```java
public boolean collectEnabled(Long tid)      // 读 SystemConfig collect.enabled，默认 true
public double  sampling(Long tid)            // collect.sampling，默认 1.0
public boolean ipAnonymize(Long tid)         // collect.ip.anonymize，默认 false
public String  maskIp(String ip)             // 末段置 0（IPv4）
```
> 注意：这三个 key 现由 `DataSeeder.seedSystemGlobal()` 种入但从未被读取——本服务是首次接线。

### 4.3 ParsingRuleService
```java
public EventPayload apply(Long tid, Long appId, EventPayload p) {
    for (ParsingRule r : rules(tid, appId)) {        // priority 升序，含 appId=0
        if (!match(r, p)) continue;
        p = action(r, p);                            // set/rename/extract/anonymize
        if (p == null) return null;                  // drop 短路
    }
    return p;
}
```

### 4.4 EventDefinitionService
```java
public ResolvedEvent resolve(Long tid, Long appId, String eventKey, String type) {
    EventDefinition def = lookup(tid, appId, eventKey);   // 缓存
    if (def == null) return switch (onMiss(tid,appId)) {  // MARK/DROP/UNKNOWN
        case DROP -> null; case UNKNOWN -> ResolvedEvent.unknown(); default -> ResolvedEvent.mark();
    };
    validateProperties(def, ...);                          // 按 propertiesSchema
    return ResolvedEvent.of(def);                          // 用标准 eventName/type
}
```

### 4.5 IdentityService
```java
public String resolve(Long tid, Long appId, String distinctId, String userId) {
    return aliasRepo.findByTenantIdAndAppIdAndDistinctId(tid, appId, distinctId)
        .map(UserIdentityAlias::getUnifiedId)
        .orElseGet(() -> strategy(tid, appId).pick(distinctId, userId));
}
@Transactional
public void identify(Long tid, Long appId, String distinctId, String userId) {
    // upsert uba_user_alias(distinct_id → unified_id=userId)
}
```

## 5. `ingest()` 集成（对齐真实代码 CollectorService.java L54-102）

在现有方法内**按序插入**四个钩子，其余逻辑（trunc / UA 解析 / 时间 / submit）保持不变：

```java
public boolean ingest(EventPayload p, String ip, String userAgent) {
    App app = resolveApp(p.getAppKey());
    if (app == null || !"ACTIVE".equals(app.getStatus())) return false;   // L55-59 不变
    Long tid = app.getTenantId(), appId = app.getId();

    // ② 治理开关（新增）——enabled=false 直接拒收；采样；IP 脱敏
    if (!configService.collectEnabled(tid)) return false;
    if (!configService.pass(tid)) return false;                 // sampling
    if (configService.ipAnonymize(tid)) ip = configService.maskIp(ip);

    // ③ 解析规则（新增）——字段映射/抽取/过滤；返回 null 表示被 drop
    p = parsingRuleService.apply(tid, appId, p);
    if (p == null) return false;

    Event e = new Event();
    e.setTenantId(tid); e.setAppId(appId);

    // ④ 事件字典（新增）——命中用标准名/类型；未命中按 onMiss
    var resolved = eventDefService.resolve(tid, appId, p.getEvent(), p.getType());
    if (resolved == null) return false;                          // DROP
    e.setEventType(resolved.type());
    e.setEventName(resolved.eventName());
    // ...（原 L66-100：distinctId/session/url/ua/geo/properties/ts 保持不变）

    // ⑤ 用户标识归并（新增）——折叠匿名 distinctId 到 unified_id
    e.setDistinctId(identityService.resolve(tid, appId, e.getDistinctId(), p.getUserId()));

    return streamEngine.submit(e);                              // L101 不变
}
```

> 集成要点：②③④⑤ 均为"未配置即等价当前行为"，保证灰度安全。distinctId 归并放在 Event 组装后、submit 前，避免影响 sessionId 生成。

## 6. Controller

### 6.1 鉴权配置接口 `/api/governance/**`
镜像 `SystemController`：首行 `requireManage()`，写操作 `auditService.record("governance", module, action, detail)`，返回 `ApiResponse.ok(...)`，分页返回 `{rows,total,page,size}`，校验失败抛 `BusinessException`。

```java
@RestController @RequestMapping("/api/governance")
public class GovernanceController {
    @GetMapping("/events")
    public ApiResponse<Map<String,Object>> events(@RequestParam Long appId,
            @RequestParam(defaultValue="0") int page, @RequestParam(defaultValue="20") int size) {
        requireManage();
        return ApiResponse.ok(eventDefService.page(scopeTenant(), appId,
            Math.max(0,page), Math.max(1, Math.min(size,200))));   // 复用 B7 clamp 约定
    }
    @PostMapping("/events") public ApiResponse<...> create(@RequestBody EventDefinition d){ requireManage(); ... }
    // PUT /events/{id}、PATCH /events/{id}/toggle、DELETE /events/{id} 同构
    // /rules、/identity、/alias 同构
}
```

### 6.2 开放端点 `/api/collect/identify`（CollectorController 内新增）
```java
@PostMapping("/identify")
public ApiResponse<Void> identify(@RequestBody IdentifyRequest r) {
    App app = collectorService.resolveApp(r.getAppKey());
    if (app == null || !"ACTIVE".equals(app.getStatus())) return ApiResponse.error(400, "无效 appKey");
    identityService.identify(app.getTenantId(), app.getId(), r.getDistinctId(), r.getUserId());
    return ApiResponse.ok(null);
}
```
> `/api/collect/**` 已在 `SecurityConfig` 白名单，无需改安全配置；需自行加限流防刷。

## 7. 权限与种子

- `PermissionCatalog.build()` 的 `collect` 分组加 `meta:manage`（"采集治理"）。
- `DataSeeder.seedRoles()`：SUPER_ADMIN / TENANT_ADMIN 追加 `meta:manage`。
- `DataSeeder` 新增幂等种子（`count()==0` 才种）：
  - 3 条内置 EventDefinition：`$pageview`/`$click`/`$leave`。
  - 1 条透传 ParsingRule（priority=100，match 全量，action=set 空操作）。
  - 1 条默认 IdentityRule（strategy=DISTINCT_FIRST）。

## 8. 前端

### 8.1 api/index.js
```js
export const governanceApi = {
  events: (params) => http.get('/governance/events', { params }),
  createEvent: (d) => http.post('/governance/events', d),
  updateEvent: (id, d) => http.put(`/governance/events/${id}`, d),
  toggleEvent: (id, enabled) => http.patch(`/governance/events/${id}/toggle`, { enabled }),
  deleteEvent: (id) => http.delete(`/governance/events/${id}`),
  // rules / identity / alias 同构
}
```

### 8.2 页面（沿用 Dict.vue / Audit.vue 范式）
- **EventDef.vue**：`el-table` + 分页（UI page 1 基 → 请求 `page-1`，响应取 `.rows`）；抽屉编辑；属性 schema 用动态表单或 JSON 编辑器；启停 `el-switch` 直调 toggle；删除用 `ElMessageBox.confirm` 包 `try/catch`（遵循 F9 约定）。
- **ParsingRule.vue**：表格 + priority 排序 + match/action JSON 编辑。
- **Identity.vue**：策略表单 + alias 查询表。
- 每个页面 `watch(() => appStore.currentAppId, load)`（遵循 F2/F5 约定，App 切换自动刷新）。

### 8.3 router/index.js
```js
{ path: '/governance/events', component: () => import('@/views/governance/EventDef.vue'),
  meta: { title:'事件字典', icon:'Tickets', group:'governance', perm:'meta:manage', tenant:true } },
// /governance/rules、/governance/identity 同组
```

## 9. SDK 接线（权威副本）
改 `backend/src/main/resources/sdk/ubax.js`（v1.1.0），`identify(userId)` 除写 localStorage 外，向 `/api/collect/identify` POST 一次 alias 上报（失败静默，不阻塞）。**不改** `frontend/public/sdk/ubax.js`（过期副本）。

## 10. 验证步骤（H2 local profile）

```
# 构建 & 启动（重建前先停后端，jar 会被锁）
mvn -q clean package -DskipTests
java -jar target/ubax-server.jar --spring.profiles.active=server... (local)

# ① 字典 CRUD（管理员 token）
curl -s -H "Authorization: Bearer $T" localhost:PORT/api/governance/events?appId=1
# ② ingest 应用字典：上报未定义事件 → 校验 onMiss 行为
curl -s -X POST localhost:PORT/api/collect/event -H 'Content-Type: application/json' \
     -d '{"appKey":"...","type":"custom","event":"order_paid","distinctId":"web-abc","properties":{"amount":99}}'
# ③ 采样/开关：改 collect.sampling=0.5 → 批量上报后入库约半数
# ④ 身份归并：匿名上报 → /identify 绑定 → 用 userId 上报 → 画像合并为一条
```
> curl 内联中文会被 Git Bash 损坏，中文 payload 用 ASCII 或浏览器 fetch 发；界面中文以浏览器渲染为准。

## 11. 分期落地顺序

1. **P1**：EventDefinition 实体/Repo/Service/Controller + ingest ④ 钩子 + EventDef.vue + `meta:manage` + 种子。
2. **P2**：ParsingRule + GovernanceConfigService（②③ 钩子，激活 collect.*）+ ParsingRule.vue。
3. **P3**：IdentityRule + UserIdentityAlias + `/identify` + SDK 接线 + Identity.vue。

每期独立构建、启动、curl + 浏览器双验证后再进入下一期。
