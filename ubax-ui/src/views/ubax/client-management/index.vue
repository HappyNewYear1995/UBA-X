<template>
  <div class="running-monitor">
    <!-- 客户端选择 -->
    <el-card shadow="never" class="dashboard-card mb-4">
      <template #header>
        <div class="card-header">
          <span class="card-title">客户端选择</span>
        </div>
      </template>
      <div class="client-selector">
        <el-select v-model="selectedClient" placeholder="请选择客户端" style="width: 300px" @change="handleClientChange">
          <el-option v-for="client in clientList" :key="client.id" :label="client.name" :value="client.id" />
        </el-select>
        <div v-if="selectedClient" class="client-info">
          <el-tag :type="getClientStatusType(selectedClientData?.status)" size="small">
            {{ selectedClientData?.statusText }}
          </el-tag>
          <span class="info-text">Pilot 版本: {{ selectedClientData?.version }}</span>
          <span class="info-text">今日上报: {{ formatNumber(selectedClientData?.events || 0) }}</span>
        </div>
      </div>
    </el-card>

    <!-- 核心指标 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon traffic">
            <Icon icon="ep:data-line" :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">今日上报事件</div>
            <div class="stat-value">{{ formatNumber(todayEvents) }}</div>
            <div class="stat-trend up">
              <Icon icon="ep:top" :size="12" /> +12.5% 较昨日
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon success">
            <Icon icon="ep:success-filled" :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">接口成功率</div>
            <div class="stat-value">{{ apiSuccessRate }}%</div>
            <div class="stat-trend up">
              <Icon icon="ep:top" :size="12" /> +0.3% 较昨日
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon latency">
            <Icon icon="ep:timer" :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">平均延迟</div>
            <div class="stat-value">{{ avgLatency }}ms</div>
            <div class="stat-trend down">
              <Icon icon="ep:bottom" :size="12" /> -8ms 较昨日
            </div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card">
          <div class="stat-icon error">
            <Icon icon="ep:warning-filled" :size="24" />
          </div>
          <div class="stat-content">
            <div class="stat-label">异常事件</div>
            <div class="stat-value">{{ errorCount }}</div>
            <div class="stat-trend down">
              <Icon icon="ep:bottom" :size="12" /> -23 较昨日
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 流量监控 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">流量监控</span>
              <el-radio-group v-model="trafficRange" size="small" @change="handleTrafficRangeChange">
                <el-radio-button value="6h">近 6 小时</el-radio-button>
                <el-radio-button value="12h">近 12 小时</el-radio-button>
                <el-radio-button value="24h">近 24 小时</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div ref="trafficChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 接口健康度 & 异常捕获 -->
    <el-row :gutter="16" class="mb-4">
      <el-col :span="12">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <span class="card-title">接口健康度</span>
          </template>
          <div class="health-metrics">
            <div class="health-item">
              <div class="health-label">上报接口成功率</div>
              <div class="health-bar">
                <el-progress :percentage="apiSuccessRate" :color="getProgressColor(apiSuccessRate)" :stroke-width="12" />
              </div>
              <div class="health-value">{{ apiSuccessRate }}%</div>
            </div>
            <div class="health-item">
              <div class="health-label">配置拉取成功率</div>
              <div class="health-bar">
                <el-progress :percentage="configSuccessRate" :color="getProgressColor(configSuccessRate)" :stroke-width="12" />
              </div>
              <div class="health-value">{{ configSuccessRate }}%</div>
            </div>
            <div class="health-item">
              <div class="health-label">平均响应延迟</div>
              <div class="health-bar">
                <el-progress :percentage="latencyPercent" :color="getProgressColor(100 - latencyPercent)" :stroke-width="12" />
              </div>
              <div class="health-value">{{ avgLatency }}ms</div>
            </div>
            <div class="health-item">
              <div class="health-label">P99 延迟</div>
              <div class="health-bar">
                <el-progress :percentage="p99Percent" :color="getProgressColor(100 - p99Percent)" :stroke-width="12" />
              </div>
              <div class="health-value">{{ p99Latency }}ms</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <span class="card-title">异常捕获</span>
          </template>
          <div class="error-metrics">
            <div class="error-grid">
              <div class="error-item">
                <div class="error-icon">
                  <Icon icon="ep:circle-close-filled" :size="28" />
                </div>
                <div class="error-content">
                  <div class="error-label">初始化失败率</div>
                  <div class="error-value">{{ initFailRate }}%</div>
                  <div class="error-count">今日 {{ initFailCount }} 次</div>
                </div>
              </div>
              <div class="error-item">
                <div class="error-icon">
                  <Icon icon="ep:warning-filled" :size="28" />
                </div>
                <div class="error-content">
                  <div class="error-label">解析错误数</div>
                  <div class="error-value">{{ parseErrorCount }}</div>
                  <div class="error-count">今日累计</div>
                </div>
              </div>
              <div class="error-item">
                <div class="error-icon">
                  <Icon icon="ep:connection" :size="28" />
                </div>
                <div class="error-content">
                  <div class="error-label">网络超时</div>
                  <div class="error-value">{{ timeoutCount }}</div>
                  <div class="error-count">今日累计</div>
                </div>
              </div>
              <div class="error-item">
                <div class="error-icon">
                  <Icon icon="ep:document-delete" :size="28" />
                </div>
                <div class="error-content">
                  <div class="error-label">数据丢弃</div>
                  <div class="error-value">{{ dropCount }}</div>
                  <div class="error-count">今日累计</div>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import * as echarts from 'echarts'

