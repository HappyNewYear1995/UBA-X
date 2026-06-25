<template>
  <div class="collect-event">
    <el-card shadow="never" class="dashboard-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="事件列表" name="events" />
        <el-tab-pane label="事件配置" name="config" />
      </el-tabs>

      <!-- 事件列表 Tab -->
      <template v-if="activeTab === 'events'">
        <el-form :model="eventQueryParams" inline class="search-form">
          <el-form-item label="事件类型">
            <el-input v-model="eventQueryParams.eventType" placeholder="请输入事件类型" clearable style="width: 160px" @keyup.enter="handleEventQuery" />
          </el-form-item>
          <el-form-item label="事件分类">
            <el-select v-model="eventQueryParams.eventCategory" placeholder="请选择" clearable style="width: 140px">
              <el-option label="认证" value="authentication" />
              <el-option label="访问" value="access" />
              <el-option label="网络" value="network" />
              <el-option label="管理" value="admin" />
              <el-option label="系统" value="system" />
            </el-select>
          </el-form-item>
          <el-form-item label="执行者类型">
            <el-select v-model="eventQueryParams.actorType" placeholder="请选择" clearable style="width: 140px">
              <el-option label="用户" value="user" />
              <el-option label="服务账号" value="service_account" />
              <el-option label="设备" value="device" />
              <el-option label="应用" value="application" />
            </el-select>
          </el-form-item>
          <el-form-item label="结果">
            <el-select v-model="eventQueryParams.result" placeholder="请选择" clearable style="width: 120px">
              <el-option label="成功" value="success" />
              <el-option label="失败" value="failure" />
              <el-option label="拒绝" value="denied" />
            </el-select>
          </el-form-item>
          <el-form-item label="严重程度">
            <el-select v-model="eventQueryParams.severity" placeholder="请选择" clearable style="width: 120px">
              <el-option label="信息" value="info" />
              <el-option label="低" value="low" />
              <el-option label="中" value="medium" />
              <el-option label="高" value="high" />
              <el-option label="严重" value="critical" />
            </el-select>
          </el-form-item>
          <el-form-item label="执行者ID">
            <el-input v-model="eventQueryParams.actorId" placeholder="请输入执行者ID" clearable style="width: 160px" @keyup.enter="handleEventQuery" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleEventQuery">
              <Icon icon="ep:search" /> 查询
            </el-button>
            <el-button @click="handleEventReset">
              <Icon icon="ep:refresh" /> 重置
            </el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="eventLoading" :data="eventList" style="width: 100%">
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
              <el-tag :type="getResultTag(row.result) as any" size="small">{{ getResultLabel(row.result) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="severity" label="严重程度" min-width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getSeverityTag(row.severity) as any" size="small">{{ getSeverityLabel(row.severity) }}</el-tag>
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

        <Pagination
          v-model:page="eventQueryParams.pageNo"
          v-model:limit="eventQueryParams.pageSize"
          :total="eventTotal"
          @pagination="getEventList"
        />
      </template>

      <!-- 事件配置 Tab -->
      <template v-if="activeTab === 'config'">
        <div class="config-header">
          <el-button type="primary" size="small" @click="handleConfigCreate">
            <Icon icon="ep:plus" /> 新增配置
          </el-button>
        </div>

        <el-form :model="configQueryParams" inline class="search-form">
          <el-form-item label="配置名称">
            <el-input v-model="configQueryParams.name" placeholder="请输入配置名称" clearable style="width: 180px" @keyup.enter="handleConfigQuery" />
          </el-form-item>
          <el-form-item label="配置编码">
            <el-input v-model="configQueryParams.code" placeholder="请输入配置编码" clearable style="width: 180px" @keyup.enter="handleConfigQuery" />
          </el-form-item>
          <el-form-item label="事件类型">
            <el-select v-model="configQueryParams.eventType" placeholder="请选择" clearable style="width: 140px">
              <el-option label="页面浏览" value="page_view" />
              <el-option label="点击事件" value="click" />
              <el-option label="自定义事件" value="custom" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="configQueryParams.status" placeholder="请选择" clearable style="width: 120px">
              <el-option label="启用" :value="1" />
              <el-option label="禁用" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleConfigQuery">
              <Icon icon="ep:search" /> 查询
            </el-button>
            <el-button @click="handleConfigReset">
              <Icon icon="ep:refresh" /> 重置
            </el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="configLoading" :data="configList" style="width: 100%">
          <el-table-column prop="name" label="配置名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="code" label="配置编码" min-width="150" show-overflow-tooltip />
          <el-table-column label="数据源" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ getDataSourceNames(row.dataSourceIds) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="dataSourceType" label="数据源类型" min-width="120" />
          <el-table-column prop="eventType" label="事件类型" min-width="120" align="center">
            <template #default="{ row }">
              <el-tag :type="getEventTypeTag(row.eventType) as any" size="small">{{ getEventTypeLabel(row.eventType) }}</el-tag>
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
              <el-button type="primary" link @click="handleConfigUpdate(row)">
                <Icon icon="ep:edit" /> 编辑
              </el-button>
              <el-button type="danger" link @click="handleConfigDelete(row)">
                <Icon icon="ep:delete" /> 删除
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <Pagination
          v-model:page="configQueryParams.pageNo"
          v-model:limit="configQueryParams.pageSize"
          :total="configTotal"
          @pagination="getConfigList"
        />
      </template>
    </el-card>

    <!-- 事件详情对话框 -->
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
          <el-tag :type="getResultTag(detailData.result) as any" size="small">{{ getResultLabel(detailData.result) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="严重程度">
          <el-tag :type="getSeverityTag(detailData.severity) as any" size="small">{{ getSeverityLabel(detailData.severity) }}</el-tag>
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

    <!-- 事件配置新增/编辑对话框 -->
    <el-dialog v-model="configDialogVisible" :title="configDialogTitle" width="680px" destroy-on-close>
      <el-form ref="configFormRef" :model="configFormData" :rules="configFormRules" label-width="110px">
        <el-form-item label="配置名称" prop="name">
          <el-input v-model="configFormData.name" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置编码" prop="code">
          <el-input v-model="configFormData.code" placeholder="请输入配置编码" />
        </el-form-item>
        <el-form-item label="数据源" prop="dataSourceIds">
          <el-select v-model="configFormData.dataSourceIds" multiple placeholder="请选择数据源" style="width: 100%">
            <el-option v-for="ds in dataSourceList" :key="ds.id" :label="ds.name" :value="ds.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="数据源类型" prop="dataSourceType">
          <el-input v-model="configFormData.dataSourceType" placeholder="请输入数据源类型" />
        </el-form-item>
        <el-form-item label="事件类型" prop="eventType">
          <el-select v-model="configFormData.eventType" placeholder="请选择事件类型" style="width: 100%">
            <el-option label="页面浏览" value="page_view" />
            <el-option label="点击事件" value="click" />
            <el-option label="自定义事件" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件属性" prop="properties">
          <el-input v-model="configFormData.properties" type="textarea" :rows="3" placeholder="请输入事件属性JSON" />
        </el-form-item>
        <el-form-item label="过滤条件" prop="filterCondition">
          <el-input v-model="configFormData.filterCondition" type="textarea" :rows="3" placeholder="请输入过滤条件JSON" />
        </el-form-item>
        <el-form-item label="字段映射" prop="fieldMapping">
          <el-input v-model="configFormData.fieldMapping" type="textarea" :rows="3" placeholder="请输入字段映射JSON" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="configFormData.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="configFormData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="configDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitConfigForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getEventPage,
  getEvent,
  getEventConfigPage,
  getEventConfig,
  createEventConfig,
  updateEventConfig,
  deleteEventConfig,
  type EventRespVO,
  type EventPageReqVO,
  type EventConfigRespVO,
  type EventConfigSaveReqVO,
  type EventConfigPageReqVO
} from '@/api/ubax/collect'
import { getDataSourceConfigList, type DataSourceConfigVO } from '@/api/infra/dataSourceConfig'

defineOptions({ name: 'CollectEvent' })

const message = useMessage()

// ===== 数据源列表 =====
const dataSourceList = ref<DataSourceConfigVO[]>([])

const loadDataSourceList = async () => {
  try {
    const data = await getDataSourceConfigList()
    dataSourceList.value = data
  } catch { /* ignore */ }
}

const getDataSourceNames = (ids?: number[]) => {
  if (!ids || ids.length === 0) return '-'
  return ids.map(id => {
    const ds = dataSourceList.value.find(d => d.id === id)
    return ds ? ds.name : String(id)
  }).join(', ')
}

// ===== Tab 切换 =====
const activeTab = ref('events')

const handleTabChange = (tab: string) => {
  if (tab === 'events' && eventList.value.length === 0) {
    getEventList()
  } else if (tab === 'config' && configList.value.length === 0) {
    getConfigList()
  }
}

// ===== 事件列表 =====
const eventLoading = ref(false)
const eventList = ref<EventRespVO[]>([])
const eventTotal = ref(0)
const detailVisible = ref(false)
const detailData = ref<EventRespVO>({} as EventRespVO)

const eventQueryParams = ref<EventPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  eventType: undefined,
  eventCategory: undefined,
  actorType: undefined,
  actorId: undefined,
  result: undefined,
  severity: undefined
})

const getEventList = async () => {
  eventLoading.value = true
  try {
    const data = await getEventPage(eventQueryParams.value)
    eventList.value = data.list
    eventTotal.value = data.total
  } finally {
    eventLoading.value = false
  }
}

const handleEventQuery = () => {
  eventQueryParams.value.pageNo = 1
  getEventList()
}

const handleEventReset = () => {
  eventQueryParams.value = { pageNo: 1, pageSize: 10, eventType: undefined, eventCategory: undefined, actorType: undefined, actorId: undefined, result: undefined, severity: undefined }
  getEventList()
}

const handleDetail = async (row: EventRespVO) => {
  try {
    const data = await getEvent(row.id)
    detailData.value = data
    detailVisible.value = true
  } catch { /* ignore */ }
}

// ===== 事件配置 =====
const configLoading = ref(false)
const configList = ref<EventConfigRespVO[]>([])
const configTotal = ref(0)
const configDialogVisible = ref(false)
const configDialogTitle = ref('')
const configFormRef = ref()
const configFormData = ref<EventConfigSaveReqVO>({ name: '', code: '', status: 1, dataSourceIds: [] })

const configFormRules = {
  name: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '配置编码不能为空', trigger: 'blur' }]
}

