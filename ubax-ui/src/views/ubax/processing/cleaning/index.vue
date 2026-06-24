<template>
  <div class="data-cleaning">
    <el-row :gutter="16">
      <!-- 自动化预处理 -->
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">自动化预处理</span>
              <el-button type="primary" size="small" @click="handleCreate">
                <Icon icon="ep:plus" /> 新增规则
              </el-button>
            </div>
          </template>
          <el-table v-loading="loading" :data="pipelineList" style="width: 100%">
            <el-table-column prop="name" label="规则名称" min-width="200" />
            <el-table-column prop="type" label="规则类型" min-width="150">
              <template #default="{ row }">
                <el-tag :type="getPipelineTypeTag(row.type)" size="small">{{ getPipelineTypeLabel(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" min-width="100">
              <template #default="{ row }">
                <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
              </template>
            </el-table-column>
            <el-table-column prop="processedCount" label="已处理数据量" min-width="150">
              <template #default="{ row }">
                <span>{{ row.processedCount ?? 0 }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" min-width="180">
              <template #default="{ row }">
                <span>{{ formatDateTime(row.updateTime) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="180" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="handleUpdate(row)">
                  <Icon icon="ep:edit" /> 编辑
                </el-button>
                <el-button type="danger" link @click="handleDelete(row)">
                  <Icon icon="ep:delete" /> 删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择规则类型" style="width: 100%">
            <el-option label="格式转换" value="format_convert" />
            <el-option label="数据清洗" value="data_clean" />
            <el-option label="数据映射" value="data_map" />
            <el-option label="数据过滤" value="data_filter" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="配置" prop="config">
          <el-input v-model="formData.config" type="textarea" :rows="4" placeholder="请输入配置JSON" />
        </el-form-item>
        <el-form-item label="关联事件" prop="eventId">
          <el-select v-model="formData.eventId" placeholder="请选择关联事件" clearable style="width: 100%">
            <el-option v-for="event in eventList" :key="event.id" :label="event.name" :value="event.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getCleanPipelinePage,
  getCleanPipeline,
  createCleanPipeline,
  updateCleanPipeline,
  deleteCleanPipeline,
  type CleanPipelineRespVO,
  type CleanPipelineSaveReqVO,
  type CleanPipelinePageReqVO
} from '@/api/ubax/collect'
import { getSimpleEventConfigList, type EventConfigRespVO } from '@/api/ubax/collect'

defineOptions({ name: 'DataCleaning' })

const message = useMessage()

const loading = ref(false)
const pipelineList = ref<CleanPipelineRespVO[]>([])
const total = ref(0)

const queryParams = ref<CleanPipelinePageReqVO>({
  pageNo: 1,
  pageSize: 100
})

const eventList = ref<EventConfigRespVO[]>([])

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref<CleanPipelineSaveReqVO>({
  name: '',
  status: 1,
  sort: 0
})

const formRules = {
  name: [{ required: true, message: '规则名称不能为空', trigger: 'blur' }]
}

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const getPipelineTypeLabel = (type: string) => {
  const map: Record<string, string> = {
    format_convert: '格式转换',
    data_clean: '数据清洗',
    data_map: '数据映射',
    data_filter: '数据过滤'
  }
  return map[type] || type
}

const getPipelineTypeTag = (type: string) => {
  const map: Record<string, string> = {
    format_convert: 'primary',
    data_clean: 'success',
    data_map: 'warning',
    data_filter: 'info'
  }
  return map[type] || ''
}

/** 获取管道列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getCleanPipelinePage(queryParams.value)
    pipelineList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

/** 获取事件列表 */
const loadEventList = async () => {
  try {
    const data = await getSimpleEventConfigList()
    eventList.value = data || []
  } catch { /* ignore */ }
}

/** 新增 */
const handleCreate = () => {
  dialogTitle.value = '新增清洗规则'
  formData.value = { name: '', status: 1, sort: 0 }
  dialogVisible.value = true
}

/** 编辑 */
const handleUpdate = async (row: CleanPipelineRespVO) => {
  dialogTitle.value = '编辑清洗规则'
  try {
    const data = await getCleanPipeline(row.id)
    formData.value = { ...data }
    dialogVisible.value = true
  } catch { /* ignore */ }
}

/** 删除 */
const handleDelete = async (row: CleanPipelineRespVO) => {
  await message.delConfirm(`确定要删除清洗规则「${row.name}」吗？`)
  await deleteCleanPipeline(row.id)
  message.success('删除成功')
  getList()
}

/** 状态变更 */
const handleStatusChange = async (row: CleanPipelineRespVO) => {
  try {
    await updateCleanPipeline({ id: row.id, name: row.name, status: row.status })
    message.success('状态更新成功')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

/** 提交表单 */
const submitForm = async () => {
  await formRef.value?.validate()
  if (formData.value.id) {
    await updateCleanPipeline(formData.value)
    message.success('更新成功')
  } else {
    await createCleanPipeline(formData.value)
    message.success('创建成功')
  }
  dialogVisible.value = false
  getList()
}

onMounted(() => {
  getList()
  loadEventList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.data-cleaning {
  padding: 16px;
  background: var(--app-content-bg-color);
  min-height: 100%;
}

.dashboard-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;
  height: 100%;

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
</style>
