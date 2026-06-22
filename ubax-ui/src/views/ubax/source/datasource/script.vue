<template>
  <div class="script-config-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">脚本管理</span>
          <el-button type="primary" @click="handleCreate">
            <Icon icon="ep:plus" /> 新增脚本
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="queryParams.name"
          placeholder="搜索脚本名称"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @keyup.enter="handleQuery"
        />
        <el-input
          v-model="queryParams.code"
          placeholder="搜索脚本编码"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @keyup.enter="handleQuery"
        />
        <el-select
          v-model="queryParams.scriptType"
          placeholder="脚本类型"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="SQL脚本" value="sql" />
          <el-option label="存储过程" value="procedure" />
          <el-option label="视图查询" value="view" />
          <el-option label="WebService" value="webservice" />
        </el-select>
        <el-select
          v-model="queryParams.status"
          placeholder="状态"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="启用" :value="0" />
          <el-option label="禁用" :value="1" />
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
        <el-table-column prop="name" label="脚本名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="code" label="脚本编码" width="140" show-overflow-tooltip />
        <el-table-column prop="dataSourceName" label="数据源" min-width="120" show-overflow-tooltip />
        <el-table-column prop="scriptTypeName" label="脚本类型" width="100" align="center">
          <template #default="{ row }">
            {{ row.scriptTypeName }}
          </template>
        </el-table-column>
        <el-table-column prop="resultTableName" label="结果表" width="140" show-overflow-tooltip />
        <el-table-column prop="executeCount" label="执行次数" width="80" align="center" />
        <el-table-column prop="lastExecuteTime" label="最后执行" width="160" align="center">
          <template #default="{ row }">
            <span v-if="row.lastExecuteTime">{{ formatTime(row.lastExecuteTime) }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastExecuteStatus" label="执行状态" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.lastExecuteStatus === 0" style="color: var(--el-color-success)">成功</span>
            <span v-else-if="row.lastExecuteStatus === 1" style="color: var(--el-color-danger)">失败</span>
            <span v-else class="text-gray">未执行</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            {{ row.status === 0 ? '启用' : '禁用' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="120" show-overflow-tooltip />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <span class="table-action" @click="handleEdit(row)">编辑</span>
            <span class="table-action success" @click="handleExecute(row)">执行</span>
            <span class="table-action warning" @click="handleLogs(row)">日志</span>
            <span class="table-action danger" @click="handleDelete(row)">删除</span>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="脚本名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入脚本名称" />
        </el-form-item>
        <el-form-item label="脚本编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入脚本编码（唯一标识）" />
        </el-form-item>
        <el-form-item label="数据源" prop="databaseId">
          <el-select v-model="formData.databaseId" placeholder="请选择数据源" style="width: 100%">
            <el-option v-for="ds in dataSourceList" :key="ds.id" :label="ds.name" :value="ds.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="脚本类型" prop="scriptType">
          <el-select v-model="formData.scriptType" placeholder="请选择脚本类型" style="width: 100%">
            <el-option label="SQL脚本" value="sql" />
            <el-option label="存储过程" value="procedure" />
            <el-option label="视图查询" value="view" />
            <el-option label="WebService" value="webservice" />
          </el-select>
        </el-form-item>
        <el-form-item label="脚本内容" prop="scriptContent">
          <el-input
            v-model="formData.scriptContent"
            type="textarea"
            :rows="6"
            :placeholder="formData.scriptType === 'procedure' ? '请输入存储过程名称' : formData.scriptType === 'webservice' ? '请输入请求体（JSON/XML），支持 {{参数名}} 模板替换' : '请输入SQL语句（视图查询请输入完整SQL，如 SELECT * FROM view_name WHERE id = ?）'"
          />
        </el-form-item>
        <el-form-item label="脚本描述">
          <el-input v-model="formData.description" placeholder="请输入脚本描述" />
        </el-form-item>
        <el-divider content-position="left">入参配置（可选，用于SQL/视图的WHERE条件或WebService模板参数）</el-divider>
        <el-form-item label="入参定义">
          <div class="param-config">
            <div v-for="(param, index) in paramDefs" :key="index" class="param-row">
              <el-input v-model="param.name" placeholder="参数名" style="width: 120px" />
              <el-select v-model="param.type" placeholder="类型" style="width: 110px">
                <el-option label="String" value="String" />
                <el-option label="Long" value="Long" />
                <el-option label="Integer" value="Integer" />
                <el-option label="Double" value="Double" />
                <el-option label="Date" value="Date" />
                <el-option label="Boolean" value="Boolean" />
              </el-select>
              <el-switch v-model="param.required" active-text="必填" inactive-text="选填" />
              <el-input v-model="param.defaultValue" placeholder="默认值" style="width: 120px" />
              <el-input v-model="param.description" placeholder="说明" style="width: 120px" />
              <el-button type="danger" link @click="paramDefs.splice(index, 1)">
                <Icon icon="ep:delete" />
              </el-button>
            </div>
            <el-button type="primary" link @click="addParamDef">
              <Icon icon="ep:plus" /> 添加参数
            </el-button>
            <div class="param-tip">
              SQL中使用 ? 作为占位符，参数按定义顺序依次绑定。示例：SELECT * FROM user WHERE id = ? AND name = ?
            </div>
          </div>
        </el-form-item>
        <template v-if="formData.scriptType === 'procedure'">
          <el-divider content-position="left">输出参数配置（存储过程用）</el-divider>
          <el-form-item label="输出参数">
            <div class="param-config">
              <div v-for="(param, index) in outputParamDefs" :key="index" class="param-row">
                <el-input v-model="outputParamDefs[index]" placeholder="输出参数名" style="width: 200px" />
                <el-button type="danger" link @click="outputParamDefs.splice(index, 1)">
                  <Icon icon="ep:delete" />
                </el-button>
              </div>
              <el-button type="primary" link @click="outputParamDefs.push('')">
                <Icon icon="ep:plus" /> 添加输出参数
              </el-button>
              <div class="param-tip">
                输出参数名需与存储过程中定义的 OUT 参数名一致，执行后将返回输出参数的值
              </div>
            </div>
          </el-form-item>
        </template>
        <el-divider content-position="left">结果持久化配置（可选）</el-divider>
        <el-form-item label="结果映射表名">
          <el-input v-model="formData.resultTableName" placeholder="执行结果将保存到此表" />
        </el-form-item>
        <el-form-item label="字段映射">
          <el-input v-model="formData.resultFieldMapping" type="textarea" :rows="2" placeholder='{"源字段":"目标字段"}' />
        </el-form-item>
        <el-form-item label="定时表达式">
          <el-input v-model="formData.cronExpression" placeholder="如: 0 0 * * * ?" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="formData.status" :active-value="0" :inactive-value="1" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 执行弹窗 -->
    <el-dialog v-model="executeDialogVisible" title="执行脚本" width="700px" :close-on-click-modal="false">
      <el-form :model="executeForm" label-width="100px">
        <el-form-item label="脚本信息">
          <div class="info-row">
            <span class="info-label">脚本名称：</span>
            <span class="info-value">{{ executeForm.scriptName }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">脚本编码：</span>
            <span class="info-value">{{ executeForm.scriptCode }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">数据源：</span>
            <span class="info-value">{{ executeForm.dataSourceName }}</span>
          </div>
        </el-form-item>
        <el-form-item label="脚本内容">
          <el-input v-model="executeForm.scriptContent" type="textarea" :rows="6" readonly />
        </el-form-item>
        <el-form-item v-if="executeParamDefs.length > 0" label="执行入参">
          <div class="param-config">
            <div v-for="(param, index) in executeParamDefs" :key="index" class="param-row">
              <span class="param-name">{{ param.name }}</span>
              <span class="param-type">({{ param.type }})</span>
              <el-input
                v-model="executeInputParams[param.name]"
                :placeholder="param.required ? '必填' : (param.defaultValue || '选填')"
                style="flex: 1"
              />
              <span v-if="param.required" class="param-required">*</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="持久化结果">
          <el-switch v-model="executeForm.persistResult" :active-value="1" :inactive-value="0" />
          <span class="switch-tip">开启后将把执行结果保存到配置的结果表中</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="executeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleExecuteConfirm" :loading="executing">
          <Icon icon="ep:play" /> 执行
        </el-button>
      </template>
    </el-dialog>

    <!-- 执行结果弹窗 -->
    <el-dialog v-model="resultDialogVisible" title="执行结果" width="800px">
      <div v-if="executeResult" class="result-container">
        <div class="result-header">
          <el-tag :type="executeResult.success ? 'success' : 'danger'" size="large">
            {{ executeResult.success ? '执行成功' : '执行失败' }}
          </el-tag>
          <span class="result-cost">耗时：{{ executeResult.costTime }}ms</span>
        </div>
        <div v-if="executeResult.success" class="result-body">
          <el-form label-width="100px">
            <el-form-item label="记录数">
              <span class="result-value">{{ executeResult.resultRecordCount || 0 }}</span>
            </el-form-item>
            <el-form-item label="影响行数">
              <span class="result-value">{{ executeResult.affectedRows || 0 }}</span>
            </el-form-item>
            <el-form-item v-if="executeResult.outputParams && Object.keys(executeResult.outputParams).length > 0" label="输出参数">
              <div class="output-params">
                <div v-for="(value, key) in executeResult.outputParams" :key="key" class="output-param-item">
                  <span class="output-param-name">{{ key }}</span>
                  <span class="output-param-value">{{ value }}</span>
                </div>
              </div>
            </el-form-item>
            <el-form-item label="是否持久化">
              <span class="result-value">{{ executeResult.persisted ? '是' : '否' }}</span>
            </el-form-item>
            <el-form-item label="持久化记录数">
              <span class="result-value">{{ executeResult.persistRecordCount || 0 }}</span>
            </el-form-item>
          </el-form>
          <el-divider content-position="left">执行结果预览</el-divider>
          <div v-if="executeResult.results && executeResult.results.length > 0" class="result-table">
            <el-table :data="executeResult.results.slice(0, 10)" border>
              <el-table-column
                v-for="(value, key) in executeResult.results[0]"
                :key="key"
                :prop="key"
                :label="key"
                show-overflow-tooltip
              />
            </el-table>
            <div v-if="executeResult.results.length > 10" class="result-more">
              仅显示前10条，共{{ executeResult.results.length }}条
            </div>
          </div>
          <div v-else class="result-empty">
            无结果数据
          </div>
        </div>
        <div v-else class="result-error">
          <Icon icon="ep:error" /> {{ executeResult.errorMessage }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 日志弹窗 -->
    <el-dialog v-model="logsDialogVisible" title="执行日志" width="900px" append-to-body>
      <div class="log-search-bar">
        <el-select
          v-model="logQueryParams.status"
          placeholder="执行状态"
          clearable
          style="width: 130px"
          @change="getLogs"
        >
          <el-option label="全部" :value="undefined" />
          <el-option label="成功" :value="0" />
          <el-option label="失败" :value="1" />
        </el-select>
      </div>
      <el-table v-loading="logsLoading" :data="logsList" stripe max-height="400">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="scriptCode" label="脚本编码" width="140" show-overflow-tooltip />
        <el-table-column prop="executeType" label="执行类型" width="100" align="center">
          <template #default="{ row }">
            {{ row.executeType === 'manual' ? '手动执行' : '定时执行' }}
          </template>
        </el-table-column>
        <el-table-column prop="inputParams" label="执行参数" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.inputParams">{{ formatInputParams(row.inputParams) }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="outputParams" label="输出参数" width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.outputParams">{{ formatOutputParams(row.outputParams) }}</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="执行状态" width="80" align="center">
          <template #default="{ row }">
            <span v-if="row.status === 0" style="color: var(--el-color-success)">成功</span>
            <span v-else-if="row.status === 1" style="color: var(--el-color-danger)">失败</span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="resultRecordCount" label="记录数" width="80" align="center" />
        <el-table-column prop="costTime" label="耗时(ms)" width="100" align="center" />
        <el-table-column prop="persisted" label="已持久化" width="80" align="center">
          <template #default="{ row }">
            {{ row.persisted === 1 ? '是' : '否' }}
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createTime" label="执行时间" width="160" align="center" :formatter="formatTime" />
        <el-table-column label="操作" width="100" align="center">
          <template #default="{ row }">
            <span class="table-action" @click="handleViewLogDetail(row)">详情</span>
            <span class="table-action danger" @click="handleDeleteLog(row)">删除</span>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:page="logQueryParams.pageNo"
        v-model:limit="logQueryParams.pageSize"
        :total="logsTotal"
        @pagination="getLogs"
      />
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import {
  getScriptConfigPage,
  createScriptConfig,
  updateScriptConfig,
  deleteScriptConfig,
  executeScript,
  getExecutionLogPage,
  deleteExecutionLog,
  getDatabaseSourcePage,
  type ScriptConfigRespVO,
  type ScriptConfigSaveReqVO,
  type ScriptConfigPageReqVO,
  type ScriptExecuteReqVO,
  type ScriptExecuteRespVO,
  type ScriptExecutionLogRespVO,
  type ScriptExecutionLogPageReqVO,
  type DatabaseSourceRespVO,
  type ScriptParamDef
} from '@/api/ubax/gather'

defineOptions({ name: 'ScriptConfig' })

const message = useMessage()

const loading = ref(false)
const configList = ref<ScriptConfigRespVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const dataSourceList = ref<DatabaseSourceRespVO[]>([])

/** 入参定义列表（新增/编辑弹窗） */
const paramDefs = ref<ScriptParamDef[]>([])

/** 输出参数定义列表（新增/编辑弹窗，存储过程用） */
const outputParamDefs = ref<string[]>([])

/** 执行入参定义（执行弹窗，从脚本配置中解析） */
const executeParamDefs = ref<ScriptParamDef[]>([])
/** 执行入参值（执行弹窗） */
const executeInputParams = ref<Record<string, any>>({})

const formData = ref<ScriptConfigSaveReqVO>({
  name: '',
  code: '',
  databaseId: 0,
  scriptType: 'sql',
  scriptContent: '',
  status: 0
})

const formRules = {
  name: [{ required: true, message: '脚本名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '脚本编码不能为空', trigger: 'blur' }],
  databaseId: [{ required: true, message: '数据源不能为空', trigger: 'change' }],
  scriptType: [{ required: true, message: '脚本类型不能为空', trigger: 'change' }],
  scriptContent: [{ required: true, message: '脚本内容不能为空', trigger: 'blur' }]
}

const queryParams = ref<ScriptConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10
})

const executeDialogVisible = ref(false)
const executing = ref(false)
const executeForm = ref({
  scriptId: 0,
  scriptName: '',
  scriptCode: '',
  dataSourceName: '',
  scriptContent: '',
  persistResult: 0
})

const resultDialogVisible = ref(false)
const executeResult = ref<ScriptExecuteRespVO | null>(null)

const logsDialogVisible = ref(false)
const logsLoading = ref(false)
const logsList = ref<ScriptExecutionLogRespVO[]>([])
const logsTotal = ref(0)
const logQueryParams = ref<ScriptExecutionLogPageReqVO>({
  pageNo: 1,
  pageSize: 10
})
const currentScriptId = ref(0)

const formatTime = (time: Date) => {
  return dateFormatter(time, 'yyyy-MM-dd HH:mm:ss')
}

/** 解析入参定义JSON */
const parseInputParams = (inputParams?: string): ScriptParamDef[] => {
  if (!inputParams) return []
  try {
    return JSON.parse(inputParams)
  } catch {
    return []
  }
}

/** 格式化执行入参用于日志展示 */
const formatInputParams = (inputParams: string): string => {
  try {
    const obj = JSON.parse(inputParams)
    return Object.entries(obj).map(([k, v]) => `${k}=${v}`).join(', ')
  } catch {
    return inputParams
  }
}

/** 格式化输出参数用于日志展示 */
const formatOutputParams = (outputParams: string): string => {
  try {
    const obj = JSON.parse(outputParams)
    return Object.entries(obj).map(([k, v]) => `${k}=${v}`).join(', ')
  } catch {
    return outputParams
  }
}

/** 解析输出参数定义JSON */
const parseOutputParams = (outputParams?: string): string[] => {
  if (!outputParams) return []
  try {
    return JSON.parse(outputParams)
  } catch {
    return []
  }
}

/** 添加参数定义 */
const addParamDef = () => {
  paramDefs.value.push({
    name: '',
    type: 'String',
    required: false,
    defaultValue: '',
    description: ''
  })
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getScriptConfigPage(queryParams.value)
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
    pageSize: 10
  }
  handleQuery()
}

const loadDataSourceList = async () => {
  const data = await getDatabaseSourcePage({ pageNo: 1, pageSize: 100, status: 0 })
  dataSourceList.value = data.list
}

const handleCreate = () => {
  dialogTitle.value = '新增脚本'
  formData.value = {
    name: '',
    code: '',
    databaseId: 0,
    scriptType: 'sql',
    scriptContent: '',
    status: 0
  }
  paramDefs.value = []
  outputParamDefs.value = []
  dialogVisible.value = true
}

const handleEdit = (row: ScriptConfigRespVO) => {
  dialogTitle.value = '编辑脚本'
  formData.value = {
    id: row.id,
    name: row.name,
    code: row.code,
    databaseId: row.databaseId,
    scriptType: row.scriptType,
    scriptContent: row.scriptContent,
    description: row.description,
    resultTableName: row.resultTableName,
    resultFieldMapping: row.resultFieldMapping,
    inputParams: row.inputParams,
    outputParams: row.outputParams,
    cronExpression: row.cronExpression,
    status: row.status,
    remark: row.remark
  }
  // 解析入参定义
  paramDefs.value = parseInputParams(row.inputParams)
  // 解析输出参数定义
  outputParamDefs.value = parseOutputParams(row.outputParams)
  dialogVisible.value = true
}

const handleDelete = async (row: ScriptConfigRespVO) => {
  await ElMessageBox.confirm(`确定要删除脚本「${row.name}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteScriptConfig(row.id)
  message.success('脚本已删除')
  getList()
}

const submitForm = async () => {
  await formRef.value?.validate()
  // 序列化入参定义（过滤掉参数名为空的行）
  const validParams = paramDefs.value.filter(p => p.name && p.name.trim())
  formData.value.inputParams = validParams.length > 0 ? JSON.stringify(validParams) : ''
  // 序列化输出参数定义（过滤掉空的参数名）
  const validOutputParams = outputParamDefs.value.filter(p => p && p.trim())
  formData.value.outputParams = validOutputParams.length > 0 ? JSON.stringify(validOutputParams) : ''
  if (formData.value.id) {
    await updateScriptConfig(formData.value)
    message.success('脚本已更新')
  } else {
    await createScriptConfig(formData.value)
    message.success('脚本已创建')
  }
  dialogVisible.value = false
  getList()
}

const handleExecute = (row: ScriptConfigRespVO) => {
  executeForm.value = {
    scriptId: row.id,
    scriptName: row.name,
    scriptCode: row.code,
    dataSourceName: row.dataSourceName || '',
    scriptContent: row.scriptContent,
    persistResult: row.resultTableName ? 1 : 0
  }
  // 解析入参定义并初始化执行入参
  executeParamDefs.value = parseInputParams(row.inputParams)
  executeInputParams.value = {}
  // 设置默认值
  executeParamDefs.value.forEach(param => {
    if (param.defaultValue !== undefined && param.defaultValue !== null && param.defaultValue !== '') {
      executeInputParams.value[param.name] = param.defaultValue
    }
  })
  executeDialogVisible.value = true
}

const handleExecuteConfirm = async () => {
  // 校验必填入参
  for (const param of executeParamDefs.value) {
    if (param.required && !executeInputParams.value[param.name]) {
      message.warning(`请填写必填参数：${param.name}`)
      return
    }
  }
  executing.value = true
  try {
    const req: ScriptExecuteReqVO = {
      scriptId: executeForm.value.scriptId,
      persistResult: executeForm.value.persistResult
    }
    // 传入执行入参
    if (executeParamDefs.value.length > 0) {
      req.inputParams = { ...executeInputParams.value }
    }
    executeResult.value = await executeScript(req)
    executeDialogVisible.value = false
    resultDialogVisible.value = true
    getList()
  } finally {
    executing.value = false
  }
}

const handleLogs = (row: ScriptConfigRespVO) => {
  currentScriptId.value = row.id
  logQueryParams.value = {
    pageNo: 1,
    pageSize: 10,
    scriptId: row.id
  }
  logsDialogVisible.value = true
  getLogs()
}

const getLogs = async () => {
  logsLoading.value = true
  try {
    const data = await getExecutionLogPage(logQueryParams.value)
    logsList.value = data.list
    logsTotal.value = data.total
  } finally {
    logsLoading.value = false
  }
}

const handleViewLogDetail = (row: ScriptExecutionLogRespVO) => {
  message.info(`日志详情：ID=${row.id}，执行时间=${row.createTime}`)
}

const handleDeleteLog = async (row: ScriptExecutionLogRespVO) => {
  await ElMessageBox.confirm('确定要删除此日志吗？', '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteExecutionLog(row.id)
  message.success('日志已删除')
  getLogs()
}

onMounted(() => {
  getList()
  loadDataSourceList()
})
</script>

<style lang="scss" scoped>
.script-config-container {
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

.table-action {
  color: var(--el-color-primary);
  cursor: pointer;
  margin: 0 6px;

  &:hover {
    opacity: 0.7;
  }

  &.success {
    color: var(--el-color-success);
  }

  &.warning {
    color: var(--el-color-warning);
  }

  &.danger {
    color: var(--el-color-danger);
  }
}

.text-gray {
  color: #999;
}

.info-row {
  display: flex;
  padding: 4px 0;
}

.info-label {
  width: 80px;
  color: #999;
}

.info-value {
  flex: 1;
  font-weight: 500;
}

.switch-tip {
  margin-left: 8px;
  font-size: 12px;
  color: #999;
}

.result-container {
  padding: 16px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 16px;
}

.result-cost {
  font-size: 14px;
  color: #666;
}

.result-body {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
}

.result-value {
  font-weight: 600;
  color: #1f2937;
}

.result-table {
  margin-top: 16px;
}

.result-more {
  text-align: center;
  padding: 8px;
  color: #999;
  font-size: 12px;
}

.result-empty {
  text-align: center;
  padding: 32px;
  color: #999;
}

.result-error {
  padding: 32px;
  text-align: center;
  color: #ef4444;
  font-size: 14px;
}

.param-config {
  width: 100%;
}

.param-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.param-name {
  font-weight: 500;
  min-width: 80px;
  color: #333;
}

.param-type {
  color: #999;
  font-size: 12px;
  min-width: 60px;
}

.param-required {
  color: #ef4444;
  font-weight: bold;
}

.param-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.5;
}

.log-search-bar {
  margin-bottom: 12px;
}

.output-params {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.output-param-item {
  display: flex;
  align-items: center;
  gap: 4px;
  background: #f0f9ff;
  padding: 4px 10px;
  border-radius: 4px;
  border: 1px solid #bae6fd;
}

.output-param-name {
  color: #0369a1;
  font-weight: 500;
  font-size: 13px;
}

.output-param-value {
  color: #1f2937;
  font-weight: 600;
  font-size: 13px;
}
</style>
