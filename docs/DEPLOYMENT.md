# UBA-X 部署与运维指南

本文覆盖三种运行形态：本地开发（H2 内存库）、生产部署（MySQL + nginx + systemd）、
以及 Agent 探针安装。

> **端口约束（强制）**：生产环境 **禁止使用 80 / 443 端口**。约定 nginx 监听 **8888**，
> 后端 Spring Boot 监听 **8081**。
>
> **服务器状态**：原测试服务器 `120.55.38.71` 已于 2026-10-16 下架，等待新服务器。
> `deploy/ssh_run.py` 中的主机/口令为历史值，上新服务器后需替换。

---

## 一、本地开发（H2 内存库）

无需安装 MySQL，适合功能验证。

### 启动后端

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

或打包运行：

```bash
mvn clean package -DskipTests
java -jar target/ubax-server.jar --spring.profiles.active=local
```

- 后端：`http://localhost:8081`，健康检查 `/health`
- H2 控制台：`http://localhost:8081/h2-console`
  （JDBC URL `jdbc:h2:mem:ubax`，用户 `sa`，空密码）

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

- 前端：`http://localhost:5173`，`/api` 已代理到 `8081`

### 关键注意事项

- **H2 profile 为 `ddl-auto: create-drop`**：每次重启后端都会重建表并由 `DataSeeder` 重新播种，
  **数据全部清空、自增 ID 重置**。仅用于开发验证，勿当持久环境。
- 重建后端前若 jar 被占用（Windows 文件锁），先停掉正在运行的后端进程：
  ```bash
  netstat -ano | grep :8081 | grep LISTENING   # 找 PID
  taskkill //F //PID <pid>
  ```
- 租户级页面（Agent 探针、应用接入等）需用 **租户用户**（`demo`）登录；超管 `admin` 无租户上下文会 401。

---

## 二、生产部署（MySQL + nginx + systemd）

### 1. 目录约定

```
/opt/ubax/
├── app/ubax-server.jar      后端可执行 jar
├── web/dist/                前端构建产物
└── logs/                    运行日志（ubax.log / ubax-error.log）
```

### 2. 准备 MySQL

创建库与账号（与 `application.yml` 默认值对应，可用环境变量覆盖）：

```sql
CREATE DATABASE ubax DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE USER 'ubax'@'127.0.0.1' IDENTIFIED BY 'ubax@2026';
GRANT ALL PRIVILEGES ON ubax.* TO 'ubax'@'127.0.0.1';
FLUSH PRIVILEGES;
```

生产 profile 使用 `ddl-auto: update`（首次启动自动建表；`DataSeeder` 仅在空库时播种）。

### 3. 构建产物

```bash
# 前端
cd frontend && npm ci && npm run build      # -> frontend/dist

# 后端
cd backend && mvn clean package -DskipTests # -> backend/target/ubax-server.jar
```

> `.deploy/backend.tar.gz` 与 `.deploy/dist.tar.gz` 为历史打包产物，**可能已过期**；
> 上线前务必重新构建并打包，不要直接复用旧包。

### 4. 上传并安装 systemd 服务

参考 `deploy/ubax.service`，复制到 `/etc/systemd/system/ubax.service`。关键环境变量：

| 变量 | 默认 | 说明 |
| --- | --- | --- |
| `MYSQL_HOST` | 127.0.0.1 | 数据库主机 |
| `MYSQL_PORT` | 3306 | 数据库端口 |
| `MYSQL_DB` | ubax | 库名 |
| `MYSQL_USER` | ubax | 用户 |
| `MYSQL_PASSWORD` | ubax@2026 | 口令（**上线前修改**） |
| `JWT_SECRET` | （示例值） | JWT 签名密钥（**上线前必须改成强随机值**） |
| `TZ` | Asia/Shanghai | 时区 |

JVM 参数（低配服务器友好）：`-Xms256m -Xmx640m -XX:MaxMetaspaceSize=256m -XX:+UseSerialGC`。

启用并启动：

```bash
systemctl daemon-reload
systemctl enable --now ubax
systemctl status ubax
curl -s http://127.0.0.1:8081/health
```

### 5. 配置 nginx

参考 `deploy/nginx-ubax.conf`，监听 **8888**（非 80/443）：

- `root /opt/ubax/web/dist`，SPA fallback `try_files $uri $uri/ /index.html`
- `/api/` 反向代理到 `http://127.0.0.1:8081`，透传 `X-Real-IP` / `X-Forwarded-For` / `X-Forwarded-Proto`
- `/health` 透传后端健康检查
- `/assets/` 命中失败直接 404（不回退 index.html），缓存 7 天
- 开启 gzip，`client_max_body_size 8m`

