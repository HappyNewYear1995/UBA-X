<template>
  <div class="analysis-page">
    <el-card shadow="never" class="glass-card filter-card">
      <el-form :inline="true" :model="filters">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item label="分析模型">
          <el-select v-model="filters.model" placeholder="选择分析模型" class="custom-select">
            <el-option label="事件分析" value="event" />
            <el-option label="漏斗分析" value="funnel" />
            <el-option label="留存分析" value="retention" />
            <el-option label="路径分析" value="path" />
            <el-option label="用户分群" value="segment" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户分群">
          <el-select v-model="filters.segment" placeholder="全部用户" class="custom-select">
            <el-option label="全部用户" value="all" />
            <el-option label="新用户" value="new" />
            <el-option label="活跃用户" value="active" />
            <el-option label="付费用户" value="paid" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button class="btn-gradient" @click="runAnalysis">
            <el-icon><Search /></el-icon>
            分析
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="24">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>分析结果</span>
              </div>
              <div class="header-actions">
                <el-button class="btn-outline" size="small">
                  <el-icon><Download /></el-icon>
                  导出
                </el-button>
                <el-button class="btn-outline" size="small">
                  <el-icon><Share /></el-icon>
                  分享
                </el-button>
              </div>
            </div>
          </template>
          <v-chart class="chart" :option="analysisOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="12">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>维度下钻</span>
            </div>
          </template>
          <el-table :data="dimensionData" style="width: 100%" class="custom-table">
            <el-table-column prop="dimension" label="维度" />
            <el-table-column prop="users" label="用户数" />
            <el-table-column prop="events" label="事件数" />
            <el-table-column prop="rate" label="占比" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>关键指标</span>
            </div>
          </template>
          <div class="metrics-grid">
            <div v-for="metric in keyMetrics" :key="metric.name" class="metric-item">
              <div class="metric-name">{{ metric.name }}</div>
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-change" :class="metric.trend > 0 ? 'up' : 'down'">
                <el-icon><Top v-if="metric.trend > 0" /><Bottom v-else /></el-icon>
                {{ metric.trend > 0 ? '+' : '' }}{{ metric.trend }}%
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Top, Bottom } from '@element-plus/icons-vue'

use([CanvasRenderer, BarChart, LineChart, GridComponent, TooltipComponent, LegendComponent])

const filters = ref({
  dateRange: null,
  model: 'event',
  segment: 'all',
})

const analysisOption = {
  tooltip: { trigger: 'axis' },
  legend: { data: ['页面浏览', '点击事件', '表单提交'], top: 0 },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['05-10', '05-11', '05-12', '05-13', '05-14', '05-15', '05-16'],
    axisLine: { lineStyle: { color: '#e2e8f0' } },
    axisLabel: { color: '#64748b' },
  },
  yAxis: { 
    type: 'value',
    splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
    axisLabel: { color: '#64748b' },
  },
  series: [
    { name: '页面浏览', type: 'bar', data: [3200, 3500, 3100, 3800, 4200, 3900, 4100], itemStyle: { color: '#6366f1', borderRadius: [4, 4, 0, 0] } },
    { name: '点击事件', type: 'bar', data: [1800, 2100, 1900, 2300, 2500, 2200, 2400], itemStyle: { color: '#8b5cf6', borderRadius: [4, 4, 0, 0] } },
    { name: '表单提交', type: 'line', smooth: true, data: [520, 610, 580, 720, 810, 690, 750], itemStyle: { color: '#10b981' }, lineStyle: { width: 3 } },
  ],
}

const dimensionData = ref([
  { dimension: 'Chrome', users: '45,230', events: '1,234,567', rate: '42.3%' },
  { dimension: 'Safari', users: '28,100', events: '892,345', rate: '26.4%' },
  { dimension: 'Firefox', users: '12,450', events: '456,789', rate: '11.7%' },
  { dimension: 'Edge', users: '9,870', events: '345,678', rate: '9.3%' },
  { dimension: '其他', users: '10,890', events: '234,567', rate: '10.3%' },
])

const keyMetrics = ref([
  { name: '日活跃用户', value: '23,456', trend: 5.2 },
  { name: '人均事件数', value: '12.8', trend: 2.1 },
  { name: '转化率', value: '8.9%', trend: -0.5 },
  { name: '留存率(7日)', value: '34.2%', trend: 1.8 },
  { name: '跳出率', value: '28.5%', trend: -3.2 },
  { name: '平均停留', value: '4m 32s', trend: 0.8 },
])

const runAnalysis = () => {
  // Placeholder for analysis logic
}
</script>

<style scoped lang="scss">
.analysis-page {
  .filter-card {
    margin-bottom: 24px;
  }

  .chart-row {
    margin-bottom: 24px;
  }

  .glass-card {
    background: var(--card-bg);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-slow);
    margin-bottom: 24px;

    &:hover {
      box-shadow: var(--shadow-lg);
    }

    :deep(.el-card__header) {
      padding: 20px 24px;
      border-bottom: 1px solid var(--border-light);
      background: transparent;
    }

    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);

    .title-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
      box-shadow: 0 0 8px rgba(99, 102, 241, 0.4);
    }
  }

  .chart {
    height: 400px;
  }

  .custom-table {
    :deep(.el-table__header-wrapper) {
      th {
        background: var(--bg-color);
        color: var(--text-secondary);
        font-weight: 600;
        font-size: 13px;
        border-bottom: 1px solid var(--border-light);
      }
    }

    :deep(.el-table__row) {
      transition: all var(--transition-fast);

      &:hover {
        background: rgba(99, 102, 241, 0.03) !important;
      }

      td {
        border-bottom: 1px solid var(--border-light);
        color: var(--text-regular);
      }
    }
  }

  .metrics-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 16px;

    .metric-item {
      padding: 20px;
      background: linear-gradient(135deg, var(--bg-color) 0%, #fff 100%);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
      transition: all var(--transition-base);

      &:hover {
        transform: translateY(-2px);
        box-shadow: var(--shadow-md);
      }

      .metric-name {
        font-size: 13px;
        color: var(--text-secondary);
        margin-bottom: 8px;
        font-weight: 500;
      }

      .metric-value {
        font-size: 24px;
        font-weight: 700;
        color: var(--text-primary);
        letter-spacing: -0.5px;
      }

      .metric-change {
        font-size: 12px;
        margin-top: 6px;
        display: flex;
        align-items: center;
        gap: 4px;
        font-weight: 600;

        &.up { color: var(--success-color); }
        &.down { color: var(--danger-color); }
      }
    }
  }

  .btn-gradient {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
    border: none;
    color: #fff;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    transition: all var(--transition-base);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .btn-outline {
    background: transparent;
    border: 1px solid var(--border-color);
    color: var(--text-regular);
    transition: all var(--transition-fast);

    &:hover {
      border-color: var(--primary-color);
      color: var(--primary-color);
    }
  }
}
</style>
