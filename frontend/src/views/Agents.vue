<template>
  <div class="page">
    <h2 class="page-title">ubax-pilot · Agent 探针管理</h2>
    <p class="page-sub">分布式采集探针的注册、心跳、远程配置下发与指令控制</p>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">探针总数</div><div class="value">{{ ov.total || 0 }}</div></div>
      <div class="stat-card"><div class="label">在线</div><div class="value" style="color:#00b578">{{ ov.online || 0 }}</div></div>
      <div class="stat-card"><div class="label">离线</div><div class="value" style="color:#8a9099">{{ ov.offline || 0 }}</div></div>
      <div class="stat-card"><div class="label">待安装</div><div class="value" style="color:#ff9f43">{{ ov.installing || 0 }}</div></div>
    </div>

    <div class="toolbar" style="margin-top:16px">
      <el-button type="primary" :icon="Plus" @click="createVisible = true">注册探针</el-button>
      <el-button :icon="Download" @click="downloadProbe">下载探针</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-switch v-model="autoRefresh" active-text="自动刷新" />
    </div>

    <div class="card">
      <el-table :data="agents" size="small" stripe>
        <el-table-column prop="name" label="名称" min-width="150" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="statusType(row.status)" effect="dark">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="host" label="主机/IP" min-width="130" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="tags" label="标签" width="130" show-overflow-tooltip />
        <el-table-column label="配置版本" width="90">
          <template #default="{ row }">v{{ row.configVersion }}</template>
        </el-table-column>
        <el-table-column prop="eventsSent" label="已上报事件" width="120" />
        <el-table-column label="最近心跳" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.lastHeartbeat) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="{ row }">
            <el-button link type="success" size="small" @click="openDetail(row)">详情</el-button>
            <el-button link type="primary" size="small" @click="openConfig(row)">配置</el-button>
            <el-dropdown @command="(c) => sendCmd(row, c)" trigger="click">
              <el-button link type="warning" size="small">指令<el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="RELOAD_CONFIG">重载配置</el-dropdown-item>
                  <el-dropdown-item command="FLUSH">立即上报</el-dropdown-item>
                  <el-dropdown-item command="RESTART">重启探针</el-dropdown-item>
                  <el-dropdown-item command="STOP">停止采集</el-dropdown-item>
                  <el-dropdown-item command="START">启动采集</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <el-button link size="small" @click="showCommands(row)">指令记录</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="createVisible" title="注册新探针" width="460px">
      <el-form :model="createForm" label-width="80px">
        <el-form-item label="名称"><el-input v-model="createForm.name" placeholder="如 web-collector-03" /></el-form-item>
        <el-form-item label="主机"><el-input v-model="createForm.host" placeholder="IP 或主机名" /></el-form-item>
        <el-form-item label="标签"><el-input v-model="createForm.tags" placeholder="如 生产,华东" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="create">生成</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="installVisible" title="探针安装信息" width="720px">
      <el-alert type="success" :closable="false" show-icon>探针已创建，请在目标主机执行以下命令完成安装：</el-alert>
      <pre class="snippet mono">{{ installCommand }}</pre>
      <p class="muted">AgentKey：<span class="mono">{{ createdKey }}</span></p>
      <template #footer>
        <el-button :icon="Download" @click="downloadProbe">下载探针脚本</el-button>
        <el-button type="primary" :icon="CopyDocument" @click="copy(installCommand)">复制命令</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="configVisible" title="远程配置下发" width="560px">
      <p class="muted">编辑 JSON 配置，保存后配置版本 +1 并向探针下发 <code>RELOAD_CONFIG</code> 指令：</p>
      <el-input v-model="configText" type="textarea" :rows="12" class="mono" />
      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" @click="saveConfig">下发配置</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="cmdVisible" title="指令记录" size="46%">
      <el-table :data="commands" size="small" stripe>
        <el-table-column prop="type" label="指令" width="140" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }"><el-tag size="small" :type="cmdStatusType(row.status)">{{ row.status }}</el-tag></template>
        </el-table-column>
        <el-table-column label="创建时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="下发时间">
          <template #default="{ row }">{{ fmtDateTime(row.deliveredAt) }}</template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <el-drawer v-model="detailVisible" title="探针详情" size="46%">
      <template v-if="detail">
        <div class="d-head">
          <span class="d-name">{{ detail.name }}</span>
          <el-tag size="small" :type="statusType(detail.status)" effect="dark">{{ statusLabel(detail.status) }}</el-tag>
        </div>
        <el-descriptions :column="1" border size="small" style="margin-top:12px">
          <el-descriptions-item label="主机">{{ detail.host || '—' }}</el-descriptions-item>
          <el-descriptions-item label="IP">{{ detail.ip || '—' }}</el-descriptions-item>
          <el-descriptions-item label="系统">{{ detail.os || '—' }} / {{ detail.arch || '—' }}</el-descriptions-item>
          <el-descriptions-item label="探针版本">{{ detail.version || '—' }}</el-descriptions-item>
          <el-descriptions-item label="标签">{{ detail.tags || '—' }}</el-descriptions-item>
          <el-descriptions-item label="配置版本">v{{ detail.configVersion }}</el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ fmtDateTime(detail.registeredAt) }}</el-descriptions-item>
          <el-descriptions-item label="最近心跳">{{ fmtDateTime(detail.lastHeartbeat) }}</el-descriptions-item>
        </el-descriptions>

        <div class="card-title" style="margin:18px 0 8px">实时指标</div>
        <div class="stat-grid">
          <div class="stat-card"><div class="label">累计上报事件</div><div class="value">{{ detail.eventsSent || 0 }}</div></div>
          <div class="stat-card"><div class="label">CPU 核心</div><div class="value">{{ m.cpuCores ?? '—' }}</div></div>
          <div class="stat-card"><div class="label">负载(1m)</div><div class="value">{{ m.load1 ?? '—' }}</div></div>
          <div class="stat-card"><div class="label">采集状态</div><div class="value small" :style="{color: m.collectEnabled ? '#00b578' : '#8a9099'}">{{ m.collectEnabled ? '运行中' : '已暂停' }}</div></div>
        </div>
        <el-descriptions :column="1" border size="small" style="margin-top:12px">
          <el-descriptions-item label="内存">
            <template v-if="m.memUsedMb != null">
              {{ m.memUsedMb }} / {{ m.memTotalMb }} MB
              <el-progress :percentage="Number(m.memPct) || 0" :stroke-width="8" style="margin-top:4px" />
            </template>
            <span v-else class="muted">—</span>
          </el-descriptions-item>
          <el-descriptions-item label="磁盘">
            <template v-if="m.diskUsePct != null">
              已用 {{ m.diskUsePct }}%（共 {{ m.diskTotalGb }} GB）
              <el-progress :percentage="Number(m.diskUsePct) || 0" :stroke-width="8" style="margin-top:4px" />
            </template>
            <span v-else class="muted">—</span>
          </el-descriptions-item>
          <el-descriptions-item label="主机运行时长">{{ uptime(m.uptimeSec) }}</el-descriptions-item>
          <el-descriptions-item label="探针运行时长">{{ uptime(m.uptimeAgentSec) }}</el-descriptions-item>
        </el-descriptions>
        <p class="muted" style="margin-top:10px;font-size:12px">指标由探针心跳上报，自动刷新间隔 8 秒。</p>
      </template>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { Plus, Refresh, ArrowDown, CopyDocument, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pilotApi } from '../api'
