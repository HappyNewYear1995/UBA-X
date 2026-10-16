<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">事件探索 · 自助看板</h2>
    <p class="page-sub">自由选择事件 + 维度 + 指标即时出图，搭建个性化分析视图</p>

    <RangeBar :initial-days="30" @change="onRange">
      <el-button type="primary" :icon="Refresh" @click="load">查询</el-button>
    </RangeBar>

    <div class="card builder">
      <div class="b-row">
        <div class="b-field">
          <span class="b-label">事件</span>
          <el-select v-model="event" size="default" style="width: 200px" @change="load">
            <el-option label="全部事件" value="" />
            <el-option v-for="e in meta.events" :key="e.name" :label="e.label + '（' + e.name + ' · ' + e.count + '）'" :value="e.name" />
          </el-select>
        </div>
        <div class="b-field">
          <span class="b-label">分组维度</span>
          <el-select v-model="dim" size="default" style="width: 160px" @change="load">
            <el-option v-for="d in meta.dimensions" :key="d.key" :label="d.label" :value="d.key" />
          </el-select>
        </div>
        <div class="b-field">
          <span class="b-label">指标</span>
          <el-radio-group v-model="metric" size="default" @change="load">
            <el-radio-button value="events">事件数</el-radio-button>
            <el-radio-button value="users">用户数</el-radio-button>
          </el-radio-group>
        </div>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">总事件</div><div class="value">{{ compact(data.summary.total) }}</div></div>
      <div class="stat-card"><div class="label">分组数</div><div class="value">{{ data.summary.groups || 0 }}</div></div>
      <div class="stat-card"><div class="label">当前事件</div><div class="value small">{{ data.summary.event || '全部事件' }}</div></div>
      <div class="stat-card"><div class="label">分组维度</div><div class="value small">{{ data.summary.dimensionLabel || '—' }}</div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">按{{ data.summary.dimensionLabel || '维度' }}分布</div>
        <BaseChart :option="barOption" height="340px" />
      </div>
      <div class="card">
        <div class="card-title">占比</div>
        <BaseChart :option="pieOption" height="340px" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">明细</div>
      <el-table :data="data.rows" size="small">
        <el-table-column prop="name" label="分组" min-width="180" show-overflow-tooltip />
        <el-table-column prop="events" label="事件数" width="120" />
        <el-table-column prop="users" label="用户数" width="120" />
        <el-table-column label="占比" min-width="180">
          <template #default="{ row }"><el-progress :percentage="row.share || 0" :stroke-width="10" /></template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import RangeBar from '../../components/RangeBar.vue'
import BaseChart from '../../components/BaseChart.vue'
import { extApi } from '../../api'
import { useAppStore } from '../../store/app'
import { compact, lastNDays } from '../../utils/date'

const appStore = useAppStore()
const range = reactive({ from: lastNDays(30)[0], to: lastNDays(30)[1] })
const event = ref('')
const dim = ref('device')
const metric = ref('events')
const meta = reactive({ events: [], dimensions: [] })
const data = reactive({ summary: {}, rows: [] })
const loading = ref(false)

async function loadMeta() {
  try {
    const m = await extApi.eventMeta({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(meta, m)
  } catch (e) { /* surfaced by http interceptor */ }
}
async function load() {
  loading.value = true
  try {
    const d = await extApi.eventExplore({ appId: appStore.currentAppId, from: range.from, to: range.to, event: event.value, dim: dim.value, metric: metric.value })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; loadMeta(); load() }

const COLORS = ['#2b5cff', '#00c2a8', '#e6a23c', '#f56c6c', '#6a3cff', '#00a0e9', '#909399', '#b8860b']
const top12 = computed(() => (data.rows || []).slice(0, 12))
const barOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 110, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: top12.value.map((r) => r.name).reverse() },
  series: [{
    type: 'bar', barMaxWidth: 22,
    data: top12.value.map((r) => r.value).reverse(),
    itemStyle: { color: '#2b5cff', borderRadius: [0, 4, 4, 0] },
    label: { show: true, position: 'right' }
  }]
}))
const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, type: 'scroll' },
  series: [{
    type: 'pie', radius: ['40%', '66%'], center: ['50%', '44%'],
    data: top12.value.map((r, i) => ({ name: r.name, value: r.value, itemStyle: { color: COLORS[i % COLORS.length] } })),
    label: { formatter: '{b}: {d}%' }
  }]
}))

onMounted(async () => { await loadMeta(); await load() })
watch(() => appStore.currentAppId, () => { loadMeta(); load() })
</script>

<style scoped>
.builder { padding: 14px 16px; }
.b-row { display: flex; gap: 24px; flex-wrap: wrap; align-items: center; }
.b-field { display: flex; align-items: center; gap: 8px; }
.b-label { font-size: 13px; color: #5a6270; font-weight: 500; }
.value.small { font-size: 14px; }
</style>
