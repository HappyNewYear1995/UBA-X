<template>
  <div class="event-container">
    <el-card shadow="never" class="dashboard-card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="事件列表" name="events" />
        <el-tab-pane label="事件配置" name="config" />
      </el-tabs>

      <template v-if="activeTab === 'events'">
        <div class="search-bar">
          <el-input
            v-model="eventQueryParams.title"
            placeholder="搜索事件标题"
            prefix-icon="ep:search"
            clearable
            class="search-input"
            @keyup.enter="handleEventQuery"
          />
          <el-input
            v-model="eventQueryParams.agentUuid"
            placeholder="搜索 Agent UUID"
            prefix-icon="ep:search"
            clearable
            class="search-input"
            @keyup.enter="handleEventQuery"
          />
          <el-select
            v-model="eventQueryParams.eventType"
            placeholder="事件类型"
            clearable
            class="filter-select"
            @change="handleEventQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="安全事件" value="security" />
            <el-option label="性能事件" value="performance" />
            <el-option label="异常事件" value="exception" />
          </el-select>
          <el-select
            v-model="eventQueryParams.eventLevel"
            placeholder="事件级别"
            clearable
            class="filter-select"
            @change="handleEventQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="紧急" :value="4" />
          </el-select>
          <el-select
            v-model="eventQueryParams.handled"
            placeholder="处理状态"
            clearable
            class="filter-select"
            @change="handleEventQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="未处理" :value="false" />
            <el-option label="已处理" :value="true" />
          </el-select>
          <el-button type="primary" @click="handleEventQuery">
            <Icon icon="ep:search" /> 搜索
          </el-button>
          <el-button @click="resetEventQuery">
            <Icon icon="ep:refresh" /> 重置
          </el-button>
        </div>

        <el-table v-loading="eventLoading" :data="eventList" stripe>
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="agentUuid" label="Agent UUID" min-width="160" show-overflow-tooltip />
          <el-table-column prop="title" label="事件标题" min-width="180" show-overflow-tooltip />
          <el-table-column prop="eventType" label="事件类型" width="110" align="center">
            <template #default="{ row }">
              <el-tag :type="getEventTypeTagType(row.eventType)" size="small" round>
                {{ getEventTypeName(row.eventType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="eventLevel" label="事件级别" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getEventLevelTagType(row.eventLevel)" size="small" round>
                {{ getEventLevelName(row.eventLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="eventSource" label="事件来源" width="120" align="center">
            <template #default="{ row }">
              {{ getEventSourceName(row.eventSource) }}
            </template>
          </el-table-column>
          <el-table-column prop="eventTime" label="事件时间" width="170" align="center">
            <template #default="{ row }">
              {{ formatDate(row.eventTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="handled" label="处理状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="row.handled ? 'success' : 'warning'" size="small" round>
                {{ row.handled ? '已处理' : '未处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleEventDetail(row)">
                <Icon icon="ep:view" /> 详情
              </el-button>
              <el-button
                v-if="!row.handled"
                type="success"
                link
                @click="handleEventHandle(row)"
              >
                <Icon icon="ep:check" /> 处理
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

      <template v-if="activeTab === 'config'">
        <div class="config-header">
          <el-button type="primary" @click="handleConfigCreate">
            <Icon icon="ep:plus" /> 新增配置
          </el-button>
        </div>

        <div class="search-bar">
          <el-input
            v-model="configQueryParams.configName"
            placeholder="搜索配置名称"
            prefix-icon="ep:search"
            clearable
            class="search-input"
            @keyup.enter="handleConfigQuery"
          />
          <el-select
            v-model="configQueryParams.matchPosition"
            placeholder="匹配位置"
            clearable
            class="filter-select"
            @change="handleConfigQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="HTTP Body" value="http_body" />
            <el-option label="HTTP Header" value="http_header" />
            <el-option label="URL" value="url" />
            <el-option label="自定义" value="custom" />
          </el-select>
          <el-select
            v-model="configQueryParams.matchType"
            placeholder="匹配类型"
            clearable
            class="filter-select"
            @change="handleConfigQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="包含" value="contains" />
            <el-option label="等于" value="equals" />
            <el-option label="正则" value="regex" />
            <el-option label="前缀" value="prefix" />
            <el-option label="后缀" value="suffix" />
          </el-select>
          <el-select
            v-model="configQueryParams.eventLevel"
            placeholder="事件级别"
            clearable
            class="filter-select"
            @change="handleConfigQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="紧急" :value="4" />
          </el-select>
          <el-select
            v-model="configQueryParams.enabled"
            placeholder="启用状态"
            clearable
            class="filter-select"
            @change="handleConfigQuery"
          >
            <el-option label="全部" :value="undefined" />
            <el-option label="启用" :value="true" />
            <el-option label="停用" :value="false" />
          </el-select>
          <el-button type="primary" @click="handleConfigQuery">
            <Icon icon="ep:search" /> 搜索
          </el-button>
          <el-button @click="resetConfigQuery">
            <Icon icon="ep:refresh" /> 重置
          </el-button>
        </div>

        <el-table v-loading="configLoading" :data="configList" stripe>
          <el-table-column prop="id" label="ID" width="70" align="center" />
          <el-table-column prop="configName" label="配置名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="matchPosition" label="匹配位置" width="120" align="center">
            <template #default="{ row }">
              <el-tag type="primary" size="small" round>
                {{ getMatchPositionName(row.matchPosition) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="matchType" label="匹配类型" width="100" align="center">
            <template #default="{ row }">
              <el-tag type="warning" size="small" round>
                {{ getMatchTypeName(row.matchType) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="matchValue" label="匹配值" min-width="160" show-overflow-tooltip />
          <el-table-column prop="matchLogic" label="匹配逻辑" width="100" align="center">
            <template #default="{ row }">
              {{ getMatchLogicName(row.matchLogic) }}
            </template>
          </el-table-column>
          <el-table-column prop="eventType" label="事件类型" width="110" align="center" />
          <el-table-column prop="eventLevel" label="事件级别" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="getEventLevelTagType(row.eventLevel)" size="small" round>
                {{ getEventLevelName(row.eventLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="enabled" label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="row.enabled ? 'success' : 'info'" size="small" round>
                {{ row.enabled ? '启用' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="80" align="center" />
          <el-table-column label="操作" width="200" fixed="right" align="center">
            <template #default="{ row }">
              <el-button type="primary" link @click="handleConfigEdit(row)">
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

    <el-dialog v-model="eventDetailDialogVisible" title="事件详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="ID">{{ eventDetailData.id }}</el-descriptions-item>
        <el-descriptions-item label="Agent UUID">{{ eventDetailData.agentUuid }}</el-descriptions-item>
        <el-descriptions-item label="事件标题" :span="2">{{ eventDetailData.title }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">
          {{ getEventTypeName(eventDetailData.eventType) }}
        </el-descriptions-item>
        <el-descriptions-item label="事件级别">
          <el-tag :type="getEventLevelTagType(eventDetailData.eventLevel)" size="small">
            {{ getEventLevelName(eventDetailData.eventLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="事件来源">
          {{ getEventSourceName(eventDetailData.eventSource) }}
        </el-descriptions-item>
        <el-descriptions-item label="事件时间">
          {{ formatDate(eventDetailData.eventTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="eventDetailData.handled ? 'success' : 'warning'" size="small">
            {{ eventDetailData.handled ? '已处理' : '未处理' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">{{ eventDetailData.handler }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">
          {{ formatDate(eventDetailData.handleTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="事件内容" :span="2">
          {{ eventDetailData.content || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">
          {{ eventDetailData.handleRemark || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="标签" :span="2">
          {{ eventDetailData.tags || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="eventHandleDialogVisible" title="处理事件" width="500px">
      <el-form :model="eventHandleForm" label-width="100px">
        <el-form-item label="事件 ID">
          <el-input v-model="eventHandleForm.id" disabled />
        </el-form-item>
        <el-form-item label="处理人">
          <el-input v-model="eventHandleForm.handler" placeholder="请输入处理人" />
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="eventHandleForm.handleRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="eventHandleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEventHandle">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="configDialogVisible" :title="configDialogTitle" width="600px">
      <el-form ref="configFormRef" :model="configFormData" :rules="configFormRules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configFormData.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置描述">
          <el-input
            v-model="configFormData.configDesc"
            type="textarea"
            :rows="2"
            placeholder="请输入配置描述"
          />
        </el-form-item>
        <el-form-item label="匹配位置" prop="matchPosition">
          <el-select v-model="configFormData.matchPosition" placeholder="请选择匹配位置" style="width: 100%">
            <el-option label="HTTP Body" value="http_body" />
            <el-option label="HTTP Header" value="http_header" />
            <el-option label="URL" value="url" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配类型" prop="matchType">
          <el-select v-model="configFormData.matchType" placeholder="请选择匹配类型" style="width: 100%">
            <el-option label="包含" value="contains" />
            <el-option label="等于" value="equals" />
            <el-option label="正则" value="regex" />
            <el-option label="前缀" value="prefix" />
            <el-option label="后缀" value="suffix" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配值" prop="matchValue">
          <el-input v-model="configFormData.matchValue" placeholder="请输入匹配值" />
        </el-form-item>
        <el-form-item label="匹配逻辑">
          <el-select v-model="configFormData.matchLogic" placeholder="请选择匹配逻辑" style="width: 100%">
            <el-option label="AND" value="and" />
            <el-option label="OR" value="or" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类型">
          <el-input v-model="configFormData.eventType" placeholder="请输入事件类型" />
        </el-form-item>
        <el-form-item label="事件级别">
          <el-select v-model="configFormData.eventLevel" placeholder="请选择事件级别" style="width: 100%">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="紧急" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件标题模板">
          <el-input v-model="configFormData.eventTitleTemplate" placeholder="请输入事件标题模板" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="configFormData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="configFormData.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="configFormData.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
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
defineOptions({ name: 'EventManagement' })

import {
  getEventPage,
  getEvent,
  handleEvent,
  type EventRespVO,
  type EventPageReqVO,
  type EventHandleReqVO
} from '@/api/ubax/gather/event/event'

import {
  getEventConfigPage,
  createEventConfig,
  updateEventConfig,
  deleteEventConfig,
  type EventConfigRespVO,
  type EventConfigPageReqVO,
  type EventConfigSaveReqVO
} from '@/api/ubax/gather/event/eventConfig'

const activeTab = ref('events')

const eventLoading = ref(false)
const eventList = ref<EventRespVO[]>([])
const eventTotal = ref(0)
const eventDetailDialogVisible = ref(false)
const eventDetailData = ref<EventRespVO>({} as EventRespVO)
const eventHandleDialogVisible = ref(false)
const eventHandleForm = ref<EventHandleReqVO>({ id: 0, handler: '', handleRemark: '' })

const eventQueryParams = ref<EventPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  agentUuid: undefined,
  eventType: undefined,
  eventLevel: undefined,
  eventSource: undefined,
  title: undefined,
  handled: undefined,
  eventTime: undefined,
  createTime: undefined
})

const configLoading = ref(false)
const configList = ref<EventConfigRespVO[]>([])
const configTotal = ref(0)
const configDialogVisible = ref(false)
const configDialogTitle = ref('')
const configFormRef = ref()
const configFormData = ref<EventConfigSaveReqVO>({
  configName: '',
  matchPosition: '',
  matchType: '',
  matchValue: '',
  matchLogic: undefined,
  eventType: undefined,
  eventLevel: undefined,
  eventTitleTemplate: undefined,
  enabled: true,
  sort: 0,
  remark: undefined
})

const configFormRules = {
  configName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
  matchPosition: [{ required: true, message: '匹配位置不能为空', trigger: 'change' }],
  matchType: [{ required: true, message: '匹配类型不能为空', trigger: 'change' }],
  matchValue: [{ required: true, message: '匹配值不能为空', trigger: 'blur' }]
}

const configQueryParams = ref<EventConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  configName: undefined,
  matchPosition: undefined,
  matchType: undefined,
  eventType: undefined,
  eventLevel: undefined,
  enabled: undefined,
  createTime: undefined
})

const eventTypeMap: Record<string, string> = {
  security: '安全事件',
  performance: '性能事件',
  exception: '异常事件'
}

const eventSourceMap: Record<string, string> = {
  http_body: 'HTTP Body',
  http_header: 'HTTP Header',
  url: 'URL',
  custom: '自定义'
}

const matchPositionMap: Record<string, string> = {
  http_body: 'HTTP Body',
  http_header: 'HTTP Header',
  url: 'URL',
  custom: '自定义'
}

const matchTypeMap: Record<string, string> = {
  contains: '包含',
  equals: '等于',
  regex: '正则',
  prefix: '前缀',
  suffix: '后缀'
}

const matchLogicMap: Record<string, string> = {
  and: 'AND',
  or: 'OR'
}

const handleTabChange = (tab: string) => {
  if (tab === 'events' && eventList.value.length === 0) {
    getEventList()
  } else if (tab === 'config' && configList.value.length === 0) {
    getConfigList()
  }
}

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

const resetEventQuery = () => {
  eventQueryParams.value = {
    pageNo: 1,
    pageSize: 10,
    agentUuid: undefined,
    eventType: undefined,
    eventLevel: undefined,
    eventSource: undefined,
    title: undefined,
    handled: undefined,
    eventTime: undefined,
    createTime: undefined
  }
  handleEventQuery()
}

const handleEventDetail = async (row: EventRespVO) => {
  eventDetailData.value = await getEvent(row.id)
  eventDetailDialogVisible.value = true
}

const handleEventHandle = (row: EventRespVO) => {
  eventHandleForm.value = { id: row.id, handler: '', handleRemark: '' }
  eventHandleDialogVisible.value = true
}

const submitEventHandle = async () => {
  await handleEvent(eventHandleForm.value)
  ElMessage.success('事件已处理')
  eventHandleDialogVisible.value = false
  getEventList()
}

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

const resetConfigQuery = () => {
  configQueryParams.value = {
    pageNo: 1,
    pageSize: 10,
    configName: undefined,
    matchPosition: undefined,
    matchType: undefined,
    eventType: undefined,
    eventLevel: undefined,
    enabled: undefined,
    createTime: undefined
  }
  handleConfigQuery()
}

const handleConfigCreate = () => {
  configDialogTitle.value = '新增配置'
  configFormData.value = {
    configName: '',
    matchPosition: '',
    matchType: '',
    matchValue: '',
    matchLogic: undefined,
    eventType: undefined,
    eventLevel: undefined,
    eventTitleTemplate: undefined,
    enabled: true,
    sort: 0,
    remark: undefined
  }
  configDialogVisible.value = true
}

const handleConfigEdit = (row: EventConfigRespVO) => {
  configDialogTitle.value = '编辑配置'
  configFormData.value = {
    id: row.id,
    configName: row.configName,
    configDesc: row.configDesc,
    matchPosition: row.matchPosition,
    matchType: row.matchType,
    matchValue: row.matchValue,
    matchLogic: row.matchLogic,
    eventType: row.eventType,
    eventLevel: row.eventLevel,
    eventTitleTemplate: row.eventTitleTemplate,
    enabled: row.enabled,
    sort: row.sort,
    remark: row.remark
  }
  configDialogVisible.value = true
}

const handleConfigDelete = async (row: EventConfigRespVO) => {
  await ElMessageBox.confirm(`确定要删除配置「${row.configName}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteEventConfig(row.id)
  ElMessage.success('配置已删除')
  getConfigList()
}

const submitConfigForm = async () => {
  await configFormRef.value?.validate()
  if (configFormData.value.id) {
    await updateEventConfig(configFormData.value)
    ElMessage.success('配置已更新')
  } else {
    await createEventConfig(configFormData.value)
    ElMessage.success('配置已创建')
  }
  configDialogVisible.value = false
  getConfigList()
}

const getEventTypeName = (type: string) => {
  return eventTypeMap[type] || type
}

const getEventTypeTagType = (type: string) => {
  const map: Record<string, string> = {
    security: 'danger',
    performance: 'warning',
    exception: 'info'
  }
  return map[type] || ''
}

const getEventLevelName = (level: number) => {
  const map: Record<number, string> = {
    1: '低',
    2: '中',
    3: '高',
    4: '紧急'
  }
  return map[level] || '未知'
}

const getEventLevelTagType = (level: number) => {
  const map: Record<number, string> = {
    1: 'info',
    2: 'warning',
    3: 'danger',
    4: 'danger'
  }
  return map[level] || ''
}

const getEventSourceName = (source: string) => {
  return eventSourceMap[source] || source
}

const getMatchPositionName = (position: string) => {
  return matchPositionMap[position] || position
}

const getMatchTypeName = (type: string) => {
  return matchTypeMap[type] || type
}

const getMatchLogicName = (logic: string) => {
  return matchLogicMap[logic] || logic
}

const formatDate = (date: Date | string | undefined) => {
  if (!date) return '-'
  return date.toString()
}

onMounted(() => {
  getEventList()
})
</script>

<style lang="scss" scoped>
.event-container {
  padding: 16px;
}

.dashboard-card {
  border-radius: 8px;

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--el-border-color-light);
    padding: 14px 20px;
  }

  :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }
}

.config-header {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
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
</style>
