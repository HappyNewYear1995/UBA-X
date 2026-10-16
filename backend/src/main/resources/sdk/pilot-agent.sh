#!/usr/bin/env bash
# ============================================================================
#  ubax-pilot · Agent 探针安装/启动器
#  Distributed collection probe for the UBA-X behavior analytics platform.
#
#  安装并前台运行:
#    curl -fsSL http://SERVER:8081/sdk/pilot-agent.sh | bash -s -- \
#         --key <agentKey> --server http://SERVER:8081 --name <name>
#
#  常用参数:
#    --key        (必填) 探针密钥, 由控制台“注册探针”生成 (pilot_xxx)
#    --server     (必填) 服务端地址, 如 http://127.0.0.1:8081
#    --name       探针名称, 默认取主机名
#    --tags       标签, 如 "生产,华东"
#    --once       只执行一轮 注册→心跳→采集 后退出 (自检/CI 用)
#    --heartbeat-interval <秒>   覆盖心跳间隔
#    --collect-interval  <秒>    覆盖采集间隔
#    --version    打印探针版本
#
#  依赖: bash + python3 (>=3.6, 仅用标准库)。Linux/macOS/Git-Bash 均可运行。
#  后台常驻:  nohup bash pilot-agent.sh --key .. --server .. >/var/log/ubax-pilot.log 2>&1 &
#  建议用 systemd 托管 (Restart=always)。
# ============================================================================
set -u

PILOT_VERSION="1.1.0"

# --version 短路 (无需 python)
for a in "$@"; do
  if [ "$a" = "--version" ]; then echo "ubax-pilot agent $PILOT_VERSION"; exit 0; fi
done

PY=""
for c in python3 python python3.12 python3.11 python3.10 python3.9 python3.8 python3.7; do
  if command -v "$c" >/dev/null 2>&1 && "$c" -c 'import sys; sys.exit(0 if sys.version_info >= (3,6) else 1)' >/dev/null 2>&1; then
    PY="$c"; break
  fi
done
if [ -z "$PY" ]; then
  echo "[ubax-pilot] ERROR: 未找到可用的 python3 (>=3.6, 仅标准库)。" >&2
  echo "[ubax-pilot]        请安装 Python 3 后重试。" >&2
  exit 3
fi

PYFILE="$(mktemp "${TMPDIR:-/tmp}/ubax-pilot-agent.XXXXXX")"
if [ -z "$PYFILE" ] || [ ! -w "$PYFILE" ]; then
  echo "[ubax-pilot] ERROR: 无法创建临时文件。" >&2
  exit 4
fi

