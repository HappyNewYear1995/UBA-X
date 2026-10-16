<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">异常预警</h2>
    <p class="page-sub">对 PV / UV / 事件量做基线学习，自动标记突增突降异常日</p>

    <RangeBar :initial-days="30" @change="onRange">
      <el-radio-group v-model="metric" size="default" @change="load">
        <el-radio-button value="events">事件量</el-radio-button>
        <el-radio-button value="pv">PV</el-radio-button>
        <el-radio-button value="uv">UV</el-radio-button>
      </el-radio-group>
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">数据点</div><div class="value">{{ data.summary.points || 0 }}</div></div>
      <div class="stat-card"><div class="label">基线均值</div><div class="value">{{ compact(data.summary.mean) }}</div></div>
      <div class="stat-card"><div class="label">标准差</div><div class="value">{{ compact(data.summary.stddev) }}</div></div>
      <div class="stat-card"><div class="label">最新值</div><div class="value">{{ compact(data.summary.latest) }}</div></div>
      <div class="stat-card"><div class="label">异常点</div><div class="value" :style="{ color: (data.summary.anomalies||0) ? '#f56c6c' : '#00c2a8' }">{{ data.summary.anomalies || 0 }}</div></div>
      <div class="stat-card"><div class="label">告警等级</div><div class="value" :class="levelClass">{{ data.summary.level || '—' }}</div></div>
    </div>

    <div class="card">
      <div class="card-title">指标趋势与异常标记</div>
      <BaseChart :option="lineOption" height="340px" />
    </div>

    <div class="card">
      <div class="card-title">异常明细</div>
      <el-table :data="data.anomalies" size="small">
        <el-table-column prop="date" label="日期" width="140" />
        <el-table-column label="方向" width="100">
          <template #default="{ row }"><el-tag size="small" :type="row.direction === '激增' ? 'danger' : 'warning'">{{ row.direction }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="value" label="实际值" width="110" />
        <el-table-column prop="expected" label="基线值" width="110" />
        <el-table-column label="偏离" width="110"><template #default="{ row }">{{ row.sigma }} σ</template></el-table-column>
        <el-table-column label="环比" min-width="120">
          <template #default="{ row }">
            <span :style="{ color: row.changePct >= 0 ? '#f56c6c' : '#00c2a8' }">{{ row.changePct >= 0 ? '+' : '' }}{{ row.changePct }}%</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!(data.anomalies || []).length" description="当前区间未检测到异常" :image-size="60" />
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
const metric = ref('events')
const data = reactive({ summary: {}, series: [], anomalies: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.anomaly({ appId: appStore.currentAppId, from: range.from, to: range.to, metric: metric.value })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const levelClass = computed(() => ({ '高': 'lv-high', '中': 'lv-mid', '正常': 'lv-low' }[data.summary.level] || ''))

const lineOption = computed(() => {
  const dates = (data.series || []).map((s) => s.date.slice(5))
  const values = (data.series || []).map((s) => s.value)
  const mean = data.summary.mean || 0
  const anomalyDates = new Set((data.anomalies || []).map((a) => a.date.slice(5)))
  const markData = (data.series || [])
    .filter((s) => anomalyDates.has(s.date.slice(5)))
    .map((s) => ({ coord: [s.date.slice(5), s.value], value: s.value }))
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 24, top: 30, bottom: 40 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value' },
    series: [{
      type: 'line', smooth: true, data: values, symbolSize: 6,
      areaStyle: { opacity: 0.12 }, itemStyle: { color: '#2b5cff' },
      markLine: { silent: true, symbol: 'none', data: [{ yAxis: mean, label: { formatter: '基线 ' + mean }, lineStyle: { color: '#909399', type: 'dashed' } }] },
      markPoint: { data: markData, symbol: 'pin', symbolSize: 44, itemStyle: { color: '#f56c6c' }, label: { color: '#fff', fontSize: 11 } }
    }]
  }
})

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.lv-high { color: #f56c6c; }
.lv-mid { color: #e6a23c; }
.lv-low { color: #67c23a; }
</style>
