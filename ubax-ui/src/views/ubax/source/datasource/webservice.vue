<template>
  <div class="datasource-config-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">WebService 数据源管理</span>
          <el-button type="primary" @click="handleCreate">
            <Icon icon="ep:plus"/>
            新增数据源
          </el-button>
        </div>
      </template>

      <div class="search-bar">
        <el-input
          v-model="queryParams.name"
          placeholder="搜索数据源名称"
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
          <el-option label="全部" :value="undefined"/>
          <el-option label="正常" :value="0"/>
          <el-option label="异常" :value="1"/>
        </el-select>
        <el-button type="primary" @click="handleQuery">
          <Icon icon="ep:search"/>
          搜索
        </el-button>
        <el-button @click="resetQuery">
          <Icon icon="ep:refresh"/>
          重置
        </el-button>
      </div>

      <el-table v-loading="loading" :data="configList" stripe>
        <el-table-column prop="id" label="ID" width="70" align="center"/>
        <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip/>
        <el-table-column prop="url" label="服务地址" min-width="200" show-overflow-tooltip/>
        <el-table-column prop="method" label="请求方法" width="100" align="center">
          <template #default="{ row }">
            {{ row.method || 'POST' }}
          </template>
        </el-table-column>
        <el-table-column prop="authType" label="认证类型" width="120" align="center">
          <template #default="{ row }">
            {{ authTypeMap[row.authType] || '无需认证' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            {{ row.status === 0 ? '正常' : '异常' }}
          </template>
        </el-table-column>
        <el-table-column prop="syncInterval" label="同步间隔" width="100" align="center">
          <template #default="{ row }">
            {{ row.syncInterval ? row.syncInterval + '秒' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip/>
        <el-table-column
          label="创建时间"
          align="center"
          prop="createTime"
          width="180"
          :formatter="dateFormatter"
        />
        <el-table-column label="操作" width="240" fixed="right" align="center">
          <template #default="{ row }">
            <span class="table-action success" @click="handleTest(row)">测试</span>
            <span class="table-action" @click="handleEdit(row)">编辑</span>
            <span class="table-action warning" @click="handleExecute(row)">执行</span>
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
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px"
               :close-on-click-modal="false">
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="120px">
        <el-form-item label="数据源名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入数据源名称"/>
        </el-form-item>
        <el-form-item label="服务地址" prop="url">
          <el-input v-model="formData.url" placeholder="请输入 WebService 地址，如 http://example.com/api"/>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="请求方法" prop="method">
              <el-select v-model="formData.method" placeholder="请选择请求方法" style="width: 100%">
                <el-option label="GET" value="GET"/>
                <el-option label="POST" value="POST"/>
                <el-option label="PUT" value="PUT"/>
                <el-option label="DELETE" value="DELETE"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="认证类型" prop="authType">
              <el-select v-model="formData.authType" placeholder="请选择认证类型" style="width: 100%">
                <el-option label="无需认证" value="none"/>
                <el-option label="Basic Auth" value="basic"/>
                <el-option label="Bearer Token" value="bearer"/>
                <el-option label="API Key" value="apikey"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="formData.authType && formData.authType !== 'none'" label="认证凭据" prop="authToken">
          <el-input
            v-model="formData.authToken"
            type="password"
            show-password
            :placeholder="formData.id ? '如需修改请清空后输入新凭据' : '请输入认证凭据'"
          />
        </el-form-item>

        <el-divider content-position="left">请求配置</el-divider>
        <el-form-item label="请求头">
          <el-input
            v-model="formData.headers"
            type="textarea"
            :rows="3"
            placeholder='JSON 格式，如 {"Content-Type":"application/json","Accept":"application/xml"}'
          />
        </el-form-item>
        <el-form-item label="请求体">
          <el-input
            v-model="formData.body"
            type="textarea"
            :rows="4"
            placeholder='JSON 格式，如 {"key":"value"}'
          />
        </el-form-item>

        <el-divider content-position="left">SOAP 配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="SOAP 命名空间">
              <el-input v-model="formData.soapNamespace" placeholder="如 http://www.w3.org/2003/05/soap-envelope"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SOAP 操作名">
              <el-input v-model="formData.soapAction" placeholder="如 http://example.com/GetData"/>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">其他配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="响应数据路径">
              <el-input v-model="formData.responsePath" placeholder="JSONPath，如 $.data.items"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="同步间隔(秒)">
              <el-input-number v-model="formData.syncInterval" :min="0" :step="10"
                               controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
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
        <el-button type="success" :loading="testLoading" @click="handleTestInForm">
          <Icon icon="ep:connection"/>
          测试连接
        </el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { dateFormatter } from '@/utils/formatTime'
import {
  createWebServiceSource,
  deleteWebServiceSource,
  getWebServiceSourcePage,
  testWebServiceConnection,
  updateWebServiceSource,
  type WebServicePageReqVO,
  type WebServiceSourceRespVO,
  type WebServiceSourceSaveReqVO
} from '@/api/ubax/gather/datasource/webservice'

defineOptions({ name: 'WebServiceSource' })

const message = useMessage()
const router = useRouter()
const loading = ref(false)
const testLoading = ref(false)
const configList = ref<WebServiceSourceRespVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

/** 认证类型映射 */
const authTypeMap: Record<string, string> = {
  none: '无需认证',
  basic: 'Basic Auth',
  bearer: 'Bearer Token',
  apikey: 'API Key'
}

const formData = ref<WebServiceSourceSaveReqVO>({
  name: '',
  url: '',
  method: 'POST',
  authType: 'none',
  syncInterval: 0
})

const formRules = {
  name: [{ required: true, message: '数据源名称不能为空', trigger: 'blur' }],
  url: [{ required: true, message: '服务地址不能为空', trigger: 'blur' }]
}

const queryParams = ref<WebServicePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  status: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await getWebServiceSourcePage(queryParams.value)
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
    name: undefined,
    status: undefined
  }
  handleQuery()
}

const handleCreate = () => {
  dialogTitle.value = '新增 WebService 数据源'
  formData.value = {
    name: '',
    url: '',
    method: 'POST',
    authType: 'none',
    syncInterval: 0
  }
  dialogVisible.value = true
}

const handleEdit = (row: WebServiceSourceRespVO) => {
  dialogTitle.value = '编辑 WebService 数据源'
  formData.value = {
    id: row.id,
    name: row.name,
    url: row.url,
    method: row.method,
    headers: row.headers,
    body: row.body,
    authType: row.authType,
    authToken: row.authToken || '',
    soapNamespace: row.soapNamespace,
    soapAction: row.soapAction,
    responsePath: row.responsePath,
    syncInterval: row.syncInterval,
    remark: row.remark
  }
  dialogVisible.value = true
}

const handleDelete = async (row: WebServiceSourceRespVO) => {
  await ElMessageBox.confirm(`确定要删除数据源「${row.name}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteWebServiceSource(row.id)
  message.success('数据源已删除')
  getList()
}

const handleTest = async (row: WebServiceSourceRespVO) => {
  try {
    const result = await testWebServiceConnection(row.id)
    if (result) {
      message.success('连接测试成功')
    } else {
      message.error('连接测试失败')
    }
  } catch {
    message.error('连接测试失败')
  }
  await getList()
}

/** 表单内测试连接 */
const handleTestInForm = async () => {
  const form = formData.value
  if (!form.url) {
    message.warning('请先填写服务地址')
    return
  }
  testLoading.value = true
  try {
    // 先保存或更新，再测试
    if (form.id) {
      await updateWebServiceSource(form)
    } else {
      const id = await createWebServiceSource(form)
      form.id = id
    }
    const result = await testWebServiceConnection(form.id!)
    if (result) {
      message.success('连接测试成功')
    } else {
      message.error('连接测试失败，请检查服务地址和认证信息')
    }
  } catch {
    message.error('连接测试失败，请检查服务地址是否可达')
  } finally {
    testLoading.value = false
    await getList()
  }
}

const handleExecute = (row: WebServiceSourceRespVO) => {
  router.push({ path: '/ubax/source/webservice/ws-execution', query: { wsId: row.id } })
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (formData.value.id) {
    await updateWebServiceSource(formData.value)
    message.success('数据源已更新')
  } else {
    await createWebServiceSource(formData.value)
    message.success('数据源已创建')
  }
  dialogVisible.value = false
  await getList()
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.datasource-config-container {
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
</style>
