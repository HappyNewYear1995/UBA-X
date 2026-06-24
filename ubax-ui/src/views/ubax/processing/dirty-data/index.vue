<template>
  <div class="dirty-data-log">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">脏数据处理日志</span>
          <el-badge :value="total" :max="999" type="warning" />
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="异常类型">
          <el-input v-model="queryParams.errorType" placeholder="请输入异常类型" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="数据来源">
          <el-input v-model="queryParams.source" placeholder="请输入数据来源" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="待处理" :value="0" />
            <el-option label="已处理" :value="1" />
            <el-option label="已忽略" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="管道ID">
          <el-input-number v-model="queryParams.pipelineId" placeholder="管道ID" :min="0" clearable style="width: 140px" />
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
        <el-table-column prop="errorType" label="异常类型" min-width="150" />
        <el-table-column prop="source" label="数据来源" min-width="150" />
        <el-table-column prop="errorCount" label="异常记录数" min-width="120" align="center" />
        <el-table-column prop="action" label="处理动作" min-width="120" />
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" align="center">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <Icon icon="ep:delete" /> 删除
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
    <el-dialog v-model="detailDialogVisible" title="异常日志详情" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentRow">
        <el-descriptions-item label="异常类型">{{ currentRow.errorType }}</el-descriptions-item>
        <el-descriptions-item label="数据来源">{{ currentRow.source }}</el-descriptions-item>
        <el-descriptions-item label="异常记录数">{{ currentRow.errorCount }}</el-descriptions-item>
        <el-descriptions-item label="处理动作">{{ currentRow.action }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentRow.status)" size="small">{{ getStatusLabel(currentRow.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="管道ID">{{ currentRow.pipelineId ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDateTime(currentRow.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ currentRow.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="详细信息" :span="2">
          <pre class="json-block">{{ formatDetail(currentRow.detail) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getErrorLogPage,
  getErrorLog,
  deleteErrorLog,
  type ErrorLogRespVO,
  type ErrorLogPageReqVO
} from '@/api/ubax/collect'

defineOptions({ name: 'DirtyDataLog' })

const message = useMessage()

const loading = ref(false)
const list = ref<ErrorLogRespVO[]>([])
const total = ref(0)

const queryParams = ref<ErrorLogPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  errorType: undefined,
  source: undefined,
  status: undefined,
  pipelineId: undefined
})

const detailDialogVisible = ref(false)
const currentRow = ref<ErrorLogRespVO | null>(null)

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const getStatusLabel = (status: number) => {
  const map: Record<number, string> = { 0: '待处理', 1: '已处理', 2: '已忽略' }
  return map[status] ?? '未知'
}

const getStatusTag = (status: number) => {
  const map: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'info' }
  return map[status] ?? ''
}

const formatDetail = (detail?: string) => {
  if (!detail) return '-'
  try {
    return JSON.stringify(JSON.parse(detail), null, 2)
  } catch {
    return detail
  }
}

/** 获取列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getErrorLogPage(queryParams.value)
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
  queryParams.value = { pageNo: 1, pageSize: 10, errorType: undefined, source: undefined, status: undefined, pipelineId: undefined }
  getList()
}

/** 查看详情 */
const handleViewDetail = async (row: ErrorLogRespVO) => {
  try {
    const data = await getErrorLog(row.id)
    currentRow.value = data
    detailDialogVisible.value = true
  } catch { /* ignore */ }
}

/** 删除 */
const handleDelete = async (row: ErrorLogRespVO) => {
  await message.delConfirm(`确定要删除该异常日志吗？`)
  await deleteErrorLog(row.id)
  message.success('删除成功')
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.dirty-data-log {
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
