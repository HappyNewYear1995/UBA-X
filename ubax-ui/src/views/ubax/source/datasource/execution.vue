<template>
  <div class="sql-execute-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">SQL 执行引擎</span>
        </div>
      </template>

      <!-- 数据源选择 -->
      <el-form :model="executeForm" label-width="100px" class="execute-form">
        <el-form-item label="数据源">
          <el-select
            v-model="executeForm.databaseId"
            placeholder="请选择数据源"
            style="width: 300px"
            filterable
            @change="handleDataSourceChange"
          >
            <el-option
              v-for="item in dataSourceList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>{{ item.name }}</span>
              <span style="color: var(--el-text-color-secondary); margin-left: 8px">
                ({{ item.dbTypeName || item.dbType }})
              </span>
            </el-option>
          </el-select>
          <el-tag v-if="currentDataSource" type="success" class="ml-10px">
            {{ currentDataSource.host }}:{{ currentDataSource.port }}/{{ currentDataSource.database }}
          </el-tag>
        </el-form-item>
      </el-form>

      <el-tabs v-model="activeTab" type="border-card">
        <!-- SQL 执行 Tab -->
        <el-tab-pane label="SQL 执行" name="sql">
          <div class="sql-editor-section">
            <el-input
              v-model="executeForm.sql"
              type="textarea"
              :rows="8"
              placeholder="请输入 SQL 语句，如 SELECT * FROM user LIMIT 10"
              class="sql-textarea"
            />
            <div class="sql-actions">
              <el-button type="primary" @click="handleExecuteSql" :disabled="!executeForm.databaseId || !executeForm.sql">
                <Icon icon="ep:caret-right" /> 执行 SQL
              </el-button>
              <el-button @click="handleClearSql">
                <Icon icon="ep:delete" /> 清空
              </el-button>
            </div>
          </div>

          <!-- 执行结果 -->
          <div v-if="sqlResult" class="sql-result">
            <div class="result-header">
              <span class="result-title">执行结果</span>
              <el-tag :type="sqlResult.success ? 'success' : 'danger'" size="small">
                {{ sqlResult.success ? '成功' : '失败' }}
              </el-tag>
              <span v-if="sqlResult.costTime" class="result-cost">耗时: {{ sqlResult.costTime }}ms</span>
              <span v-if="sqlResult.affectedRows !== undefined" class="result-rows">
                影响行数: {{ sqlResult.affectedRows }}
              </span>
            </div>

            <el-alert
              v-if="!sqlResult.success && sqlResult.errorMessage"
              :title="sqlResult.errorMessage"
              type="error"
              :closable="false"
              show-icon
              class="result-error"
            />

            <div v-if="sqlResult.success && sqlResult.results && sqlResult.results.length > 0" class="result-table">
              <el-table :data="sqlResult.results" border stripe max-height="500">
                <el-table-column
                  v-for="key in resultColumns"
                  :key="key"
                  :prop="key"
                  :label="key"
                  min-width="120"
                  show-overflow-tooltip
                />
              </el-table>
              <div class="result-count">共 {{ sqlResult.results.length }} 条记录</div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 存储过程 Tab -->
        <el-tab-pane label="存储过程" name="procedure">
          <div class="procedure-section">
            <el-form :model="procedureForm" label-width="120px">
              <el-form-item label="存储过程名称">
                <el-input v-model="procedureForm.procedureName" placeholder="如 sp_get_user_list" />
              </el-form-item>
              <el-form-item label="输入参数">
                <el-input
                  v-model="procedureForm.inputParamsText"
                  type="textarea"
                  :rows="3"
                  placeholder='JSON 数组格式，如 ["param1", 123, true]'
                />
              </el-form-item>
              <el-form-item label="输出参数">
                <el-input
                  v-model="procedureForm.outputParamNamesText"
                  type="textarea"
                  :rows="2"
                  placeholder='JSON 数组格式，如 ["out_count", "out_message"]'
                />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleExecuteProcedure" :disabled="!executeForm.databaseId || !procedureForm.procedureName">
                  <Icon icon="ep:caret-right" /> 执行存储过程
                </el-button>
              </el-form-item>
            </el-form>

            <!-- 存储过程执行结果 -->
            <div v-if="procedureResult" class="sql-result">
              <div class="result-header">
                <span class="result-title">执行结果</span>
                <el-tag :type="procedureResult.success ? 'success' : 'danger'" size="small">
                  {{ procedureResult.success ? '成功' : '失败' }}
                </el-tag>
                <span v-if="procedureResult.costTime" class="result-cost">耗时: {{ procedureResult.costTime }}ms</span>
              </div>

              <el-alert
                v-if="!procedureResult.success && procedureResult.errorMessage"
                :title="procedureResult.errorMessage"
                type="error"
                :closable="false"
                show-icon
              />

              <div v-if="procedureResult.success && procedureResult.outputParams && Object.keys(procedureResult.outputParams).length > 0" class="output-params-section">
                <span class="output-params-label">输出参数：</span>
                <div class="output-params">
                  <div v-for="(value, key) in procedureResult.outputParams" :key="key" class="output-param-item">
                    <span class="output-param-name">{{ key }}</span>
                    <span class="output-param-value">{{ value }}</span>
                  </div>
                </div>
              </div>

              <div v-if="procedureResult.success && procedureResult.results && procedureResult.results.length > 0" class="result-table">
                <el-table :data="procedureResult.results" border stripe max-height="500">
                  <el-table-column
                    v-for="key in procedureResultColumns"
                    :key="key"
                    :prop="key"
                    :label="key"
                    min-width="120"
                    show-overflow-tooltip
                  />
                </el-table>
                <div class="result-count">共 {{ procedureResult.results.length }} 条记录</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

        <!-- 视图查询 Tab -->
        <el-tab-pane label="视图查询" name="view">
          <div class="view-section">
            <el-form :model="viewForm" label-width="120px">
              <el-form-item label="视图名称">
                <el-input v-model="viewForm.viewName" placeholder="如 v_user_summary" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleExecuteView" :disabled="!executeForm.databaseId || !viewForm.viewName">
                  <Icon icon="ep:caret-right" /> 查询视图
                </el-button>
              </el-form-item>
            </el-form>

            <!-- 视图查询结果 -->
            <div v-if="viewResult" class="sql-result">
              <div class="result-header">
                <span class="result-title">查询结果</span>
                <el-tag :type="viewResult.success ? 'success' : 'danger'" size="small">
                  {{ viewResult.success ? '成功' : '失败' }}
                </el-tag>
                <span v-if="viewResult.costTime" class="result-cost">耗时: {{ viewResult.costTime }}ms</span>
              </div>

              <el-alert
                v-if="!viewResult.success && viewResult.errorMessage"
                :title="viewResult.errorMessage"
                type="error"
                :closable="false"
                show-icon
              />

              <div v-if="viewResult.success && viewResult.results && viewResult.results.length > 0" class="result-table">
                <el-table :data="viewResult.results" border stripe max-height="500">
                  <el-table-column
                    v-for="key in viewResultColumns"
                    :key="key"
                    :prop="key"
                    :label="key"
                    min-width="120"
                    show-overflow-tooltip
                  />
                </el-table>
                <div class="result-count">共 {{ viewResult.results.length }} 条记录</div>
              </div>
            </div>
          </div>
        </el-tab-pane>

      </el-tabs>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import {
  getDatabaseSourcePage,
  executeSql,
  executeProcedure,
  executeViewQuery,
  type DatabaseSourceRespVO,
  type SqlExecuteReqVO,
  type SqlExecuteRespVO,
  type ProcedureReqVO
} from '@/api/ubax/gather/datasource/database'

