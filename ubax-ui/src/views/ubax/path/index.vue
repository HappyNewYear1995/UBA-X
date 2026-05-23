<template>
  <div class="path-analysis">
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户路径分析</span>
              <div class="header-actions">
                <el-select v-model="chartType" placeholder="图表类型" style="width: 150px">
                  <el-option label="桑基图" value="sankey" />
                  <el-option label="旅程图" value="journey" />
                </el-select>
                <el-select v-model="startEvent" placeholder="起始事件" style="width: 150px">
                  <el-option label="打开应用" value="app_open" />
                  <el-option label="首页浏览" value="home_view" />
                  <el-option label="登录" value="login" />
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

          <!-- 桑基图 -->
          <Echart :options="sankeyOptions" :height="350" />

          <!-- 路径统计 -->
          <el-table :data="pathStats" style="width: 100%; margin-top: 20px">
            <el-table-column prop="path" label="用户路径" min-width="300" />
            <el-table-column prop="users" label="用户数" min-width="120" />
            <el-table-column prop="percentage" label="占比" min-width="100">
              <template #default="{ row }">
                {{ row.percentage }}%
              </template>
            </el-table-column>
            <el-table-column prop="avgDuration" label="平均耗时" min-width="120" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'

defineOptions({ name: 'PathAnalysis' })

const chartType = ref('sankey')
const startEvent = ref('app_open')
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()])

const sankeyOptions = reactive<EChartsOption>({
  tooltip: { trigger: 'item', triggerOn: 'mousemove' },
  series: {
    type: 'sankey',
    layout: 'none',
    emphasis: { focus: 'adjacency' },
    data: [
      { name: '打开应用' },
      { name: '首页浏览' },
      { name: '商品列表' },
      { name: '商品详情' },
      { name: '加入购物车' },
      { name: '生成订单' },
      { name: '完成支付' },
      { name: '退出应用' }
    ],
    links: [
      { source: '打开应用', target: '首页浏览', value: 10000 },
      { source: '首页浏览', target: '商品列表', value: 8000 },
      { source: '首页浏览', target: '退出应用', value: 2000 },
      { source: '商品列表', target: '商品详情', value: 6000 },
      { source: '商品列表', target: '退出应用', value: 2000 },
      { source: '商品详情', target: '加入购物车', value: 4000 },
      { source: '商品详情', target: '退出应用', value: 2000 },
      { source: '加入购物车', target: '生成订单', value: 3000 },
      { source: '加入购物车', target: '退出应用', value: 1000 },
      { source: '生成订单', target: '完成支付', value: 2000 },
      { source: '生成订单', target: '退出应用', value: 1000 }
    ],
    lineStyle: {
      color: 'source',
      curveness: 0.5
    }
  }
})

const pathStats = ref([
  { path: '打开应用 → 首页浏览 → 商品列表 → 商品详情 → 加入购物车 → 生成订单 → 完成支付', users: 2000, percentage: 20, avgDuration: '8m 32s' },
  { path: '打开应用 → 首页浏览 → 商品列表 → 商品详情 → 退出应用', users: 2000, percentage: 20, avgDuration: '3m 15s' },
  { path: '打开应用 → 首页浏览 → 商品列表 → 退出应用', users: 2000, percentage: 20, avgDuration: '1m 45s' },
  { path: '打开应用 → 首页浏览 → 退出应用', users: 2000, percentage: 20, avgDuration: '0m 32s' },
  { path: '打开应用 → 退出应用', users: 2000, percentage: 20, avgDuration: '0m 12s' }
])

const handleQuery = () => {
  ElMessage.success('查询成功')
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.path-analysis {
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