defineOptions({ name: 'RunningMonitor' })

const trafficChartRef = ref<HTMLElement>()
const selectedClient = ref(1)
const trafficRange = ref('24h')

const clientList = ref([
  { id: 1, name: 'UBA-X 官网', status: 'online', statusText: '在线', version: 'v2.1.0', events: 45230 },
  { id: 2, name: 'UBA-X 管理后台', status: 'online', statusText: '在线', version: 'v2.1.0', events: 12450 },
  { id: 3, name: 'UBA-X Android App', status: 'online', statusText: '在线', version: 'v1.8.3', events: 89234 },
  { id: 4, name: 'UBA-X iOS App', status: 'offline', statusText: '离线', version: 'v1.8.3', events: 0 },
  { id: 5, name: '微信小程序', status: 'error', statusText: '异常', version: 'v1.5.0', events: 3420 },
  { id: 6, name: '支付宝小程序', status: 'online', statusText: '在线', version: 'v1.5.0', events: 5680 }
])

const selectedClientData = computed(() => clientList.value.find(c => c.id === selectedClient.value))

const todayEvents = computed(() => selectedClientData.value?.events || 0)
const apiSuccessRate = ref(99.7)
const configSuccessRate = ref(99.9)
const avgLatency = ref(45)
const p99Latency = ref(128)
const latencyPercent = computed(() => Math.min(avgLatency.value / 2, 100))
const p99Percent = computed(() => Math.min(p99Latency.value / 5, 100))
const errorCount = ref(12)
const initFailRate = ref(0.3)
const initFailCount = ref(8)
const parseErrorCount = ref(23)
const timeoutCount = ref(15)
const dropCount = ref(42)

