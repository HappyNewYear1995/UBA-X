# UBA-X 架构设计

本文描述 UBA-X 的分层架构、采集管线、多租户隔离、权限模型、探针闭环与数据模型。

---

## 1. 总体分层

```
┌─────────────────────────────────────────────────────────────┐
│  接入层  浏览器 SDK(ubax.js) · 服务端 API · Agent 探针         │
└───────────────────────────┬─────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  网关/安全层  nginx(:8888) · Spring Security · JwtAuthFilter │
└───────────────────────────┬─────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  应用层  Controller (analytics/ext/app/pilot/system/tenant)  │
│  ─────────────────────────────────────────────────────────  │
│  领域服务  CollectorService · StreamEngine · AnalyticsService│
│           ExtService · PilotService · SystemService · RBAC   │
└───────────────────────────┬─────────────────────────────────┘
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  持久层  Spring Data JPA / Hibernate → MySQL(生产) / H2(本地)│
└─────────────────────────────────────────────────────────────┘
```

后端按业务域分包（`com.ubax.<domain>`），每个域内典型结构为 `controller / service / domain(实体) / repo`：

| 包 | 职责 |
| --- | --- |
| `analytics` | 核心分析：概览、趋势、实时、事件/页面、漏斗、留存、热力图、会话 |
| `ext` | 数据应用市场（14 个扩展分析） |
| `app` | 应用与 appKey 管理、接入 SDK 片段 |
| `collector` | 采集入口 + `StreamEngine` 流式处理 |
| `stream` | 实时分钟聚合（`RealtimeMinute`） |
| `event` | 事件领域模型 |
| `profile` | 用户画像 |
| `pilot` | Agent 探针：管理面 + 探针面 |
| `sdk` | `/sdk/**` 探针脚本与浏览器 SDK 下发 |
| `system` | 系统管理：配置/字典/公告/监控/任务/审计/登录日志/账户 |
| `tenant` | 租户 / 用户 / 角色 / 鉴权 |
| `assistant` | AI 数据助手 |
| `common` | 安全配置、JWT、`TenantContext`、`ApiResponse`、异常 |
| `bootstrap` | `DataSeeder` 种子数据 |

---

## 2. 采集管线

从数据产生到可查询的完整链路：

```
SDK / API / 探针
      │  POST /api/collect/{event|batch|beacon}
      ▼
CollectorController  ── 解析 clientIp(XFF/X-Real-IP/remoteAddr) + User-Agent
      ▼
CollectorService.ingest(payload, ip, ua)
      │  ① 校验 appKey（未知/停用 → 拒绝）
      │  ② 补全租户、appId、时间、IP、UA、地理
      ▼
StreamEngine.submit(Event)  ── 有界阻塞队列(默认 50000)，满则丢弃并计数
      ▼
ubax-stream-worker(单线程)  ── poll + drainTo 批量(≤512)
      │  ③ streamWriter.persistEvents(batch) → Event 落库
      │  ④ 内存窗口累加：MinuteAcc(pv/uv/events)、ProfileDelta(增量)
      ▼
ubax-stream-flusher(每 5s)  ── flush()
      │  ⑤ setMinute() → RealtimeMinute（分钟级 rollup）
      │  ⑥ upsertProfile() → UserProfile（画像增量）
      ▼
可被 Analytics / Ext / Profile 查询
```

### StreamEngine 设计要点

`StreamEngine` 是「受限硬件上 Flink 作业的轻量替代」：

- **解耦**：有界 `LinkedBlockingQueue` 把采集（生产）与落库（消费）解耦，削峰填谷；队列满时 `offer` 失败并计入 `dropped`，不阻塞采集线程。
- **批量**：worker 用 `poll(400ms)` + `drainTo(511)` 攒批，单次最多 512 条，降低数据库往返。
- **内存窗口**：`ConcurrentHashMap` 维护两类累加器——
  - `minutes`：key = `tenant|app|minuteEpoch`，累计 pv/uv/events；
  - `profiles`：key = `tenant|distinctId`，累计事件增量、最近事件、是否新会话。
- **周期 flush**：`ScheduledExecutorService` 每 `flush-interval-ms`（默认 5000）把窗口写库并清空增量。
- **自动淘汰**：超过 3 分钟的分钟窗口从内存移除；活跃会话集合超 2 万条时清空，防内存膨胀。
- **优雅停机**：`@PreDestroy` 停 worker/flusher，排空剩余队列并最后 flush，尽量不丢数据。
- **可观测**：`metrics()` 暴露 received/persisted/dropped/queueSize/queueCapacity/activeWindows，经 `/api/analytics/stream-metrics` 查询。

