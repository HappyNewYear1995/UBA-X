<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">地域分析</h2>
    <p class="page-sub">按地域 / 城市下钻访问量，观察地域结构与设备差异</p>

    <RangeBar @change="onRange">
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">事件量</div><div class="value">{{ compact(data.summary.events) }}</div></div>
      <div class="stat-card"><div class="label">覆盖地域</div><div class="value">{{ data.summary.regions || 0 }}</div></div>
      <div class="stat-card"><div class="label">覆盖城市</div><div class="value">{{ data.summary.cities || 0 }}</div></div>
      <div class="stat-card"><div class="label">TOP 地域</div><div class="value small">{{ data.summary.topRegion || '—' }}</div></div>
      <div class="stat-card"><div class="label">TOP 地域占比</div><div class="value">{{ data.summary.topRegionShare || 0 }}<small> %</small></div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">地域 PV / UV</div>
        <BaseChart :option="regionOption" height="320px" />
      </div>
      <div class="card">
        <div class="card-title">地域 × 设备（TOP6 地域）</div>
        <BaseChart :option="deviceOption" height="320px" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">TOP5 地域每日趋势</div>
      <BaseChart :option="dailyOption" height="300px" />
    </div>

    <div class="card">
      <div class="card-title">城市明细 TOP12</div>
      <el-table :data="data.cities" size="small">
        <el-table-column prop="name" label="地域 / 城市" min-width="200" />
        <el-table-column prop="pv" label="PV" width="100" />
        <el-table-column prop="users" label="UV" width="100" />
      </el-table>
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
const data = reactive({ summary: {}, regions: [], cities: [], devices: [], deviceMatrix: [], daily: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.geo({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const regionOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['PV', 'UV'] },
  grid: { left: 45, right: 20, top: 40, bottom: 60 },
  xAxis: { type: 'category', data: data.regions.map((r) => r.name), axisLabel: { rotate: 30 } },
  yAxis: { type: 'value' },
  series: [
    { name: 'PV', type: 'bar', data: data.regions.map((r) => r.pv), itemStyle: { color: '#2b5cff', borderRadius: [3, 3, 0, 0] } },
    { name: 'UV', type: 'bar', data: data.regions.map((r) => r.users), itemStyle: { color: '#00c2a8', borderRadius: [3, 3, 0, 0] } }
  ]
}))

const COLORS = ['#2b5cff', '#00c2a8', '#e6a23c', '#f56c6c', '#909399', '#6a3cff']
const deviceOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: data.devices },
  grid: { left: 45, right: 20, top: 40, bottom: 60 },
  xAxis: { type: 'category', data: data.deviceMatrix.map((r) => r.region), axisLabel: { rotate: 30 } },
  yAxis: { type: 'value' },
  series: data.devices.map((d, i) => ({
    name: d, type: 'bar', stack: 'device',
    data: data.deviceMatrix.map((r) => r[d] || 0),
    itemStyle: { color: COLORS[i % COLORS.length] }
  }))
}))

const dailyOption = computed(() => {
  const keys = data.daily.length ? Object.keys(data.daily[0]).filter((k) => k !== 'date') : []
  return {
    tooltip: { trigger: 'axis' },
    legend: { data: keys },
    grid: { left: 45, right: 20, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: data.daily.map((d) => (d.date || '').slice(5)) },
    yAxis: { type: 'value' },
    series: keys.map((k, i) => ({
      name: k, type: 'line', smooth: true,
      data: data.daily.map((d) => d[k]),
      itemStyle: { color: COLORS[i % COLORS.length] }
    }))
  }
})

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.value.small { font-size: 18px; }
</style>