const formatNumber = (num: number) => {
  if (num >= 10000) return `${(num / 10000).toFixed(1)}w`
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`
  return num.toString()
}

const getClientStatusType = (status: string) => {
  const map: Record<string, string> = { online: 'success', offline: 'info', error: 'danger' }
  return map[status] || ''
}

const getProgressColor = (percent: number) => {
  if (percent >= 95) return '#67c23a'
  if (percent >= 80) return '#e6a23c'
  return '#f56c6c'
}

let trafficChart: echarts.ECharts | null = null

const initTrafficChart = () => {
  if (!trafficChartRef.value) return
  trafficChart = echarts.init(trafficChartRef.value)

  const hours = trafficRange.value === '6h'
    ? ['18:00', '19:00', '20:00', '21:00', '22:00', '23:00']
    : trafficRange.value === '12h'
      ? ['12:00', '13:00', '14:00', '15:00', '16:00', '17:00', '18:00', '19:00', '20:00', '21:00', '22:00', '23:00']
      : ['00:00', '02:00', '04:00', '06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00']

  const option = {
    tooltip: { trigger: 'axis' as const, formatter: '{b}<br/>{a}: {c} 次' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: { type: 'category' as const, data: hours, boundaryGap: false, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisLabel: { color: '#6b7280' } },
    yAxis: { type: 'value' as const, axisLine: { show: false }, axisLabel: { color: '#6b7280' }, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [{
      name: '上报事件',
      type: 'line' as const,
      smooth: true,
      symbol: 'none',
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(99, 102, 241, 0.3)' }, { offset: 1, color: 'rgba(99, 102, 241, 0.05)' }]) },
      lineStyle: { width: 2, color: '#6366f1' },
      data: trafficRange.value === '6h'
        ? [3200, 4100, 5800, 6200, 4500, 3800]
        : trafficRange.value === '12h'
          ? [2800, 3200, 3800, 4200, 5100, 5800, 6200, 5500, 4800, 4200, 3800, 3200]
          : [1200, 800, 600, 1500, 4200, 5800, 6200, 5500, 4800, 4200, 3800, 2500]
    }]
  }
  trafficChart.setOption(option)
}

const handleClientChange = () => {
  initTrafficChart()
}

const handleTrafficRangeChange = () => {
  initTrafficChart()
}

onMounted(() => {
  initTrafficChart()
  window.addEventListener('resize', () => {
    trafficChart?.resize()
  })
})

onUnmounted(() => {
  trafficChart?.dispose()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.running-monitor {
  padding: 16px;
  background: var(--app-content-bg-color);
  min-height: 100%;
}

.dashboard-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--app-content-card-border);
    padding: 14px 20px;
  }

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

:deep(.el-radio-group) {
  .el-radio-button {
    margin-left: 8px;

    &:first-child {
      margin-left: 0;
    }
  }
}

.client-selector {
  display: flex;
  align-items: center;
  gap: 16px;

  .client-info {
    display: flex;
    align-items: center;
    gap: 12px;

    .info-text {
      font-size: 13px;
      color: var(--app-content-text-color-secondary);
    }
  }
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: var(--radius-lg);
  background: var(--app-content-card-bg);
  border: 1px solid var(--app-content-card-border);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;

    &.traffic { background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%); color: #fff; }
    &.success { background: linear-gradient(135deg, #10b981 0%, #34d399 100%); color: #fff; }
    &.latency { background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%); color: #fff; }
    &.error { background: linear-gradient(135deg, #ef4444 0%, #f87171 100%); color: #fff; }
  }

  .stat-content {
    flex: 1;

    .stat-label {
      font-size: 13px;
      color: var(--app-content-text-color-secondary);
    }

    .stat-value {
      font-size: 22px;
      font-weight: 600;
      color: var(--app-content-text-color-primary);
      margin-top: 4px;
    }

    .stat-trend {
      font-size: 12px;
      margin-top: 4px;

      &.up { color: #10b981; }
      &.down { color: #ef4444; }
    }
  }
}

.chart-container {
  width: 100%;
  height: 300px;
}

.health-metrics {
  .health-item {
    display: flex;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid var(--app-content-card-border);

    &:last-child { border-bottom: none; }

    .health-label {
      width: 120px;
      font-size: 13px;
      color: var(--app-content-text-color-secondary);
      flex-shrink: 0;
    }

    .health-bar {
      flex: 1;
      margin: 0 16px;
    }

    .health-value {
      width: 60px;
      font-size: 14px;
      font-weight: 600;
      color: var(--app-content-text-color-primary);
      text-align: right;
      flex-shrink: 0;
    }
  }
}

.error-metrics {
  .error-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;
  }

  .error-item {
    display: flex;
    align-items: center;
    padding: 16px;
    background: var(--app-content-bg-color);
    border-radius: var(--radius-md);
    border: 1px solid var(--app-content-card-border);

    .error-icon {
      width: 48px;
      height: 48px;
      border-radius: var(--radius-md);
      background: #fef2f2;
      color: #ef4444;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-right: 12px;
      flex-shrink: 0;
    }

    .error-content {
      flex: 1;

      .error-label {
        font-size: 12px;
        color: var(--app-content-text-color-secondary);
      }

      .error-value {
        font-size: 20px;
        font-weight: 600;
        color: var(--app-content-text-color-primary);
        margin-top: 2px;
      }

      .error-count {
        font-size: 12px;
        color: var(--app-content-text-color-secondary);
        margin-top: 2px;
      }
    }
  }
}
</style>
