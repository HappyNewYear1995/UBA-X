<template>
  <div class="ws-execute-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">Web 服务调用测试</span>
        </div>
      </template>

      <el-form :model="executeForm" label-width="100px" class="execute-form">
        <el-form-item label="Web服务">
          <el-select
            v-model="executeForm.databaseId"
            placeholder="请选择 WebService 数据源"
            style="width: 300px"
            filterable
            @change="handleDataSourceChange"
          >
            <el-option
              v-for="item in wsSourceList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <span>{{ item.name }}</span>
              <span style="color: var(--el-text-color-secondary); margin-left: 8px">
                ({{ item.method || 'POST' }})
              </span>
            </el-option>
          </el-select>
          <el-tag v-if="currentWsSource" :type="currentWsSource.status === 0 ? 'success' : 'danger'" class="ml-10px">
            {{ currentWsSource.status === 0 ? '正常' : '异常' }}
          </el-tag>
        </el-form-item>
      </el-form>

      <el-form :model="wsForm" label-width="120px">
        <el-form-item label="请求 URL">
          <el-input v-model="wsForm.url" placeholder="覆盖数据源默认 URL（留空使用默认）" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="请求方法">
              <el-select v-model="wsForm.method" style="width: 100%">
                <el-option label="使用默认" value="" />
                <el-option label="GET" value="GET" />
                <el-option label="POST" value="POST" />
                <el-option label="PUT" value="PUT" />
                <el-option label="DELETE" value="DELETE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证类型">
              <el-select v-model="wsForm.authType" style="width: 100%">
                <el-option label="使用默认" value="" />
                <el-option label="无需认证" value="none" />
                <el-option label="Basic Auth" value="basic" />
                <el-option label="Bearer Token" value="bearer" />
                <el-option label="API Key" value="apikey" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="wsForm.authType && wsForm.authType !== 'none'" label="认证凭据">
          <el-input v-model="wsForm.authToken" placeholder="覆盖数据源默认凭据" />
        </el-form-item>
        <el-form-item label="请求头">
          <el-input
            v-model="wsForm.headers"
            type="textarea"
            :rows="2"
            placeholder='覆盖数据源默认请求头，JSON 格式'
          />
        </el-form-item>
        <el-form-item label="请求体">
          <el-input
            v-model="wsForm.body"
            type="textarea"
            :rows="4"
            placeholder="JSON 格式请求体（POST/PUT 时使用）"
          />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SOAP 操作名">
              <el-input v-model="wsForm.soapAction" placeholder="覆盖数据源默认 SOAP 操作" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="响应数据路径">
              <el-input v-model="wsForm.responsePath" placeholder="JSONPath，如 $.data.list" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item>
          <el-button type="primary" @click="handleExecuteWebService" :disabled="!executeForm.databaseId">
            <Icon icon="ep:caret-right" /> 执行请求
          </el-button>
          <el-button @click="handleClearWs">
            <Icon icon="ep:delete" /> 清空
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 执行结果 -->
      <div v-if="wsResult" class="sql-result">
        <div class="result-header">
          <span class="result-title">执行结果</span>
          <el-tag :type="wsResult.success ? 'success' : 'danger'" size="small">
            {{ wsResult.success ? '成功' : '失败' }}
          </el-tag>
          <span v-if="wsResult.costTime" class="result-cost">耗时: {{ wsResult.costTime }}ms</span>
          <span v-if="wsResult.affectedRows !== undefined" class="result-rows">
            数据行数: {{ wsResult.affectedRows }}
          </span>
        </div>

        <el-alert
          v-if="!wsResult.success && wsResult.errorMessage"
          :title="wsResult.errorMessage"
          type="error"
          :closable="false"
          show-icon
        />

        <div v-if="wsResult.success && wsResult.results && wsResult.results.length > 0" class="result-table">
          <el-table :data="wsResult.results" border stripe max-height="500">
            <el-table-column
              v-for="key in wsResultColumns"
              :key="key"
              :prop="key"
              :label="key"
              min-width="120"
              show-overflow-tooltip
            />
          </el-table>
          <div class="result-count">共 {{ wsResult.results.length }} 条记录</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
import {
  getWebServiceSourcePage,
  executeWebService as executeWsApi,
  type WebServiceSourceRespVO,
  type WebServiceExecuteReqVO
} from '@/api/ubax/gather/datasource/webservice'
import type { SqlExecuteRespVO } from '@/api/ubax/gather/datasource/database'

defineOptions({ name: 'WebServiceExecution' })

const message = useMessage()
const route = useRoute()

const wsSourceList = ref<WebServiceSourceRespVO[]>([])
const currentWsSource = ref<WebServiceSourceRespVO | null>(null)

const executeForm = ref({
  databaseId: undefined as number | undefined
})

const wsForm = ref({
  url: '',
  method: '',
  headers: '',
  body: '',
  authType: '',
  authToken: '',
  soapNamespace: '',
  soapAction: '',
  responsePath: ''
})

const wsResult = ref<SqlExecuteRespVO | null>(null)

const wsResultColumns = computed(() => {
  if (!wsResult.value?.results || wsResult.value.results.length === 0) return []
  return Object.keys(wsResult.value.results[0])
})

const loadWsSourceList = async () => {
  try {
    const data = await getWebServiceSourcePage({ pageNo: 1, pageSize: 100, status: 0 })
    wsSourceList.value = data.list
  } catch {
    // 静默失败
  }
}

const handleDataSourceChange = (id: number) => {
  currentWsSource.value = wsSourceList.value.find((item) => item.id === id) || null
  wsResult.value = null
}

const handleExecuteWebService = async () => {
  if (!executeForm.value.databaseId) {
    message.warning('请先选择 WebService 数据源')
    return
  }

  const reqVO: WebServiceExecuteReqVO = {
    databaseId: executeForm.value.databaseId
  }
  if (wsForm.value.url) reqVO.url = wsForm.value.url
  if (wsForm.value.method) reqVO.method = wsForm.value.method
  if (wsForm.value.headers) reqVO.headers = wsForm.value.headers
  if (wsForm.value.body) reqVO.body = wsForm.value.body
  if (wsForm.value.authType) reqVO.authType = wsForm.value.authType
  if (wsForm.value.authToken) reqVO.authToken = wsForm.value.authToken
  if (wsForm.value.soapNamespace) reqVO.soapNamespace = wsForm.value.soapNamespace
  if (wsForm.value.soapAction) reqVO.soapAction = wsForm.value.soapAction
  if (wsForm.value.responsePath) reqVO.responsePath = wsForm.value.responsePath

  try {
    wsResult.value = await executeWsApi(reqVO)
    if (!wsResult.value.success) {
      message.error(wsResult.value.errorMessage || 'WebService 请求失败')
    }
  } catch {
    message.error('WebService 请求异常')
  }
}

const handleClearWs = () => {
  wsForm.value = {
    url: '',
    method: '',
    headers: '',
    body: '',
    authType: '',
    authToken: '',
    soapNamespace: '',
    soapAction: '',
    responsePath: ''
  }
  wsResult.value = null
}

onMounted(() => {
  loadWsSourceList()
  const wsId = route.query.wsId
  if (wsId) {
    executeForm.value.databaseId = Number(wsId)
  }
})
</script>

<style lang="scss" scoped>
.ws-execute-container {
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

.result-table {
  margin-top: 12px;
}

.result-count {
  margin-top: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
  text-align: right;
}
</style>
