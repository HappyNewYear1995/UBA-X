<template>
  <div class="metrics-dashboard">
    <el-row :gutter="16" class="metric-row">
      <el-col v-for="metric in coreMetrics" :key="metric.label" :xs="12" :sm="12" :md="6" :lg="6" :xl="6">
        <el-card shadow="never" class="metric-card" :class="metric.type">
          <div class="metric-content">
            <div class="metric-icon" :style="{ background: metric.iconBg }">
              <Icon :icon="metric.icon" :size="24" />
            </div>
            <div class="metric-info">
              <div class="metric-label">{{ metric.label }}</div>
              <div class="metric-value">
                <CountTo :start-val="0" :end-val="metric.value" :duration="2000" />
              </div>
              <div class="metric-trend" :class="metric.trend > 0 ? 'up' : 'down'">
                <Icon :icon="metric.trend > 0 ? 'ep:top' : 'ep:bottom'" :size="12" />
                <span>{{ Math.abs(metric.trend) }}%</span>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户行为趋势</span>
              <el-radio-group v-model="trendPeriod" size="small">
                <el-radio-button value="day">今日</el-radio-button>
                <el-radio-button value="week">本周</el-radio-button>
                <el-radio-button value="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <Echart :options="behaviorTrendOptions" :height="320" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'

defineOptions({ name: 'MetricsDashboard' })

const trendPeriod = ref('week')

const coreMetrics = ref([
  {
    label: '日活跃用户',
    value: 12846,
    trend: 12.5,
    icon: 'ep:user',
    iconBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    type: 'primary'
  },
  {
    label: '转化率',
    value: 34,
    trend: -2.1,
    icon: 'ep:trend-charts',
    iconBg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    type: 'warning'
  },
  {
    label: '事件总量',
    value: 89234,
    trend: 8.3,
    icon: 'ep:data-line',
    iconBg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    type: 'success'
  },
  {
    label: '页面浏览量',
    value: 45678,
    trend: 5.7,
    icon: 'ep:view',
    iconBg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    type: 'info'
  }
])

const behaviorTrendOptions = reactive<EChartsOption>({
  tooltip: {
    trigger: 'axis',
    axisPointer: { type: 'cross', label: { backgroundColor: '#6a7985' } }
  },
  legend: {
    data: ['页面浏览', '点击事件', '转化行为', '异常行为'],
    top: 10
  },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['00:00', '03:00', '06:00', '09:00', '12:00', '15:00', '18:00', '21:00']
  },
  yAxis: { type: 'value' },
  series: [
    {
      name: '页面浏览',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#667eea' },
      data: [120, 132, 101, 134, 90, 230, 210, 180]
    },
    {
      name: '点击事件',
      type: 'line',
      smooth: true,
      areaStyle: { opacity: 0.3 },
      itemStyle: { color: '#f093fb' },
      data: [220, 182, 191, 234, 290, 330, 310, 260]
    },
    {
      name: '转化行为',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#4facfe' },
      data: [50, 62, 41, 74, 60, 90, 80, 70]
    },
    {
      name: '异常行为',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#fa709a' },
      data: [5, 8, 3, 12, 6, 15, 10, 7]
    }
  ]
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.metrics-dashboard {
  padding: 16px;
  background: var(--app-content-bg-color);
  min-height: 100%;
}

.metric-row {
  margin-bottom: 16px;
}

.chart-row {
  margin-bottom: 16px;
}

.metric-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;
  transition: all 0.3s ease !important;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg) !important;
  }

  :deep(.el-card__body) {
    padding: 0 !important;
  }
}

.metric-content {
  display: flex;
  align-items: center;
  padding: 20px;
}

.metric-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
  flex-shrink: 0;
}

.metric-info {
  flex: 1;
}

.metric-label {
  font-size: 13px;
  color: var(--app-content-text-color-secondary);
  margin-bottom: 4px;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--app-content-text-color-primary);
  line-height: 1.2;
}

.metric-trend {
  display: flex;
  align-items: center;
  font-size: 12px;
  margin-top: 4px;

  &.up {
    color: #43e97b;
  }

  &.down {
    color: #fa709a;
  }
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
    padding: 16px;
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
</style>