时区固定 `UTC+8`，分钟对齐用 `truncatedTo(MINUTES)`。

---

## 3. 多租户隔离

- **载体**：`TenantContext`（`ThreadLocal<Principal>`）。`Principal` 含 `userId / tenantId / username / roles`。
- **写入**：`JwtAuthenticationFilter` 解析 Bearer token → `TenantContext.set(principal)`，同时刷新在线用户注册表（`OnlineUserRegistry.touch`）。请求结束 `finally` 中 `TenantContext.clear()`，杜绝线程复用串号。
- **读取**：服务层用 `TenantContext.requireTenantId()` 取租户并 scope 所有查询；租户为空抛 `BusinessException(401)`。
- **平台超管特例**：`SUPER_ADMIN` 的 `tenantId == null`，可跨租户管理（租户/用户/角色），但**无租户上下文**，因此不能访问 `requireTenantId()` 的租户级接口（如 Agent 探针、应用、分析）。前端路由守卫据此把超管引导到 `/users`，租户用户引导到 `/dashboard`。

> **已知陷阱**：租户级接口对超管返回 `code:401`（HTTP 仍 200），前端拦截器会清 token 跳登录。
> 全局调用链的接口必须兼容 `tenantId == null` 的超管，避免误踢。

---

## 4. 认证与权限（RBAC）

### 认证

- 无状态 JWT（jjwt 0.12.6），`SessionCreationPolicy.STATELESS`，CSRF 关闭。
- 登录 `/api/auth/login` 校验 BCrypt 口令，签发含 `tenantId/roles` 的 token（默认 720 分钟）。
- 过滤器链：`JwtAuthenticationFilter` 置于 `UsernamePasswordAuthenticationFilter` 之前；
  token 无效则保持匿名（由 `authorizeHttpRequests` 决定是否放行）。

### 授权

- **权限点**（细粒度）：`analytics:view`、`data:view`、`assistant:use`、`app:manage`、`agent:manage`、
  `user:manage`、`role:manage`、`tenant:manage`、`system:manage`。
- **角色**（`Role` 实体，`permissions` 逗号分隔）：

  | 角色 | 说明 | 权限点 |
  | --- | --- | --- |
  | SUPER_ADMIN | 平台超管（跨租户） | 全部 |
  | ADMIN | 租户管理员 | 除 tenant:manage 外全部 |
  | ANALYST | 数据分析师 | analytics/data/assistant/app/agent |
  | VIEWER | 只读访客 | analytics/data/assistant |

- **前端联动**：路由 `meta.perm` + `meta.tenant` + `meta.superAdmin`；`router.beforeEach` 校验 token、
  权限点（`auth.has(perm)`）与租户上下文，菜单按权限动态过滤，无权限跳各自首页。
- **开放路径**：`/api/auth/**`、`/api/collect/**`、`/api/pilot/agents/**`、`/sdk/**`、`/demo/**`、健康检查。

---

## 5. Agent 探针闭环

`ubax-pilot` 分两个平面：

### 管理面 `/api/pilot/manage`（JWT + 租户）

运营者注册探针、下发配置、发指令、看指标与指令记录。`PilotService`：

- `createAgent`：建 `Agent`（状态 INSTALLING），生成 `agentKey`，构建默认 config，回填租户默认 appKey，
  返回 `{agent, installCommand}`（含 `<SERVER>` 占位）。
- `updateConfig`：配置版本 +1，自动追加 `RELOAD_CONFIG` 指令。
- `sendCommand`：建 `AgentCommand`（PENDING）。
- `@Scheduled(fixedDelay=30000) markOffline`：静默超 90s 的 ONLINE 探针置 OFFLINE。

### 探针面 `/api/pilot/agents`（开放，凭 agentKey）

- `register`：探针上报 agentKey/host/ip/version/os/arch/name/tags；服务端观测 remote IP（NAT 友好），
  IP 为空时回填，返回最新配置（`configWithAppKey` 自动注入默认 appKey）。
- `heartbeat` / `pull`：上报 metrics/eventsSent/configVersion，返回最新配置 + 待执行指令列表。
- `result`：回报指令执行结果（DONE/FAILED）。

### 探针运行时（`sdk/pilot-agent.sh`）

Bash 外壳内嵌纯标准库 Python 3：

