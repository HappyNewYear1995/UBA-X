<template>
  <div class="funnel-analysis">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-left">
          <el-select v-model="selectedFunnel" placeholder="选择漏斗" class="filter-select" @change="handleQuery">
            <el-option label="注册到支付" value="register_payment" />
            <el-option label="浏览到购买" value="view_purchase" />
            <el-option label="登录到活跃" value="login_active" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="filter-date"
            @change="handleQuery"
          />
        </div>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" /> 查询
        </el-button>
      </div>
    </el-card>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16" class="metrics-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <Icon icon="ep:user" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">总用户数</div>
            <div class="metric-value">{{ formatNumber(totalUsers) }}</div>
            <div class="metric-trend">
              <span class="trend-up">
                <Icon icon="ep:top" :size="12" /> 12.5%
              </span>
              <span class="trend-label">较上期</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
            <Icon icon="ep:check" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">最终转化</div>
            <div class="metric-value">{{ finalConversion }}%</div>
            <div class="metric-trend">
              <span class="trend-up">
                <Icon icon="ep:top" :size="12" /> 3.2%
              </span>
              <span class="trend-label">较上期</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
            <Icon icon="ep:switch" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">平均转化率</div>
            <div class="metric-value">{{ avgConversion }}%</div>
            <div class="metric-trend">
              <span class="trend-down">
                <Icon icon="ep:bottom" :size="12" /> 1.8%
              </span>
              <span class="trend-label">较上期</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <Icon icon="ep:timer" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">平均耗时</div>
            <div class="metric-value">{{ avgTime }}</div>
            <div class="metric-trend">
              <span class="trend-label">全程时长</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 漏斗图表 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">转化漏斗</span>
          <div class="header-actions">
            <el-radio-group v-model="chartView" size="small">
              <el-radio-button label="funnel">漏斗图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      <Echart :options="funnelOptions" :height="320" />
    </el-card>

    <!-- 漏斗步骤详情 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">步骤详情</span>
        </div>
      </template>
      <el-table :data="funnelSteps" class="funnel-table" stripe>
        <el-table-column prop="step" label="步骤" min-width="150">
          <template #default="{ row, $index }">
            <div class="step-cell">
              <div class="step-badge" :style="{ background: stepColors[$index] }">{{ $index + 1 }}</div>
              <span class="step-name">{{ row.step }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="users" label="用户数" min-width="120" align="right">
          <template #default="{ row }">
            <span class="users-value">{{ formatNumber(row.users) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conversionRate" label="步骤转化率" min-width="130" align="center">
          <template #default="{ row, $index }">
            <div class="conversion-cell">
              <div class="conversion-bar">
                <div class="conversion-fill" :style="{ width: row.conversionRate + '%', background: stepColors[$index] }"></div>
              </div>
              <span :class="row.conversionRate < 50 ? 'text-danger' : 'text-success'">{{ row.conversionRate }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="overallRate" label="总转化率" min-width="120" align="center">
          <template #default="{ row }">
            <span class="overall-rate">{{ row.overallRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="lossRate" label="流失率" min-width="120" align="center">
          <template #default="{ row }">
            <span class="text-danger">{{ row.lossRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgTime" label="平均耗时" min-width="120" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.avgTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="100" fixed="right" align="center">
          <template #default>
            <el-button type="primary" link>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'

defineOptions({ name: 'FunnelAnalysis' })

const selectedFunnel = ref('register_payment')
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()])
const chartView = ref('funnel')

const stepColors = ['#667eea', '#764ba2', '#4facfe', '#43e97b', '#fa709a']

const totalUsers = computed(() => funnelSteps.value[0]?.users || 0)
const finalConversion = computed(() => {
  const steps = funnelSteps.value
  if (steps.length < 2) return 0
  const last = steps[steps.length - 1]
  return Math.round((last.users / steps[0].users) * 100)
})
const avgConversion = computed(() => {
  const steps = funnelSteps.value
  if (steps.length < 2) return 0
  const rates = steps.slice(1).map(s => s.conversionRate)
  return Math.round(rates.reduce((a, b) => a + b, 0) / rates.length)
})
const avgTime = computed(() => '15m 47s')

const funnelOptions = reactive<EChartsOption>({
  tooltip: { trigger: 'item', formatter: '{b} : {c}人 ({d}%)' },
  series: [
        {
          name: '转化漏斗',
          type: 'funnel',
          left: '20%',
          top: 20,
          bottom: 20,
          width: '60%',
          min: 0,
          max: 100,
          minSize: '15%',
          maxSize: '85%',
          sort: 'descending',
          gap: 6,
          label: {
            show: true,
            position: 'inside',
            formatter: '{b}\n{c}人',
            fontSize: 12,
            color: '#fff'
          },
          labelLine: { length: 8, lineStyle: { width: 1, type: 'solid' } },
          itemStyle: { borderColor: '#fff', borderWidth: 1, shadowBlur: 8, shadowColor: 'rgba(0,0,0,0.1)' },
          emphasis: {
            itemStyle: { shadowBlur: 15, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' }
          },
          data: [
            { value: 100, name: '访问用户', itemStyle: { color: '#667eea' } },
            { value: 80, name: '浏览商品', itemStyle: { color: '#764ba2' } },
            { value: 60, name: '加入购物车', itemStyle: { color: '#4facfe' } },
            { value: 40, name: '生成订单', itemStyle: { color: '#43e97b' } },
            { value: 20, name: '完成支付', itemStyle: { color: '#fa709a' } }
          ]
        }
      ]
})

const funnelSteps = ref([
  { step: '访问用户', users: 12846, conversionRate: 100, overallRate: 100, lossRate: 0, avgTime: '-' },
  { step: '浏览商品', users: 10277, conversionRate: 80, overallRate: 80, lossRate: 20, avgTime: '2m 15s' },
  { step: '加入购物车', users: 7708, conversionRate: 75, overallRate: 60, lossRate: 25, avgTime: '5m 32s' },
  { step: '生成订单', users: 5139, conversionRate: 67, overallRate: 40, lossRate: 33, avgTime: '3m 48s' },
  { step: '完成支付', users: 2570, conversionRate: 50, overallRate: 20, lossRate: 50, avgTime: '4m 12s' }
])

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const handleQuery = () => {
  ElMessage.success('查询成功')
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.funnel-analysis {
  padding: 16px;
  background: var(--app-content-bg-color);
  min-height: 100%;
}

// 筛选栏
.filter-card {
  margin-bottom: 16px;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;

  :deep(.el-card__body) {
    padding: 12px 20px;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  width: 180px;
}

.filter-date {
  width: 260px;
}

// 指标卡片
.metrics-row {
  margin-bottom: 16px;
}

.metric-card {
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

  .metric-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    color: #fff;
    flex-shrink: 0;
  }

  .metric-content {
    flex: 1;
    min-width: 0;

    .metric-label {
      font-size: 13px;
      color: var(--app-content-text-color-secondary);
      margin-bottom: 4px;
    }

    .metric-value {
      font-size: 24px;
      font-weight: 600;
      color: var(--app-content-text-color-primary);
      line-height: 1.2;
    }

    .metric-trend {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 4px;
      font-size: 12px;

      .trend-up {
        color: #10b981;
        display: flex;
        align-items: center;
        gap: 2px;
      }

      .trend-down {
        color: #ef4444;
        display: flex;
        align-items: center;
        gap: 2px;
      }

      .trend-label {
        color: var(--app-content-text-color-secondary);
      }
    }
  }
}

// 图表卡片
.chart-card {
  margin-bottom: 16px;
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

// 表格卡片
.table-card {
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
    --el-table-border-color: var(--app-content-card-border);
    --el-table-header-bg-color: var(--app-content-card-bg);
  }
}

// 通用卡片头部
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

// 步骤单元格
.step-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.step-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-name {
  font-weight: 500;
  color: var(--app-content-text-color-primary);
}

// 用户数
.users-value {
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

// 转化率单元格
.conversion-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conversion-bar {
  width: 60px;
  height: 6px;
  background: var(--app-content-card-border);
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}

.conversion-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

// 总转化率
.overall-rate {
  font-weight: 500;
  color: var(--app-content-text-color-primary);
}

// 时间文本
.time-text {
  color: var(--app-content-text-color-secondary);
  font-size: 13px;
}

// 文字颜色
.text-danger {
  color: #ef4444;
}

.text-success {
  color: #10b981;
}
</style>
