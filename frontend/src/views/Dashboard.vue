<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">概览看板</h2>
    <p class="page-sub">核心指标总览与趋势、来源与设备分布</p>

    <RangeBar @change="onRange">
      <el-button :icon="Refresh" circle @click="loadAll" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">浏览量 PV</div><div class="value">{{ compact(ov.pv) }}</div></div>
      <div class="stat-card"><div class="label">访客数 UV</div><div class="value">{{ compact(ov.uv) }}</div></div>
      <div class="stat-card"><div class="label">访问次数</div><div class="value">{{ compact(ov.visits) }}</div></div>
      <div class="stat-card"><div class="label">新增用户</div><div class="value">{{ compact(ov.newUsers) }}</div></div>
      <div class="stat-card"><div class="label">平均停留</div><div class="value">{{ ov.avgDuration || 0 }}<small> 秒</small></div></div>
      <div class="stat-card"><div class="label">跳出率</div><div class="value">{{ ov.bounceRate || 0 }}<small> %</small></div></div>
    </div>

    <div class="card">
      <div class="card-title">访问趋势（PV / UV）</div>
      <BaseChart :option="trendOption" height="320px" />
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">设备分布</div>
        <BaseChart :option="deviceOption" height="280px" />
      </div>
      <div class="card">
        <div class="card-title">浏览器分布</div>
        <BaseChart :option="browserOption" height="280px" />
      </div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">地区 TOP</div>
        <BaseChart :option="regionOption" height="300px" />
      </div>
      <div class="card">
        <div class="card-title">来源渠道</div>
        <BaseChart :option="refOption" height="300px" />
      </div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">热门页面</div>
        <el-table :data="pages" size="small" height="300">
          <el-table-column prop="path" label="页面路径" min-width="140" show-overflow-tooltip />
          <el-table-column prop="pv" label="PV" width="80" />
          <el-table-column prop="uv" label="UV" width="80" />
        </el-table>
      </div>
      <div class="card">
        <div class="card-title">热门事件</div>
        <el-table :data="events" size="small" height="300">
          <el-table-column prop="event" label="事件" min-width="140" show-overflow-tooltip />
          <el-table-column prop="count" label="次数" width="90" />
          <el-table-column prop="users" label="用户数" width="90" />
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import RangeBar from '../components/RangeBar.vue'
import BaseChart from '../components/BaseChart.vue'
import { analyticsApi } from '../api'
import { useAppStore } from '../store/app'
import { compact, lastNDays } from '../utils/date'

const appStore = useAppStore()
const ov = reactive({ pv: 0, uv: 0, visits: 0, newUsers: 0, avgDuration: 0, bounceRate: 0 })
const trend = ref([])
const dist = reactive({ device: [], browser: [], region: [], referrer: [] })
const pages = ref([])
const events = ref([])
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })
const loading = ref(false)

function params() {
  return { appId: appStore.currentAppId, from: range.from, to: range.to }
}

async function loadAll() {
  loading.value = true
  try {
    const p = params()
    const [o, t, d, pg, ev] = await Promise.all([
      analyticsApi.overview(p),
      analyticsApi.trend(p),
      analyticsApi.distributions(p),
      analyticsApi.pages({ ...p, limit: 10 }),
      analyticsApi.events({ ...p, limit: 10 })
    ])
    Object.assign(ov, o)
    trend.value = t
    Object.assign(dist, d)
    pages.value = pg
    events.value = ev
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}

function onRange(r) { range.from = r.from; range.to = r.to; loadAll() }

const trendOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['PV', 'UV'] },
  grid: { left: 45, right: 20, top: 40, bottom: 40 },
  xAxis: { type: 'category', data: trend.value.map((d) => d.date.slice(5)) },
  yAxis: { type: 'value' },
  series: [
    { name: 'PV', type: 'line', smooth: true, areaStyle: { opacity: .15 }, data: trend.value.map((d) => d.pv), itemStyle: { color: '#2b5cff' } },
    { name: 'UV', type: 'line', smooth: true, areaStyle: { opacity: .15 }, data: trend.value.map((d) => d.uv), itemStyle: { color: '#00c2a8' } }
  ]
}))

function pieOption(data, colors) {
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    color: colors,
    series: [{ type: 'pie', radius: ['42%', '68%'], center: ['50%', '44%'], data: data.map((d) => ({ name: d.name, value: d.value })), label: { formatter: '{b}\n{d}%' } }]
  }
}
const deviceOption = computed(() => pieOption(dist.device, ['#2b5cff', '#00c2a8', '#ff9f43', '#6a3cff', '#f5576c']))
const browserOption = computed(() => pieOption(dist.browser, ['#4e79a7', '#f28e2b', '#59a14f', '#e15759', '#b07aa1', '#76b7b2']))

const regionOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 80, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: dist.region.slice(0, 10).map((d) => d.name).reverse() },
  series: [{ type: 'bar', data: dist.region.slice(0, 10).map((d) => d.value).reverse(), itemStyle: { color: '#2b5cff', borderRadius: [0, 4, 4, 0] } }]
}))

const refOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 130, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: dist.referrer.slice(0, 8).map((d) => d.name).reverse() },
  series: [{ type: 'bar', data: dist.referrer.slice(0, 8).map((d) => d.value).reverse(), itemStyle: { color: '#00c2a8', borderRadius: [0, 4, 4, 0] } }]
}))

watch(() => appStore.currentAppId, loadAll)
onMounted(async () => { await appStore.load(); loadAll() })
</script>
