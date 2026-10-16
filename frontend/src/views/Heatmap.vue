<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">点击热力图</h2>
    <p class="page-sub">聚合页面点击坐标，直观呈现用户注意力分布</p>

    <div class="toolbar">
      <span class="muted">页面</span>
      <el-select v-model="path" style="width: 240px" @change="load" filterable>
        <el-option v-for="p in paths" :key="p" :label="p" :value="p" />
      </el-select>
      <RangeBar @change="onRange" />
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">点击总数</div><div class="value">{{ data.total || 0 }}</div></div>
      <div class="stat-card"><div class="label">热区数量</div><div class="value">{{ cells.length }}</div></div>
      <div class="stat-card"><div class="label">最热元素</div><div class="value mono" style="font-size:18px">{{ topPoints[0]?.element || '-' }}</div><div class="sub-label">热度 {{ topPoints[0]?.density || 0 }} · {{ topPoints[0]?.count || 0 }} 次点击</div></div>
      <div class="stat-card"><div class="label">当前页面</div><div class="value mono" style="font-size:18px">{{ path || '-' }}</div></div>
    </div>

    <div class="card heatmap-card">
      <div class="card-title">
        <span>点击分布 · 模拟页面视口 1440 × 900</span>
        <span class="hm-legend"><i class="dot cold"></i>低密度<i class="bar"></i><i class="dot hot"></i>高密度</span>
      </div>
      <BaseChart :option="option" height="540px" />
    </div>

    <div class="card">
      <div class="card-title">热区明细 TOP</div>
      <el-table :data="topPoints" size="small" stripe>
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="element" label="主元素" min-width="180" />
        <el-table-column label="热区中心" width="150">
          <template #default="{ row }">({{ Math.round(row.x) }}, {{ Math.round(row.y) }})</template>
        </el-table-column>
        <el-table-column prop="count" label="点击次数" width="110" sortable />
        <el-table-column label="热度" min-width="180" sortable :sort-by="'density'">
          <template #default="{ row }">
            <div class="heat-cell">
              <el-progress :percentage="Math.round(row.density / maxDensity * 100)" :stroke-width="12" text-inside
                :color="heatColor(row.density / maxDensity)" />
              <span class="heat-val">{{ row.density }}</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import RangeBar from '../components/RangeBar.vue'
import BaseChart from '../components/BaseChart.vue'
import { analyticsApi } from '../api'
import { useAppStore } from '../store/app'
import { lastNDays } from '../utils/date'

const appStore = useAppStore()
const data = reactive({ total: 0, points: [] })
const paths = ref(['/'])
const path = ref('/')
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })
const loading = ref(false)

// ---- density aggregation -------------------------------------------------
// Raw clicks come back as one point per click (count≈1 each), so colouring by
// raw count yields a flat, single-colour scatter — not a heatmap. We bin clicks
// into a CELL×CELL grid, sum per-cell hits, then smooth with a 3×3 neighbourhood
// so overlapping regions accumulate into genuine hot/cold gradients.
const CELL = 48
const COLS = Math.ceil(1440 / CELL)
const ROWS = Math.ceil(900 / CELL)

const cells = computed(() => {
  const pts = data.points || []
  if (!pts.length) return []
  // raw grid accumulation
  const grid = Array.from({ length: ROWS }, () => new Array(COLS).fill(0))
  const elems = Array.from({ length: ROWS }, () => Array.from({ length: COLS }, () => ({})))
  for (const p of pts) {
    const cx = Math.min(COLS - 1, Math.max(0, Math.floor(p.x / CELL)))
    const cy = Math.min(ROWS - 1, Math.max(0, Math.floor(p.y / CELL)))
    grid[cy][cx] += (p.count || 1)
    const e = p.element || '未知元素'
    elems[cy][cx][e] = (elems[cy][cx][e] || 0) + (p.count || 1)
  }
  // 3×3 neighbourhood smoothing (centre weighted) → density field
  const out = []
  for (let y = 0; y < ROWS; y++) {
    for (let x = 0; x < COLS; x++) {
      if (!grid[y][x]) continue
      let density = 0
      for (let dy = -1; dy <= 1; dy++) {
        for (let dx = -1; dx <= 1; dx++) {
          const ny = y + dy, nx = x + dx
          if (ny < 0 || ny >= ROWS || nx < 0 || nx >= COLS) continue
          density += grid[ny][nx] * (dx === 0 && dy === 0 ? 1 : 0.45)
        }
      }
      // dominant element in this cell
      const em = elems[y][x]
      const element = Object.keys(em).sort((a, b) => em[b] - em[a])[0] || '未知元素'
      out.push({
        x: x * CELL + CELL / 2,
        y: y * CELL + CELL / 2,
        count: grid[y][x],
        density: Math.round(density),
        element
      })
    }
  }
  return out
})

const maxDensity = computed(() => Math.max(1, ...cells.value.map((c) => c.density)))
const topPoints = computed(() => [...cells.value].sort((a, b) => b.density - a.density).slice(0, 30))

// map a 0..1 ratio to the same cold→hot ramp used by the chart's visualMap
function heatColor(t) {
  const stops = [[43, 92, 255], [0, 194, 168], [255, 230, 109], [255, 159, 67], [245, 34, 45]]
  const clamped = Math.min(1, Math.max(0, t)) * (stops.length - 1)
  const i = Math.floor(clamped)
  const f = clamped - i
  const a = stops[i]
  const b = stops[Math.min(stops.length - 1, i + 1)]
  const c = a.map((v, k) => Math.round(v + (b[k] - v) * f))
  return `rgb(${c[0]},${c[1]},${c[2]})`
}

