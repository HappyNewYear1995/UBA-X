<template>
  <div class="data-log-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">数据日志</span>
          <div class="header-actions">
            <el-button size="small" @click="handleRefresh">
              <Icon icon="ep:refresh" /> 刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="事件类型">
          <el-select v-model="queryParams.eventType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="页面浏览" value="page_view" />
            <el-option label="按钮点击" value="button_click" />
            <el-option label="表单提交" value="form_submit" />
            <el-option label="自定义事件" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="客户端">
          <el-input v-model="queryParams.appId" placeholder="请输入 AppId" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="设备 ID">
          <el-input v-model="queryParams.deviceId" placeholder="请输入设备 ID" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="用户 ID">
          <el-input v-model="queryParams.userId" placeholder="请输入用户 ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" /> 查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志统计 -->
      <el-row :gutter="16" class="log-stats">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <Icon icon="ep:document" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">总日志数</div>
              <div class="stat-value">{{ total }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon page">
              <Icon icon="ep:view" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">页面浏览</div>
              <div class="stat-value">{{ pageViewCount }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon event">
              <Icon icon="ep:operation" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">事件上报</div>
              <div class="stat-value">{{ eventCount }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon today">
              <Icon icon="ep:calendar" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">当前页日志</div>
              <div class="stat-value">{{ list.length }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 日志列表 -->
      <el-table v-loading="loading" :data="list" class="log-table mt-4">
        <el-table-column prop="logId" label="日志 ID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="appId" label="客户端" min-width="130" show-overflow-tooltip />
        <el-table-column prop="eventType" label="事件类型" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getEventTypeTag(row.eventType)" size="small">{{ getEventTypeLabel(row.eventType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceId" label="设备 ID" min-width="150" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户 ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="platform" label="平台" min-width="100" align="center" />
        <el-table-column prop="collectTime" label="采集时间" min-width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatDateTime(row.collectTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <Icon icon="ep:delete" /> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <Pagination
          v-model:page="queryParams.pageNo"
          v-model:limit="queryParams.pageSize"
          :total="total"
          @pagination="getList"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志 ID">{{ currentLog.logId }}</el-descriptions-item>
        <el-descriptions-item label="客户端">{{ currentLog.appId }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">
          <el-tag :type="getEventTypeTag(currentLog.eventType)" size="small">{{ getEventTypeLabel(currentLog.eventType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="设备 ID">{{ currentLog.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ currentLog.userId }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ currentLog.platform }}</el-descriptions-item>
        <el-descriptions-item label="采集时间">{{ formatDateTime(currentLog.collectTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentLog.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="事件属性" :span="2">
          <pre class="json-block">{{ formatProperties(currentLog.properties) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getDataLogPage,
  getDataLog,
  deleteDataLog,
  type DataLogRespVO,
  type DataLogPageReqVO
} from '@/api/ubax/collect'

defineOptions({ name: 'DataLog' })

const message = useMessage()

const loading = ref(false)
const list = ref<DataLogRespVO[]>([])
const total = ref(0)

const queryParams = ref<DataLogPageReqVO>({
  pageNo: 1,
  pageSize: 20,
  eventType: undefined,
  appId: undefined,
  deviceId: undefined,
  userId: undefined
})

const detailDialogVisible = ref(false)
const currentLog = ref<DataLogRespVO | null>(null)

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const getEventTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    page_view: '页面浏览',
    button_click: '按钮点击',
    form_submit: '表单提交',
    custom: '自定义事件'
  }
  return map[type] || type
}

const getEventTypeTag = (type: string) => {
  const map: Record<string, string> = {
    page_view: 'primary',
    button_click: 'success',
    form_submit: 'warning',
    custom: 'info'
  }
  return map[type] || ''
}

const formatProperties = (properties?: string) => {
  if (!properties) return '-'
  try {
    return JSON.stringify(JSON.parse(properties), null, 2)
  } catch {
    return properties
  }
}

const pageViewCount = computed(() => list.value.filter(l => l.eventType === 'page_view').length)
const eventCount = computed(() => list.value.filter(l => l.eventType !== 'page_view').length)

/** 获取列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getDataLogPage(queryParams.value)
    list.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 搜索 */
const handleQuery = () => {
  queryParams.value.pageNo = 1
  getList()
}

/** 重置 */
const handleReset = () => {
  queryParams.value = { pageNo: 1, pageSize: 20, eventType: undefined, appId: undefined, deviceId: undefined, userId: undefined }
  getList()
}

/** 刷新 */
const handleRefresh = () => {
  getList()
}

/** 查看详情 */
const handleViewDetail = async (row: DataLogRespVO) => {
  try {
    const data = await getDataLog(row.id)
    currentLog.value = data
    detailDialogVisible.value = true
  } catch { /* ignore */ }
}

/** 删除 */
const handleDelete = async (row: DataLogRespVO) => {
  await message.delConfirm(`确定要删除该数据日志吗？`)
  await deleteDataLog(row.id)
  message.success('删除成功')
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.data-log-container {
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
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

.search-form {
  margin-bottom: 16px;

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.log-stats {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: var(--radius-lg);
  background: var(--app-content-card-bg);
  border: 1px solid var(--app-content-card-border);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;

    &.total {
      background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
      color: #fff;
    }

    &.today {
      background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
      color: #fff;
    }

    &.page {
      background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
      color: #fff;
    }

    &.event {
      background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
      color: #fff;
    }
  }

  .stat-content {
    flex: 1;

    .stat-label {
      font-size: 13px;
      color: var(--app-content-text-color-secondary);
    }

    .stat-value {
      font-size: 20px;
      font-weight: 600;
      color: var(--app-content-text-color-primary);
      margin-top: 4px;
    }
  }
}

.log-table {
  :deep(.el-table__header) {
    th {
      background: var(--border-color-light);
      font-weight: 600;
    }
  }

  :deep(.el-table__row) {
    &:hover {
      background: var(--border-color-light);
    }
  }
}

.time-text {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 12px;
  color: var(--app-content-text-color-secondary);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.json-block {
  background: var(--app-content-bg-color);
  padding: 12px;
  border-radius: var(--radius-md);
  font-size: 12px;
  max-height: 200px;
  overflow: auto;
  margin: 0;
  font-family: 'SF Mono', 'Consolas', monospace;
}
</style>
