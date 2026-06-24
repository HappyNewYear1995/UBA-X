<template>
  <div class="collect-event">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">事件管理</span>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="事件类型">
          <el-input v-model="queryParams.eventType" placeholder="请输入事件类型" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="事件分类">
          <el-select v-model="queryParams.eventCategory" placeholder="请选择" clearable style="width: 140px">
            <el-option label="认证" value="authentication" />
            <el-option label="访问" value="access" />
            <el-option label="网络" value="network" />
            <el-option label="管理" value="admin" />
            <el-option label="系统" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行者类型">
          <el-select v-model="queryParams.actorType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="用户" value="user" />
            <el-option label="服务账号" value="service_account" />
            <el-option label="设备" value="device" />
            <el-option label="应用" value="application" />
          </el-select>
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="queryParams.result" placeholder="请选择" clearable style="width: 120px">
            <el-option label="成功" value="success" />
            <el-option label="失败" value="failure" />
            <el-option label="拒绝" value="denied" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="queryParams.severity" placeholder="请选择" clearable style="width: 120px">
            <el-option label="信息" value="info" />
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="严重" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item label="执行者ID">
          <el-input v-model="queryParams.actorId" placeholder="请输入执行者ID" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" /> 查询
          </el-button>
          <el-button @click="handleReset">
            <Icon icon="ep:refresh" /> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table v-loading="loading" :data="list" style="width: 100%">
        <el-table-column prop="eventId" label="事件ID" min-width="180" show-overflow-tooltip />
        <el-table-column prop="eventType" label="事件类型" min-width="130" show-overflow-tooltip />
        <el-table-column prop="eventCategory" label="事件分类" min-width="100" align="center">
          <template #default="{ row }">
            <span>{{ getCategoryLabel(row.eventCategory) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="actorId" label="执行者ID" min-width="130" show-overflow-tooltip />
        <el-table-column prop="actorType" label="执行者类型" min-width="110" align="center">
          <template #default="{ row }">
            <span>{{ getActorTypeLabel(row.actorType) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="动作" min-width="120" show-overflow-tooltip />
        <el-table-column prop="result" label="结果" min-width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getResultTag(row.result)" size="small">{{ getResultLabel(row.result) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getSeverityTag(row.severity)" size="small">{{ getSeverityLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="eventTime" label="事件时间" min-width="170" align="center">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.eventTime) }}</span>
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

      <!-- 分页 -->
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="事件详情" width="800px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="事件ID" :span="2">{{ detailData.eventId }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">{{ detailData.eventType }}</el-descriptions-item>
        <el-descriptions-item label="事件分类">{{ getCategoryLabel(detailData.eventCategory) }}</el-descriptions-item>
        <el-descriptions-item label="数据源ID">{{ detailData.sourceId }}</el-descriptions-item>
        <el-descriptions-item label="管道ID">{{ detailData.pipelineId }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">执行者</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="执行者ID">{{ detailData.actorId }}</el-descriptions-item>
        <el-descriptions-item label="执行者类型">{{ getActorTypeLabel(detailData.actorType) }}</el-descriptions-item>
        <el-descriptions-item label="执行者属性" :span="2">
          <pre class="json-content">{{ formatJson(detailData.actorAttributes) }}</pre>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">时间</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="事件时间">{{ formatDateTime(detailData.eventTime) }}</el-descriptions-item>
        <el-descriptions-item label="摄入时间">{{ formatDateTime(detailData.ingestTime) }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ formatDateTime(detailData.processTime) }}</el-descriptions-item>
        <el-descriptions-item label="时区">{{ detailData.eventTimeZone }}</el-descriptions-item>
        <el-descriptions-item label="位置">{{ detailData.location }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">行为</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="动作">{{ detailData.action }}</el-descriptions-item>
        <el-descriptions-item label="目标资源">{{ detailData.targetResource }}</el-descriptions-item>
        <el-descriptions-item label="目标资源类型">{{ detailData.targetResourceType }}</el-descriptions-item>
        <el-descriptions-item label="结果">
          <el-tag :type="getResultTag(detailData.result)" size="small">{{ getResultLabel(detailData.result) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="getSeverityTag(detailData.severity)" size="small">{{ getSeverityLabel(detailData.severity) }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">扩展信息</el-divider>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="会话ID">{{ detailData.sessionId }}</el-descriptions-item>
        <el-descriptions-item label="关联ID">{{ detailData.correlationId }}</el-descriptions-item>
        <el-descriptions-item label="标签">{{ detailData.tags }}</el-descriptions-item>
        <el-descriptions-item label="富化信息" :span="2">
          <pre class="json-content">{{ formatJson(detailData.enrichments) }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="原始事件" :span="2">
          <pre class="json-content">{{ formatJson(detailData.rawEvent) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getEventPage,
  getEvent,
  type EventRespVO,
  type EventPageReqVO
} from '@/api/ubax/collect'

defineOptions({ name: 'CollectEvent' })

const loading = ref(false)
const list = ref<EventRespVO[]>([])
const total = ref(0)

const queryParams = ref<EventPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  eventType: undefined,
  eventCategory: undefined,
  actorType: undefined,
  actorId: undefined,
  result: undefined,
  severity: undefined
})

const detailVisible = ref(false)
const detailData = ref<EventRespVO>({} as EventRespVO)

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const formatJson = (str?: string) => {
  if (!str) return ''
  try {
    return JSON.stringify(JSON.parse(str), null, 2)
  } catch {
    return str
  }
}

const getCategoryLabel = (val?: string) => {
  const map: Record<string, string> = { authentication: '认证', access: '访问', network: '网络', admin: '管理', system: '系统' }
  return map[val || ''] || val || ''
}

const getActorTypeLabel = (val?: string) => {
  const map: Record<string, string> = { user: '用户', service_account: '服务账号', device: '设备', application: '应用' }
  return map[val || ''] || val || ''
}

const getResultLabel = (val?: string) => {
  const map: Record<string, string> = { success: '成功', failure: '失败', denied: '拒绝' }
  return map[val || ''] || val || ''
}

const getResultTag = (val?: string) => {
  const map: Record<string, string> = { success: 'success', failure: 'danger', denied: 'warning' }
  return map[val || ''] || ''
}

const getSeverityLabel = (val?: string) => {
  const map: Record<string, string> = { info: '信息', low: '低', medium: '中', high: '高', critical: '严重' }
  return map[val || ''] || val || ''
}

const getSeverityTag = (val?: string) => {
  const map: Record<string, string> = { info: 'info', low: 'success', medium: 'warning', high: 'danger', critical: 'danger' }
  return map[val || ''] || ''
}

/** 获取列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getEventPage(queryParams.value)
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
  queryParams.value = { pageNo: 1, pageSize: 10, eventType: undefined, eventCategory: undefined, actorType: undefined, actorId: undefined, result: undefined, severity: undefined }
  getList()
}

/** 查看详情 */
const handleDetail = async (row: EventRespVO) => {
  try {
    const data = await getEvent(row.id)
    detailData.value = data
    detailVisible.value = true
  } catch { /* ignore */ }
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.collect-event {
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

.search-form {
  margin-bottom: 16px;

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.json-content {
  margin: 0;
  padding: 8px;
  background: var(--app-content-bg-color);
  border-radius: 4px;
  font-size: 12px;
  line-height: 1.5;
  max-height: 200px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-all;
}
</style>
