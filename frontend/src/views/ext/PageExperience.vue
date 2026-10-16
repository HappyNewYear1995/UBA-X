<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">页面性能与体验分析</h2>
    <p class="page-sub">统计各页面停留时长、跳出率与退出情况，定位体验薄弱环节</p>

    <RangeBar :initial-days="14" @change="onRange">
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">页面数</div><div class="value">{{ data.summary.pages || 0 }}</div></div>
      <div class="stat-card"><div class="label">平均停留</div><div class="value">{{ data.summary.avgDwell || 0 }}<small> s</small></div></div>
      <div class="stat-card"><div class="label">整体跳出率</div><div class="value">{{ data.summary.bounceRate || 0 }}<small> %</small></div></div>
      <div class="stat-card"><div class="label">最热页面</div><div class="value small">{{ data.summary.topPage || '—' }}</div></div>
      <div class="stat-card"><div class="label">最粘页面</div><div class="value small">{{ data.summary.mostEngaging || '—' }}</div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">页面 PV TOP</div>
        <BaseChart :option="pvOption" height="320px" />
      </div>
      <div class="card">
        <div class="card-title">平均停留时长（秒）</div>
        <BaseChart :option="dwellOption" height="320px" />
      </div>
    </div>

    <div class="card">
      <div class="card-title">页面明细</div>
      <el-table :data="data.pages" size="small">
        <el-table-column prop="path" label="页面路径" min-width="160" show-overflow-tooltip />
        <el-table-column prop="title" label="标题" min-width="120" show-overflow-tooltip />
        <el-table-column prop="pv" label="PV" width="90" />
        <el-table-column prop="uv" label="UV" width="90" />
        <el-table-column label="平均停留" width="110">
          <template #default="{ row }">{{ row.avgDwell }}s</template>
        </el-table-column>
        <el-table-column prop="exits" label="退出" width="90" />
        <el-table-column label="跳出率" min-width="140">
          <template #default="{ row }">
            <el-progress :percentage="row.bounceRate || 0" :stroke-width="10" :color="bounceColor(row.bounceRate)" />
          </template>
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
import { lastNDays } from '../../utils/date'

const appStore = useAppStore()
const range = reactive({ from: lastNDays(14)[0], to: lastNDays(14)[1] })
const data = reactive({ summary: {}, pages: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.pageExperience({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

function bounceColor(v) { return v >= 50 ? '#f56c6c' : v >= 25 ? '#e6a23c' : '#00c2a8' }

const top8 = computed(() => (data.pages || []).slice(0, 8))
const pvOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 100, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: top8.value.map((p) => p.path).reverse() },
  series: [{ type: 'bar', barMaxWidth: 20, data: top8.value.map((p) => p.pv).reverse(), itemStyle: { color: '#2b5cff', borderRadius: [0, 4, 4, 0] } }]
}))
const dwellOption = computed(() => ({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  grid: { left: 100, right: 30, top: 20, bottom: 30 },
  xAxis: { type: 'value' },
  yAxis: { type: 'category', data: top8.value.map((p) => p.path).reverse() },
  series: [{ type: 'bar', barMaxWidth: 20, data: top8.value.map((p) => p.avgDwell).reverse(), itemStyle: { color: '#00c2a8', borderRadius: [0, 4, 4, 0] } }]
}))

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.value.small { font-size: 14px; }
</style>
