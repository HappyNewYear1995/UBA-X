<template>
  <div class="dashboard-page">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <div class="stat-card-wrapper" :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="stat-card">
            <div class="stat-bg" :class="stat.color"></div>
            <div class="stat-content">
              <div class="stat-icon" :class="stat.color">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
            <div class="stat-trend" :class="stat.trendType">
              <el-icon v-if="stat.trendType"><Top v-if="stat.trendType === 'up'" /><Bottom v-else /></el-icon>
              <span>{{ stat.trend }}</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>用户活跃趋势</span>
              </div>
              <el-radio-group v-model="trendRange" size="small" class="custom-radio">
                <el-radio-button value="7d">近7天</el-radio-button>
                <el-radio-button value="30d">近30天</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <v-chart class="chart" :option="trendOption" autoresize />
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>事件类型分布</span>
              </div>
            </div>
          </template>
          <v-chart class="chart" :option="pieOption" autoresize />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="table-row">
      <el-col :span="24">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>实时事件流</span>
              </div>
              <el-button class="btn-gradient" size="small">查看全部</el-button>
            </div>
          </template>
          <el-table :data="eventList" style="width: 100%" class="custom-table">
            <el-table-column prop="time" label="时间" width="180" />
            <el-table-column prop="user" label="用户" width="150" />
            <el-table-column prop="event" label="事件类型" width="180" />
            <el-table-column prop="page" label="页面" />
            <el-table-column prop="device" label="设备" width="150" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <span class="status-dot" :class="row.status === 'normal' ? 'normal' : 'suspicious'"></span>
                <span>{{ row.status === 'normal' ? '正常' : '可疑' }}</span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent, TitleComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { User, View, Timer, Warning, Top, Bottom } from '@element-plus/icons-vue'

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent, TitleComponent])

const trendRange = ref('7d')

const stats = ref([
  { value: '128,456', label: '总用户数', icon: 'User', color: 'primary', trend: '+12.5% 较昨日', trendType: 'up' },
  { value: '2,847,392', label: '今日事件数', icon: 'View', color: 'success', trend: '+8.3% 较昨日', trendType: 'up' },
  { value: '4m 32s', label: '平均停留时长', icon: 'Timer', color: 'warning', trend: '-2.1% 较昨日', trendType: 'down' },
  { value: '3', label: '待处理异常', icon: 'Warning', color: 'danger', trend: '与昨日持平', trendType: '' },
])

const trendOption = {
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'],
    axisLine: { lineStyle: { color: '#e2e8f0' } },
    axisLabel: { color: '#64748b' },
  },
  yAxis: { 
    type: 'value',
    splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
    axisLabel: { color: '#64748b' },
  },
  series: [
    {
      name: '活跃用户',
      type: 'line',
      smooth: true,
      showSymbol: false,
      lineStyle: { width: 3 },
      data: [1200, 1320, 1010, 1340, 1900, 2300, 2100],
      itemStyle: { color: '#6366f1' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(99, 102, 241, 0.25)' },
            { offset: 1, color: 'rgba(99, 102, 241, 0.02)' },
          ],
        },
      },
    },
    {
      name: '新增用户',
      type: 'line',
      smooth: true,
      showSymbol: false,
      lineStyle: { width: 3 },
      data: [320, 420, 310, 440, 500, 630, 510],
      itemStyle: { color: '#10b981' },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(16, 185, 129, 0.2)' },
            { offset: 1, color: 'rgba(16, 185, 129, 0.02)' },
          ],
        },
      },
    },
  ],
}

