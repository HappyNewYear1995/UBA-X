<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">留存分析</h2>
    <p class="page-sub">以某日活跃用户为同期群，追踪其后 N 日的回访留存</p>

    <div class="toolbar">
      <span class="muted">同期群日期</span>
      <el-date-picker v-model="anchor" type="date" value-format="YYYY-MM-DD" :clearable="false" @change="load" />
      <span class="muted">追踪天数</span>
      <el-select v-model="days" style="width: 110px" @change="load">
        <el-option :value="7" label="7 天" /><el-option :value="14" label="14 天" /><el-option :value="30" label="30 天" />
      </el-select>
      <el-button :icon="Refresh" circle @click="load" />
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">同期群规模</div><div class="value">{{ data.cohortSize || 0 }}</div></div>
      <div class="stat-card"><div class="label">次日留存</div><div class="value">{{ dayRate(1) }}<small> %</small></div></div>
      <div class="stat-card"><div class="label">7日留存</div><div class="value">{{ dayRate(7) }}<small> %</small></div></div>
      <div class="stat-card"><div class="label">锚点日期</div><div class="value" style="font-size:20px">{{ data.anchor || '-' }}</div></div>
    </div>

    <div class="card">
      <div class="card-title">留存曲线</div>
      <BaseChart :option="chartOption" height="340px" />
    </div>

    <div class="card">
      <div class="card-title">留存明细</div>
      <el-table :data="data.retention || []" size="small" stripe>
        <el-table-column prop="day" label="第 N 日" width="100" />
        <el-table-column prop="date" label="日期" width="150" />
        <el-table-column prop="retained" label="留存用户" width="120" />
        <el-table-column label="留存率" min-width="220">
          <template #default="{ row }">
            <el-progress :percentage="Number(row.rate)" :stroke-width="14" text-inside />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import BaseChart from '../components/BaseChart.vue'
import { analyticsApi } from '../api'
import { useAppStore } from '../store/app'
import { fmtDate } from '../utils/date'

const appStore = useAppStore()
const data = reactive({ cohortSize: 0, anchor: '', retention: [] })
const days = ref(7)
function defaultAnchor() {
  const d = new Date(); d.setDate(d.getDate() - days.value)
  return fmtDate(d)
}
const anchor = ref(defaultAnchor())
const loading = ref(false)

function dayRate(n) {
  const row = (data.retention || []).find((r) => r.day === n)
  return row ? row.rate : 0
}

async function load() {
  loading.value = true
  try {
    const res = await analyticsApi.retention({ appId: appStore.currentAppId, from: anchor.value, days: days.value })
    Object.assign(data, res)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}

const chartOption = computed(() => ({
  tooltip: { trigger: 'axis', formatter: (p) => `第 ${p[0].name} 日<br/>留存率 ${p[0].value}%` },
  grid: { left: 50, right: 30, top: 30, bottom: 40 },
  xAxis: { type: 'category', data: (data.retention || []).map((r) => r.day) },
  yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' } },
  series: [{
    type: 'line', smooth: true, areaStyle: { opacity: .18 }, itemStyle: { color: '#2b5cff' },
    data: (data.retention || []).map((r) => r.rate), label: { show: true, formatter: '{c}%' }
  }]
}))

watch(() => appStore.currentAppId, load)
onMounted(async () => { await appStore.load(); load() })
</script>
