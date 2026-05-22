<template>
  <div class="funnel-analysis">
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">漏斗分析</span>
              <div class="header-actions">
                <el-select v-model="selectedFunnel" placeholder="选择漏斗" style="width: 200px">
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
                  style="width: 240px"
                />
                <el-button type="primary" @click="handleQuery">
                  <Icon icon="ep:search" /> 查询
                </el-button>
              </div>
            </div>
          </template>

          <!-- 漏斗图表 -->
          <Echart :options="funnelOptions" :height="400" />

          <!-- 漏斗步骤详情 -->
          <el-table :data="funnelSteps" style="width: 100%; margin-top: 20px">
            <el-table-column prop="step" label="步骤" width="150" />
            <el-table-column prop="users" label="用户数" width="120" />
            <el-table-column prop="conversionRate" label="转化率" width="120">
              <template #default="{ row }">
                <span :class="row.conversionRate < 50 ? 'text-red-500' : 'text-green-500'">
                  {{ row.conversionRate }}%
                </span>
              </template>
            </el-table-column>
            <el-table-column prop="lossRate" label="流失率" width="120">
              <template #default="{ row }">
                <span class="text-red-500">{{ row.lossRate }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="avgTime" label="平均耗时" width="120" />
            <el-table-column label="操作" width="100">
              <template #default>
                <el-button type="primary" link>详情</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'

defineOptions({ name: 'FunnelAnalysis' })

const selectedFunnel = ref('register_payment')
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()])

const funnelOptions = reactive<EChartsOption>({
  tooltip: { trigger: 'item', formatter: '{b} : {c}人 ({d}%)' },
  series: [
    {
      name: '转化漏斗',
      type: 'funnel',
      left: '10%',
      top: 20,
      bottom: 20,
      width: '80%',
      min: 0,
      max: 100,
      minSize: '0%',
      maxSize: '100%',
      sort: 'descending',
      gap: 2,
      label: { show: true, position: 'inside' },
      itemStyle: { borderColor: '#fff', borderWidth: 1 },
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
  { step: '访问用户', users: 12846, conversionRate: 100, lossRate: 0, avgTime: '-' },
  { step: '浏览商品', users: 10277, conversionRate: 80, lossRate: 20, avgTime: '2m 15s' },
  { step: '加入购物车', users: 7708, conversionRate: 75, lossRate: 25, avgTime: '5m 32s' },
  { step: '生成订单', users: 5139, conversionRate: 67, lossRate: 33, avgTime: '3m 48s' },
  { step: '完成支付', users: 2570, conversionRate: 50, lossRate: 50, avgTime: '4m 12s' }
])

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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
</style>
