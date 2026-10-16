<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">事件分析</h2>
    <p class="page-sub">自定义与预置事件的触发次数与触达用户数</p>

    <RangeBar @change="onRange">
      <el-input v-model="keyword" placeholder="搜索事件名" clearable style="width: 200px" :prefix-icon="Search" />
    </RangeBar>

    <div class="card">
      <div class="card-title">事件触发排行</div>
      <BaseChart :option="chartOption" :height="Math.max(280, filtered.length * 34) + 'px'" />
    </div>

    <div class="card">
      <div class="card-title">事件明细</div>
      <el-table :data="filtered" size="small" stripe>
        <el-table-column type="index" label="#" width="55" />
        <el-table-column prop="event" label="事件名" min-width="180" />
        <el-table-column prop="count" label="触发次数" width="130" sortable />
        <el-table-column prop="users" label="触发用户数" width="130" sortable />
        <el-table-column label="人均次数" width="120">
          <template #default="{ row }">{{ row.users ? (row.count / row.users).toFixed(2) : '-' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import RangeBar from '../components/RangeBar.vue'
import BaseChart from '../components/BaseChart.vue'
import { analyticsApi } from '../api'
import { useAppStore } from '../store/app'
import { lastNDays } from '../utils/date'

const appStore = useAppStore()
const rows = ref([])
const keyword = ref('')
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })
const loading = ref(false)

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  const data = k ? rows.value.filter((r) => (r.event || '').toLowerCase().includes(k)) : rows.value
  return data
})

async function load() {
  loading.value = true
  try {
    rows.value = await analyticsApi.events({ appId: appStore.currentAppId, from: range.from, to: range.to, limit: 50 })
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

const chartOption = computed(() => {
  const data = filtered.value.slice(0, 20).reverse()
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['触发次数', '用户数'] },
    grid: { left: 130, right: 40, top: 30, bottom: 20 },
    xAxis: { type: 'value' },
    yAxis: { type: 'category', data: data.map((d) => d.event) },
    series: [
      { name: '触发次数', type: 'bar', data: data.map((d) => d.count), itemStyle: { color: '#2b5cff', borderRadius: [0, 4, 4, 0] } },
      { name: '用户数', type: 'bar', data: data.map((d) => d.users), itemStyle: { color: '#00c2a8', borderRadius: [0, 4, 4, 0] } }
    ]
  }
})

watch(() => appStore.currentAppId, load)
onMounted(async () => { await appStore.load(); load() })
</script>