import { fmtDateTime } from '../utils/date'

const agents = ref([])
const ov = reactive({})
const autoRefresh = ref(true)
let timer = null

const createVisible = ref(false)
const createForm = reactive({ name: '', host: '', tags: '' })
const installVisible = ref(false)
const installCommand = ref('')
const createdKey = ref('')

const configVisible = ref(false)
const configText = ref('{}')
const configAgent = ref(null)

const cmdVisible = ref(false)
const commands = ref([])

const detailVisible = ref(false)
const detail = ref(null)
const m = computed(() => {
  if (!detail.value) return {}
  try { return JSON.parse(detail.value.metrics || '{}') } catch (e) { return {} }
})
function openDetail(row) { detail.value = row; detailVisible.value = true }
function uptime(sec) {
  sec = Number(sec) || 0
  if (sec <= 0) return '—'
  const d = Math.floor(sec / 86400), h = Math.floor((sec % 86400) / 3600), mm = Math.floor((sec % 3600) / 60)
  if (d > 0) return `${d}天${h}小时`
  if (h > 0) return `${h}小时${mm}分`
  return `${mm}分${sec % 60}秒`
}

function statusType(s) { return { ONLINE: 'success', OFFLINE: 'info', INSTALLING: 'warning' }[s] || 'info' }
function statusLabel(s) { return { ONLINE: '在线', OFFLINE: '离线', INSTALLING: '待安装' }[s] || s }
function cmdStatusType(s) { return { DONE: 'success', DELIVERED: 'primary', PENDING: 'warning', FAILED: 'danger' }[s] || 'info' }

