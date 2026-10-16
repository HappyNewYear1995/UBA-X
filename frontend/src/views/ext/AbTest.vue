<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">A/B 实验</h2>
    <p class="page-sub">将用户按稳定哈希分为 A / B 两组，对比核心指标差异并做显著性检验</p>

    <RangeBar :initial-days="30" @change="onRange">
      <el-button type="primary" :icon="Refresh" @click="load">查询</el-button>
    </RangeBar>

    <div class="card builder">
      <div class="b-row">
        <span class="b-label">实验指标</span>
        <el-select v-model="metric" style="width: 200px" @change="load">
          <el-option v-for="m in METRICS" :key="m.value" :label="m.label" :value="m.value" />
        </el-select>
        <span class="b-hint">{{ metricHint }}</span>
      </div>
    </div>

    <div class="ab-cards">
      <div class="ab-card a">
        <div class="ab-name">{{ s.variantA?.name || '变体 A' }}</div>
        <div class="ab-metric">
          <span class="ab-big">{{ primaryA }}</span>
          <span class="ab-unit">{{ unitLabel }}</span>
        </div>
        <div class="ab-sub">样本用户 {{ compact(s.variantA?.users) }}<template v-if="isProportion"> · 转化 {{ compact(s.variantA?.conversions) }}</template></div>
      </div>
      <div class="ab-vs">
        <div class="ab-lift" :class="liftClass">{{ liftText }}</div>
        <div class="ab-lift-label">B 相对 A 提升</div>
        <el-tag :type="s.significant ? 'success' : 'info'" effect="dark" round size="small" style="margin-top:8px">
          {{ s.significant ? '差异显著' : '差异不显著' }}
        </el-tag>
      </div>
      <div class="ab-card b">
        <div class="ab-name">{{ s.variantB?.name || '变体 B' }}</div>
        <div class="ab-metric">
          <span class="ab-big">{{ primaryB }}</span>
          <span class="ab-unit">{{ unitLabel }}</span>
        </div>
        <div class="ab-sub">样本用户 {{ compact(s.variantB?.users) }}<template v-if="isProportion"> · 转化 {{ compact(s.variantB?.conversions) }}</template></div>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">检验方法</div><div class="value small">{{ testName }}</div></div>
      <div class="stat-card"><div class="label">{{ s.statName || 'z' }} 统计量</div><div class="value">{{ fmt(s.stat) }}</div></div>
      <div class="stat-card"><div class="label">P 值</div><div class="value">{{ fmt(s.pValue, 4) }}</div></div>
      <div class="stat-card"><div class="label">置信水平</div><div class="value">{{ s.confidence || '95%' }}</div></div>
      <div class="stat-card"><div class="label">胜出方</div><div class="value small">{{ s.winner || '无' }}</div></div>
    </div>

    <div class="card conclusion">
      <div class="card-title">实验结论</div>
      <p class="concl-text">{{ s.conclusion || '—' }}</p>
    </div>

    <div class="card">
      <div class="card-title">每日{{ s.metricLabel || '指标' }}走势（A vs B）</div>
      <BaseChart :option="dailyOption" height="340px" />
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
const metric = ref('purchase')
const data = reactive({ summary: {}, daily: [] })
const loading = ref(false)

const METRICS = [
  { value: 'purchase', label: '购买转化率' },
  { value: 'add_to_cart', label: '加购转化率' },
  { value: 'begin_checkout', label: '结算转化率' },
  { value: 'sign_up', label: '注册转化率' },
  { value: 'order_value', label: '客单价' },
  { value: 'engagement', label: '人均事件数' }
]
const PROPORTION = new Set(['purchase', 'add_to_cart', 'begin_checkout', 'sign_up'])