```bash
nginx -t && systemctl reload nginx
```

访问 `http://<host>:8888`。

> 注意：`/sdk/**` 与 `/demo/**` 由后端提供。nginx 的 `location /` 会把未命中静态文件的请求
> fallback 到 `index.html`，若需让 `/sdk/pilot-agent.sh` 直达后端，请为其单独加一条
> `location /sdk/ { proxy_pass http://127.0.0.1:8081; }`（本地开发走 Vite 代理时无此问题）。

### 6. 远程运维脚本

`deploy/ssh_run.py`（依赖 `paramiko`）封装了 SSH 执行与 SFTP 上传：

```bash
python deploy/ssh_run.py run "systemctl restart ubax"
python deploy/ssh_run.py upload .deploy/backend.tar.gz /opt/ubax/backend.tar.gz
```

> 上新服务器后先修改脚本顶部的 `HOST` / `USER` / `PW`。
> Git Bash 传 `/root` 等绝对路径时加 `MSYS_NO_PATHCONV=1`，避免路径被改写。

---

## 三、Agent 探针安装

探针是 Bash 外壳内嵌纯标准库 Python 3 运行时，目标主机只需有 `bash` 与 `python3`（≥3.6），
无需 curl/jq 参与探针逻辑。

### 1. 注册探针

在「Agent 探针」页点击「注册探针」，填写名称/主机/标签，生成后弹窗给出安装命令与 AgentKey。
也可点击「下载探针」把 `pilot-agent.sh` 保存到本地再分发。

### 2. 一键安装（curl | bash）

```bash
curl -fsSL http://<host>:8081/sdk/pilot-agent.sh | bash -s -- \
  --key <AGENT_KEY> \
  --server <host>:8081 \
  --name web-collector-03 \
  --tags 生产,华东
```

参数：

| 参数 | 说明 |
| --- | --- |
| `--key` | 注册时生成的 AgentKey（必填） |
| `--server` | 后端地址 `host:port`（必填） |
| `--name` | 探针名（可选，覆盖注册名） |
| `--tags` | 标签（可选） |
| `--once` | 单次模式：注册→心跳→采集→再心跳后退出（适合定时任务/联调） |
| `--heartbeat-interval` | 心跳间隔毫秒（默认 15000） |
| `--collect-interval` | 采集间隔毫秒（默认 5000） |
| `--version` | 打印探针版本后退出 |

### 3. 运行循环

注册 → 心跳（上报主机指标：CPU 核数、负载、内存、磁盘、运行时长）→ 拉取并执行指令 →
采集事件（`$agent.telemetry`）上报到 `/api/collect/batch`。

- 后端每 30 秒巡检，探针静默 90 秒自动置为「离线」。
- 支持指令：`RELOAD_CONFIG` / `START` / `STOP` / `FLUSH` / `RESTART` / `UPGRADE`。
- 新装探针自动注入租户默认 appKey，开箱即采集。

### 4. 后台常驻（示例）

```bash
nohup bash -c 'curl -fsSL http://<host>:8081/sdk/pilot-agent.sh | bash -s -- \
  --key <AGENT_KEY> --server <host>:8081 --name prod-01' \
  >/var/log/ubax-pilot.log 2>&1 &
```

生产建议封装为 systemd unit 以便开机自启与崩溃重拉。

### 平台兼容性

探针指标读取依赖 Linux `/proc`（`/proc/loadavg`、`/proc/meminfo`、`/proc/uptime`）。
在 Windows/macOS 上这些文件不存在，负载/内存会优雅降级为空（前端显示「—」），
其余功能（注册、心跳、指令、采集）正常。部署到真实 Linux 主机后指标自动填充。

---

## 四、上线检查清单

- [ ] 重新构建前端 `dist` 与后端 `jar`（勿用 `.deploy/` 旧包）
- [ ] 修改 `MYSQL_PASSWORD` 为强口令
- [ ] 修改 `JWT_SECRET` 为强随机值
- [ ] nginx 监听 8888，**未占用 80/443**
- [ ] `/sdk/` 能直达后端（探针下载与安装命令可用）
- [ ] `curl http://<host>:8081/health` 返回 UP
- [ ] 用 `demo` 登录验证 Agent 探针页可访问（非超管）
- [ ] 注册一个探针并跑通心跳/指令闭环
