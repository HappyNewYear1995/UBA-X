# UBA-X

**数智化用户行为洞察与分析系统**

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.9-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.x-green.svg)](https://vuejs.org/)

## 项目简介

UBA-X（User Behavior Analytics）是一个企业级的用户行为分析平台，提供从数据采集、传输、存储到分析洞察的全链路解决方案。系统采用微服务架构设计，支持多租户管理，可灵活部署于各种规模的业务场景。

## 核心特性

- **全链路数据采集**：支持 Web、H5、App、小程序等多端数据采集
- **实时数据处理**：基于 Flink 的实时数据流处理引擎
- **智能分析洞察**：提供页面访问、事件追踪、用户画像等多维度分析
- **多租户架构**：完善的租户隔离与权限管理体系
- **灵活存储方案**：支持多种数据存储策略，满足不同规模需求
- **Agent 探针管理**：通过 ubax-pilot 实现分布式数据采集探针的远程控制与配置管理
- **可视化报表**：丰富的数据可视化组件，直观展示分析结果

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.5.9 | 核心框架 |
| MyBatis Plus | - | ORM 框架 |
| Liquibase | - | 数据库版本管理 |
| Redis | - | 缓存中间件 |
| Flink | - | 实时计算引擎 |
| MapStruct | 1.6.3 | 对象转换 |
| Lombok | 1.18.42 | 代码简化 |
| Hutool | 5.8.25 | 工具类库 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Vite | - | 构建工具 |
| Element Plus | - | UI 组件库 |
| TypeScript | - | 类型系统 |
| Pinia | - | 状态管理 |
| Vue Router | - | 路由管理 |

## 项目结构

```
uba-x/
├── ubax-dependencies/              # 依赖管理 BOM
├── ubax-framework/                 # 框架核心
│   ├── ubax-common/                # 通用工具类
│   ├── ubax-spring-boot-starter-web/           # Web 组件
│   ├── ubax-spring-boot-starter-security/      # 安全组件
│   ├── ubax-spring-boot-starter-mybatis/       # MyBatis 组件
│   ├── ubax-spring-boot-starter-redis/         # Redis 组件
│   ├── ubax-spring-boot-starter-mq/            # 消息队列组件
│   ├── ubax-spring-boot-starter-job/           # 定时任务组件
│   ├── ubax-spring-boot-starter-excel/         # Excel 组件
│   ├── ubax-spring-boot-starter-monitor/       # 监控组件
│   ├── ubax-spring-boot-starter-protection/    # 服务保障组件
│   ├── ubax-spring-boot-starter-websocket/     # WebSocket 组件
│   ├── ubax-spring-boot-starter-biz-tenant/    # 多租户组件
│   ├── ubax-spring-boot-starter-biz-ip/        # IP 组件
│   └── ubax-spring-boot-starter-biz-data-permission/  # 数据权限组件
├── ubax-server/                    # 主启动模块
├── ubax-module-system/             # 系统管理模块
├── ubax-module-infra/              # 基础设施模块
├── ubax-module-collect/            # 数据采集模块
├── ubax-module-storage/            # 数据存储模块
├── ubax-module-analysis/           # 数据分析模块
├── ubax-module-flink/              # Flink 实时计算模块
├── ubax-module-pilot/              # Agent 探针管理模块
└── ubax-ui/                        # 前端项目
```

## 模块说明

### ubax-framework

框架核心层，提供项目所需的各种 Starter 组件：

- **ubax-common**: 通用工具类、常量、枚举、异常处理等
- **ubax-spring-boot-starter-web**: Web 相关配置、全局异常处理、统一返回格式
- **ubax-spring-boot-starter-security**: 安全认证、权限控制、JWT 支持
- **ubax-spring-boot-starter-mybatis**: MyBatis Plus 集成、自动填充、分页插件
- **ubax-spring-boot-starter-redis**: Redis 缓存、分布式锁
- **ubax-spring-boot-starter-mq**: 消息队列集成
- **ubax-spring-boot-starter-job**: 定时任务（Quartz）
- **ubax-spring-boot-starter-biz-tenant**: 多租户支持、租户隔离
- **ubax-spring-boot-starter-excel**: Excel 导入导出
- **ubax-spring-boot-starter-monitor**: 监控与链路追踪
- **ubax-spring-boot-starter-protection**: 限流、防重、幂等
- **ubax-spring-boot-starter-websocket**: WebSocket 支持
- **ubax-spring-boot-starter-biz-ip**: IP 地址解析
- **ubax-spring-boot-starter-biz-data-permission**: 数据权限控制

### ubax-module-system

系统管理模块，提供基础的系统管理功能：

- 用户管理、角色管理、权限管理
- 菜单管理、部门管理、岗位管理
- 字典管理、参数配置
- 登录日志、操作日志
- 租户管理、租户套餐
- OAuth2 认证、第三方登录
- 短信服务、邮件服务、通知服务

### ubax-module-infra

基础设施模块，提供系统基础设施支持：

- 文件管理、文件存储配置
- 代码生成器
- 定时任务管理
- API 访问日志、错误日志
- Redis 监控

### ubax-module-collect

数据采集模块，负责用户行为数据的采集与接入。

### ubax-module-storage

数据存储模块，提供灵活的数据存储策略。

### ubax-module-analysis

数据分析模块，提供用户行为分析功能：

- 页面访问分析
- 事件追踪分析
- 用户画像分析
- 转化漏斗分析

### ubax-module-flink

Flink 实时计算模块，基于 Apache Flink 实现：

- 实时数据流处理
- 实时指标计算
- 实时告警

### ubax-module-pilot

Agent 探针管理模块，用于与 ubax-pilot 探针通讯和控制交互：

- Agent 注册与心跳管理
- SSE 长连接推送配置与命令
- Agent 在线状态监控
- 远程命令下发
- Agent 数据持久化

**接口说明**：

| 接口路径 | 方法 | 说明 | 类型 |
|---------|------|------|------|
| `/pilot/agent/config` | GET | 探针拉取配置 | App |
| `/pilot/agent/heartbeat` | POST | 心跳上报 | App |
| `/pilot/agent/push` | GET | SSE 推送流 | App |
| `/pilot/agent/page` | GET | Agent 分页列表 | Admin |
| `/pilot/agent/get` | GET | Agent 详情 | Admin |
| `/pilot/agent/push-command` | POST | 推送命令 | Admin |

### ubax-ui

前端管理后台，基于 Vue 3 + Element Plus 构建：

- 响应式布局，支持多端适配
- 丰富的组件库
- 动态路由、权限控制
- 多标签页、主题切换

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 18+（前端）
- pnpm 8+（前端）

### 后端启动

1. **克隆项目**
   ```bash
   git clone https://gitee.com/FoleyZhao/uba-x.git
   cd uba-x
   ```

2. **初始化数据库**
   ```bash
   # 创建数据库
   mysql -u root -p -e "CREATE DATABASE ubax DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   
   # 项目启动时 Liquibase 会自动执行数据库脚本
   ```

3. **修改配置**
   ```bash
   # 编辑配置文件
   vim ubax-server/src/main/resources/application-dev.yaml
   ```
   
   修改数据库连接、Redis 连接等配置。

4. **编译项目**
   ```bash
   mvn clean install -DskipTests
   ```

5. **启动服务**
   ```bash
   cd ubax-server
   mvn spring-boot:run
   ```

### 前端启动

1. **安装依赖**
   ```bash
   cd ubax-ui
   pnpm install
   ```

2. **启动开发服务器**
   ```bash
   pnpm dev
   ```

3. **访问系统**
   
   打开浏览器访问 `http://localhost:3000`

## 配置说明

### 数据库配置

在 `application-dev.yaml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/ubax?useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: your_password
```

### Redis 配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_password
```

### Liquibase 配置

```yaml
spring:
  liquibase:
    enabled: true
    change-log: classpath:db/changelog/master.xml
```

## 开发规范

### 代码规范

- 遵循阿里巴巴 Java 开发手册
- 使用 Lombok 简化代码
- 使用 MapStruct 进行对象转换
- Controller 层严禁业务逻辑，统一返回 `CommonResult<T>`
- 业务异常统一抛出 `ServiceException`

### 数据库规范

- 使用 Liquibase 管理数据库版本
- 表名使用 `模块_业务` 命名（如 `system_users`）
- 所有表必须包含 `create_time`、`update_time`、`creator`、`updater`、`deleted` 字段
- 使用 `BaseDO` 作为实体基类

### 接口规范

- Controller 分为 `admin`（管理后台）和 `app`（用户端）
- 接口路径格式 `/模块/业务`
- 使用 Swagger 注解标注接口信息
- 参数校验使用 JSR-303 注解

## 数据库版本管理

项目使用 Liquibase 进行数据库版本管理，脚本位于 `ubax-server/src/main/resources/db/changelog/`：

```
db/changelog/
├── master.xml                    # 主入口
├── schema/                       # 表结构变更（DDL）
│   ├── changelog-schema.xml
│   └── V1__init_schema.sql
└── data/                         # 数据变更（DML）
    ├── changelog-data.xml
    └── V1__init_data.sql
```

**新增变更**：
- 表结构变更：在 `schema/` 下创建 SQL 文件，并在 `changelog-schema.xml` 中引用
- 数据变更：在 `data/` 下创建 SQL 文件，并在 `changelog-data.xml` 中引用

## 部署

### Docker 部署

（待补充）

### 生产环境配置

（待补充）

## 常见问题

### Q: 启动时数据库初始化失败？

A: 确保 MySQL 版本为 8.0+，并创建了 `ubax` 数据库。Liquibase 会在启动时自动执行建表脚本。

### Q: 如何添加新的业务模块？

A: 
1. 在根 `pom.xml` 中添加新模块
2. 创建模块目录结构，参考 `ubax-module-system`
3. 在 `ubax-server` 中引入新模块依赖

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

## 许可证

本项目采用 Apache 2.0 许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目地址：https://gitee.com/FoleyZhao/uba-x
- 问题反馈：提交 Issue

## 致谢

感谢以下开源项目：

- [Spring Boot](https://spring.io/projects/spring-boot)
- [MyBatis Plus](https://baomidou.com/)
- [Vue.js](https://vuejs.org/)
- [Element Plus](https://element-plus.org/)
- [Liquibase](https://www.liquibase.org/)