const pieOption = {
  tooltip: { trigger: 'item' },
  legend: { bottom: '0%', left: 'center', itemWidth: 12, itemHeight: 12 },
  series: [
    {
      name: '事件类型',
      type: 'pie',
      radius: ['45%', '75%'],
      center: ['50%', '45%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 3 },
      label: { show: false, position: 'center' },
      emphasis: { 
        label: { show: true, fontSize: 18, fontWeight: 'bold', color: '#0f172a' },
        scaleSize: 10,
      },
      data: [
        { value: 1048, name: '页面浏览', itemStyle: { color: '#6366f1' } },
        { value: 735, name: '点击事件', itemStyle: { color: '#8b5cf6' } },
        { value: 580, name: '表单提交', itemStyle: { color: '#10b981' } },
        { value: 484, name: '视频播放', itemStyle: { color: '#f59e0b' } },
        { value: 300, name: '分享', itemStyle: { color: '#ef4444' } },
      ],
    },
  ],
}

const eventList = ref([
  { time: '2026-05-16 14:32:15', user: 'user_8f3a2', event: 'page_view', page: '/home', device: 'Chrome / Windows', status: 'normal' },
  { time: '2026-05-16 14:32:12', user: 'user_2c1b9', event: 'button_click', page: '/product/detail', device: 'Safari / iOS', status: 'normal' },
  { time: '2026-05-16 14:32:08', user: 'user_5d7e4', event: 'form_submit', page: '/checkout', device: 'Chrome / macOS', status: 'normal' },
  { time: '2026-05-16 14:31:55', user: 'user_1a9f3', event: 'video_play', page: '/tutorial', device: 'Firefox / Linux', status: 'suspicious' },
  { time: '2026-05-16 14:31:42', user: 'user_6b2c8', event: 'share', page: '/article/123', device: 'Chrome / Android', status: 'normal' },
])
</script>

<style scoped lang="scss">
.dashboard-page {
  .stats-row {
    margin-bottom: 24px;
  }

  .chart-row {
    margin-bottom: 24px;
  }

  .stat-card-wrapper {
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
  }

  .stat-card {
    position: relative;
    padding: 24px;
    background: var(--card-bg);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-slow);
    overflow: hidden;
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-xl);
      border-color: transparent;

      .stat-bg {
        opacity: 0.15;
        transform: scale(1.1);
      }

      .stat-icon {
        transform: scale(1.1) rotate(5deg);
      }
    }

    .stat-bg {
      position: absolute;
      top: -20px;
      right: -20px;
      width: 120px;
      height: 120px;
      border-radius: 50%;
      opacity: 0.08;
      transition: all var(--transition-slow);
      filter: blur(30px);

      &.primary { background: linear-gradient(135deg, #6366f1, #818cf8); }
      &.success { background: linear-gradient(135deg, #10b981, #34d399); }
      &.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
      &.danger { background: linear-gradient(135deg, #ef4444, #f87171); }
    }

    .stat-content {
      display: flex;
      align-items: center;
      margin-bottom: 16px;
      position: relative;
      z-index: 1;

      .stat-icon {
        width: 52px;
        height: 52px;
        border-radius: var(--radius-md);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #fff;
        margin-right: 16px;
        transition: all var(--transition-base);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

        &.primary { background: linear-gradient(135deg, #6366f1, #818cf8); }
        &.success { background: linear-gradient(135deg, #10b981, #34d399); }
        &.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
        &.danger { background: linear-gradient(135deg, #ef4444, #f87171); }
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: var(--text-primary);
          letter-spacing: -0.5px;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: var(--text-secondary);
          margin-top: 4px;
          font-weight: 500;
        }
      }
    }

    .stat-trend {
      font-size: 12px;
      display: flex;
      align-items: center;
      gap: 4px;
      font-weight: 500;
      padding-top: 12px;
      border-top: 1px solid var(--border-light);
      position: relative;
      z-index: 1;

      &.up { color: var(--success-color); }
      &.down { color: var(--danger-color); }
      &:not(.up):not(.down) { color: var(--text-secondary); }
    }
  }

  .glass-card {
    background: var(--card-bg);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-slow);

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
  }

  .chart {
    height: 350px;
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

  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 8px;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      width: 100%;
      height: 100%;
      border-radius: 50%;
      animation: pulse 2s infinite;
    }

    &.normal {
      background: var(--success-color);
      &::after { background: var(--success-color); }
    }

    &.suspicious {
      background: var(--warning-color);
      &::after { background: var(--warning-color); }
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
}

:deep(.custom-radio) {
  .el-radio-button__inner {
    border-radius: 8px;
    border: 1px solid var(--border-color);
    background: transparent;
    color: var(--text-regular);
    transition: all var(--transition-fast);

    &:hover {
      color: var(--primary-color);
    }
  }

  .el-radio-button__original-radio:checked + .el-radio-button__inner {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
    border-color: var(--primary-color);
    color: #fff;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
  }
}
</style>