defineOptions({ name: 'DatabaseSourceSqlExecute' })

const message = useMessage()
const route = useRoute()

const activeTab = ref('sql')
const dataSourceList = ref<DatabaseSourceRespVO[]>([])
const currentDataSource = ref<DatabaseSourceRespVO | null>(null)

const executeForm = ref({
  databaseId: undefined as number | undefined,
  sql: ''
})

const procedureForm = ref({
  procedureName: '',
  inputParamsText: '',
  outputParamNamesText: ''
})

const viewForm = ref({
  viewName: ''
})

const sqlResult = ref<SqlExecuteRespVO | null>(null)
const procedureResult = ref<SqlExecuteRespVO | null>(null)
const viewResult = ref<SqlExecuteRespVO | null>(null)

const resultColumns = computed(() => {
  if (!sqlResult.value?.results || sqlResult.value.results.length === 0) return []
  return Object.keys(sqlResult.value.results[0])
})

const procedureResultColumns = computed(() => {
  if (!procedureResult.value?.results || procedureResult.value.results.length === 0) return []
  return Object.keys(procedureResult.value.results[0])
})

const viewResultColumns = computed(() => {
  if (!viewResult.value?.results || viewResult.value.results.length === 0) return []
  return Object.keys(viewResult.value.results[0])
})