const configQueryParams = ref<EventConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  code: undefined,
  eventType: undefined,
  status: undefined
})

const getConfigList = async () => {
  configLoading.value = true
  try {
    const data = await getEventConfigPage(configQueryParams.value)
    configList.value = data.list
    configTotal.value = data.total
  } finally {
    configLoading.value = false
  }
}

const handleConfigQuery = () => {
  configQueryParams.value.pageNo = 1
  getConfigList()
}

const handleConfigReset = () => {
  configQueryParams.value = { pageNo: 1, pageSize: 10, name: undefined, code: undefined, eventType: undefined, status: undefined }
  getConfigList()
}

const handleConfigCreate = () => {
  configDialogTitle.value = '新增事件配置'
  configFormData.value = { name: '', code: '', status: 1, dataSourceIds: [] }
  configDialogVisible.value = true
}

const handleConfigUpdate = async (row: EventConfigRespVO) => {
  configDialogTitle.value = '编辑事件配置'
  try {
    const data = await getEventConfig(row.id)
    configFormData.value = { ...data }
    configDialogVisible.value = true
  } catch { /* ignore */ }
}

const handleConfigDelete = async (row: EventConfigRespVO) => {
  await message.delConfirm(`确定要删除事件配置「${row.name}」吗？`)
  await deleteEventConfig(row.id)
  message.success('删除成功')
  getConfigList()
}

const handleStatusChange = async (row: EventConfigRespVO) => {
  try {
    await updateEventConfig({ id: row.id, name: row.name, code: row.code, status: row.status })
    message.success('状态更新成功')
  } catch {
    row.status = row.status === 1 ? 0 : 1
  }
}

const submitConfigForm = async () => {
  await configFormRef.value?.validate()
  if (configFormData.value.id) {
    await updateEventConfig(configFormData.value)
    message.success('更新成功')
  } else {
    await createEventConfig(configFormData.value)
    message.success('创建成功')
  }
  configDialogVisible.value = false
  getConfigList()
}

// ===== 工具函数 =====
const formatDateTime = (date: Date | string | undefined) => {
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

const getEventTypeLabel = (type: string) => {
  const map: Record<string, string> = { page_view: '页面浏览', click: '点击事件', custom: '自定义事件' }
  return map[type] || type
}

const getEventTypeTag = (type: string) => {
  const map: Record<string, string> = { page_view: 'primary', click: 'success', custom: 'info' }
  return map[type] || ''
}

onMounted(() => {
  getEventList()
  loadDataSourceList()
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

  :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }
}

.config-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
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
