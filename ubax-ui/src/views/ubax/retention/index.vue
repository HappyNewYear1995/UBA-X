<template>
  <div class="retention-analysis">
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">留存分析</span>
              <div class="header-actions">
                <el-select v-model="retentionType" placeholder="留存类型" style="width: 150px">
                  <el-option label="次日留存" value="next_day" />
                  <el-option label="7日留存" value="7_days" />
                  <el-option label="30日留存" value="30_days" />
                </el-select>
                <el-select v-model="dimension" placeholder="统计维度" style="width: 120px">
                  <el-option label="按日" value="day" />
                  <el-option label="按周" value="week" />
                  <el-option label="按月" value="month" />
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

          <!-- 留存趋势图 -->
          <Echart :options="retentionTrendOptions" :height="350" />

          <!-- 留存表格 -->
          <el-table :data="retentionTable" style="width: 100%; margin-top: 20px" border>
            <el-table-column prop="date" label="日期" width="120" fixed />
            <el-table-column prop="newUsers" label="新增用户" width="100" />
            <el-table-column prop="day1" label="第1天" width="90">
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.day1) }">{{ row.day1 }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="day2" label="第2天" width="90">
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.day2) }">{{ row.day2 }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="day3" label="第3天" width="90">
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.day3) }">{{ row.day3 }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="day4" label="第4天" width="90">
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.day4) }">{{ row.day4 }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="day5" label="第5天" width="90">
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.day5) }">{{ row.day5 }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="day6" label="第6天" width="90">
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.day6) }">{{ row.day6 }}%</span>
              </template>
            </el-table-column>
            <el-table-column prop="day7" label="第7天" width="90">
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.day7) }">{{ row.day7 }}%</span>
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

defineOptions({ name: 'RetentionAnalysis' })

const retentionType = ref('next_day')
const dimension = ref('day')
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()])

const retentionTrendOptions = reactive<EChartsOption>({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['次日留存', '7日留存', '30日留存'], top: 10 },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
  },
  yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
  series: [
    {
      name: '次日留存',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#667eea' },
      data: [45, 52, 48, 55, 60, 42, 38]
    },
    {
      name: '7日留存',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#4facfe' },
      data: [25, 30, 28, 32, 35, 22, 20]
    },
    {
      name: '30日留存',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#43e97b' },
      data: [12, 15, 14, 18, 20, 10, 8]
    }
  ]
})

const retentionTable = ref([
  { date: '05-15', newUsers: 1200, day1: 45, day2: 38, day3: 32, day4: 28, day5: 25, day6: 22, day7: 20 },
  { date: '05-16', newUsers: 1350, day1: 48, day2: 40, day3: 35, day4: 30, day5: 27, day6: 24, day7: 22 },
  { date: '05-17', newUsers: 1100, day1: 42, day2: 36, day3: 30, day4: 26, day5: 23, day6: 20, day7: 18 },
  { date: '05-18', newUsers: 1450, day1: 50, day2: 42, day3: 37, day4: 32, day5: 28, day6: 25, day7: 23 },
  { date: '05-19', newUsers: 1280, day1: 46, day2: 39, day3: 33, day4: 29, day5: 26, day6: 23, day7: 21 }
])

const getRetentionColor = (value: number) => {
  if (value >= 40) return '#43e97b'
  if (value >= 25) return '#4facfe'
  return '#fa709a'
}

const handleQuery = () => {
  ElMessage.success('查询成功')
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.retention-analysis {
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
