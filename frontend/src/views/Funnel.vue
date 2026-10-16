<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">漏斗分析</h2>
    <p class="page-sub">按有序步骤统计用户转化，评估关键路径流失</p>

    <RangeBar @change="onRange" />

    <div class="card">
      <div class="card-title">漏斗步骤（按顺序）</div>
      <div class="steps">
        <el-tag
          v-for="(s, i) in steps"
          :key="i"
          closable
          size="large"
          class="step-tag"
          @close="removeStep(i)"
        >{{ i + 1 }}. {{ s }}</el-tag>
        <el-select v-model="pick" placeholder="+ 添加步骤" filterable style="width: 200px" @change="addStep">
          <el-option v-for="e in availableEvents" :key="e" :label="e" :value="e" />
        </el-select>
      </div>
      <div class="presets">
        <span class="muted">预设：</span>
        <el-button size="small" @click="setPreset('purchase')">购买转化</el-button>
        <el-button size="small" @click="setPreset('signup')">注册转化</el-button>
        <el-button size="small" type="primary" :loading="loading" @click="load">计算漏斗</el-button>
      </div>
    </div>

    <div class="row2">
      <div class="card">
        <div class="card-title">转化漏斗</div>
        <BaseChart :option="funnelOption" height="360px" />
      </div>
      <div class="card">
        <div class="card-title">步骤明细</div>
        <el-table :data="result.steps" size="small" stripe>
          <el-table-column type="index" label="步骤" width="60" />
          <el-table-column prop="step" label="事件" min-width="140" />
          <el-table-column prop="users" label="用户数" width="100" />
          <el-table-column label="总体转化" width="110">
            <template #default="{ row }">{{ row.rate }}%</template>
          </el-table-column>
          <el-table-column label="环比上一步" width="120">
            <template #default="{ row }">{{ row.stepRate }}%</template>
          </el-table-column>
        </el-table>
      </div>
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
const steps = ref(['view_product', 'add_to_cart', 'begin_checkout', 'purchase'])
const pick = ref('')
const availableEvents = ref([])
const result = reactive({ steps: [] })
const loading = ref(false)
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })

function addStep(v) { if (v) { steps.value.push(v); pick.value = '' } }
function removeStep(i) { steps.value.splice(i, 1) }
function setPreset(k) {
  steps.value = k === 'purchase'
    ? ['view_product', 'add_to_cart', 'begin_checkout', 'purchase']
    : ['view_product', 'sign_up']
  load()
}
function onRange(r) { range.from = r.from; range.to = r.to; load() }

async function load() {
  if (!steps.value.length) return
  loading.value = true
  try {
    const data = await analyticsApi.funnel({ appId: appStore.currentAppId, steps: steps.value, from: range.from, to: range.to })
    result.steps = data.steps
  } catch (e) { /* surfaced by http interceptor */ } finally { loading.value = false }
}

async function loadEvents() {
  try {
    const evs = await analyticsApi.events({ appId: appStore.currentAppId, from: range.from, to: range.to, limit: 50 })
    availableEvents.value = evs.map((e) => e.event).filter((n) => n && !n.startsWith('$'))
  } catch (e) { /* surfaced by http interceptor */ }
}

const funnelOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 人' },
  series: [{
    type: 'funnel', left: '10%', width: '80%', top: 10, bottom: 10,
    minSize: '20%', sort: 'descending', gap: 4,
    label: { show: true, position: 'inside', formatter: '{b}\n{c}' },
    data: result.steps.map((s) => ({ name: s.step, value: s.users })),
    color: ['#2b5cff', '#4d7dff', '#00c2a8', '#ff9f43', '#f5576c', '#6a3cff']
  }]
}))

watch(() => appStore.currentAppId, () => { loadEvents(); load() })
onMounted(async () => { await appStore.load(); await loadEvents(); load() })
</script>

<style scoped>
.steps { display: flex; gap: 8px; flex-wrap: wrap; align-items: center; }
.step-tag { font-size: 13px; }
.presets { margin-top: 14px; display: flex; gap: 10px; align-items: center; }
</style>
