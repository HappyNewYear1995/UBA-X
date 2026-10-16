<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">访问安全分析</h2>
    <p class="page-sub">识别高频访问、爬虫 UA 与突发脚本会话等异常流量</p>

    <RangeBar @change="onRange">
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">扫描事件</div><div class="value">{{ compact(data.summary.events) }}</div></div>
      <div class="stat-card"><div class="label">独立 IP</div><div class="value">{{ compact(data.summary.distinctIps) }}</div></div>
      <div class="stat-card"><div class="label">风险 IP</div><div class="value">{{ data.summary.riskyIps || 0 }}</div></div>
      <div class="stat-card"><div class="label">机器人事件</div><div class="value">{{ compact(data.summary.botEvents) }}</div></div>
      <div class="stat-card"><div class="label">突发会话</div><div class="value">{{ data.summary.burstSessions || 0 }}</div></div>
      <div class="stat-card"><div class="label">风险等级</div><div class="value" :class="levelClass">{{ data.summary.riskLevel || '—' }}</div></div>
    </div>

    <div class="card">
      <div class="card-title">24 小时事件分布</div>
      <BaseChart :option="hourlyOption" height="240px" />
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">高频 IP TOP15</div>
        <el-table :data="data.topIps" size="small" height="320">
          <el-table-column prop="ip" label="IP" min-width="120" show-overflow-tooltip />
          <el-table-column prop="events" label="事件" width="80" />
          <el-table-column prop="users" label="用户" width="70" />
          <el-table-column prop="ratio" label="倍率" width="70" />
          <el-table-column label="风险" width="90">
            <template #default="{ row }">
              <el-tag size="small" :type="riskType(row.risk)">{{ riskLabel(row.risk) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="判定依据" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ (row.reasons || []).join('；') || '—' }}</template>
          </el-table-column>
        </el-table>
      </div>
      <div class="card">
        <div class="card-title">可疑 User-Agent</div>
        <el-table :data="data.botAgents" size="small" height="320">
          <el-table-column prop="ua" label="UA" min-width="220" show-overflow-tooltip />
          <el-table-column prop="events" label="事件" width="90" />
          <el-table-column prop="users" label="用户" width="90" />
        </el-table>
        <el-empty v-if="!data.botAgents.length" description="未检测到爬虫 UA" :image-size="60" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">突发脚本会话（短时高频）</div>
      <el-table :data="data.burstSessions" size="small">
        <el-table-column prop="sessionId" label="会话 ID" min-width="200" show-overflow-tooltip />
        <el-table-column prop="distinctId" label="用户" min-width="140" show-overflow-tooltip />
        <el-table-column prop="events" label="事件数" width="100" />
        <el-table-column prop="spanSeconds" label="持续(秒)" width="100" />
      </el-table>
      <el-empty v-if="!data.burstSessions.length" description="未检测到突发会话" :image-size="60" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import RangeBar from '../../components/RangeBar.vue'
import BaseChart from '../../components/BaseChart.vue'
import { extApi } from '../../api'
import { useAppStore } from '../../store/app'
import { compact, lastNDays } from '../../utils/date'

const appStore = useAppStore()
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })
const data = reactive({ summary: {}, topIps: [], botAgents: [], burstSessions: [], hourly: [] })
const loading = ref(false)

const levelClass = computed(() => ({ '高': 'lv-high', '中': 'lv-mid', '低': 'lv-low' }[data.summary.riskLevel] || ''))
function riskType(r) { return { HIGH: 'danger', MEDIUM: 'warning', NORMAL: 'info' }[r] || 'info' }
function riskLabel(r) { return { HIGH: '高危', MEDIUM: '中危', NORMAL: '正常' }[r] || r }

async function load() {
  loading.value = true
  try {
    const d = await extApi.security({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const hourlyOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 45, right: 20, top: 20, bottom: 30 },
  xAxis: { type: 'category', data: data.hourly.map((h) => h.hour + '时') },
  yAxis: { type: 'value' },
  series: [{
    type: 'bar',
    data: data.hourly.map((h) => h.events),
    itemStyle: { color: '#2b5cff', borderRadius: [3, 3, 0, 0] }
  }]
}))

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.lv-high { color: #f56c6c; }
.lv-mid { color: #e6a23c; }
.lv-low { color: #67c23a; }
</style>
