<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">点击热力图</h2>
    <p class="page-sub">聚合页面点击坐标，直观呈现用户注意力分布</p>

    <div class="toolbar">
      <span class="muted">页面</span>
      <el-select v-model="path" style="width: 240px" @change="onPathChange" filterable>
        <el-option v-for="p in paths" :key="p" :label="p" :value="p" />
      </el-select>
      <RangeBar @change="onRange" />
      <el-button :icon="Refresh" :loading="shotLoading" :disabled="!canReal" @click="reloadSnapshot"
        title="重新抓取真实页面截图">重拍快照</el-button>
    </div>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">点击总数</div><div class="value">{{ data.total || 0 }}</div></div>
      <div class="stat-card"><div class="label">热区数量</div><div class="value">{{ cells.length }}</div></div>
      <div class="stat-card"><div class="label">最热元素</div><div class="value mono" style="font-size:18px">{{ topPoints[0]?.element || '-' }}</div><div class="sub-label">热度 {{ topPoints[0]?.density || 0 }} · {{ topPoints[0]?.count || 0 }} 次点击</div></div>
      <div class="stat-card"><div class="label">当前页面</div><div class="value mono" style="font-size:18px">{{ path || '-' }}</div></div>
    </div>

    <div class="card heatmap-card">
      <div class="card-title">
        <span>
          点击分布 ·
          <el-tag v-if="mode === 'real'" type="success" size="small" effect="plain">真实页面 {{ pageW }} × {{ pageH }}</el-tag>
          <el-tag v-else type="info" size="small" effect="plain">模拟视口 {{ pageW }} × {{ pageH }}</el-tag>
          <span v-if="mode === 'real' && data.url" class="hm-url mono">{{ data.url }}</span>
        </span>
        <span class="hm-legend"><i class="dot cold"></i>低密度<i class="bar"></i><i class="dot hot"></i>高密度</span>
      </div>

      <!-- real-page overlay: screenshot + absolutely-positioned heat blobs -->
      <div v-if="mode === 'real' && shotUrl" class="shot-viewport">
        <div class="shot-wrap" :style="{ width: shotDisplayWidth + 'px' }">
          <img :src="shotUrl" class="shot-img" alt="页面快照" draggable="false" />
          <div
            v-for="(c, i) in normCells" :key="i"
            class="heat-blob"
            :style="{
              left: (c.cnx * 100) + '%',
              top: (c.cny * 100) + '%',
              width: c.px + 'px', height: c.px + 'px',
              background: blobGradient(c.density / maxDensity)
            }"
            :title="`${c.element} · 点击 ${c.count} 次 · 热度 ${c.density}`"
          ></div>
        </div>
        <p v-if="shotError" class="shot-note">{{ shotError }}</p>
      </div>

      <!-- fallback: abstract ECharts canvas -->
      <template v-else>
        <p v-if="fallbackReason" class="shot-note">{{ fallbackReason }}</p>
        <BaseChart :option="option" :height="chartHeight" />
      </template>
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
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import RangeBar from '../components/RangeBar.vue'
import BaseChart from '../components/BaseChart.vue'
import { analyticsApi } from '../api'
import { useAppStore } from '../store/app'
import { lastNDays } from '../utils/date'

const appStore = useAppStore()
const data = reactive({ total: 0, points: [], url: null, hasGeometry: false, page: { docW: 1440, docH: 900 }, viewport: { w: 1440, h: 900 } })
const paths = ref(['/'])
const path = ref('/')
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })
const loading = ref(false)

// real-page snapshot overlay state
const mode = ref('sim')            // 'real' | 'sim'
const shotUrl = ref(null)          // object URL of the PNG blob
const shotLoading = ref(false)
const shotError = ref('')          // note shown under a real snapshot
const fallbackReason = ref('')     // note shown above the abstract canvas
const shotDisplayWidth = ref(720)  // rendered width of the screenshot (px)