/** 加载数据源列表 */
const loadDataSourceList = async () => {
  try {
    const data = await getDatabaseSourcePage({ pageNo: 1, pageSize: 100, status: 0 })
    dataSourceList.value = data.list
  } catch {
    // 静默失败
  }
}

/** 数据源切换 */
const handleDataSourceChange = (id: number) => {
  currentDataSource.value = dataSourceList.value.find((item) => item.id === id) || null
  sqlResult.value = null
  procedureResult.value = null
  viewResult.value = null
}

/** 执行 SQL */
const handleExecuteSql = async () => {
  if (!executeForm.value.databaseId) {
    message.warning('请先选择数据源')
    return
  }
  if (!executeForm.value.sql.trim()) {
    message.warning('请输入 SQL 语句')
    return
  }

  try {
    const data: SqlExecuteReqVO = {
      databaseId: executeForm.value.databaseId,
      sql: executeForm.value.sql.trim()
    }
    sqlResult.value = await executeSql(data)
    if (!sqlResult.value.success) {
      message.error(sqlResult.value.errorMessage || 'SQL 执行失败')
    }
  } catch {
    message.error('SQL 执行异常')
  }
}

/** 清空 SQL */
const handleClearSql = () => {
  executeForm.value.sql = ''
  sqlResult.value = null
}

/** 执行存储过程 */
const handleExecuteProcedure = async () => {
  if (!executeForm.value.databaseId) {
    message.warning('请先选择数据源')
    return
  }

  const reqVO: ProcedureReqVO = {
    databaseId: executeForm.value.databaseId,
    procedureName: procedureForm.value.procedureName
  }

  // 解析输入参数
  if (procedureForm.value.inputParamsText.trim()) {
    try {
      reqVO.inputParams = JSON.parse(procedureForm.value.inputParamsText)
    } catch {
      message.warning('输入参数格式错误，请使用 JSON 数组格式')
      return
    }
  }

  // 解析输出参数
  if (procedureForm.value.outputParamNamesText.trim()) {
    try {
      reqVO.outputParamNames = JSON.parse(procedureForm.value.outputParamNamesText)
    } catch {
      message.warning('输出参数格式错误，请使用 JSON 数组格式')
      return
    }
  }

  try {
    procedureResult.value = await executeProcedure(reqVO)
    if (!procedureResult.value.success) {
      message.error(procedureResult.value.errorMessage || '存储过程执行失败')
    }
  } catch {
    message.error('存储过程执行异常')
  }
}

/** 执行视图查询 */
const handleExecuteView = async () => {
  if (!executeForm.value.databaseId) {
    message.warning('请先选择数据源')
    return
  }
  if (!viewForm.value.viewName.trim()) {
    message.warning('请输入视图名称')
    return
  }

  try {
    viewResult.value = await executeViewQuery(executeForm.value.databaseId, viewForm.value.viewName.trim())
    if (!viewResult.value.success) {
      message.error(viewResult.value.errorMessage || '视图查询失败')
    }
  } catch {
    message.error('视图查询异常')
  }
}

onMounted(() => {
  loadDataSourceList()

  // 如果从数据源列表跳转过来，自动选中对应数据源
  const databaseId = route.query.databaseId
  if (databaseId) {
    executeForm.value.databaseId = Number(databaseId)
  }
})
</script>

<style lang="scss" scoped>
.sql-execute-container {
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

.execute-form {
  margin-bottom: 16px;
}

.ml-10px {
  margin-left: 10px;
}

.sql-editor-section {
  margin-bottom: 16px;
}

.sql-textarea {
  font-family: 'Courier New', monospace;
  font-size: 14px;
}

.sql-actions {
  margin-top: 12px;
  display: flex;
  gap: 12px;
}

.sql-result {
  margin-top: 16px;
  padding: 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.result-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.result-cost,
.result-rows {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.result-error {
  margin-bottom: 12px;
}

.result-table {
  margin-top: 12px;
}

.result-count {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: right;
}

.procedure-section,
.view-section {
  padding: 16px 0;
}

.output-params-section {
  margin-bottom: 12px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.output-params-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  white-space: nowrap;
  line-height: 28px;
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
