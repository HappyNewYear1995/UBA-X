<template>
  <div class="processing-script-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">处理脚本</span>
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
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <span class="table-action" @click="handleEdit(row)">编辑</span>
            <span class="table-action success" @click="handleExecute(row)">执行</span>
            <span class="table-action warning" @click="handleViewLog(row)">日志</span>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="960px" :close-on-click-modal="false" class="script-dialog">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="脚本名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入脚本名称" />
        </el-form-item>
        <el-form-item label="脚本编码" prop="code">
          <el-input v-model="formData.code" placeholder="请输入脚本编码（唯一标识）" />
        </el-form-item>
        <el-form-item label="脚本内容" prop="scriptContent" class="script-editor-form-item">
          <div class="script-editor">
            <div class="script-editor__header">
              <span class="script-editor__lang">Groovy</span>
              <span class="script-editor__hint">
                可用变量：invoker · inputParams · logger
              </span>
            </div>
            <div class="script-editor__body">
              <div class="script-editor__lines">
                <div v-for="n in lineCount" :key="n" class="script-editor__line-num">{{ n }}</div>
              </div>
              <textarea
                ref="scriptTextareaRef"
                v-model="formData.scriptContent"
                class="script-editor__textarea"
                placeholder="// Groovy 处理脚本 - 通用数据处理与编排&#10;// 可用: invoker.callWebService() / invoker.getJdbcTemplate() / logger.info()&#10;// 脚本需返回 List&lt;Map&gt; 格式结果&#10;&#10;def wsResult = invoker.callWebService(1, [param: 'test'])&#10;def jdbc = invoker.getJdbcTemplate()&#10;return wsResult.results"
                spellcheck="false"
                @scroll="handleScriptScroll"
                @input="updateLineCount"
              ></textarea>
            </div>
            <div class="script-editor__footer">
              <span>行数：{{ lineCount }}</span>
              <span>字符：{{ formData.scriptContent?.length || 0 }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="脚本描述">
          <el-input v-model="formData.description" placeholder="请输入脚本描述" />
        </el-form-item>
        <el-divider content-position="left">入参配置（可选）</el-divider>
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
          </div>
        </el-form-item>
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
    <el-dialog v-model="executeDialogVisible" title="执行处理脚本" width="700px" :close-on-click-modal="false">
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
        </div>
        <div v-else class="result-error">
          {{ executeResult.errorMessage }}
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resultDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 执行日志弹窗 -->
    <el-dialog v-model="logDialogVisible" title="执行日志" width="900px">
      <div class="log-filter-bar">
        <el-select v-model="logQueryParams.status" placeholder="执行状态" clearable style="width: 120px" @change="getLogList">
          <el-option label="成功" :value="0" />
          <el-option label="失败" :value="1" />
        </el-select>
        <el-button @click="getLogList"><Icon icon="ep:refresh" /> 刷新</el-button>
      </div>
      <el-table v-loading="logLoading" :data="logList" stripe>
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="scriptName" label="脚本" width="120" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="70" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.status === 0 ? 'var(--el-color-success)' : 'var(--el-color-danger)' }">
              {{ row.status === 0 ? '成功' : '失败' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="80" align="center" />
        <el-table-column prop="resultRecordCount" label="记录数" width="70" align="center" />
        <el-table-column prop="persisted" label="持久化" width="70" align="center">
          <template #default="{ row }">
            <span v-if="row.persisted === 1" style="color: var(--el-color-success)">是</span>
            <span v-else style="color: #999">否</span>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="执行时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <span class="table-action" @click="handleViewLogDetail(row)">详情</span>
          </template>
        </el-table-column>
      </el-table>
      <Pagination
        v-model:page="logQueryParams.pageNo"
        v-model:limit="logQueryParams.pageSize"
        :total="logTotal"
        @pagination="getLogList"
      />
    </el-dialog>

    <!-- 日志详情弹窗 -->
    <el-dialog v-model="logDetailDialogVisible" title="日志详情" width="800px">
      <div v-if="logDetail" class="log-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="脚本名称">{{ logDetail.scriptName }}</el-descriptions-item>
          <el-descriptions-item label="脚本编码">{{ logDetail.scriptCode }}</el-descriptions-item>
          <el-descriptions-item label="执行状态">
            <el-tag :type="logDetail.status === 0 ? 'success' : 'danger'" size="small">
              {{ logDetail.status === 0 ? '成功' : '失败' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="执行耗时">{{ logDetail.costTime }}ms</el-descriptions-item>
          <el-descriptions-item label="结果记录数">{{ logDetail.resultRecordCount || 0 }}</el-descriptions-item>
          <el-descriptions-item label="是否持久化">{{ logDetail.persisted === 1 ? '是' : '否' }}</el-descriptions-item>
          <el-descriptions-item label="执行时间" :span="2">{{ formatTime(logDetail.createTime) }}</el-descriptions-item>
        </el-descriptions>
        <div v-if="logDetail.inputParams" class="log-section">
          <div class="log-section-title">执行入参</div>
          <pre class="log-pre">{{ formatJson(logDetail.inputParams) }}</pre>
        </div>
        <div v-if="logDetail.errorMessage" class="log-section">
          <div class="log-section-title" style="color: var(--el-color-danger)">错误信息</div>
          <pre class="log-pre log-error">{{ logDetail.errorMessage }}</pre>
        </div>
        <div v-if="logDetail.persistError" class="log-section">
          <div class="log-section-title" style="color: var(--el-color-danger)">持久化错误</div>
          <pre class="log-pre log-error">{{ logDetail.persistError }}</pre>
        </div>
        <div v-if="logDetail.executeResult" class="log-section">
          <div class="log-section-title">执行结果</div>
          <pre class="log-pre">{{ formatJson(logDetail.executeResult) }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="logDetailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import {
  getProcessingScriptPage,
  createProcessingScript,
  updateProcessingScript,
  deleteProcessingScript,
  executeProcessingScript,
  getProcessingScriptLogPage,
  getProcessingScriptLog,
  type ProcessingScriptRespVO,
  type ProcessingScriptSaveReqVO,
  type ProcessingScriptPageReqVO,
  type ProcessingScriptExecuteReqVO,
  type ProcessingScriptExecuteRespVO,
  type ProcessingScriptLogRespVO,
  type ProcessingScriptLogPageReqVO
} from '@/api/ubax/gather/datasource/processing-script'

interface ScriptParamDef {
  name: string
  type: string
  required: boolean
  defaultValue?: any
  description?: string
}

defineOptions({ name: 'ProcessingScript' })

const message = useMessage()
const loading = ref(false)
const configList = ref<ProcessingScriptRespVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const paramDefs = ref<ScriptParamDef[]>([])
const executeParamDefs = ref<ScriptParamDef[]>([])
const executeInputParams = ref<Record<string, any>>({})

const formData = ref<ProcessingScriptSaveReqVO>({
  name: '',
  code: '',
  scriptContent: '',
  status: 0
})

const formRules = {
  name: [{ required: true, message: '脚本名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '脚本编码不能为空', trigger: 'blur' }],
  scriptContent: [{ required: true, message: '脚本内容不能为空', trigger: 'blur' }]
}

const queryParams = ref<ProcessingScriptPageReqVO>({
  pageNo: 1,
  pageSize: 10
})

const executeDialogVisible = ref(false)
const executing = ref(false)
const executeForm = ref({ scriptId: 0, scriptName: '', scriptCode: '', persistResult: 0 })

const resultDialogVisible = ref(false)
const executeResult = ref<ProcessingScriptExecuteRespVO | null>(null)

// 日志相关
const logDialogVisible = ref(false)
const logLoading = ref(false)
const logList = ref<ProcessingScriptLogRespVO[]>([])
const logTotal = ref(0)
const logQueryParams = ref<ProcessingScriptLogPageReqVO>({ pageNo: 1, pageSize: 10 })
const logDetailDialogVisible = ref(false)
const logDetail = ref<ProcessingScriptLogRespVO | null>(null)

const scriptTextareaRef = ref<HTMLTextAreaElement>()
const lineCount = ref(1)

const updateLineCount = () => {
  const content = formData.value.scriptContent || ''
  lineCount.value = content ? content.split('\n').length : 1
}

const handleScriptScroll = () => {
  const textarea = scriptTextareaRef.value
  const lines = textarea?.parentElement?.querySelector('.script-editor__lines') as HTMLElement
  if (textarea && lines) {
    lines.scrollTop = textarea.scrollTop
  }
}

const formatTime = (time: Date) => dateFormatter(time, 'yyyy-MM-dd HH:mm:ss')

const parseInputParams = (inputParams?: string): ScriptParamDef[] => {
  if (!inputParams) return []
  try { return JSON.parse(inputParams) } catch { return [] }
}

const addParamDef = () => {
  paramDefs.value.push({ name: '', type: 'String', required: false, defaultValue: '', description: '' })
}

const getList = async () => {
  loading.value = true
  try {
    const data = await getProcessingScriptPage(queryParams.value)
    configList.value = data.list
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const handleQuery = () => { queryParams.value.pageNo = 1; getList() }
const resetQuery = () => { queryParams.value = { pageNo: 1, pageSize: 10 }; handleQuery() }

const handleCreate = () => {
  dialogTitle.value = '新增处理脚本'
  formData.value = { name: '', code: '', scriptContent: '', status: 0 }
  paramDefs.value = []
  lineCount.value = 1
  dialogVisible.value = true
}

const handleEdit = (row: ProcessingScriptRespVO) => {
  dialogTitle.value = '编辑处理脚本'
  formData.value = {
    id: row.id, name: row.name, code: row.code, scriptContent: row.scriptContent,
    description: row.description, resultTableName: row.resultTableName,
    resultFieldMapping: row.resultFieldMapping, inputParams: row.inputParams,
    cronExpression: row.cronExpression, status: row.status, remark: row.remark
  }
  paramDefs.value = parseInputParams(row.inputParams)
  nextTick(() => updateLineCount())
  dialogVisible.value = true
}

const handleDelete = async (row: ProcessingScriptRespVO) => {
  await ElMessageBox.confirm(`确定要删除脚本「${row.name}」吗？`, '警告', { type: 'warning' })
  await deleteProcessingScript(row.id)
  message.success('脚本已删除')
  getList()
}

const submitForm = async () => {
  await formRef.value?.validate()
  const validParams = paramDefs.value.filter(p => p.name && p.name.trim())
  formData.value.inputParams = validParams.length > 0 ? JSON.stringify(validParams) : ''
  if (formData.value.id) {
    await updateProcessingScript(formData.value)
    message.success('脚本已更新')
  } else {
    await createProcessingScript(formData.value)
    message.success('脚本已创建')
  }
  dialogVisible.value = false
  getList()
}

const handleExecute = (row: ProcessingScriptRespVO) => {
  executeForm.value = {
    scriptId: row.id, scriptName: row.name, scriptCode: row.code,
    persistResult: row.resultTableName ? 1 : 0
  }
  executeParamDefs.value = parseInputParams(row.inputParams)
  executeInputParams.value = {}
  executeParamDefs.value.forEach(p => {
    if (p.defaultValue) executeInputParams.value[p.name] = p.defaultValue
  })
  executeDialogVisible.value = true
}

const handleExecuteConfirm = async () => {
  for (const p of executeParamDefs.value) {
    if (p.required && !executeInputParams.value[p.name]) {
      message.warning(`请填写必填参数：${p.name}`)
      return
    }
  }
  executing.value = true
  try {
    const req: ProcessingScriptExecuteReqVO = {
      scriptId: executeForm.value.scriptId,
      persistResult: executeForm.value.persistResult
    }
    if (executeParamDefs.value.length > 0) req.inputParams = { ...executeInputParams.value }
    executeResult.value = await executeProcessingScript(req)
    executeDialogVisible.value = false
    resultDialogVisible.value = true
    getList()
  } finally {
    executing.value = false
  }
}

const handleViewLog = (row: ProcessingScriptRespVO) => {
  logQueryParams.value = { pageNo: 1, pageSize: 10, scriptId: row.id }
  logDialogVisible.value = true
  getLogList()
}

const getLogList = async () => {
  logLoading.value = true
  try {
    const data = await getProcessingScriptLogPage(logQueryParams.value)
    logList.value = data.list
    logTotal.value = data.total
  } finally {
    logLoading.value = false
  }
}

const handleViewLogDetail = async (row: ProcessingScriptLogRespVO) => {
  const data = await getProcessingScriptLog(row.id)
  logDetail.value = data
  logDetailDialogVisible.value = true
}

const formatJson = (str: string | undefined) => {
  if (!str) return ''
  try { return JSON.stringify(JSON.parse(str), null, 2) } catch { return str }
}

onMounted(() => { getList() })
</script>

<style lang="scss" scoped>
.processing-script-container { padding: 16px; }
.dashboard-card { border-radius: 8px; :deep(.el-card__header) { border-bottom: 1px solid var(--el-border-color-light); padding: 14px 20px; } }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-title { font-size: 15px; font-weight: 600; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.search-input { width: 200px; }
.filter-select { width: 130px; }
.table-action { color: var(--el-color-primary); cursor: pointer; margin: 0 6px; &:hover { opacity: 0.7; } &.success { color: var(--el-color-success); } &.warning { color: var(--el-color-warning); } &.danger { color: var(--el-color-danger); } }
.text-gray { color: #999; }
.info-row { display: flex; padding: 4px 0; }
.info-label { width: 80px; color: #999; }
.info-value { flex: 1; font-weight: 500; }
.param-config { width: 100%; }
.param-row { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; }
.param-name { font-weight: 500; min-width: 80px; color: #333; }
.param-type { color: #999; font-size: 12px; min-width: 60px; }
.param-required { color: #ef4444; font-weight: bold; }
.result-container { padding: 16px; }
.result-header { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.result-cost { font-size: 14px; color: #666; }
.result-body { background: #fafafa; padding: 16px; border-radius: 8px; }
.result-value { font-weight: 600; color: #1f2937; }
.result-table { margin-top: 16px; }
.result-more { text-align: center; padding: 8px; color: #999; font-size: 12px; }
.result-error { padding: 32px; text-align: center; color: #ef4444; font-size: 14px; }

.log-filter-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.log-detail { padding: 8px 0; }
.log-section { margin-top: 16px; }
.log-section-title { font-weight: 600; margin-bottom: 8px; font-size: 14px; }
.log-pre { background: #f5f5f5; padding: 12px; border-radius: 6px; font-size: 12px; max-height: 300px; overflow: auto; white-space: pre-wrap; word-break: break-all; margin: 0; }
.log-error { color: #ef4444; background: #fef2f2; }

// 脚本编辑器
.script-editor {
  width: 100%;
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  overflow: hidden;
  transition: border-color 0.2s;

  &:focus-within {
    border-color: var(--el-color-primary);
    box-shadow: 0 0 0 1px var(--el-color-primary) inset;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 8px 14px;
    background: #1e1e1e;
    border-bottom: 1px solid #333;
  }

  &__lang {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    color: #4ec9b0;
    font-size: 12px;
    font-weight: 600;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    letter-spacing: 0.5px;

    &::before {
      content: '';
      display: inline-block;
      width: 8px;
      height: 8px;
      background: #4ec9b0;
      border-radius: 50%;
    }
  }

  &__hint {
    color: #6a9955;
    font-size: 11px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  }

  &__body {
    display: flex;
    background: #1e1e1e;
    height: 360px;
  }

  &__lines {
    flex-shrink: 0;
    width: 44px;
    padding: 12px 0;
    background: #1e1e1e;
    border-right: 1px solid #333;
    overflow: hidden;
    user-select: none;
  }

  &__line-num {
    height: 20px;
    line-height: 20px;
    padding-right: 10px;
    text-align: right;
    color: #5a5a5a;
    font-size: 12px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  }

  &__textarea {
    flex: 1;
    padding: 12px 14px;
    background: #1e1e1e;
    border: none;
    outline: none;
    resize: none;
    color: #d4d4d4;
    font-size: 13px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    line-height: 20px;
    tab-size: 4;
    white-space: pre;
    overflow: auto;

    &::placeholder {
      color: #555;
    }

    &::-webkit-scrollbar {
      width: 8px;
      height: 8px;
    }

    &::-webkit-scrollbar-track {
      background: #1e1e1e;
    }

    &::-webkit-scrollbar-thumb {
      background: #444;
      border-radius: 4px;

      &:hover {
        background: #555;
      }
    }
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 16px;
    padding: 6px 14px;
    background: #252526;
    border-top: 1px solid #333;
    color: #6a6a6a;
    font-size: 11px;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  }
}

.script-editor-form-item {
  :deep(.el-form-item__content) {
    line-height: normal;
  }
}
</style>