const s = computed(() => data.summary || {})
const isProportion = computed(() => s.value.type === 'proportion')
const unitLabel = computed(() => (isProportion.value ? '%' : (s.value.metric === 'order_value' ? '元' : '次')))
const metricHint = computed(() => {
  const m = METRICS.find((x) => x.value === metric.value)
  return isProportion.value
    ? `比较两组「${m?.label}」的比例差异（双比例 z 检验）`
    : `比较两组「${m?.label}」的均值差异（Welch t 检验）`
})
const testName = computed(() => (isProportion.value ? '双比例 z 检验' : 'Welch t 检验'))

const primaryA = computed(() => (isProportion.value ? fmt(s.value.variantA?.rate) : fmt(s.value.variantA?.mean)))
const primaryB = computed(() => (isProportion.value ? fmt(s.value.variantB?.rate) : fmt(s.value.variantB?.mean)))
const liftText = computed(() => {
  const l = s.value.lift
  if (l == null) return '—'
  return (l > 0 ? '+' : '') + Number(l).toFixed(1) + '%'
})
const liftClass = computed(() => {
  const l = s.value.lift
  if (l == null) return ''
  return l > 0 ? 'up' : l < 0 ? 'down' : ''
})

function fmt(v, digits = 2) {
  if (v == null || v === '') return '—'
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(digits) : String(v)
}

async function load() {
  loading.value = true
  try {
    const d = await extApi.abTest({ appId: appStore.currentAppId, from: range.from, to: range.to, metric: metric.value })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const dailyOption = computed(() => {
  const days = (data.daily || []).map((d) => d.date)
  return {
    tooltip: { trigger: 'axis' },
    legend: { top: 0, data: ['变体 A', '变体 B'] },
    grid: { left: 50, right: 24, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: days, axisLabel: { rotate: days.length > 12 ? 40 : 0 } },
    yAxis: { type: 'value' },
    series: [
      { name: '变体 A', type: 'line', smooth: true, showSymbol: false, lineStyle: { width: 2.5 }, itemStyle: { color: '#2b5cff' }, areaStyle: { color: 'rgba(43,92,255,.08)' }, data: (data.daily || []).map((d) => Number(d.a)) },
      { name: '变体 B', type: 'line', smooth: true, showSymbol: false, lineStyle: { width: 2.5 }, itemStyle: { color: '#e6703c' }, areaStyle: { color: 'rgba(230,112,60,.08)' }, data: (data.daily || []).map((d) => Number(d.b)) }
    ]
  }
})

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.builder { padding: 14px 16px; }
.b-row { display: flex; gap: 16px; flex-wrap: wrap; align-items: center; }
.b-label { font-size: 13px; color: #5a6270; font-weight: 500; }
.b-hint { font-size: 12px; color: #9aa2af; }
.value.small { font-size: 15px; }

.ab-cards { display: grid; grid-template-columns: 1fr auto 1fr; gap: 14px; align-items: stretch; margin-bottom: 16px; }
.ab-card { border-radius: 12px; padding: 18px 20px; color: #fff; }
.ab-card.a { background: linear-gradient(135deg, #4b7bff, #2b5cff); }
.ab-card.b { background: linear-gradient(135deg, #f0a020, #e6703c); }
.ab-name { font-size: 13px; opacity: .9; }
.ab-metric { display: flex; align-items: baseline; gap: 6px; margin: 8px 0 6px; }
.ab-big { font-size: 34px; font-weight: 700; line-height: 1; }
.ab-unit { font-size: 14px; opacity: .9; }
.ab-sub { font-size: 12px; opacity: .88; }
.ab-vs { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 0 22px; min-width: 130px; }
.ab-lift { font-size: 28px; font-weight: 700; color: #5a6270; }
.ab-lift.up { color: #00a870; }
.ab-lift.down { color: #f56c6c; }
.ab-lift-label { font-size: 12px; color: #9aa2af; margin-top: 2px; }

.conclusion { padding: 16px 18px; }
.concl-text { margin: 6px 0 0; font-size: 14px; color: #485061; line-height: 1.7; }
</style>
