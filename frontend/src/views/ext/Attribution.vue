<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">归因分析</h2>
    <p class="page-sub">还原用户转化前的多触点渠道，对比不同归因模型下各渠道的贡献分配</p>

    <RangeBar :initial-days="30" @change="onRange">
      <el-button type="primary" :icon="Refresh" @click="load">查询</el-button>
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">转化用户</div><div class="value">{{ compact(data.summary.convertingUsers) }}</div></div>
      <div class="stat-card"><div class="label">总转化次数</div><div class="value">{{ compact(data.summary.totalConversions) }}</div></div>
      <div class="stat-card"><div class="label">总转化金额</div><div class="value">¥{{ compact(data.summary.totalValue) }}</div></div>
      <div class="stat-card"><div class="label">渠道数</div><div class="value">{{ data.summary.channels || 0 }}</div></div>
      <div class="stat-card"><div class="label">人均触点</div><div class="value">{{ data.summary.avgTouchpoints || 0 }}</div></div>
    </div>

    <div class="card builder">
      <div class="b-row">
        <span class="b-label">归因模型</span>
        <el-radio-group v-model="model">
          <el-radio-button value="firstTouch">首次触点</el-radio-button>
          <el-radio-button value="lastTouch">末次触点</el-radio-button>
          <el-radio-button value="linear">线性</el-radio-button>
          <el-radio-button value="positional">位置(U型)</el-radio-button>
        </el-radio-group>
        <span class="b-hint">{{ modelHint }}</span>
      </div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">各渠道归因转化</div>
        <BaseChart :option="barOption" height="340px" />
      </div>
      <div class="card">
        <div class="card-title">转化贡献占比</div>
        <BaseChart :option="pieOption" height="340px" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">模型对比（转化次数）</div>
      <BaseChart :option="compareOption" height="320px" />
    </div>

    <div class="card">
      <div class="card-title">{{ modelLabel }} 明细</div>
      <el-table :data="rows" size="small">
        <el-table-column prop="channel" label="渠道" min-width="120" />
        <el-table-column label="归因转化" width="120">
          <template #default="{ row }">{{ Number(row.conversions).toFixed(1) }}</template>
        </el-table-column>
        <el-table-column label="归因金额" width="120">
          <template #default="{ row }">¥{{ compact(row.value) }}</template>
        </el-table-column>
        <el-table-column label="转化占比" min-width="160">
          <template #default="{ row }"><el-progress :percentage="row.convShare || 0" :stroke-width="10" /></template>
        </el-table-column>
        <el-table-column label="金额占比" min-width="160">
          <template #default="{ row }"><el-progress :percentage="row.valueShare || 0" :stroke-width="10" color="#00a870" /></template>
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
const model = ref('lastTouch')
const data = reactive({ summary: {}, models: { firstTouch: [], lastTouch: [], linear: [], positional: [] } })
const loading = ref(false)

const MODEL_LABELS = { firstTouch: '首次触点', lastTouch: '末次触点', linear: '线性', positional: '位置(U型)' }
const MODEL_HINTS = {
  firstTouch: '全部转化归功于用户接触的第一个渠道，衡量拉新引流能力',
  lastTouch: '全部转化归功于转化前最后一个渠道，衡量临门一脚的收口能力',
  linear: '转化在全部触点渠道间平均分配，兼顾全链路贡献',
  positional: '首末各 40%，中间触点平分 20%，兼顾引流与收口'
}
const modelLabel = computed(() => MODEL_LABELS[model.value])
const modelHint = computed(() => MODEL_HINTS[model.value])
const rows = computed(() => data.models[model.value] || [])

async function load() {
  loading.value = true
  try {
    const d = await extApi.attribution({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const COLORS = ['#2b5cff', '#00c2a8', '#e6a23c', '#f56c6c', '#6a3cff', '#00a0e9', '#909399', '#b8860b']

const barOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 80, right: 40, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: rows.value.map((r) => r.channel).reverse() },
  series: [{
    type: 'bar', barMaxWidth: 24,
    data: rows.value.map((r) => Number(Number(r.conversions).toFixed(1))).reverse(),
    itemStyle: { color: '#2b5cff', borderRadius: [0, 4, 4, 0] },
    label: { show: true, position: 'right' }
  }]
}))

const pieOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { bottom: 0, type: 'scroll' },
  series: [{
    type: 'pie', radius: ['40%', '66%'], center: ['50%', '44%'],
    data: rows.value.map((r, i) => ({ name: r.channel, value: Number(Number(r.conversions).toFixed(1)), itemStyle: { color: COLORS[i % COLORS.length] } })),
    label: { formatter: '{b}: {d}%' }
  }]
}))

const compareOption = computed(() => {
  const channels = Array.from(new Set(['firstTouch', 'lastTouch', 'linear', 'positional'].flatMap((k) => (data.models[k] || []).map((r) => r.channel))))
  const series = ['firstTouch', 'lastTouch', 'linear', 'positional'].map((k, i) => {
    const map = Object.fromEntries((data.models[k] || []).map((r) => [r.channel, Number(Number(r.conversions).toFixed(1))]))
    return {
      name: MODEL_LABELS[k], type: 'bar', barMaxWidth: 18,
      itemStyle: { color: COLORS[i % COLORS.length], borderRadius: [3, 3, 0, 0] },
      data: channels.map((c) => map[c] || 0)
    }
  })
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { top: 0 },
    grid: { left: 50, right: 24, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: channels },
    yAxis: { type: 'value' },
    series
  }
})

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.builder { padding: 14px 16px; }
.b-row { display: flex; gap: 16px; flex-wrap: wrap; align-items: center; }
.b-label { font-size: 13px; color: #5a6270; font-weight: 500; }
.b-hint { font-size: 12px; color: #9aa2af; margin-left: 4px; }
</style>
