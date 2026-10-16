<template>
  <div class="page">
    <h2 class="page-title">系统监控</h2>
    <p class="page-sub">服务器 / JVM 运行状态、数据规模与在线用户实时概览</p>

    <div class="toolbar">
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <el-switch v-model="autoRefresh" active-text="自动刷新(10s)" style="margin-left:8px" />
      <span class="muted" style="margin-left:auto">更新于 {{ fmtDateTime(data.timestamp) }}</span>
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">在线用户</div><div class="value">{{ data.onlineCount || 0 }}</div></div>
      <div class="stat-card"><div class="label">事件总量</div><div class="value">{{ compact(data.data?.events) }}</div></div>
      <div class="stat-card"><div class="label">用户画像</div><div class="value">{{ compact(data.data?.profiles) }}</div></div>
      <div class="stat-card"><div class="label">运行时长</div><div class="value small">{{ data.jvm?.uptime || '—' }}</div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">服务器信息</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="主机名">{{ data.server?.hostName }}</el-descriptions-item>
          <el-descriptions-item label="操作系统">{{ data.server?.osName }} {{ data.server?.osVersion }}</el-descriptions-item>
          <el-descriptions-item label="系统架构">{{ data.server?.osArch }}</el-descriptions-item>
          <el-descriptions-item label="CPU 核心">{{ data.server?.cpuCores }} 核</el-descriptions-item>
          <el-descriptions-item label="JVM">{{ data.server?.jvmName }}</el-descriptions-item>
        </el-descriptions>
      </div>
      <div class="card">
        <div class="card-title">JVM 运行状态</div>
        <el-descriptions :column="1" border size="small">
          <el-descriptions-item label="Java 版本">{{ data.jvm?.javaVersion }}</el-descriptions-item>
          <el-descriptions-item label="Spring Boot">{{ data.jvm?.springBoot }}</el-descriptions-item>
          <el-descriptions-item label="启动时间">{{ fmtDateTime(data.jvm?.startTime) }}</el-descriptions-item>
          <el-descriptions-item label="非堆内存">{{ data.jvm?.nonHeapUsedMb }} MB</el-descriptions-item>
          <el-descriptions-item label="堆内存">
            {{ data.jvm?.heapUsedMb }} / {{ data.jvm?.heapMaxMb }} MB
            <el-progress :percentage="Number(data.jvm?.heapUsagePct) || 0" :stroke-width="8" style="margin-top:6px" />
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </div>

    <div class="card">
      <div class="card-title">数据规模<span class="muted" style="font-weight:400;font-size:12px">{{ data.data?.scoped ? '（当前租户）' : '（全平台）' }}</span></div>
      <div class="stat-grid">
        <div class="stat-card"><div class="label">事件</div><div class="value">{{ compact(data.data?.events) }}</div></div>
        <div class="stat-card"><div class="label">用户画像</div><div class="value">{{ compact(data.data?.profiles) }}</div></div>
        <div class="stat-card"><div class="label">系统用户</div><div class="value">{{ data.data?.users }}</div></div>
        <div class="stat-card"><div class="label">应用</div><div class="value">{{ data.data?.apps }}</div></div>
        <div class="stat-card"><div class="label">采集节点</div><div class="value">{{ data.data?.agents }}</div></div>
        <div class="stat-card"><div class="label">审计日志</div><div class="value">{{ compact(data.data?.auditLogs) }}</div></div>
      </div>
    </div>

    <div class="card">
      <div class="card-title">在线用户</div>
      <el-table :data="data.onlineUsers || []" size="small" stripe>
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="ip" label="IP" width="160" />
        <el-table-column prop="location" label="归属" width="120" />
        <el-table-column prop="browser" label="客户端" min-width="240" show-overflow-tooltip />
        <el-table-column label="空闲" width="110">
          <template #default="{ row }">{{ idle(row.idleSeconds) }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { systemApi } from '../../api'
import { compact, fmtDateTime } from '../../utils/date'

const data = ref({})
const autoRefresh = ref(true)
let timer = null

function idle(sec) {
  sec = Number(sec) || 0
  if (sec < 60) return sec + ' 秒前'
  return Math.floor(sec / 60) + ' 分钟前'
}
async function load() { data.value = await systemApi.monitor() }

function startTimer() {
  stopTimer()
  if (autoRefresh.value) timer = setInterval(load, 10000)
}
function stopTimer() { if (timer) { clearInterval(timer); timer = null } }

watch(autoRefresh, startTimer)
onMounted(() => { load(); startTimer() })
onBeforeUnmount(stopTimer)
</script>

<style scoped>
.value.small { font-size: 18px; }
</style>
