# UBA-X API 接口参考

后端基址：`http://<host>:8081`。除采集端点（`/api/collect/**`）与探针端点（`/api/pilot/agents/**`）外，
所有 `/api/**` 接口均需 JWT 鉴权。

---

## 通用约定

### 响应封装

所有 `/api/**` 接口统一返回 `ApiResponse` 信封：

```json
{ "code": 0, "message": "success", "data": { } }
```

- `code = 0` 表示成功，`data` 为业务数据（`NON_NULL`，为空时省略）。
- `code = 401`（HTTP 仍可能为 200）表示未认证 —— **前端拦截器会据此清除 token 并跳转登录页**。
- 其他非零 `code` 为业务错误，`message` 为错误描述。

> 前端 axios 拦截器在 `code === 0` 时自动解包，直接把 `data` 交给调用方。

### 鉴权

登录成功后返回 JWT，后续请求携带：

```
Authorization: Bearer <token>
```

Token 有效期由 `ubax.jwt.expiration-minutes` 控制（默认 720 分钟）。JWT 中携带 `tenantId`，
服务端通过 `TenantContext` 做租户隔离。

### 分页规范（重要）

- **后端 `page` 参数为 0 基**（第 0 页是第一页）。
- 分页接口返回 `Map{ rows, total, page, size }`，不是裸数组。
- 前端 Element Plus `el-pagination` 的 `current-page` 为 1 基，调用时须传 `page: uiPage - 1`。
- 部分列表接口（如 `/api/account/logins`）返回 `{ rows, total }`，取值用 `.rows`。

### 开放端点

以下路径在 `SecurityConfig` 中 `permitAll`，无需 JWT：

- `/api/auth/**`（登录/登出/me）
- `/api/collect/**`（数据采集，凭 appKey 校验）
- `/api/pilot/agents/**`（探针面，凭 body 中 agentKey 校验）
- `/actuator/health`、`/health`、`/api/health`
- `/sdk/**`、`/demo/**`、`/`

CORS 对所有来源开放（`allowedOriginPatterns: *`，允许携带凭证）。

---

## 鉴权 · `/api/auth`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，body `{username, password}`，返回 token + 用户信息 |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 当前登录用户 |

---

## 数据采集 · `/api/collect`（开放）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/collect/event` | 单事件上报 |
| POST | `/api/collect/batch` | 批量上报，body `{appKey, events:[...]}` |
| POST | `/api/collect/beacon` | `sendBeacon` 友好端点，接受 `text/plain` / `application/json` / `*/*` |
| GET | `/api/collect/health` | 采集端健康检查 |

批量上报响应：`{ "accepted": <int>, "rejected": <int> }`。
未知或停用的 appKey 会被拒绝（计入 `rejected`）。服务端从 `X-Forwarded-For` / `X-Real-IP` / `remoteAddr`
解析客户端 IP，并读取 `User-Agent`。

事件体（`EventPayload`）主要字段：`appKey`、`event`、`distinctId`、`sessionId`、`properties`、
`url`、`path`、`title`、`referrer`、设备/浏览器/OS/地理/屏幕等。

---

## 核心分析 · `/api/analytics`

均需 `analytics:view` 权限 + 租户上下文。公共查询参数：`appId`、`from`、`to`（日期字符串，可选）。

| 方法 | 路径 | 主要参数 | 说明 |
| --- | --- | --- | --- |
| GET | `/overview` | appId, from, to | 概览指标 |
| GET | `/trend` | appId, from, to | 趋势序列 |
| GET | `/realtime` | appId, minutes(默认30) | 实时分钟聚合 |
| GET | `/live` | limit(默认30，≤100) | 实时事件流 |
| GET | `/events` | appId, from, to, limit(默认20，≤100) | Top 事件 |
| GET | `/pages` | appId, from, to, limit(默认20，≤100) | Top 页面 |
| GET | `/distributions` | appId, from, to | 维度分布 |
| POST | `/funnel` | body `{appId, steps:[], from, to}` | 漏斗分析 |
| GET | `/retention` | appId, from, days(默认7) | 留存分析 |
| GET | `/heatmap` | appId, path, from, to | 点击热力图 |
| GET | `/sessions` | appId, from, to, limit(默认30，≤100) | 会话列表 |
| GET | `/sessions/{sessionId}` | — | 单会话事件明细 |
| GET | `/stream-metrics` | — | StreamEngine 队列/吞吐指标 |

---

## 数据应用 · `/api/ext`

均需 `analytics:view` 权限 + 租户上下文，参数以 `appId` / `from` / `to` / 维度为主。

| 方法 | 路径 | 对应应用 |
| --- | --- | --- |
| GET | `/security` | 访问安全分析 |
| GET | `/geo` | 地域分析 |
| GET | `/fingerprint` | 客户端指纹 |
| GET | `/conversion-path` | 转化路径分析 |
| GET | `/funnel-compare` | 漏斗对比 |
| GET | `/retention-segments` | 留存分群 |
| GET | `/churn-health` | 流失预警 / 健康分 |
| GET | `/rfm` | RFM 价值分层 |
| GET | `/journey-users` | 用户旅程（用户列表） |
| GET | `/journey` | 用户旅程时间线 |
| GET | `/page-experience` | 页面性能与体验 |
| GET | `/anomaly` | 异常预警 |
| GET | `/event-meta` | 事件元数据（探索器用） |
| GET | `/event-explore` | 事件探索 |
| GET | `/attribution` | 归因分析 |
| GET | `/ab-test` | A/B 实验 |

---

## 应用与接入 · `/api/apps`

