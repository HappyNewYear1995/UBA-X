<template>
  <div class="event-config-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">事件配置</span>
          <el-button type="primary" @click="handleCreate">
            <Icon icon="ep:plus" /> 新增配置
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="queryParams.configName"
          placeholder="搜索配置名称"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @keyup.enter="handleQuery"
        />
        <el-select
          v-model="queryParams.matchPosition"
          placeholder="匹配位置"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="HTTP Body" value="http_body" />
          <el-option label="HTTP Header" value="http_header" />
          <el-option label="URL" value="url" />
          <el-option label="自定义" value="custom" />
        </el-select>
        <el-select
          v-model="queryParams.matchType"
          placeholder="匹配类型"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="包含" value="contains" />
          <el-option label="等于" value="equals" />
          <el-option label="正则" value="regex" />
          <el-option label="前缀" value="prefix" />
          <el-option label="后缀" value="suffix" />
        </el-select>
        <el-select
          v-model="queryParams.eventLevel"
          placeholder="事件级别"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="低" :value="1" />
          <el-option label="中" :value="2" />
          <el-option label="高" :value="3" />
          <el-option label="紧急" :value="4" />
        </el-select>
        <el-select
          v-model="queryParams.enabled"
          placeholder="启用状态"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="启用" :value="true" />
          <el-option label="停用" :value="false" />
        </el-select>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
      </div>

      <el-table v-loading="loading" :data="configList" stripe>
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
            <el-button type="primary" link @click="handleEdit(row)">
              <Icon icon="ep:edit" /> 编辑
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <Icon icon="ep:delete" /> 删除
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="formData.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置描述">
          <el-input
            v-model="formData.configDesc"
            type="textarea"
            :rows="2"
            placeholder="请输入配置描述"
          />
        </el-form-item>
        <el-form-item label="匹配位置" prop="matchPosition">
          <el-select v-model="formData.matchPosition" placeholder="请选择匹配位置" style="width: 100%">
            <el-option label="HTTP Body" value="http_body" />
            <el-option label="HTTP Header" value="http_header" />
            <el-option label="URL" value="url" />
            <el-option label="自定义" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配类型" prop="matchType">
          <el-select v-model="formData.matchType" placeholder="请选择匹配类型" style="width: 100%">
            <el-option label="包含" value="contains" />
            <el-option label="等于" value="equals" />
            <el-option label="正则" value="regex" />
            <el-option label="前缀" value="prefix" />
            <el-option label="后缀" value="suffix" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配值" prop="matchValue">
          <el-input v-model="formData.matchValue" placeholder="请输入匹配值" />
        </el-form-item>
        <el-form-item label="匹配逻辑">
          <el-select v-model="formData.matchLogic" placeholder="请选择匹配逻辑" style="width: 100%">
            <el-option label="AND" value="and" />
            <el-option label="OR" value="or" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件类型">
          <el-input v-model="formData.eventType" placeholder="请输入事件类型" />
        </el-form-item>
        <el-form-item label="事件级别">
          <el-select v-model="formData.eventLevel" placeholder="请选择事件级别" style="width: 100%">
            <el-option label="低" :value="1" />
            <el-option label="中" :value="2" />
            <el-option label="高" :value="3" />
            <el-option label="紧急" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件标题模板">
          <el-input v-model="formData.eventTitleTemplate" placeholder="请输入事件标题模板" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="formData.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="启用状态">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="formData.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
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
defineOptions({ name: 'EventConfig' })

import {
  getEventConfigPage,
  createEventConfig,
  updateEventConfig,
  deleteEventConfig,
  type EventConfigRespVO,
  type EventConfigPageReqVO,
  type EventConfigSaveReqVO
} from 'src/api/ubax/gather/event/eventConfig'

const loading = ref(false)
const configList = ref<EventConfigRespVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const formData = ref<EventConfigSaveReqVO>({
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

const formRules = {
  configName: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }],
  matchPosition: [{ required: true, message: '匹配位置不能为空', trigger: 'change' }],
  matchType: [{ required: true, message: '匹配类型不能为空', trigger: 'change' }],
  matchValue: [{ required: true, message: '匹配值不能为空', trigger: 'blur' }]
}

const queryParams = ref<EventConfigPageReqVO>({
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

const getList = async () => {
  loading.value = true
  try {
    const data = await getEventConfigPage(queryParams.value)
    configList.value = data.list
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
    configName: undefined,
    matchPosition: undefined,
    matchType: undefined,
    eventType: undefined,
    eventLevel: undefined,
    enabled: undefined,
    createTime: undefined
  }
  handleQuery()
}

const handleCreate = () => {
  dialogTitle.value = '新增配置'
  formData.value = {
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
  dialogVisible.value = true
}

const handleEdit = (row: EventConfigRespVO) => {
  dialogTitle.value = '编辑配置'
  formData.value = {
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
  dialogVisible.value = true
}

const handleDelete = async (row: EventConfigRespVO) => {
  await ElMessageBox.confirm(`确定要删除配置「${row.configName}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteEventConfig(row.id)
  ElMessage.success('配置已删除')
  getList()
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (formData.value.id) {
    await updateEventConfig(formData.value)
    ElMessage.success('配置已更新')
  } else {
    await createEventConfig(formData.value)
    ElMessage.success('配置已创建')
  }
  dialogVisible.value = false
  getList()
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

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.event-config-container {
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
</style>
