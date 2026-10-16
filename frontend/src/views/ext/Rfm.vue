<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">RFM 用户价值分层</h2>
    <p class="page-sub">按最近活跃 / 频次 / 金额三维打分，自动划分用户价值客群</p>

    <RangeBar :initial-days="30" @change="onRange">
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">用户总数</div><div class="value">{{ compact(data.summary.users) }}</div></div>
      <div class="stat-card"><div class="label">付费用户</div><div class="value">{{ compact(data.summary.payingUsers) }}</div></div>
      <div class="stat-card"><div class="label">平均 R</div><div class="value">{{ data.summary.avgR || 0 }}</div></div>
      <div class="stat-card"><div class="label">平均 F</div><div class="value">{{ data.summary.avgF || 0 }}</div></div>
      <div class="stat-card"><div class="label">主力客群</div><div class="value small">{{ data.summary.topSegment || '—' }}</div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">客群分布</div>
        <BaseChart :option="pieOption" height="320px" />
      </div>
      <div class="card">
        <div class="card-title">客群人数</div>
        <BaseChart :option="barOption" height="320px" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">高价值用户 TOP20</div>
      <el-table :data="data.table" size="small">
        <el-table-column prop="distinctId" label="用户" min-width="110" show-overflow-tooltip />
        <el-table-column prop="region" label="地域" width="100" show-overflow-tooltip />
        <el-table-column label="R" width="90"><template #default="{ row }"><el-tag size="small" effect="plain">{{ row.rs }}</el-tag> <span class="raw">{{ row.r }}d</span></template></el-table-column>
        <el-table-column label="F" width="90"><template #default="{ row }"><el-tag size="small" effect="plain">{{ row.fs }}</el-tag> <span class="raw">{{ row.f }}</span></template></el-table-column>
        <el-table-column label="M" width="110"><template #default="{ row }"><el-tag size="small" effect="plain">{{ row.ms }}</el-tag> <span class="raw">¥{{ row.m }}</span></template></el-table-column>
        <el-table-column prop="score" label="总分" width="80" />
        <el-table-column label="客群" min-width="140">
          <template #default="{ row }"><el-tag size="small" :type="segType(row.segment)">{{ row.segment }}</el-tag></template>
        </el-table-column>
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
const range = reactive({ from: lastNDays(30)[0], to: lastNDays(30)[1] })
const data = reactive({ summary: {}, segments: [], table: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.rfm({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

function segType(s) {
  if (s && s.startsWith('重要价值')) return 'danger'
  if (s && s.startsWith('重要')) return 'warning'
  if (s && s.startsWith('一般价值')) return 'success'
  return 'info'
}

const COLORS = ['#f56c6c', '#e6a23c', '#2b5cff', '#00c2a8', '#6a3cff', '#00a0e9', '#909399', '#b8860b']
const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0, type: 'scroll' },
  series: [{
    type: 'pie', radius: ['40%', '66%'], center: ['50%', '44%'],
    data: (data.segments || []).map((s, i) => ({ name: s.name, value: s.value, itemStyle: { color: COLORS[i % COLORS.length] } })),
    label: { formatter: '{d}%' }
  }]
}))
const barOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 110, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: (data.segments || []).map((s) => s.name).reverse() },
  series: [{
    type: 'bar', barMaxWidth: 22,
    data: (data.segments || []).map((s) => s.value).reverse(),
    itemStyle: { color: '#2b5cff', borderRadius: [0, 4, 4, 0] },
    label: { show: true, position: 'right' }
  }]
}))

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.value.small { font-size: 15px; }
.raw { color: #8a94a6; font-size: 12px; margin-left: 2px; }
</style>
