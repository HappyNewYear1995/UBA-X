# 采集治理子系统 · 产品需求文档（PRD）

> 功能名称：规则 / 事件 / 用户标识配置（采集治理）
> 版本：v1.0（设计评审稿）
> 归属模块：`governance`（采集治理），权限码 `meta:manage`
> 关联文档：[ARCHITECTURE.md](./ARCHITECTURE.md) · [TECHNICAL.md](./TECHNICAL.md)

---

## 1. 背景与问题

UBA-X 目前的采集链路里，**事件解析逻辑是硬编码的单点**（`CollectorService.ingest()`）。运营或管理员无法在不改代码、不发版的前提下调整采集口径，由此产生三类问题：

| # | 问题 | 现状 | 影响 |
| --- | --- | --- | --- |
| P1 | **事件是自由字符串** | 任意 `event` / `type` 都能入库，无字典、无属性 schema、无校验 | 事件名脏乱、口径不统一，分析结果不可信 |
| P2 | **采集开关是死的** | `DataSeeder` 已种 `collect.enabled` / `collect.sampling` / `collect.ip.anonymize`，但 `CollectorService` **从不读取** | 全局开关、采样、IP 脱敏形同虚设 |
| P3 | **用户身份不归并** | `UserProfile` 以 `distinctId` 为唯一键，`userId` last-write-wins，无 alias 表、无服务端 identify 端点 | 同一真人登录前后（匿名 `web-uuid` → 真实 `userId`）分裂成两条画像，留存/转化被低估 |

## 2. 目标与非目标

**目标**：让采集口径**配置化、可视化、可审计**。管理员通过前端页面即可定义"哪些事件有效、事件如何解析、用户如何归并"，无需研发介入。

**非目标（本期不做）**：
- 不做实时规则热更新的毫秒级生效（允许 ≤ 缓存 TTL 的延迟，默认 60s 或变更即失效）。
- 不做跨租户的事件字典共享市场。
- 不改造历史存量事件（仅对配置生效后的新事件生效）。

## 3. 用户角色

| 角色 | 权限 | 场景 |
| --- | --- | --- |
| 平台超管（SUPER_ADMIN，tenantId=null） | 跨租户查看全部配置 | 平台巡检、口径治理 |
| 租户管理员（TENANT_ADMIN） | 管理本租户配置 | 日常事件字典维护、规则调整 |
| 普通分析人员 | 只读或无权 | 依赖治理后的干净数据做分析 |

权限统一由新权限码 `meta:manage` 控制，纳入 `PermissionCatalog` 单一权限源。

## 4. 功能清单

### 4.1 事件配置（EventDefinition）— 优先级 P1

定义"允许采集哪些事件"，把自由字符串收敛为标准字典。

- **字段**：事件键（event_key，如 `$pageview`）、显示名、事件类型（pageview/click/leave/custom）、属性 schema（JSON，声明属性名/类型/是否必填）、校验策略、是否入库（enabled）、版本号。
- **作用域**：`tenant_id` + `app_id`（`app_id=0` 表示该租户全 App 生效）。
- **未命中策略（可配）**：`放行并标记` / `丢弃` / `降级为 $unknown`。默认"放行并标记"以保证向后兼容，不丢数据。
- **能力**：列表（分页/按 App/按类型筛选）、新增、编辑、启停、删除、从现有事件一键"提取为字典项"。

**验收**：在页面新增一个 `order_paid` 事件并声明属性 `amount:number:required`；上报缺失 `amount` 的该事件 → 按校验策略处理（拒绝或标记），审计日志留痕。

### 4.2 解析规则（ParsingRule）— 优先级 P2

配置化地在事件入库前做字段映射 / 属性抽取 / 过滤 / 路由，并**真正激活** `collect.*` 死开关。

- **字段**：优先级（priority，小者先）、匹配条件（match，JSON：按 event/type/app/path 等）、动作（action，JSON：set/rename/extract/drop/route/anonymize）、是否启用。
- **内置能力**：
  - 全局开关：读取 `collect.enabled`（false 时整体拒收）。
  - 采样：读取 `collect.sampling`（0~1，按比例放行）。
  - IP 脱敏：读取 `collect.ip.anonymize`（true 时抹去末段）。
  - 字段映射/属性抽取：把 `properties` 或 URL query 里的值提升为标准字段。
- **作用域**：`tenant_id` + `app_id`（同上）。

**验收**：把 `collect.sampling` 调为 0.5 → 大批量上报后入库量约为半数；配一条 `drop` 规则匹配 `type=heartbeat` → 该类事件不再入库。

### 4.3 用户标识（IdentityRule + Alias）— 优先级 P3

解决登录前后画像分裂，提供身份归并。

- **IdentityRule 字段**：归并策略（distinctId 优先 / userId 优先 / 显式 alias）、是否启用、作用域。
- **Alias 机制**：新增服务端开放端点 `POST /api/collect/identify`，入参 `{appKey, distinctId, userId}`，写入 `uba_user_alias`（distinct_id → unified_id）。解析时先查 alias，把匿名 distinctId 折叠到已登录 unified_id。
- **SDK 接线**：`ubax.identify(userId)` 除本地存储外，同时上报服务端 alias（改**权威副本** `backend/src/main/resources/sdk/ubax.js`，不改过期的 `frontend/public/sdk/ubax.js`）。
- **前端**：标识策略配置 + alias 关系查询页。

**验收**：先匿名上报若干事件（distinctId=`web-abc`），再调用 identify 绑定 `userId=u100`，随后用 `u100` 上报 → 画像合并为一条，会话/事件计数累加而非新建。

## 5. 交互与页面

在左侧导航新增 **采集治理** 分组（或并入"系统管理"组），三个页面沿用现有 `Dict.vue`（数组型）/ `Audit.vue`（分页型，UI 1 基 → 请求 `page-1`，响应取 `.rows`）范式：

| 页面 | 路由 | 主要交互 |
| --- | --- | --- |
| 事件字典 | `/governance/events` | 表格 + 抽屉编辑 + 属性 schema 编辑器 + 启停开关 |
| 解析规则 | `/governance/rules` | 表格（可拖拽调优先级）+ match/action JSON 编辑 + 启停 |
| 用户标识 | `/governance/identity` | 策略表单 + alias 关系查询表 |

所有写操作触发 `auditService.record(...)`，可在审计日志中追溯。

## 6. 数据埋点

治理子系统自身的操作纳入审计：`type=governance`，`module` 取 `event_def` / `parsing_rule` / `identity`，`action` 取 `create/update/toggle/delete`，`detail` 记录关键变更摘要。

## 7. 分期与里程碑

| 期 | 范围 | 交付判定 |
| --- | --- | --- |
| **P1** | 事件字典全链路（实体/API/ingest 集成/前端页/权限/种子） | 页面 CRUD 通过 + curl 验证 ingest 应用字典 |
| **P2** | 解析规则 + 激活 collect.* 死开关 | 采样/开关/脱敏生效可验证 |
| **P3** | 用户标识 + alias 表 + /identify + SDK 接线 | 登录前后画像合并可验证 |

每期独立可上线、可回滚。H2 local profile 重启重种用于验证。

## 8. 风险与兼容

- **向后兼容**：默认策略必须"放行"，绝不能因未配置字典而丢弃历史可用事件。未命中默认"标记放行"。
- **性能**：配置查询全部走内存缓存（仿 `resolveApp`），避免每条事件打 DB；配置变更时主动失效。
- **数据迁移**：新表，`ddl-auto` 自动建表；无存量迁移。历史事件不受影响。
