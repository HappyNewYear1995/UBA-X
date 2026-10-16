<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">流失预警 / 用户健康分</h2>
    <p class="page-sub">基于最近活跃与访问频次计算健康分，输出高流失风险名单</p>

    <RangeBar :initial-days="14" @change="onRange">
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">用户总数</div><div class="value">{{ compact(data.summary.users) }}</div></div>
      <div class="stat-card"><div class="label">平均健康分</div><div class="value">{{ data.summary.avgScore || 0 }}</div></div>
      <div class="stat-card"><div class="label">高风险</div><div class="value" style="color:#f56c6c">{{ data.summary.highRisk || 0 }}</div></div>
      <div class="stat-card"><div class="label">中风险</div><div class="value" style="color:#e6a23c">{{ data.summary.midRisk || 0 }}</div></div>
      <div class="stat-card"><div class="label">7日活跃率</div><div class="value">{{ data.summary.activeRate || 0 }}<small> %</small></div></div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">风险分布</div>
        <BaseChart :option="pieOption" height="300px" />
      </div>
      <div class="card">
        <div class="card-title">高流失风险用户 TOP15</div>
        <el-table :data="data.riskList" size="small" height="300">
          <el-table-column prop="distinctId" label="用户" min-width="110" show-overflow-tooltip />
          <el-table-column prop="region" label="地域" width="90" show-overflow-tooltip />
          <el-table-column prop="device" label="设备" width="90" />
          <el-table-column prop="recencyDays" label="沉默天数" width="90" />
          <el-table-column prop="visitCount" label="访问次数" width="90" />
          <el-table-column label="健康分" width="90">
            <template #default="{ row }">
              <span :style="{ color: scoreColor(row.score), fontWeight: 600 }">{{ row.score }}</span>
            </template>
          </el-table-column>
          <el-table-column label="风险" width="80">
            <template #default="{ row }"><el-tag size="small" :type="riskType(row.risk)">{{ row.risk }}</el-tag></template>
          </el-table-column>
        </el-table>
      </div>
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
const range = reactive({ from: lastNDays(14)[0], to: lastNDays(14)[1] })
const data = reactive({ summary: {}, buckets: [], riskList: [] })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.churnHealth({ appId: appStore.currentAppId, from: range.from, to: range.to })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

function scoreColor(s) { return s >= 70 ? '#00c2a8' : s >= 45 ? '#e6a23c' : '#f56c6c' }
function riskType(r) { return { '高': 'danger', '中': 'warning', '低': 'success' }[r] || 'info' }

const BUCKET_COLORS = { '高流失风险': '#f56c6c', '中流失风险': '#e6a23c', '健康': '#00c2a8' }
const pieOption = computed(() => ({
  tooltip: { trigger: 'item' },
  legend: { bottom: 0 },
  series: [{
    type: 'pie', radius: ['42%', '68%'], center: ['50%', '45%'],
    data: (data.buckets || []).map((b) => ({ name: b.name, value: b.value, itemStyle: { color: BUCKET_COLORS[b.name] } })),
    label: { formatter: '{b}\n{c} ({d}%)' }
  }]
}))

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>