// ---- density aggregation -------------------------------------------------
// Clicks are binned into a CELL×CELL grid over the real page dimensions, summed
// per cell, then smoothed with a 3×3 neighbourhood so overlapping regions build
// genuine hot/cold gradients. Absolute document coords (clientX+scrollX) mean the
// same cells drive both the abstract canvas and the real-screenshot overlay.
const CELL = 48
const pageW = computed(() => Math.max(320, data.page?.docW || 1440))
const pageH = computed(() => Math.max(320, data.page?.docH || 900))
const canReal = computed(() => !!data.url && !!data.hasGeometry)

const cells = computed(() => {
  const pts = data.points || []
  if (!pts.length) return []
  const COLS = Math.ceil(pageW.value / CELL)
  const ROWS = Math.ceil(pageH.value / CELL)
  const grid = Array.from({ length: ROWS }, () => new Array(COLS).fill(0))
  const elems = Array.from({ length: ROWS }, () => Array.from({ length: COLS }, () => ({})))
  for (const p of pts) {
    const cx = Math.min(COLS - 1, Math.max(0, Math.floor((p.x || 0) / CELL)))
    const cy = Math.min(ROWS - 1, Math.max(0, Math.floor((p.y || 0) / CELL)))
    grid[cy][cx] += (p.count || 1)
    const e = p.element || '未知元素'
    elems[cy][cx][e] = (elems[cy][cx][e] || 0) + (p.count || 1)
  }
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
      const em = elems[y][x]
      const element = Object.keys(em).sort((a, b) => em[b] - em[a])[0] || '未知元素'
      const px = x * CELL + CELL / 2
      const py = y * CELL + CELL / 2
      out.push({
        x: px, y: py,
        cnx: px / pageW.value, cny: py / pageH.value,
        count: grid[y][x], density: Math.round(density), element
      })
    }
  }
  return out
})

const maxDensity = computed(() => Math.max(1, ...cells.value.map((c) => c.density)))
const topPoints = computed(() => [...cells.value].sort((a, b) => b.density - a.density).slice(0, 30))

// overlay blobs: size grows with density, expressed in rendered-image pixels
const normCells = computed(() => {
  const max = maxDensity.value
  const scale = shotDisplayWidth.value / pageW.value   // page px -> rendered px
  return cells.value.map((c) => ({
    ...c,
    px: Math.max(18, Math.min(120, (26 + (c.density / max) * 70) * Math.max(0.4, scale)))
  }))
})

// map a 0..1 ratio to the cold→hot ramp
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
function blobGradient(t) {
  const c = heatColor(t).match(/\d+/g).map(Number)
  const [r, g, b] = c
  return `radial-gradient(circle, rgba(${r},${g},${b},0.72) 0%, rgba(${r},${g},${b},0.34) 42%, rgba(${r},${g},${b},0) 72%)`
}

const chartHeight = computed(() => {
  // keep the abstract canvas at the real page aspect ratio, capped for sanity
  return Math.max(420, Math.min(900, Math.round(540 * pageH.value / pageW.value))) + 'px'
})

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
    data.points = res.points || []
    data.url = res.url || null
    data.hasGeometry = !!res.hasGeometry
    data.page = res.page || { docW: 1440, docH: 900 }
    data.viewport = res.viewport || { w: 1440, h: 900 }
    await resolveSnapshot()
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}

// Decide real vs fallback and fetch the PNG when possible.
async function resolveSnapshot() {
  releaseShot()
  shotError.value = ''
  fallbackReason.value = ''
  if (!data.url || !data.hasGeometry) {
    mode.value = 'sim'
    fallbackReason.value = !data.hasGeometry
      ? '当前时间范围内暂无带页面几何的新版采集数据（需 SDK ≥ 1.2.0 重新采集），已回退到模拟视口。'
      : '该页面暂无可识别的真实 URL，已回退到模拟视口。'
    return
  }
  await fetchShot()
}

