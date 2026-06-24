<template>
  <div class="event-config">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">事件配置管理</span>
          <el-button type="primary" size="small" @click="handleCreate">
            <Icon icon="ep:plus" /> 新增配置
          </el-button>
        </div>
      </template>

      <!-- 搜索栏 -->
      <el-form :model="queryParams" inline class="search-form">
        <el-form-item label="配置名称">
          <el-input v-model="queryParams.name" placeholder="请输入配置名称" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="配置编码">
          <el-input v-model="queryParams.code" placeholder="请输入配置编码" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="queryParams.eventType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="页面浏览" value="page_view" />
            <el-option label="点击事件" value="click" />
            <el-option label="自定义事件" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
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
        <el-table-column prop="name" label="配置名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="code" label="配置编码" min-width="150" show-overflow-tooltip />
        <el-table-column prop="dataSourceType" label="数据源类型" min-width="120" />
        <el-table-column prop="eventType" label="事件类型" min-width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="getEventTypeTag(row.eventType)" size="small">{{ getEventTypeLabel(row.eventType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" align="center">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
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

      <!-- 分页 -->
      <Pagination
        v-model:page="queryParams.pageNo"
        v-model:limit="queryParams.pageSize"
        :total="total"
        @pagination="getList"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="110px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入配置编码" />
        </el-form-item>
        <el-form-item label="数据源ID" prop="dataSourceId">
          <el-input-number v-model="formData.dataSourceId" :min="0" placeholder="请输入数据源ID" style="width: 100%" />
        </el-form-item>
        <el-form-item label="数据源类型" prop="dataSourceType">
          <el-input v-model="formData.dataSourceType" placeholder="请输入数据源类型" />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="formData.eventType" placeholder="请选择事件类型" style="width: 100%">
            <el-option label="页面浏览" value="page_view" />
            <el-option label="点击事件" value="click" />
            <el-option label="自定义事件" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件属性" prop="properties">
          <el-input v-model="formData.properties" type="textarea" :rows="3" placeholder="请输入事件属性JSON" />
        </el-form-item>
        <el-form-item label="过滤条件" prop="filterCondition">
          <el-input v-model="formData.filterCondition" type="textarea" :rows="3" placeholder="请输入过滤条件JSON" />
        </el-form-item>
        <el-form-item label="字段映射" prop="fieldMapping">
          <el-input v-model="formData.fieldMapping" type="textarea" :rows="3" placeholder="请输入字段映射JSON" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
  getEventConfigPage,
  getEventConfig,
  createEventConfig,
  updateEventConfig,
  deleteEventConfig,
  type EventConfigRespVO,
  type EventConfigSaveReqVO,
  type EventConfigPageReqVO
} from '@/api/ubax/collect'

defineOptions({ name: 'EventConfig' })

const message = useMessage()

const loading = ref(false)
const list = ref<EventConfigRespVO[]>([])
const total = ref(0)

const queryParams = ref<EventConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  eventType: undefined,
  status: undefined
})

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref<EventConfigSaveReqVO>({
  name: '',
  code: '',
  status: 1
})

const formRules = {
  name: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '配置编码不能为空', trigger: 'blur' }]
}

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const getEventTypeLabel = (type: string) => {
  const map: Record<string, string> = { page_view: '页面浏览', click: '点击事件', custom: '自定义事件' }
  return map[type] || type
}

const getEventTypeTag = (type: string) => {
  const map: Record<string, string> = { page_view: 'primary', click: 'success', custom: 'info' }
  return map[type] || ''
}

/** 获取列表 */
const getList = async () => {
  loading.value = true
  try {
    const data = await getEventConfigPage(queryParams.value)
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
  queryParams.value = { pageNo: 1, pageSize: 10, name: undefined, code: undefined, eventType: undefined, status: undefined }
  getList()
}

/** 新增 */
const handleCreate = () => {
  dialogTitle.value = '新增事件配置'
  formData.value = { name: '', code: '', status: 1 }
  dialogVisible.value = true
}

/** 编辑 */
const handleUpdate = async (row: EventConfigRespVO) => {
  dialogTitle.value = '编辑事件配置'
  try {
    const data = await getEventConfig(row.id)
    formData.value = { ...data }
    dialogVisible.value = true
  } catch { /* ignore */ }
}

/** 删除 */
const handleDelete = async (row: EventConfigRespVO) => {
  await message.delConfirm(`确定要删除事件配置「${row.name}」吗？`)
  await deleteEventConfig(row.id)
  message.success('删除成功')
  getList()
}

/** 状态变更 */
const handleStatusChange = async (row: EventConfigRespVO) => {
  try {
    await updateEventConfig({ id: row.id, name: row.name, code: row.code, status: row.status })
    message.success('状态更新成功')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

/** 提交表单 */
const submitForm = async () => {
  await formRef.value?.validate()
  if (formData.value.id) {
    await updateEventConfig(formData.value)
    message.success('更新成功')
  } else {
    await createEventConfig(formData.value)
    message.success('创建成功')
  }
  dialogVisible.value = false
  getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.event-config {
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
</style>
