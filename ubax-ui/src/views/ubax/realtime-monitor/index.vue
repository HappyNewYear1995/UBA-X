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

defineOptions({ name: 'RealtimeMonitor' })

const realTimeMetrics = ref([
  { name: '活跃用户数', value: 12846, color: '#667eea', status: 'normal' },
  { name: '页面浏览量', value: 45678, color: '#4facfe', status: 'normal' },
  { name: '转化率', value: 34, color: '#43e97b', status: 'normal' },
  { name: '异常事件', value: 7, color: '#fa709a', status: 'warning' }
])

const realTimeTrendOptions = reactive<EChartsOption>({
  tooltip: { trigger: 'axis', axisPointer: { type: 'cross' } },
  legend: { data: ['活跃用户', '事件量', '转化率'], top: 10 },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['14:00', '14:05', '14:10', '14:15', '14:20', '14:25', '14:30']
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '活跃用户',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#667eea' },
      data: [12000, 12200, 12500, 12400, 12600, 12700, 12846]
    },
    {
      name: '事件量',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#4facfe' },
      data: [42000, 43000, 44000, 43500, 44500, 45000, 45678]
    },
    {
      name: '转化率',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#43e97b' },
      data: [32, 33, 34, 33, 35, 34, 34]
    }
  ]
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
