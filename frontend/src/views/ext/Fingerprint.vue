<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">客户端指纹</h2>
    <p class="page-sub">基于 设备 / 系统 / 浏览器 / 分辨率 组合识别客户端环境分布</p>

    <RangeBar @change="onRange">
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">事件量</div><div class="value">{{ compact(data.summary.events) }}</div></div>
      <div class="stat-card"><div class="label">用户数</div><div class="value">{{ compact(data.summary.users) }}</div></div>
      <div class="stat-card"><div class="label">指纹组合</div><div class="value">{{ compact(data.summary.fingerprints) }}</div></div>
      <div class="stat-card"><div class="label">稀有指纹(≤1人)</div><div class="value">{{ data.summary.rareFingerprints || 0 }}</div></div>
      <div class="stat-card"><div class="label">TOP10 用户覆盖</div><div class="value">{{ data.summary.top10Coverage || 0 }}<small> %</small></div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">操作系统分布</div>
        <BaseChart :option="pieOption(data.os)" height="280px" />
      </div>
      <div class="card">
        <div class="card-title">浏览器分布</div>
        <BaseChart :option="pieOption(data.browser)" height="280px" />
      </div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">屏幕分辨率分布</div>
        <BaseChart :option="barOption(data.screen)" height="280px" />
      </div>
      <div class="card">
        <div class="card-title">设备类型分布</div>
        <BaseChart :option="pieOption(data.device)" height="280px" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">TOP 指纹组合</div>
      <el-table :data="data.top" size="small">
        <el-table-column prop="fp" label="指纹（设备 · 系统 · 浏览器 · 分辨率）" min-width="280" show-overflow-tooltip />
        <el-table-column prop="users" label="用户数" width="90" />
        <el-table-column prop="events" label="事件数" width="90" />
        <el-table-column label="用户占比" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.share || 0" :stroke-width="10" />
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import RangeBar from '../../components/RangeBar.vue'
import BaseChart from '../../components/BaseChart.vue'
import { extApi } from '../../api'
import { useAppStore } from '../../store/app'
import { compact, lastNDays } from '../../utils/date'

const appStore = useAppStore()
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })
const data = reactive({ summary: {}, top: [], os: [], browser: [], screen: [], device: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.fingerprint({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

function pieOption(list) {
  return {
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{
      type: 'pie', radius: ['40%', '65%'], center: ['50%', '45%'],
      data: (list || []).map((d) => ({ name: d.name, value: d.value })),
      label: { formatter: '{b}: {d}%' }
    }]
  }
}
function barOption(list) {
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 90, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: (list || []).map((d) => d.name).reverse() },
    series: [{ type: 'bar', data: (list || []).map((d) => d.value).reverse(), itemStyle: { color: '#6a3cff', borderRadius: [0, 3, 3, 0] } }]
  }
}

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>
