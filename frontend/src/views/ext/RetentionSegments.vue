<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">留存分群</h2>
    <p class="page-sub">按维度分群对比前后周期留存率，定位高留存与易流失人群</p>

    <RangeBar :initial-days="14" @change="onRange">
      <el-select v-model="dim" size="default" style="width: 140px" @change="load">
        <el-option v-for="d in dims" :key="d.value" :label="d.label" :value="d.value" />
      </el-select>
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">前周期用户</div><div class="value">{{ compact(data.summary.cohort) }}</div></div>
      <div class="stat-card"><div class="label">留存用户</div><div class="value">{{ compact(data.summary.retained) }}</div></div>
      <div class="stat-card"><div class="label">整体留存率</div><div class="value">{{ data.summary.rate || 0 }}<small> %</small></div></div>
      <div class="stat-card"><div class="label">分群数</div><div class="value">{{ data.summary.segments || 0 }}</div></div>
      <div class="stat-card"><div class="label">周期中点</div><div class="value small">{{ data.summary.midPoint || '—' }}</div></div>
    </div>

    <div class="card">
      <div class="card-title">各分群留存率对比</div>
      <BaseChart :option="barOption" height="320px" />
    </div>

    <div class="card">
      <div class="card-title">分群明细</div>
      <el-table :data="data.segments" size="small">
        <el-table-column prop="name" label="分群" min-width="160" />
        <el-table-column prop="cohort" label="前周期用户" width="120" />
        <el-table-column prop="retained" label="留存用户" width="120" />
        <el-table-column label="留存率" min-width="200">
          <template #default="{ row }">
            <el-progress :percentage="row.rate || 0" :stroke-width="12" :color="rateColor(row.rate)" />
          </template>
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
const data = reactive({ summary: {}, segments: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.retentionSegments({ appId: appStore.currentAppId, from: range.from, to: range.to, dim: dim.value })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

function rateColor(v) { return v >= 60 ? '#00c2a8' : v >= 40 ? '#e6a23c' : '#f56c6c' }

const barOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 45, right: 20, top: 20, bottom: 60 },
  xAxis: { type: 'category', data: (data.segments || []).map((s) => s.name), axisLabel: { rotate: 20 } },
  yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
  series: [{
    type: 'bar', barMaxWidth: 48,
    data: (data.segments || []).map((s) => ({ value: s.rate, itemStyle: { color: rateColor(s.rate), borderRadius: [4, 4, 0, 0] } })),
    label: { show: true, position: 'top', formatter: '{c}%' }
  }]
}))

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.value.small { font-size: 15px; }
</style>
