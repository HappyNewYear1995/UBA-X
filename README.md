# UBA-X · 数智化用户行为洞察与分析系统

> User Behavior Analytics Platform —— 一套面向多租户 SaaS 的用户行为采集、实时分析与洞察平台。
> 前端 Vue 3 + Element Plus，后端 Spring Boot 3 + JPA，内置分布式 Agent 探针、浏览器埋点 SDK、
> 40+ 分析视图与 AI 数据助手。

---

## 目录

- [产品简介](#产品简介)
- [核心特性](#核心特性)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [快速开始（本地）](#快速开始本地)
- [默认账号](#默认账号)
- [功能模块一览](#功能模块一览)
- [数据接入](#数据接入)
- [Agent 探针](#agent-探针)
- [项目结构](#项目结构)
- [生产部署](#生产部署)
- [文档索引](#文档索引)

---

## 产品简介

UBA-X 帮助企业把「用户在产品里做了什么」变成可查询、可下钻、可预警的数据资产。系统覆盖从
**数据采集**（浏览器 SDK / 服务端 API / 分布式探针）到 **实时计算**（分钟级流式聚合）再到
**分析洞察**（漏斗、留存、路径、归因、A/B、RFM、流失预警等）的完整链路，并提供多租户隔离、
基于角色的权限控制（RBAC）、操作审计与 AI 自然语言查询助手。

平台采用多租户架构：平台超级管理员跨租户运营，租户管理员管理本租户的用户、应用与数据，
数据分析师与只读访客按需查看分析结果。

## 核心特性

- **多租户 SaaS**：租户级数据隔离（`TenantContext` ThreadLocal 贯穿全链路），平台超管与租户管理员分层运营。
- **实时行为分析**：概览看板、实时分析（分钟级流式聚合）、事件/页面/漏斗/留存分析。
- **深度洞察**：用户画像、点击热力图、会话回放、用户旅程时间线。
- **数据应用市场**：14 个开箱即用的分析应用（安全风控、地域、转化路径、归因、A/B 实验、RFM、流失预警、异常检测等）。
- **AI 数据助手**：自然语言查询平台数据与指标。
- **分布式 Agent 探针**：远程注册、心跳、指标上报、配置下发与指令控制（重载/启停/重启/立即上报/升级）。
- **浏览器埋点 SDK**：自动采集 pageview / click / leave，支持 SPA 路由与 `sendBeacon` 兜底。
- **完整系统管理**：用户、角色权限、参数配置、数据字典、通知公告、系统监控、定时任务、操作日志、登录日志、个人中心。
- **RBAC 权限体系**：细粒度权限点 + 角色，前端路由/菜单按权限动态过滤。

## 技术栈

| 层次 | 技术 |
| --- | --- |
| 前端 | Vue 3.5、Vite 5、Element Plus 2.8、Pinia 2、Vue Router 4、ECharts 5、Axios |
| 后端 | Spring Boot 3.3.5、Java 17、Spring Data JPA、Spring Security、Hibernate |
| 鉴权 | JWT（jjwt 0.12.6），无状态会话 |
| 数据库 | 生产 MySQL 8；本地开发 H2 内存库（MySQL 兼容模式） |
| 探针 | Bash + Python 3（仅标准库）运行时，通过 `/sdk/**` 下发 |
| 构建 | Maven（后端）、Vite（前端） |

## 系统架构

```
┌──────────────┐   ┌──────────────┐   ┌───────────────────┐
│ 浏览器 SDK    │   │ 服务端 API    │   │ Agent 探针         │
│ (ubax.js)    │   │ (自定义埋点)  │   │ (pilot-agent.sh)  │
└──────┬───────┘   └──────┬───────┘   └─────────┬─────────┘
       │  /api/collect/*  │                     │ /api/pilot/agents/*
       ▼                  ▼                     ▼
┌─────────────────────────────────────────────────────────┐
│                   Spring Boot 后端 (:8081)                │
│  Collector → StreamEngine(异步队列+5s flush) → 持久化      │
│  Event / RealtimeMinute / UserProfile                     │
│  ─────────────────────────────────────────────────────   │
│  Analytics · Ext(数据应用) · Pilot(探针管理) · System     │
│  Assistant(AI) · Tenant/User/Role(RBAC) · SDK 下发        │
└───────────────────────────┬─────────────────────────────┘
                            ▼
                   MySQL / H2  +  JWT 鉴权
                            ▲
┌───────────────────────────┴─────────────────────────────┐
│              Vue 3 前端 (:5173 / nginx :8888)             │
└───────────────────────────────────────────────────────────┘
```

采集管线：`/api/collect/batch` → `CollectorService.ingest`（校验 appKey，拒绝未知/停用应用）
→ `StreamEngine`（有界异步队列 + worker + 5 秒批量 flush）→ 落库 `Event` / `RealtimeMinute` /
`UserProfile`。详见 [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)。

## 快速开始（本地）

本地开发使用 H2 内存库，无需安装 MySQL。**注意：H2 profile 为 `create-drop`，每次重启后端都会重新播种，数据会被清空。**

### 环境要求

- JDK 17
- Node.js 18+（前端）
- Maven 3.9+（后端；仓库 `.tools/` 内置便携版 JDK17 与 Maven，可选用）

### 1. 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
# 或先打包再运行：
mvn clean package -DskipTests
java -jar target/ubax-server.jar --spring.profiles.active=local
```

后端监听 `http://localhost:8081`，健康检查 `http://localhost:8081/health`。
H2 控制台（local profile）：`http://localhost:8081/h2-console`。

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端监听 `http://localhost:5173`，已配置 `/api` 代理到后端 `8081`。浏览器打开即可登录。

### 3. 生产构建

```bash
cd frontend && npm run build   # 产物在 frontend/dist
cd backend  && mvn clean package -DskipTests   # 产物 backend/target/ubax-server.jar
```

## 默认账号

本地 H2 每次启动由 `DataSeeder` 播种以下账号：

| 用户名 | 密码 | 角色 | 租户 | 说明 |
| --- | --- | --- | --- | --- |
| `admin` | `admin123` | SUPER_ADMIN | 无（平台级） | 跨租户管理，落地页为「用户管理」 |
| `demo` | `demo123` | ADMIN | 租户 1 | 租户管理员，可查看包括 Agent 探针在内的全部租户功能 |
| `analyst` | `analyst123` | ANALYST | 租户 1 | 数据分析师，只读分析 + 应用管理 |

> **提示**：Agent 探针、应用接入等租户级页面（路由 `meta.tenant=true`）需用 **租户用户**（如 `demo`）登录。
> 平台超管 `admin` 无租户上下文，访问这些接口会返回 401 并被拦截器踢回登录页。

内置角色与权限点：

| 角色 | 权限点 |
| --- | --- |
| SUPER_ADMIN | analytics:view, data:view, assistant:use, app:manage, agent:manage, user:manage, role:manage, tenant:manage, system:manage |
| ADMIN | analytics:view, data:view, assistant:use, app:manage, agent:manage, user:manage, role:manage, system:manage |
| ANALYST | analytics:view, data:view, assistant:use, app:manage, agent:manage |
| VIEWER | analytics:view, data:view, assistant:use |

## 功能模块一览

**分析（`analytics:view`）**

- 概览看板 `/dashboard`、实时分析 `/realtime`
- 事件分析 `/events`、页面分析 `/pages`、漏斗分析 `/funnel`、留存分析 `/retention`

**洞察（`data:view`）**

- 用户画像 `/profiles`（+ 详情 `/profiles/:distinctId`）、点击热力图 `/heatmap`、会话回放 `/sessions`

**数据应用市场（`/ext`，14 个应用）**

安全风控、地域分析、转化路径、漏斗对比、客户端指纹、留存分群、流失预警/健康分、RFM 价值分层、
用户旅程时间线、页面性能与体验、异常预警、事件探索、归因分析、A/B 实验。

**采集与接入**

- 应用与接入 `/apps`（`app:manage`）：创建应用、生成 appKey、轮换密钥、查看接入 SDK 片段。
- Agent 探针 `/agents`（`agent:manage`）：探针注册、心跳、指标、远程配置、指令控制、下载探针脚本。

**AI 助手** `/assistant`（`assistant:use`）：自然语言查询数据。

**系统管理（`system:manage` / `user:manage` / `role:manage` / `tenant:manage`）**

用户管理、角色权限、参数设置、数据字典、通知公告、系统监控、定时任务、操作日志、登录日志、个人中心、租户管理（超管）。

## 数据接入

### 浏览器 SDK

在应用页面引入即可自动采集 pageview / click / leave：

```html
<script>
  window.UBAX_CONFIG = {
    appKey: 'ubax_demo_mini_0002',   // 在「应用与接入」页获取
    server: 'http://your-host:8081',
    autoTrack: true
  };
</script>
<script src="http://your-host:8081/sdk/ubax.js"></script>
<script>
  ubax.identify('user-123');          // 绑定业务用户 ID（可选）
  ubax.track('purchase', { amount: 99 });  // 自定义事件
</script>
```

### 服务端 API

```bash
curl -X POST http://your-host:8081/api/collect/batch \
  -H 'Content-Type: application/json' \
  -d '{"appKey":"ubax_demo_mini_0002","events":[{"event":"$pageview","distinctId":"u1","properties":{}}]}'
```

采集端点 `/api/collect/**` 为开放端点（凭 appKey 校验），详见 [docs/API.md](docs/API.md)。

## Agent 探针

`ubax-pilot` 是分布式采集探针，Bash 外壳内嵌纯标准库 Python 3 运行时，无需 curl/jq 依赖。

在「Agent 探针」页注册探针后，可复制安装命令，或直接点击「下载探针」获取 `pilot-agent.sh`：

```bash
curl -fsSL http://your-host:8081/sdk/pilot-agent.sh | bash -s -- \
  --key <AGENT_KEY> \
  --server your-host:8081 \
  --name web-collector-03 \
  --tags 生产,华东
```

探针运行时循环：**注册 → 心跳（上报主机指标）→ 拉取并执行指令 → 采集事件上报**。
支持指令：`RELOAD_CONFIG`（重载配置）、`START`/`STOP`（启停采集）、`FLUSH`（立即上报）、
`RESTART`（重启）、`UPGRADE`（升级）。后端每 30 秒巡检，探针静默 90 秒自动置为离线。

新装探针会自动注入租户默认 appKey，开箱即采集。详见 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)。

## 项目结构

```
ubax/
├── backend/                     Spring Boot 后端
│   └── src/main/
│       ├── java/com/ubax/
│       │   ├── analytics/       核心分析（概览/趋势/实时/漏斗/留存/热力/会话）
│       │   ├── app/             应用与 appKey 管理
│       │   ├── assistant/       AI 数据助手
│       │   ├── bootstrap/       DataSeeder 种子数据
│       │   ├── collector/       采集入口 + StreamEngine
│       │   ├── common/          安全配置 / JWT 过滤器 / 审计
│       │   ├── event/           事件领域模型
│       │   ├── ext/             数据应用（14 个扩展分析）
│       │   ├── pilot/           Agent 探针管理面 + 探针面
│       │   ├── profile/         用户画像
│       │   ├── sdk/             /sdk/** 探针与 JS SDK 下发
│       │   ├── stream/          实时分钟聚合
│       │   ├── system/          系统管理（配置/字典/公告/监控/任务/日志/账户）
│       │   └── tenant/          租户 / 用户 / 角色 / 鉴权
│       └── resources/
│           ├── sdk/pilot-agent.sh   探针运行时
│           ├── sdk/ubax.js          浏览器 SDK
│           ├── application.yml      生产（MySQL）
│           └── application-local.yml 本地（H2）
├── frontend/                    Vue 3 前端
│   └── src/
│       ├── views/               40+ 页面（含 ext/ 数据应用、system/ 系统管理）
│       ├── api/                 axios 封装 + 各模块 API
│       ├── router/              路由与权限守卫
│       ├── store/               Pinia（auth）
│       └── layouts/             主布局
├── deploy/                      nginx / systemd / ssh 部署脚本
├── .deploy/                     打包产物（backend.tar.gz / dist.tar.gz，可能过期）
└── docs/                        产品文档
```

## 生产部署

生产使用 MySQL + nginx 反向代理 + systemd 托管。**端口约束：禁止使用 80/443**，nginx 监听 8888，
后端监听 8081。完整步骤见 [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)。

> 当前 `.deploy/` 下的 `backend.tar.gz` 与 `dist.tar.gz` 为历史打包产物，可能已过期；
> 上线前请重新执行 `npm run build` 与 `mvn clean package` 并重打包。

## 文档索引

- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) —— 分层架构、采集管线、多租户与 RBAC、探针闭环、数据模型
- [docs/API.md](docs/API.md) —— 全部 REST 端点、鉴权、请求/响应约定、分页规范
- [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) —— 本地运行、生产部署、探针安装、运维

## 许可

本项目为内部产品代码，未附开源许可证。如需对外分发请先明确授权。
