<template>
  <div class="monitor-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">运行监控</span>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="queryParams.agentUuid"
          placeholder="搜索 Agent UUID"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @keyup.enter="handleQuery"
        />
        <el-select
          v-model="queryParams.metricType"
          placeholder="指标类型"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="事件指标" value="event" />
          <el-option label="流量指标" value="traffic" />
          <el-option label="性能指标" value="performance" />
        </el-select>
        <el-select
          v-model="queryParams.timeGranularity"
          placeholder="时间粒度"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="分钟" value="minute" />
          <el-option label="小时" value="hour" />
          <el-option label="天" value="day" />
        </el-select>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
      </div>

      <el-table v-loading="loading" :data="metricList" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="agentUuid" label="Agent UUID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="metricType" label="指标类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getMetricTypeTagType(row.metricType)" size="small" round>
              {{ getMetricTypeName(row.metricType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="metricName" label="指标名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="metricValue" label="指标值" width="120" align="center">
          <template #default="{ row }">
            <span class="metric-value">{{ row.metricValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="metricUnit" label="单位" width="80" align="center" />
        <el-table-column prop="timeGranularity" label="时间粒度" width="100" align="center">
          <template #default="{ row }">
            {{ getTimeGranularityName(row.timeGranularity) }}
          </template>
        </el-table-column>
        <el-table-column prop="metricTime" label="指标时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDate(row.metricTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              <Icon icon="ep:view" /> 详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <el-dialog v-model="detailDialogVisible" title="指标详情" width="600px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ detailData.id }}</el-descriptions-item>
        <el-descriptions-item label="Agent UUID">{{ detailData.agentUuid }}</el-descriptions-item>
        <el-descriptions-item label="指标类型">
          {{ getMetricTypeName(detailData.metricType) }}
        </el-descriptions-item>
        <el-descriptions-item label="指标名称">{{ detailData.metricName }}</el-descriptions-item>
        <el-descriptions-item label="指标值">
          <span class="metric-value">{{ detailData.metricValue }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="单位">{{ detailData.metricUnit }}</el-descriptions-item>
        <el-descriptions-item label="时间粒度">
          {{ getTimeGranularityName(detailData.timeGranularity) }}
        </el-descriptions-item>
        <el-descriptions-item label="指标时间">
          {{ formatDate(detailData.metricTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">
          {{ formatDate(detailData.startTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="结束时间">
          {{ formatDate(detailData.endTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="扩展数据" :span="2">
          {{ detailData.extraData || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'MonitorMetrics' })

import {
  getMetricPage,
  getMetric,
  type MonitorMetricRespVO,
  type MonitorMetricPageReqVO
} from '@/api/ubax/gather/monitor'

const loading = ref(false)
const metricList = ref<MonitorMetricRespVO[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const detailData = ref<MonitorMetricRespVO>({} as MonitorMetricRespVO)

const queryParams = ref<MonitorMetricPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  agentUuid: undefined,
  metricType: undefined,
  metricName: undefined,
  timeGranularity: undefined,
  metricTime: undefined,
  createTime: undefined
})

const metricTypeMap: Record<string, string> = {
  event: '事件指标',
  traffic: '流量指标',
  performance: '性能指标'
}

const timeGranularityMap: Record<string, string> = {
  minute: '分钟',
  hour: '小时',
  day: '天'
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getMetricPage(queryParams.value)
    metricList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => {
  queryParams.value.pageNo = 1
  getList()
}

const resetQuery = () => {
  queryParams.value = {
    pageNo: 1,
    pageSize: 10,
    agentUuid: undefined,
    metricType: undefined,
    metricName: undefined,
    timeGranularity: undefined,
    metricTime: undefined,
    createTime: undefined
  }
  handleQuery()
}

const handleDetail = async (row: MonitorMetricRespVO) => {
  detailData.value = await getMetric(row.id)
  detailDialogVisible.value = true
}

const getMetricTypeName = (type: string) => {
  return metricTypeMap[type] || type
}

const getMetricTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    event: 'primary',
    traffic: 'success',
    performance: 'warning'
  }
  return map[type] || ''
}

const getTimeGranularityName = (granularity: string) => {
  return timeGranularityMap[granularity] || granularity
}

const formatDate = (date: Date | string | undefined) => {
  if (!date) return '-'
  return date.toString()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.monitor-container {
  padding: 16px;
}

.dashboard-card {
  border-radius: 8px;

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--el-border-color-light);
    padding: 14px 20px;
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
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.filter-select {
  width: 130px;
}

.metric-value {
  font-weight: 600;
  color: var(--el-color-primary);
}
</style>
