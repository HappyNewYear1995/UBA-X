<template>
  <div class="page">
    <h2 class="page-title">实时分析 <el-tag size="small" type="danger" effect="dark" class="live">LIVE</el-tag></h2>
    <p class="page-sub">内置实时流处理引擎 · 每分钟窗口聚合，5 秒自动刷新</p>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">近{{ minutes }}分钟 PV</div><div class="value">{{ rt.pv }}</div></div>
      <div class="stat-card"><div class="label">近{{ minutes }}分钟 UV</div><div class="value">{{ rt.uv }}</div></div>
      <div class="stat-card"><div class="label">事件总数</div><div class="value">{{ rt.events }}</div></div>
      <div class="stat-card"><div class="label">引擎队列</div><div class="value">{{ sm.queueSize || 0 }}<small> / {{ sm.queueCapacity || 0 }}</small></div></div>
      <div class="stat-card"><div class="label">已持久化</div><div class="value">{{ compact(sm.persisted) }}</div></div>
      <div class="stat-card"><div class="label">丢弃</div><div class="value">{{ sm.dropped || 0 }}</div></div>
    </div>

    <div class="card">
      <div class="card-title">
        实时流量趋势
        <el-radio-group v-model="minutes" size="small" @change="loadRealtime">
          <el-radio-button :value="15">15分钟</el-radio-button>
          <el-radio-button :value="30">30分钟</el-radio-button>
          <el-radio-button :value="60">60分钟</el-radio-button>
        </el-radio-group>
      </div>
      <BaseChart :option="rtOption" height="300px" />
    </div>

    <div class="card">
      <div class="card-title">实时事件流 <span class="muted">（最新 {{ feed.length }} 条）</span></div>
      <el-table :data="feed" size="small" height="420" stripe>
        <el-table-column prop="time" label="时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.time) }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="90">
          <template #default="{ row }"><el-tag size="small" :type="tagType(row.type)">{{ row.type }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="event" label="事件" width="130" />
        <el-table-column prop="path" label="页面" min-width="130" show-overflow-tooltip />
        <el-table-column prop="distinctId" label="访客" width="120" show-overflow-tooltip />
        <el-table-column label="设备" width="150">
          <template #default="{ row }">{{ row.device }} / {{ row.browser }}</template>
        </el-table-column>
        <el-table-column label="地区" width="140">
          <template #default="{ row }">{{ [row.region, row.city].filter(Boolean).join(' ') || '-' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import BaseChart from '../components/BaseChart.vue'
import { analyticsApi } from '../api'
import { useAppStore } from '../store/app'
import { compact, fmtDateTime } from '../utils/date'

const appStore = useAppStore()
const rt = reactive({ pv: 0, uv: 0, events: 0, series: [] })
const sm = reactive({})
const feed = ref([])
const minutes = ref(30)
let timer = null

function tagType(t) {
  return { pageview: 'primary', click: 'warning', event: 'success', custom: 'success', leave: 'info' }[t] || 'info'
}

async function loadRealtime(silent) {
  const cfg = silent === true ? { silent: true } : undefined
  try { Object.assign(rt, await analyticsApi.realtime({ appId: appStore.currentAppId, minutes: minutes.value }, cfg)) } catch (e) { /* keep last good data */ }
}
async function loadFeed(silent) {
  const cfg = silent === true ? { silent: true } : undefined
  try { feed.value = await analyticsApi.live({ limit: 40 }, cfg) } catch (e) { /* keep last good data */ }
}
async function loadMetrics(silent) {
  const cfg = silent === true ? { silent: true } : undefined
  try { Object.assign(sm, await analyticsApi.streamMetrics(cfg)) } catch (e) {}
}
let inFlight = false
async function loadAll(silent) {
  if (inFlight) return          // skip overlapping polls when the backend is slow
  inFlight = true
  try { await Promise.all([loadRealtime(silent), loadFeed(silent), loadMetrics(silent)]) }
  finally { inFlight = false }
}

const rtOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['PV', 'UV', '事件'] },
  grid: { left: 45, right: 20, top: 40, bottom: 50 },
  xAxis: { type: 'category', data: rt.series.map((d) => d.time.slice(11, 16)) },
  yAxis: { type: 'value' },
  series: [
    { name: 'PV', type: 'line', smooth: true, showSymbol: false, areaStyle: { opacity: .18 }, data: rt.series.map((d) => d.pv), itemStyle: { color: '#2b5cff' } },
    { name: 'UV', type: 'line', smooth: true, showSymbol: false, data: rt.series.map((d) => d.uv), itemStyle: { color: '#00c2a8' } },
    { name: '事件', type: 'bar', data: rt.series.map((d) => d.events), itemStyle: { color: '#c7d6ff' } }
  ]
}))

onMounted(async () => {
  await appStore.load()
  await loadAll(false)
  timer = setInterval(() => loadAll(true), 5000)
})
watch(() => appStore.currentAppId, () => loadAll(false))
onBeforeUnmount(() => timer && clearInterval(timer))
</script>

<style scoped>
.live { margin-left: 8px; vertical-align: middle; }
</style>
