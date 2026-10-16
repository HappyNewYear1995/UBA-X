<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">页面分析</h2>
    <p class="page-sub">页面浏览量、访客数与平均停留时长排行</p>

    <RangeBar @change="onRange" />

    <div class="card">
      <div class="card-title">页面浏览 TOP</div>
      <BaseChart :option="chartOption" :height="Math.max(280, rows.length * 32) + 'px'" />
    </div>

    <div class="card">
      <div class="card-title">页面明细</div>
      <el-table :data="rows" size="small" stripe>
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="path" label="页面路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
        <el-table-column prop="pv" label="浏览量 PV" width="120" sortable />
        <el-table-column prop="uv" label="访客 UV" width="110" sortable />
        <el-table-column label="平均停留" width="120">
          <template #default="{ row }">{{ row.avgDuration }} 秒</template>
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
const rows = ref([])
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    rows.value = await analyticsApi.pages({ appId: appStore.currentAppId, from: range.from, to: range.to, limit: 30 })
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const chartOption = computed(() => {
  const data = rows.value.slice(0, 15).reverse()
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 140, right: 40, top: 20, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map((d) => d.path) },
    series: [{ type: 'bar', data: data.map((d) => d.pv), itemStyle: { color: '#2b5cff', borderRadius: [0, 4, 4, 0] }, label: { show: true, position: 'right' } }]
  }
})

watch(() => appStore.currentAppId, load)
onMounted(async () => { await appStore.load(); load() })
</script>
