<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">转化路径分析</h2>
    <p class="page-sub">还原会话内访问路径，统计高频路径与到目标的转化率</p>

    <RangeBar :initial-days="14" @change="onRange">
      <el-select v-model="goal" size="default" style="width: 150px" @change="load">
        <el-option v-for="g in goals" :key="g.value" :label="g.label" :value="g.value" />
      </el-select>
      <el-button :icon="Refresh" circle @click="load" />
    </RangeBar>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">会话数</div><div class="value">{{ compact(data.summary.sessions) }}</div></div>
      <div class="stat-card"><div class="label">转化会话</div><div class="value">{{ compact(data.summary.convertedSessions) }}</div></div>
      <div class="stat-card"><div class="label">转化率</div><div class="value">{{ data.summary.convRate || 0 }}<small> %</small></div></div>
      <div class="stat-card"><div class="label">不同路径</div><div class="value">{{ data.summary.distinctPaths || 0 }}</div></div>
      <div class="stat-card"><div class="label">平均步数</div><div class="value">{{ data.summary.avgSteps || 0 }}</div></div>
    </div>

    <div class="card">
      <div class="card-title">路径流向（TOP 路径 · 分层桑基图）</div>
      <BaseChart :option="sankeyOption" height="420px" />
    </div>

    <div class="card">
      <div class="card-title">高频路径 TOP12</div>
      <el-table :data="data.topPaths" size="small">
        <el-table-column label="路径" min-width="360">
          <template #default="{ row }">
            <span class="path-line">{{ row.path }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="sessions" label="会话" width="90" />
        <el-table-column prop="users" label="用户" width="90" />
        <el-table-column label="占比" width="160">
          <template #default="{ row }"><el-progress :percentage="row.share || 0" :stroke-width="10" /></template>
        </el-table-column>
        <el-table-column label="达成目标" width="100">
          <template #default="{ row }">
            <el-tag size="small" :type="row.converted ? 'success' : 'info'">{{ row.converted ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
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
const range = reactive({ from: lastNDays(14)[0], to: lastNDays(14)[1] })
const goal = ref('purchase')
const goals = [
  { label: '完成购买', value: 'purchase' },
  { label: '开始结算', value: 'begin_checkout' },
  { label: '加入购物车', value: 'add_to_cart' },
  { label: '注册', value: 'sign_up' }
]
const data = reactive({ summary: {}, topPaths: [], sankey: { nodes: [], links: [] } })
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const d = await extApi.conversionPath({ appId: appStore.currentAppId, from: range.from, to: range.to, goal: goal.value })
    Object.assign(data, d)
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

// Build a cycle-free layered sankey from top paths: node key = position + name.
const sankeyOption = computed(() => {
  const nodeMap = new Map()
  const linkMap = new Map()
  for (const p of data.topPaths || []) {
    const nodes = p.nodes || []
    for (let i = 0; i < nodes.length; i++) {
      nodeMap.set(i + ':' + nodes[i], { name: i + ':' + nodes[i], label: nodes[i], depth: i })
      if (i + 1 < nodes.length) {
        const s = i + ':' + nodes[i]
        const t = (i + 1) + ':' + nodes[i + 1]
        linkMap.set(s + '>' + t, { source: s, target: t, value: (linkMap.get(s + '>' + t)?.value || 0) + (p.sessions || 0) })
      }
    }
  }
  const COLORS = ['#2b5cff', '#00c2a8', '#e6a23c', '#f56c6c', '#6a3cff', '#00a0e9']
  return {
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: [{
      type: 'sankey',
      left: 12, right: 130, top: 12, bottom: 12,
      nodeWidth: 14, nodeGap: 10,
      data: Array.from(nodeMap.values()).map((n) => ({
        name: n.name,
        itemStyle: { color: COLORS[n.depth % COLORS.length] }
      })),
      links: Array.from(linkMap.values()),
      label: {
        formatter: (p) => (nodeMap.get(p.name)?.label || p.name),
        fontSize: 12, color: '#3a4252'
      },
      lineStyle: { color: 'gradient', opacity: 0.35, curveness: 0.5 },
      emphasis: { focus: 'adjacency' }
    }]
  }
})

onMounted(load)
watch(() => appStore.currentAppId, load)
</script>

<style scoped>
.path-line { font-size: 12.5px; color: #3a4252; }
</style>
