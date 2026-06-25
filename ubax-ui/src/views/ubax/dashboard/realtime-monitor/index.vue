<template>
  <div class="realtime-monitor">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">指标实时监控</span>
          <span class="live-indicator">
            <span class="live-dot"></span>
            分钟级更新
          </span>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col v-for="metric in realTimeMetrics" :key="metric.name" :xs="12" :sm="8" :md="6" :lg="6" :xl="6">
          <div class="metric-card" :style="{ borderLeft: `4px solid ${metric.color}` }">
            <div class="metric-name">{{ metric.name }}</div>
            <div class="metric-value" :style="{ color: metric.color }">
              <CountTo :start-val="0" :end-val="metric.value" :duration="1500" />
            </div>
            <div class="metric-status" :class="metric.status">
              <Icon :icon="metric.status === 'normal' ? 'ep:success-filled' : 'ep:warning-filled'" :size="12" />
              <span>{{ metric.status === 'normal' ? '正常' : '异常' }}</span>
            </div>
          </div>
        </el-col>
      </el-row>

      <div class="mt-4">
        <Echart :options="realTimeTrendOptions" :height="300" />
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'
import { getRealtimeMetrics, getMetricsTrend } from '@/api/ubax/app'
import type { RealtimeMetricsRespVO, MetricsTrendRespVO } from '@/api/ubax/app'

defineOptions({ name: 'RealtimeMonitor' })

const loading = ref(false)

// 实时指标
const realTimeMetrics = ref([
  { name: '活跃用户数', value: 0, color: '#667eea', status: 'normal' },
  { name: '页面浏览量', value: 0, color: '#4facfe', status: 'normal' },
  { name: '转化率', value: 0, color: '#43e97b', status: 'normal' },
  { name: '异常事件', value: 0, color: '#fa709a', status: 'normal' }
])

// 趋势数据
const trendData = ref<MetricsTrendRespVO[]>([])
const realTimeTrendOptions = reactive<EChartsOption>({
  tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
  legend: { data: ['活跃用户', '事件量', '转化率'], top: 10 },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: [] as string[]
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '活跃用户',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#667eea' },
      data: [] as number[]
    },
    {
      name: '事件量',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#4facfe' },
      data: [] as number[]
    },
    {
      name: '转化率',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#43e97b' },
      data: [] as number[]
    }
  ]
})

/** 加载实时指标 */
const loadMetrics = async () => {
  try {
    loading.value = true
    const res = await getRealtimeMetrics()
    const data = res as unknown as RealtimeMetricsRespVO
    realTimeMetrics.value[0].value = data.activeUsers || 0
    realTimeMetrics.value[0].status = data.activeUsers > 0 ? 'normal' : 'warning'
    realTimeMetrics.value[1].value = data.pageViews || 0
    realTimeMetrics.value[1].status = data.pageViews > 0 ? 'normal' : 'warning'
    realTimeMetrics.value[2].value = Math.round(data.conversionRate || 0)
    realTimeMetrics.value[2].status = (data.conversionRate || 0) > 0 ? 'normal' : 'warning'
    realTimeMetrics.value[3].value = data.anomalyEvents || 0
    realTimeMetrics.value[3].status = (data.anomalyEvents || 0) > 10 ? 'warning' : 'normal'
  } catch (e) {
    console.error('加载实时指标失败', e)
  } finally {
    loading.value = false
  }
}

/** 加载趋势数据 */
const loadTrend = async () => {
  try {
    const endTime = new Date()
    const startTime = new Date(endTime.getTime() - 24 * 60 * 60 * 1000)
    const formatTime = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`

    const [activeRes, eventRes, rateRes] = await Promise.all([
      getMetricsTrend({ metricName: 'activeUsers', startTime: formatTime(startTime), endTime: formatTime(endTime), interval: '1h' }),
      getMetricsTrend({ metricName: 'pageViews', startTime: formatTime(startTime), endTime: formatTime(endTime), interval: '1h' }),
      getMetricsTrend({ metricName: 'conversionRate', startTime: formatTime(startTime), endTime: formatTime(endTime), interval: '1h' })
    ])

    const activeData = (activeRes as unknown as MetricsTrendRespVO[]) || []
    const eventData = (eventRes as unknown as MetricsTrendRespVO[]) || []
    const rateData = (rateRes as unknown as MetricsTrendRespVO[]) || []

    // 更新图表
    const xAxis = realTimeTrendOptions.xAxis as any
    xAxis.data = activeData.map(item => item.time?.substring(11, 16) || '')
    ;(realTimeTrendOptions.series as any[])[0].data = activeData.map(item => item.value || 0)
    ;(realTimeTrendOptions.series as any[])[1].data = eventData.map(item => item.value || 0)
    ;(realTimeTrendOptions.series as any[])[2].data = rateData.map(item => item.value || 0)
  } catch (e) {
    console.error('加载趋势数据失败', e)
  }
}

/** 初始化 */
onMounted(() => {
  loadMetrics()
  loadTrend()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.realtime-monitor {
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

  :deep(.el-table) {
    width: 100% !important;
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

.live-indicator {
  display: flex;
  align-items: center;
  font-size: 12px;
  color: #43e97b;
}

.live-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #43e97b;
  margin-right: 6px;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.2); }
}

.metric-card {
  padding: 16px;
  border-radius: var(--radius-md);
  background: var(--app-content-card-bg);
  border: 1px solid var(--app-content-card-border);
  margin-bottom: 12px;
}

.metric-name {
  font-size: 13px;
  color: var(--app-content-text-color-secondary);
  margin-bottom: 8px;
}

.metric-value {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}

.metric-status {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;

  &.normal {
    color: #43e97b;
  }

  &.warning {
    color: #fa709a;
  }
}
</style>