async function loadPaths() {
  try {
    const pages = await analyticsApi.pages({ appId: appStore.currentAppId, from: range.from, to: range.to, limit: 30 })
    paths.value = pages.map((p) => p.path).filter(Boolean)
    if (!paths.value.includes(path.value) && paths.value.length) path.value = paths.value[0]
  } catch (e) { /* surfaced by http interceptor */ }
}
async function load() {
  loading.value = true
  try {
    const res = await analyticsApi.heatmap({ appId: appStore.currentAppId, path: path.value, from: range.from, to: range.to })
    data.total = res.total
    data.points = res.points
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; loadPaths().then(load) }

const option = computed(() => {
  // one aggregated heat point per occupied grid cell, coloured/sized by density
  const pts = cells.value.map((c) => ({ value: [c.x, c.y, c.density], name: c.element, raw: c.count }))
  const max = maxDensity.value

  // Simulated page sections (y-ranges in the 0..900 viewport) drawn as faint bands
  // so scattered clicks read against a page-like layout instead of an empty canvas.
  const bands = [
    { name: '顶部导航', y0: 0, y1: 90 },
    { name: 'Banner 区', y0: 90, y1: 320 },
    { name: '主内容区', y0: 320, y1: 720 },
    { name: '页脚', y0: 720, y1: 900 }
  ]
  const markAreaData = bands.map((b, i) => ([
    { yAxis: b.y0, itemStyle: { color: i % 2 ? 'rgba(43,92,255,0.025)' : 'rgba(43,92,255,0.05)' } },
    { yAxis: b.y1 }
  ]))
  const markLineData = bands.slice(1).map((b) => ({
    yAxis: b.y0,
    label: { show: false },
    lineStyle: { color: 'rgba(43,92,255,0.12)', type: 'dashed', width: 1 }
  }))

  return {
    animationDuration: 400,
    tooltip: {
      backgroundColor: 'rgba(17,24,39,0.92)', borderWidth: 0, padding: [8, 12],
      textStyle: { color: '#fff', fontSize: 12 },
      formatter: (o) => `<div style="font-weight:600;margin-bottom:3px">${o.data.name || '热区'}</div>
        <div style="opacity:.8">热区中心 (${Math.round(o.value[0])}, ${Math.round(o.value[1])})</div>
        <div style="opacity:.8">点击 <b style="color:#ffe66d">${o.data.raw}</b> 次 · 热度 <b style="color:#ff9f43">${o.value[2]}</b></div>`
    },
    grid: { left: 56, right: 16, top: 12, bottom: 56, backgroundColor: '#fbfcfe', show: true, borderColor: '#eef0f4', borderWidth: 1 },
    visualMap: {
      min: 0, max, dimension: 2, calculable: true, orient: 'horizontal', left: 'center', bottom: 6,
      itemWidth: 12, itemHeight: 130, precision: 0,
      inRange: { color: ['#2b5cff', '#00c2a8', '#ffe66d', '#ff9f43', '#f5222d'] },
      text: ['热', '冷'], textStyle: { color: '#8a9099', fontSize: 11 }
    },
    xAxis: { type: 'value', min: 0, max: 1440, show: false },
    yAxis: { type: 'value', min: 0, max: 900, inverse: true, show: false, splitLine: { show: false } },
    // left-edge section labels
    graphic: bands.map((b) => ({
      type: 'text', left: 6, top: `${((b.y0 + b.y1) / 2 / 900 * 100).toFixed(1)}%`,
      style: { text: b.name, fill: '#b0b6c0', fontSize: 11, textAlign: 'left' },
      z: 5, silent: true
    })),
    series: [
      // soft glow layer — translucent halos that visually merge into heat blobs
      {
        type: 'scatter', coordinateSystem: 'cartesian2d', z: 1,
        symbolSize: (v) => 34 + Math.min(64, (v[2] / max) * 64),
        itemStyle: { opacity: 0.2, shadowBlur: 26, shadowColor: 'rgba(245,34,45,0.3)' },
        data: pts, silent: true,
        markArea: { silent: true, data: markAreaData },
        markLine: { silent: true, symbol: 'none', data: markLineData }
      },
      // crisp core layer — the aggregated heat cells
      {
        type: 'scatter', coordinateSystem: 'cartesian2d', z: 3,
        symbolSize: (v) => 10 + Math.min(26, (v[2] / max) * 26),
        itemStyle: { opacity: 0.85, borderColor: 'rgba(255,255,255,0.65)', borderWidth: 1 },
        emphasis: { focus: 'self', itemStyle: { opacity: 1, borderColor: '#fff', borderWidth: 2 } },
        data: pts
      }
    ]
  }
})

watch(() => appStore.currentAppId, () => loadPaths().then(load))
onMounted(async () => { await appStore.load(); await loadPaths(); load() })
</script>

<style scoped>
.sub-label { color: #8a9099; font-size: 12px; margin-top: 4px; }

.heatmap-card :deep(.card-title) { gap: 12px; }
.hm-legend { display: inline-flex; align-items: center; gap: 6px; font-size: 12px; font-weight: 400; color: #8a9099; }
.hm-legend .dot { width: 9px; height: 9px; border-radius: 50%; display: inline-block; }
.hm-legend .dot.cold { background: #2b5cff; }
.hm-legend .dot.hot { background: #f5222d; }
.hm-legend .bar { width: 60px; height: 6px; border-radius: 3px; display: inline-block;
  background: linear-gradient(90deg, #2b5cff, #00c2a8, #ffe66d, #ff9f43, #f5222d); }

/* detail table heat column: progress bar + numeric density side by side */
.heat-cell { display: flex; align-items: center; gap: 8px; }
.heat-cell :deep(.el-progress) { flex: 1; min-width: 0; }
.heat-val { font-size: 12px; color: #6b7280; min-width: 28px; text-align: right; font-variant-numeric: tabular-nums; }
</style>