需 `app:manage` 权限 + 租户上下文。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/apps` | 应用列表 |
| GET | `/api/apps/{id}` | 应用详情 |
| POST | `/api/apps` | 创建应用 |
| PUT | `/api/apps/{id}` | 更新应用 |
| DELETE | `/api/apps/{id}` | 删除应用 |
| POST | `/api/apps/{id}/rotate-key` | 轮换 appKey |
| GET | `/api/apps/{id}/sdk?server=<host>` | 获取接入 SDK 片段 |

---

## 用户画像 · `/api/profiles`

需 `data:view` 权限 + 租户上下文。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/profiles` | 画像列表（分页参数） |
| GET | `/api/profiles/stats` | 画像统计 |
| GET | `/api/profiles/{distinctId}` | 单用户画像详情 |

---

## Agent 探针 · 管理面 `/api/pilot/manage`

需 `agent:manage` 权限 + 租户上下文（**必须用租户用户访问，超管无租户上下文会 401**）。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/overview` | 探针总览（total / online / offline / installing） |
| GET | `/agents` | 探针列表 |
| GET | `/agents/{id}` | 探针详情 |
| POST | `/agents` | 注册探针，body `{name, host, tags, config?}`，返回 `{agent, installCommand}` |
| PUT | `/agents/{id}/config` | 下发配置（JSON），配置版本 +1 并触发 `RELOAD_CONFIG` |
| POST | `/agents/{id}/command` | 下发指令，body `{type, payload?}` |
| GET | `/agents/{id}/commands` | 指令记录 |
| DELETE | `/agents/{id}` | 删除探针 |

指令类型：`RELOAD_CONFIG` / `START` / `STOP` / `RESTART` / `FLUSH` / `UPGRADE`。
指令状态：`PENDING` → `DELIVERED` → `DONE` / `FAILED`。

`installCommand` 含 `<SERVER>` 占位符，前端替换为 `location.hostname:8081`。

## Agent 探针 · 探针面 `/api/pilot/agents`（开放，凭 agentKey）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/register` | 探针注册，body 含 `agentKey/host/ip/version/os/arch/name/tags` |
| POST | `/heartbeat` | 心跳，上报 `metrics/eventsSent/configVersion`，返回最新配置 + 待执行指令 |
| POST | `/pull` | 拉取指令（等价 heartbeat 响应） |
| POST | `/result` | 回报指令执行结果 |

服务端在 register/heartbeat 时观测探针 remote IP（NAT 友好），并在配置为空时回填 IP。
新注册探针会自动注入租户默认 appKey（`configWithAppKey`），实现开箱即采集。

---

## SDK 下发 · `/sdk`（开放）

| 方法 | 路径 | Content-Type | 缓存 |
| --- | --- | --- | --- |
| GET | `/sdk/pilot-agent.sh` | text/plain;charset=UTF-8 | no-cache（保证升级即时生效） |
| GET | `/sdk/ubax.js` | application/javascript;charset=UTF-8 | max-age 5min |

前端「Agent 探针」页的「下载探针」按钮即 fetch `/sdk/pilot-agent.sh` 后以 Blob 触发浏览器下载。

---

## AI 数据助手 · `/api/assistant`

需 `assistant:use` 权限。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/assistant/chat` | 自然语言对话，body 含消息内容 |
| GET | `/assistant/suggest` | 推荐问题 |

---

## 系统管理 · `/api/system`

需 `system:manage` 权限（日志类）+ 租户上下文。

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/audit` | 操作日志（分页：page 0 基，返回 `{rows,total,page,size}`） |
| GET | `/audit/modules` | 审计模块去重列表 |
| GET | `/login-log` | 登录日志（分页同上） |
| GET | `/config` | 参数配置 |
| PUT | `/config` | 保存参数（body 为编辑项） |
| GET | `/dict/types` | 字典类型 |
| GET | `/dict?type=<t>` | 按类型取字典 |
| POST | `/dict` | 新增/保存字典 |
| DELETE | `/dict/{id}` | 删除字典 |
| GET | `/notice` | 公告列表 |
| GET | `/notice/published` | 已发布公告（顶栏用） |
| POST | `/notice` | 保存公告 |
| DELETE | `/notice/{id}` | 删除公告 |
| GET | `/monitor` | 系统监控（JVM/内存/线程等） |
| GET | `/task` | 定时任务列表 |
| GET | `/task/logs` | 任务执行日志（分页） |
| PUT | `/task/{id}/toggle?enabled=<bool>` | 启停任务 |
| POST | `/task/{id}/run` | 立即执行任务 |

---

## 个人中心 · `/api/account`

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/me` | 当前账户信息 |
| PUT | `/profile` | 更新资料 |
| PUT | `/password` | 修改密码 |
| GET | `/logins?limit=20` | 我的登录历史，返回 `{rows,total}` |

---

## 租户 / 用户 / 角色

### 租户 · `/api/tenants`（超管，`tenant:manage`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/tenants` | 租户列表 |
| GET | `/api/tenants/current` | 当前租户 |
| POST | `/api/tenants` | 创建租户 |
| PUT | `/api/tenants/{id}` | 更新租户 |

### 用户 · `/api/users`（`user:manage`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/users` | 用户列表 |
| POST | `/api/users` | 创建用户 |
| PUT | `/api/users/{id}` | 更新用户 |
| POST | `/api/users/{id}/reset-password` | 重置密码 |
| DELETE | `/api/users/{id}` | 删除用户 |

### 角色 · `/api/roles`（`role:manage`）

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/roles` | 角色列表 |
| GET | `/api/roles/permissions` | 权限点目录 |
| POST | `/api/roles` | 创建角色 |
| PUT | `/api/roles/{code}` | 更新角色 |
| DELETE | `/api/roles/{code}` | 删除角色 |

---

## 健康检查

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/health`、`/api/health`、`/actuator/health` | 存活探测 |