```
bash 外壳
  ├─ 探测可用 python（逐个校验版本 ≥3.6，避开 Windows Store 桩）
  ├─ 释放内嵌 Python 到 mktemp 临时文件
  └─ 执行 Agent，退出后清理临时文件

Agent.run()
  ├─ register()（带重试）
  ├─ --once 模式：heartbeat → collect → heartbeat → exit
  └─ 常驻：tick 循环
        ├─ heartbeat()：上报 read_metrics()（/proc/loadavg·meminfo·uptime·cpu_count·disk_usage，全部 try 守护）
        │              应用返回配置，遍历并 handle_command()
        ├─ collect()：构造 $agent.telemetry 事件 → POST /api/collect/batch → 累加 events_sent
        └─ 指令：RELOAD_CONFIG/START/STOP/FLUSH/RESTART(os.execv)/UPGRADE(重新下载脚本)
```

指令状态机：`PENDING`（管理面创建）→ `DELIVERED`（探针 heartbeat 拉取）→ `DONE`/`FAILED`（探针 result 回报）。

浏览器 SDK（`sdk/ubax.js`）为独立 IIFE：localStorage 持久化 distinctId/sessionId（30 分钟过期）/userId，
自动埋点 pageview/click/leave，劫持 `history.pushState`+popstate 支持 SPA，`sendBeacon` 优先、XHR 兜底，
暴露 `window.ubax.{track,pageview,identify,reset,flush,...}`。

---

## 6. 数据模型

JPA 实体（`@Entity`），Lombok `@Data`，生产 `ddl-auto: update` / 本地 `create-drop`：

| 实体 | 表义 | 关键字段 |
| --- | --- | --- |
| `Tenant` | 租户 | name, status |
| `User` | 用户 | tenantId, username, password(BCrypt), role |
| `Role` | 角色 | code, name, permissions(逗号分隔) |
| `App` | 应用 | tenantId, appKey, status(ACTIVE/…) |
| `Event` | 行为事件 | tenantId, appId, eventType, distinctId, sessionId, properties, url/path, ip, ua, geo, device, eventTime |
| `RealtimeMinute` | 分钟聚合 | tenantId, appId, minute, pv, uv, events |
| `UserProfile` | 用户画像 | tenantId, distinctId, 累计事件/pv/访问, 最近活跃, tags |
| `Agent` | 探针 | tenantId, agentKey, name, host, ip, status(INSTALLING/ONLINE/OFFLINE), version, os, arch, tags, config(JSON), configVersion, metrics(JSON), eventsSent, lastHeartbeat |
| `AgentCommand` | 探针指令 | agentId, type, status(PENDING/DELIVERED/DONE/FAILED), payload, createdAt, deliveredAt |
| `AuditLog` | 操作日志 | tenantId, userId, module, action, detail, ip, createdAt |
| `Dict` | 数据字典 | tenantId, type, label, value |
| `Notice` | 通知公告 | tenantId, title, content, published, createdAt |
| `ScheduledTask` | 定时任务 | name, cron, enabled |
| `TaskLog` | 任务日志 | taskId, status, message, createdAt |
| `SystemConfig` | 参数配置 | tenantId, key, value |

事件 `properties` 以 JSON 文本存储（H2 MySQL 模式用 `TEXT` 列定义，`NON_KEYWORDS` 放开 `MINUTE/VALUE/KEY` 保留字）。

---

## 7. 前端架构

- **构建**：Vite 5 + `@vitejs/plugin-vue`；`manualChunks` 拆分 echarts / element 大包。
- **状态**：Pinia（`store/auth`：token、user、权限判定 `has(perm)`）；token/user 持久化 localStorage。
- **路由**：`createWebHistory`，主布局 `MainLayout` 嵌套子路由；数据应用 `/ext` 二级嵌套（Hub + Gallery + 14 应用）。
- **HTTP**：单例 axios（`baseURL:/api`，超时 20s）；请求拦截注入 Bearer；响应拦截统一解包 `code:0→data`，
  `code:401`/HTTP 401 清 token 跳登录。
- **视图分组**：分析 / 洞察 / 数据应用 / 采集接入 / 系统管理，菜单按 `meta.group` 与权限渲染。

---

## 8. 关键设计取舍

- **内存流处理 vs 外部流引擎**：用 `StreamEngine`（进程内队列+窗口）替代 Flink/Kafka，降低部署复杂度，
  适配低配服务器；代价是单实例、重启丢未 flush 窗口（已用优雅停机缓解）。后续量大可平滑替换为外部 MQ + 流作业。
- **探针零依赖**：Bash+Python 标准库，避免目标主机装 curl/jq/agent 包，`curl | bash` 即可拉起。
- **H2 本地 profile**：MySQL 兼容模式让本地无需装库即可全功能验证；`create-drop` + `DataSeeder` 保证每次干净可复现。
- **统一响应信封**：`{code,message,data}` 让前端拦截器集中处理成功解包与 401 踢登录，简化各页面错误处理。