async function fetchShot() {
  shotLoading.value = true
  try {
    const blob = await analyticsApi.snapshot({ appId: appStore.currentAppId, path: path.value, from: range.from, to: range.to })
    if (blob && blob.size) {
      shotUrl.value = URL.createObjectURL(blob)
      mode.value = 'real'
      shotError.value = ''
    } else {
      throw new Error('empty')
    }
  } catch (e) {
    mode.value = 'sim'
    shotUrl.value = null
    const reason = e?.response?.status === 404 ? '真实页面快照不可用（站点不可达 / 禁止抓取 / 内网地址已拦截 / 未安装无头浏览器），已回退到模拟视口。' : '快照获取失败，已回退到模拟视口。'
    fallbackReason.value = reason
  } finally {
    shotLoading.value = false
  }
}

function reloadSnapshot() {
  // force a fresh capture by busting the cache-buster isn't needed; server TTL governs.
  // Just re-request; if the browser is available it re-shoots when cache is stale.
  fetchShot()
}

function releaseShot() {
  if (shotUrl.value) { URL.revokeObjectURL(shotUrl.value); shotUrl.value = null }
}

function onPathChange() { load() }
function onRange(r) { range.from = r.from; range.to = r.to; loadPaths().then(load) }

const option = computed(() => {
  const pts = cells.value.map((c) => ({ value: [c.x, c.y, c.density], name: c.element, raw: c.count }))
  const max = maxDensity.value
  const W = pageW.value, H = pageH.value
  const bands = [
    { name: '顶部导航', y0: 0, y1: H * 0.1 },
    { name: 'Banner 区', y0: H * 0.1, y1: H * 0.36 },
    { name: '主内容区', y0: H * 0.36, y1: H * 0.8 },
    { name: '页脚', y0: H * 0.8, y1: H }
  ]
  const markAreaData = bands.map((b, i) => ([
    { yAxis: b.y0, itemStyle: { color: i % 2 ? 'rgba(43,92,255,0.025)' : 'rgba(43,92,255,0.05)' } },
    { yAxis: b.y1 }
  ]))
  const markLineData = bands.slice(1).map((b) => ({
    yAxis: b.y0, label: { show: false },
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
    xAxis: { type: 'value', min: 0, max: W, show: false },
    yAxis: { type: 'value', min: 0, max: H, inverse: true, show: false, splitLine: { show: false } },
    graphic: bands.map((b) => ({
      type: 'text', left: 6, top: `${((b.y0 + b.y1) / 2 / H * 100).toFixed(1)}%`,
      style: { text: b.name, fill: '#b0b6c0', fontSize: 11, textAlign: 'left' },
      z: 5, silent: true
    })),
    series: [
      {
        type: 'scatter', coordinateSystem: 'cartesian2d', z: 1,
        symbolSize: (v) => 34 + Math.min(64, (v[2] / max) * 64),
        itemStyle: { opacity: 0.2, shadowBlur: 26, shadowColor: 'rgba(245,34,45,0.3)' },
        data: pts, silent: true,
        markArea: { silent: true, data: markAreaData },
        markLine: { silent: true, symbol: 'none', data: markLineData }
      },
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
onBeforeUnmount(releaseShot)
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
.hm-url { margin-left: 8px; font-size: 12px; color: #9aa2af; font-weight: 400; }

/* real-page overlay */
.shot-viewport { display: flex; flex-direction: column; align-items: center; gap: 10px; }
.shot-wrap { position: relative; line-height: 0; border: 1px solid #eef0f4; border-radius: 8px; overflow: hidden;
  box-shadow: 0 2px 14px rgba(20,30,60,0.06); background: #fff; }
.shot-img { display: block; width: 100%; height: auto; user-select: none; }
.heat-blob { position: absolute; transform: translate(-50%, -50%); border-radius: 50%;
  pointer-events: auto; mix-blend-mode: multiply; }
.shot-note { font-size: 12.5px; color: #b26a00; background: #fff7e8; border: 1px solid #ffe2b0;
  padding: 7px 12px; border-radius: 8px; margin: 4px 0 0; }

/* detail table heat column */
.heat-cell { display: flex; align-items: center; gap: 8px; }
.heat-cell :deep(.el-progress) { flex: 1; min-width: 0; }
.heat-val { font-size: 12px; color: #6b7280; min-width: 28px; text-align: right; font-variant-numeric: tabular-nums; }
</style>