let inFlight = false
async function load(silent) {
  if (inFlight) return
  inFlight = true
  const cfg = silent === true ? { silent: true } : undefined
  try {
    agents.value = await pilotApi.agents(cfg)
    Object.assign(ov, await pilotApi.overview(cfg))
  } catch (e) { /* keep last good data; silent polls don't toast */ }
  finally { inFlight = false }
}
async function create() {
  if (!createForm.name) { ElMessage.warning('请输入名称'); return }
  const res = await pilotApi.create({ ...createForm })
  installCommand.value = res.installCommand.replace(/<SERVER>/g, location.hostname + ':8081')
  createdKey.value = res.agent.agentKey
  createVisible.value = false
  installVisible.value = true
  Object.assign(createForm, { name: '', host: '', tags: '' })
  load()
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除探针「${row.name}」？`, '提示', { type: 'warning' })
  } catch (e) { return }
  await pilotApi.remove(row.id); ElMessage.success('已删除'); load()
}
function openConfig(row) {
  configAgent.value = row
  try { configText.value = JSON.stringify(JSON.parse(row.config || '{}'), null, 2) }
  catch (e) { configText.value = row.config || '{}' }
  configVisible.value = true
}
async function saveConfig() {
  let cfg
  try { cfg = JSON.parse(configText.value) } catch (e) { ElMessage.error('JSON 格式错误'); return }
  await pilotApi.updateConfig(configAgent.value.id, cfg)
  ElMessage.success('配置已下发'); configVisible.value = false; load()
}
async function sendCmd(row, type) {
  await pilotApi.command(row.id, { type })
  ElMessage.success('指令已下发：' + type)
}
async function showCommands(row) {
  commands.value = await pilotApi.commands(row.id)
  cmdVisible.value = true
}
async function copy(text) {
  try { await navigator.clipboard.writeText(text); ElMessage.success('已复制') } catch (e) {}
}

// backend that serves /sdk/** (same host the install command points at)
const sdkBase = `${location.protocol}//${location.hostname}:8081`

async function downloadProbe() {
  const url = `${sdkBase}/sdk/pilot-agent.sh`
  try {
    const resp = await fetch(url)
    if (!resp.ok) throw new Error('HTTP ' + resp.status)
    const text = await resp.text()
    const blob = new Blob([text], { type: 'text/x-shellscript;charset=utf-8' })
    const a = document.createElement('a')
    a.href = URL.createObjectURL(blob)
    a.download = 'pilot-agent.sh'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    setTimeout(() => URL.revokeObjectURL(a.href), 1000)
    ElMessage.success('探针脚本已开始下载')
  } catch (e) {
    // fetch failed (CORS / backend down) — fall back to a direct link
    ElMessage.warning('直接下载失败，已在新窗口打开脚本地址，可手动另存为')
    window.open(url, '_blank')
  }
}

onMounted(() => {
  load()
  timer = setInterval(() => { if (autoRefresh.value) load(true) }, 8000)
})
onBeforeUnmount(() => timer && clearInterval(timer))
</script>

<style scoped>
.snippet { background: #111827; color: #d6e2ff; padding: 14px; border-radius: 8px; font-size: 12.5px; overflow-x: auto; white-space: pre-wrap; word-break: break-all; }
code { background: #f0f2f5; padding: 1px 5px; border-radius: 4px; }
.d-head { display: flex; align-items: center; gap: 10px; }
.d-name { font-size: 17px; font-weight: 600; }
.value.small { font-size: 18px; }
</style>
