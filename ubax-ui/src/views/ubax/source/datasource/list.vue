<template>
  <div class="datasource-config-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">数据源管理</span>
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
          v-model="queryParams.dbType"
          placeholder="数据库类型"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined"/>
          <el-option label="MySQL" value="mysql"/>
          <el-option label="PostgreSQL" value="postgresql"/>
          <el-option label="Oracle" value="oracle"/>
          <el-option label="SQL Server" value="sqlserver"/>
        </el-select>
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
        <el-table-column prop="name" label="数据源名称" min-width="140" show-overflow-tooltip/>
        <el-table-column prop="dbType" label="数据库类型" width="120" align="center">
          <template #default="{ row }">
            {{ row.dbTypeName || row.dbType }}
          </template>
        </el-table-column>
        <el-table-column label="连接地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.host }}:{{ row.port }}/{{ row.database }}
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="100" show-overflow-tooltip/>
        <el-table-column prop="protocol" label="协议" width="80" align="center">
          <template #default="{ row }">
            {{ row.protocol ? row.protocol.toUpperCase() : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90" align="center">
          <template #default="{ row }">
            {{ row.status === 0 ? '正常' : '异常' }}
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
            <span class="table-action warning" @click="handleSqlExecute(row)">SQL测试</span>
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
        <el-form-item label="数据库类型" prop="dbType">
          <el-select v-model="formData.dbType" placeholder="请选择数据库类型" style="width: 100%"
                     @change="handleDbTypeChange">
            <el-option label="MySQL" value="mysql"/>
            <el-option label="PostgreSQL" value="postgresql"/>
            <el-option label="Oracle" value="oracle"/>
            <el-option label="SQL Server" value="sqlserver"/>
          </el-select>
        </el-form-item>
        <el-divider content-position="left">连接配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="14">
            <el-form-item label="主机地址" prop="host">
              <el-input v-model="formData.host" placeholder="如 127.0.0.1"/>
            </el-form-item>
          </el-col>
          <el-col :span="10">
            <el-form-item label="端口" prop="port">
              <el-input-number v-model="formData.port" :min="1" :max="65535"
                               controls-position="right" style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="数据库名" prop="database">
          <el-input v-model="formData.database" placeholder="请输入数据库名"/>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="formData.username" placeholder="请输入用户名"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="formData.password"
                type="password"
                show-password
                :placeholder="formData.id ? '如需修改请清空后输入新密码' : '请输入密码'"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">高级配置</el-divider>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="连接协议">
              <el-select v-model="formData.protocol" placeholder="请选择协议" style="width: 100%">
                <el-option label="TCP (默认)" value="tcp"/>
                <el-option label="SSL" value="ssl"/>
                <el-option label="SSL 验证" value="ssl-verify"/>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="密码加密方式">
              <el-select v-model="formData.passwordEncryptType" placeholder="请选择加密方式"
                         style="width: 100%">
                <el-option label="明文" value="plain"/>
                <el-option label="AES" value="aes"/>
                <el-option label="RSA" value="rsa"/>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item v-if="formData.protocol !== 'tcp'" label="SSL 证书路径">
          <el-input v-model="formData.sslCertPath" placeholder="客户端证书路径"/>
        </el-form-item>
        <el-form-item v-if="formData.protocol !== 'tcp'" label="SSL 私钥路径">
          <el-input v-model="formData.sslKeyPath" placeholder="私钥路径"/>
        </el-form-item>
        <el-form-item v-if="formData.protocol !== 'tcp'" label="SSL CA 证书路径">
          <el-input v-model="formData.sslCaPath" placeholder="CA 证书路径"/>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大连接数">
              <el-input-number v-model="formData.maxPoolSize" :min="1" :max="100"
                               style="width: 100%"/>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="连接超时 (ms)">
              <el-input-number v-model="formData.connectionTimeout" :min="1000" :step="1000"
                               style="width: 100%"/>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="额外参数">
          <el-input
            v-model="formData.connectionParams"
            type="textarea"
            :rows="2"
            placeholder='JSON 格式，如 {"useSSL":"true","serverTimezone":"Asia/Shanghai"}'
          />
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
import {dateFormatter} from '@/utils/formatTime'
import {
  createDatabaseSource,
  type DatabaseSourcePageReqVO,
  type DatabaseSourceRespVO,
  type DatabaseSourceSaveReqVO,
  type DatabaseTestReqVO,
  deleteDatabaseSource,
  getDatabaseSourcePage,
  testDatabaseSourceConnection,
  updateDatabaseSource
} from '@/api/ubax/gather/datasource/database'

defineOptions({name: 'DatabaseList'})

const message = useMessage()
const router = useRouter()
const loading = ref(false)
const testLoading = ref(false)
const configList = ref<DatabaseSourceRespVO[]>([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()

const formData = ref<DatabaseSourceSaveReqVO>({
  name: '',
  dbType: 'mysql',
  host: '',
  port: 3306,
  database: '',
  username: '',
  password: '',
  protocol: 'tcp',
  passwordEncryptType: 'plain',
  maxPoolSize: 20,
  connectionTimeout: 30000
})

const formRules = {
  name: [{required: true, message: '数据源名称不能为空', trigger: 'blur'}],
  dbType: [{required: true, message: '数据库类型不能为空', trigger: 'change'}],
  host: [{required: true, message: '主机地址不能为空', trigger: 'blur'}],
  port: [{required: true, message: '端口号不能为空', trigger: 'blur'}],
  database: [{required: true, message: '数据库名不能为空', trigger: 'blur'}],
  username: [{required: true, message: '用户名不能为空', trigger: 'blur'}],
  password: [
    {
      validator: (_rule: any, value: string, callback: any) => {
        // 编辑模式下密码可为空（表示不修改）
        if (!formData.value.id && !value) {
          callback(new Error('密码不能为空'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/** 数据库类型默认端口映射 */
const defaultPortMap: Record<string, number> = {
  mysql: 3306,
  postgresql: 5432,
  oracle: 1521,
  sqlserver: 1433
}

/** 数据库类型切换时自动填充默认端口 */
const handleDbTypeChange = (dbType: string) => {
  const defaultPort = defaultPortMap[dbType]
  if (defaultPort) {
    formData.value.port = defaultPort
  }
}

const queryParams = ref<DatabaseSourcePageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined,
  dbType: undefined,
  status: undefined
})

const getList = async () => {
  loading.value = true
  try {
    const data = await getDatabaseSourcePage(queryParams.value)
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
    dbType: undefined,
    status: undefined
  }
  handleQuery()
}

const handleCreate = () => {
  dialogTitle.value = '新增数据源'
  formData.value = {
    name: '',
    dbType: 'mysql',
    host: '',
    port: 3306,
    database: '',
    username: '',
    password: '',
    protocol: 'tcp',
    passwordEncryptType: 'plain',
    maxPoolSize: 20,
    connectionTimeout: 30000
  }
  dialogVisible.value = true
}

const handleEdit = (row: DatabaseSourceRespVO) => {
  dialogTitle.value = '编辑数据源'
  formData.value = {
    id: row.id,
    name: row.name,
    dbType: row.dbType,
    host: row.host,
    port: row.port,
    database: row.database,
    username: row.username,
    password: row.password || '',
    url: row.url,
    protocol: row.protocol || 'tcp',
    sslCertPath: row.sslCertPath,
    sslKeyPath: row.sslKeyPath,
    sslCaPath: row.sslCaPath,
    passwordEncryptType: row.passwordEncryptType || 'plain',
    connectionParams: row.connectionParams,
    maxPoolSize: row.maxPoolSize,
    connectionTimeout: row.connectionTimeout,
    remark: row.remark
  }
  dialogVisible.value = true
}

const handleDelete = async (row: DatabaseSourceRespVO) => {
  await ElMessageBox.confirm(`确定要删除数据源「${row.name}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
  await deleteDatabaseSource(row.id)
  message.success('数据源已删除')
  getList()
}

const handleTest = async (row: DatabaseSourceRespVO) => {
  try {
    const result = await testDatabaseSourceConnection({id: row.id})
    if (result) {
      message.success('连接测试成功')
    } else {
      message.error('连接测试失败')
    }
  } catch {
    message.error('连接测试失败')
  }
  await getList();
}

/** 表单内测试连接 */
const handleTestInForm = async () => {
  const form = formData.value
  if (!form.host || !form.port || !form.dbType || !form.database || !form.username) {
    message.warning('请先填写完整的连接信息')
    return
  }
  testLoading.value = true
  try {
    const testData: DatabaseTestReqVO = {
      dbType: form.dbType,
      host: form.host,
      port: form.port,
      database: form.database,
      username: form.username,
      password: form.password === '******' ? undefined : form.password,
      protocol: form.protocol,
      sslCertPath: form.sslCertPath,
      sslKeyPath: form.sslKeyPath,
      sslCaPath: form.sslCaPath,
      connectionParams: form.connectionParams
    }
    // 编辑模式下，如果密码为脱敏值，传 ID 让后端用原密码测试
    if (form.id && form.password === '******') {
      testData.id = form.id
    }
    const result = await testDatabaseSourceConnection(testData)
    if (result) {
      message.success('连接测试成功')
    } else {
      message.error('连接测试失败，请检查主机地址、端口和认证信息')
    }
  } catch {
    message.error('连接测试失败，请检查主机地址和端口是否可达')
  } finally {
    testLoading.value = false
  }
}

const handleSqlExecute = (row: DatabaseSourceRespVO) => {
  router.push({path: '/ubax/source/datasource/execution', query: {databaseId: row.id}})
}

const submitForm = async () => {
  await formRef.value?.validate()
  if (formData.value.id) {
    await updateDatabaseSource(formData.value)
    message.success('数据源已更新')
  } else {
    await createDatabaseSource(formData.value)
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