cat > "$PYFILE" <<'PYEOF'
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""ubax-pilot Agent 探针 (embedded). stdlib-only: urllib/json/os/platform/socket."""
import sys, os, json, time, platform, socket, signal, random, argparse, shutil
from urllib import request as urlreq
from urllib import error as urlerr

VERSION = "1.1.0"

def log(msg):
    sys.stderr.write("[%s] [ubax-pilot] %s\n" % (time.strftime("%Y-%m-%d %H:%M:%S"), msg))
    sys.stderr.flush()

def http_post(url, obj, timeout=20):
    """POST JSON, return (http_code, parsed_envelope_dict)."""
    data = json.dumps(obj).encode("utf-8")
    req = urlreq.Request(url, data=data, method="POST",
                         headers={"Content-Type": "application/json",
                                  "User-Agent": "ubax-pilot/" + VERSION})
    try:
        with urlreq.urlopen(req, timeout=timeout) as r:
            raw = r.read().decode("utf-8", "replace")
            try:
                return r.getcode(), json.loads(raw)
            except Exception:
                return r.getcode(), {"code": -1, "message": raw[:200]}
    except urlerr.HTTPError as e:
        raw = e.read().decode("utf-8", "replace") if e.fp else ""
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, {"code": e.code, "message": raw[:200]}
    except Exception as e:
        return 0, {"code": -1, "message": str(e)}

def host_ip():
    s = None
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        return s.getsockname()[0]
    except Exception:
        try:
            return socket.gethostbyname(socket.gethostname())
        except Exception:
            return ""
    finally:
        if s:
            try: s.close()
            except Exception: pass

def read_metrics():
    m = {}
    try:
        with open("/proc/loadavg") as f:
            parts = f.read().split()
            m["load1"] = float(parts[0]); m["load5"] = float(parts[1])
    except Exception:
        try:
            la = os.getloadavg(); m["load1"] = round(la[0], 2); m["load5"] = round(la[1], 2)
        except Exception:
            pass
    try:
        m["cpuCores"] = os.cpu_count()
    except Exception:
        pass
    try:
        info = {}
        with open("/proc/meminfo") as f:
            for line in f:
                k, v = line.split(":", 1)
                info[k.strip()] = int(v.strip().split()[0])  # kB
        total = info.get("MemTotal", 0)
        avail = info.get("MemAvailable", info.get("MemFree", 0))
        if total:
            m["memTotalMb"] = total // 1024
            m["memUsedMb"] = (total - avail) // 1024
            m["memPct"] = round((total - avail) * 100.0 / total, 1)
    except Exception:
        pass
    try:
        with open("/proc/uptime") as f:
            m["uptimeSec"] = int(float(f.read().split()[0]))
    except Exception:
        pass
    try:
        du = shutil.disk_usage("/")
        m["diskTotalGb"] = du.total // (1024 ** 3)
        m["diskUsePct"] = round(du.used * 100.0 / du.total, 1)
    except Exception:
        pass
    return m

DEFAULT_CFG = {
    "enabled": True,
    "collectIntervalMs": 5000,
    "heartbeatIntervalMs": 15000,
    "batchSize": 50,
    "sampling": 1.0,
    "appKey": None,
    "endpoints": ["/api/collect/batch"],
}

class Agent:
    def __init__(self, args):
        self.key = args.key
        self.server = args.server.rstrip("/")
        self.name = args.name or socket.gethostname()
        self.tags = args.tags
        self.once = args.once
        self.agent_id = None
        self.cfg = dict(DEFAULT_CFG)
        self.cfg_version = 0
        self.events_sent = 0
        self.running = True
        self.collect_enabled = True
        self.registered = False
        self.distinct_id = "agent-" + self.name
        self.session_id = "%s-%d" % (self.name, int(time.time()))
        self.started_at = int(time.time())
        self._appkey_warned = False
        if args.heartbeat_interval:
            self.cfg["heartbeatIntervalMs"] = int(args.heartbeat_interval * 1000)
        if args.collect_interval:
            self.cfg["collectIntervalMs"] = int(args.collect_interval * 1000)
        # 覆盖服务端下发的间隔 (命令行优先)
        self._cli_hb = bool(args.heartbeat_interval)
        self._cli_collect = bool(args.collect_interval)

    # ---- config ----
    def apply_config(self, cfg, version=None):
        if not isinstance(cfg, dict):
            return
        merged = dict(self.cfg)
        merged.update(cfg)
        # 命令行显式指定的间隔不被远端覆盖
        if self._cli_hb:
            merged["heartbeatIntervalMs"] = self.cfg["heartbeatIntervalMs"]
        if self._cli_collect:
            merged["collectIntervalMs"] = self.cfg["collectIntervalMs"]
        changed = merged != self.cfg
        self.cfg = merged
        if version is not None and version != self.cfg_version:
            self.cfg_version = version
            changed = True
        if "enabled" in cfg:
            self.collect_enabled = bool(cfg.get("enabled"))
        if changed:
            log("配置已应用 v%s: enabled=%s collect=%dms hb=%dms appKey=%s sampling=%s" % (
                self.cfg_version, self.collect_enabled, int(self.cfg.get("collectIntervalMs", 5000)),
                int(self.cfg.get("heartbeatIntervalMs", 15000)), self.cfg.get("appKey"), self.cfg.get("sampling")))

    # ---- agent plane ----
    def register(self):
        body = {"agentKey": self.key, "host": socket.gethostname(), "ip": host_ip(),
                "version": VERSION, "os": platform.system().lower(), "arch": platform.machine(),
                "name": self.name}
        if self.tags:
            body["tags"] = self.tags
        code, env = http_post(self.server + "/api/pilot/agents/register", body)
        if env.get("code") != 0:
            log("注册失败 (http=%s): %s" % (code, env.get("message")))
            return False
        d = env.get("data") or {}
        self.agent_id = d.get("agentId")
        self.apply_config(d.get("config") or {}, d.get("configVersion"))
        self.registered = True
        log("注册成功: id=%s name=%s server=%s" % (self.agent_id, d.get("name") or self.name, self.server))
        return True

    def heartbeat(self):
        metrics = read_metrics()
        metrics.update({"agentVersion": VERSION, "eventsSent": self.events_sent,
                        "uptimeAgentSec": int(time.time()) - self.started_at,
                        "collectEnabled": self.collect_enabled})
        body = {"agentKey": self.key, "configVersion": self.cfg_version,
                "eventsSent": self.events_sent, "metrics": metrics}
        code, env = http_post(self.server + "/api/pilot/agents/heartbeat", body)
        if env.get("code") != 0:
            log("心跳失败 (http=%s): %s" % (code, env.get("message")))
            return
        d = env.get("data") or {}
        self.apply_config(d.get("config") or {}, d.get("configVersion"))
        for c in (d.get("commands") or []):
            self.handle_command(c)

    def report_result(self, cmd_id, status, result):
        http_post(self.server + "/api/pilot/agents/result",
                  {"agentKey": self.key, "commandId": cmd_id, "status": status, "result": result})

    def handle_command(self, cmd):
        cid = cmd.get("id"); ctype = (cmd.get("type") or "").upper()
        log("收到指令 #%s: %s" % (cid, ctype))
        try:
            if ctype == "RELOAD_CONFIG":
                self.report_result(cid, "DONE", "config v%s reloaded" % self.cfg_version)
            elif ctype == "START":
                self.collect_enabled = True
                self.report_result(cid, "DONE", "collection started")
            elif ctype == "STOP":
                self.collect_enabled = False
                self.report_result(cid, "DONE", "collection stopped")
            elif ctype == "FLUSH":
                n = self.collect(force=True)
                self.report_result(cid, "DONE", "flushed %d events" % n)
            elif ctype == "RESTART":
                self.report_result(cid, "DONE", "restarting")
                self.restart()
            elif ctype == "UPGRADE":
                ok = self.upgrade(cmd.get("payload"))
                self.report_result(cid, "DONE" if ok else "FAILED",
                                   "upgrade ok" if ok else "upgrade failed")
                if ok:
                    self.restart()
            else:
                self.report_result(cid, "FAILED", "unsupported command: %s" % ctype)
        except Exception as e:
            try: self.report_result(cid, "FAILED", str(e)[:200])
            except Exception: pass

    # ---- collection ----
    def collect(self, force=False):
        app_key = self.cfg.get("appKey")
        if not app_key:
            if not self._appkey_warned:
                log("未配置 appKey, 跳过事件采集 (请在控制台为探针配置 appKey)。")
                self._appkey_warned = True
            return 0
        if not force and (not self.collect_enabled or not self.cfg.get("enabled", True)):
            return 0
        sampling = float(self.cfg.get("sampling", 1.0) or 1.0)
        batch_max = max(1, int(self.cfg.get("batchSize", 50)))
        n = 1 if force else random.randint(1, min(3, batch_max))
        metrics = read_metrics()
        events = []
        for _ in range(n):
            if random.random() > sampling:
                continue
            events.append({
                "type": "custom",
                "event": "$agent.telemetry",
                "distinctId": self.distinct_id,
                "sessionId": self.session_id,
                "ts": int(time.time() * 1000),
                "properties": dict(metrics, agent=self.name, agentId=self.agent_id),
            })
        if not events:
            return 0
        code, env = http_post(self.server + "/api/collect/batch",
                              {"appKey": app_key, "events": events})
        accepted = 0
        if env.get("code") == 0:
            accepted = int((env.get("data") or {}).get("accepted", 0))
            self.events_sent += accepted
        else:
            log("采集上报失败 (http=%s): %s" % (code, env.get("message")))
        return accepted

    # ---- lifecycle ----
    def restart(self):
        log("重启探针 ...")
        try:
            os.execv(sys.executable, [sys.executable, os.path.abspath(sys.argv[0])] + sys.argv[1:])
        except Exception as e:
            log("重启失败: %s" % e)

    def upgrade(self, payload):
        url = self.server + "/sdk/pilot-agent.sh"
        log("升级: 从 %s 拉取最新探针" % url)
        tmp = None
        try:
            req = urlreq.Request(url, headers={"User-Agent": "ubax-pilot/" + VERSION})
            with urlreq.urlopen(req, timeout=30) as r:
                content = r.read()
            tmp = os.path.join(os.environ.get("TMPDIR", "/tmp"), "ubax-pilot-agent.upgrade.%d" % os.getpid())
            with open(tmp, "wb") as f:
                f.write(content)
            log("升级脚本已下载 (%d bytes): %s" % (len(content), tmp))
            # 用新脚本替换当前进程 (通过 bash 重新执行)
            self._upgrade_path = tmp
            return True
        except Exception as e:
            log("升级失败: %s" % e)
            if tmp:
                try: os.remove(tmp)
                except Exception: pass
            return False

    def _exec_upgraded(self):
        path = getattr(self, "_upgrade_path", None)
        if not path:
            return
        bash = shutil.which("bash") or "/bin/bash"
        log("执行升级后的探针 ...")
        os.execv(bash, [bash, path] + sys.argv[1:])

    def stop(self, signum=None, frame=None):
        if not self.running:
            return
        self.running = False
        log("收到退出信号, 正在刷新并停止 ...")

    def run(self):
        signal.signal(signal.SIGINT, self.stop)
        try:
            signal.signal(signal.SIGTERM, self.stop)
        except Exception:
            pass
        log("启动 ubax-pilot agent %s (python %s)" % (VERSION, platform.python_version()))
        # 注册 (带重试)
        tries = 0
        while self.running and not self.registered:
            if self.register():
                break
            tries += 1
            if self.once or tries >= 5:
                if not self.registered:
                    log("注册未成功, 退出。")
                    return 2 if self.once else 1
            time.sleep(min(30, 3 * tries))

        if self.once:
            self.heartbeat()
            sent = self.collect(force=True)
            # 再心跳一次, 把本次采集的 eventsSent 同步到服务端
            self.heartbeat()
            log("--once 自检完成: 已注册, 心跳正常, 本次采集 %d 条, 累计上报 %d 条。" % (sent, self.events_sent))
            return 0

        last_hb = 0.0
        last_collect = 0.0
        while self.running:
            now = time.time() * 1000
            hb_ms = max(3000, int(self.cfg.get("heartbeatIntervalMs", 15000)))
            col_ms = max(1000, int(self.cfg.get("collectIntervalMs", 5000)))
            if now - last_hb >= hb_ms:
                self.heartbeat()
                last_hb = now
                if getattr(self, "_upgrade_path", None):
                    self._exec_upgraded()
            if now - last_collect >= col_ms:
                self.collect()
                last_collect = now
            # 1s tick, 便于及时响应退出信号
            for _ in range(10):
                if not self.running:
                    break
                time.sleep(0.1)
        # 退出前刷新
        try:
            self.collect(force=True)
        except Exception:
            pass
        log("探针已停止。累计上报 %d 条事件。" % self.events_sent)
        return 0

def main():
    p = argparse.ArgumentParser(add_help=True, description="ubax-pilot agent probe")
    p.add_argument("--key", required=True, help="agent key (pilot_xxx)")
    p.add_argument("--server", required=True, help="server base url, e.g. http://127.0.0.1:8081")
    p.add_argument("--name", default=None, help="agent name (default: hostname)")
    p.add_argument("--tags", default=None, help="comma separated tags")
    p.add_argument("--once", action="store_true", help="register+heartbeat+collect once then exit")
    p.add_argument("--heartbeat-interval", type=float, default=None, help="seconds")
    p.add_argument("--collect-interval", type=float, default=None, help="seconds")
    p.add_argument("--version", action="store_true")
    a = p.parse_args()
    if a.version:
        print("ubax-pilot agent %s" % VERSION); return 0
    if not a.server.startswith("http"):
        a.server = "http://" + a.server
    return Agent(a).run()

if __name__ == "__main__":
    try:
        sys.exit(main())
    except KeyboardInterrupt:
        sys.exit(0)
PYEOF

"$PY" "$PYFILE" "$@"
rc=$?
rm -f "$PYFILE" 2>/dev/null || true
exit $rc
