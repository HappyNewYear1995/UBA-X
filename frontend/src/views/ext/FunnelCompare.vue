<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">漏斗对比</h2>
    <p class="page-sub">按维度分组对比转化漏斗，量化每一步的差异</p>

    <RangeBar :initial-days="14" @change="onRange">
      <el-select v-model="dim" size="default" style="width: 140px" @change="load">
        <el-option v-for="d in dims" :key="d.value" :label="d.label" :value="d.value" />
      </el-select>
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">参与用户</div><div class="value">{{ compact(data.summary.users) }}</div></div>
      <div class="stat-card"><div class="label">进入漏斗</div><div class="value">{{ compact(data.summary.entry) }}</div></div>
      <div class="stat-card"><div class="label">完成购买</div><div class="value">{{ compact(data.summary.purchase) }}</div></div>
      <div class="stat-card"><div class="label">整体转化率</div><div class="value">{{ data.summary.overallRate || 0 }}<small> %</small></div></div>
      <div class="stat-card"><div class="label">对比分组</div><div class="value small">{{ (data.summary.segments || []).join(' vs ') || '—' }}</div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">各步转化率对比（%）</div>
        <BaseChart :option="rateOption" height="320px" />
      </div>
      <div class="card">
        <div class="card-title">各步人数对比</div>
        <BaseChart :option="countOption" height="320px" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">漏斗明细</div>
      <el-table :data="tableRows" size="small">
        <el-table-column prop="step" label="步骤" min-width="140" />
        <el-table-column v-for="s in data.series" :key="s.segment" :label="s.segment + ' 人数'" width="140">
          <template #default="{ row }">{{ row.counts[s.segment] }}</template>
        </el-table-column>
        <el-table-column v-for="s in data.series" :key="s.segment + '_r'" :label="s.segment + ' 转化'" width="140">
          <template #default="{ row }">{{ row.rates[s.segment] }} %</template>
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
const range = reactive({ from: lastNDays(14)[0], to: lastNDays(14)[1] })
const dim = ref('device')
const dims = [
  { label: '设备', value: 'device' },
  { label: '操作系统', value: 'os' },
  { label: '浏览器', value: 'browser' },
  { label: '地域', value: 'region' },
  { label: '流量来源', value: 'source' }
]
const data = reactive({ summary: {}, steps: [], series: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.funnelCompare({ appId: appStore.currentAppId, from: range.from, to: range.to, dim: dim.value })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const COLORS = ['#2b5cff', '#00c2a8', '#e6a23c', '#f56c6c']
const stepLabels = computed(() => (data.steps || []).map((s) => s.label))

const rateOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { data: (data.series || []).map((s) => s.segment) },
  grid: { left: 45, right: 20, top: 40, bottom: 40 },
  xAxis: { type: 'category', data: stepLabels.value },
  yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
  series: (data.series || []).map((s, i) => ({
    name: s.segment, type: 'line', smooth: true, symbolSize: 7,
    data: s.rates, itemStyle: { color: COLORS[i % COLORS.length] }
  }))
}))

const countOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: (data.series || []).map((s) => s.segment) },
  grid: { left: 45, right: 20, top: 40, bottom: 40 },
  xAxis: { type: 'category', data: stepLabels.value },
  yAxis: { type: 'value' },
  series: (data.series || []).map((s, i) => ({
    name: s.segment, type: 'bar', data: s.counts,
    itemStyle: { color: COLORS[i % COLORS.length], borderRadius: [3, 3, 0, 0] }
  }))
}))

const tableRows = computed(() => {
  const counts = {}, rates = {}
  for (const s of data.series || []) { counts[s.segment] = s.counts; rates[s.segment] = s.rates }
  return (data.steps || []).map((st, i) => ({
    step: st.label,
    counts: Object.fromEntries((data.series || []).map((s) => [s.segment, s.counts[i]])),
    rates: Object.fromEntries((data.series || []).map((s) => [s.segment, s.rates[i]]))
  }))
})

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.value.small { font-size: 15px; }
</style>
