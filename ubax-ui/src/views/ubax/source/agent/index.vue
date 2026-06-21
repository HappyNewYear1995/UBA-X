<template>
  <div class="app-list-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">Agent 列表</span>
        </div>
      </template>

      <div class="stats-row">
        <div class="stats-section terminal-section">
          <div class="section-title">终端类型</div>
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon windows">
                <Icon icon="ep:monitor" :size="24"/>
              </div>
              <div class="stat-content">
                <div class="stat-label">Windows</div>
                <div class="stat-value">{{ windowsCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon linux">
                <Icon icon="ep:cpu" :size="24"/>
              </div>
              <div class="stat-content">
                <div class="stat-label">Linux</div>
                <div class="stat-value">{{ linuxCount }}</div>
              </div>
            </div>
          </div>
        </div>
        <div class="stats-section platform-section">
          <div class="section-title">平台类型</div>
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon auto">
                <Icon icon="ep:refresh" :size="24"/>
              </div>
              <div class="stat-content">
                <div class="stat-label">自动</div>
                <div class="stat-value">{{ autoCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon web">
                <Icon icon="ep:monitor" :size="24"/>
              </div>
              <div class="stat-content">
                <div class="stat-label">Web</div>
                <div class="stat-value">{{ webCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon h5">
                <Icon icon="ep:cellphone" :size="24"/>
              </div>
              <div class="stat-content">
                <div class="stat-label">H5</div>
                <div class="stat-value">{{ h5Count }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon app">
                <Icon icon="ep:iphone" :size="24"/>
              </div>
              <div class="stat-content">
                <div class="stat-label">App</div>
                <div class="stat-value">{{ appCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon mini">
                <Icon icon="ep:chat-dot-round" :size="24"/>
              </div>
              <div class="stat-content">
                <div class="stat-label">微信小程序</div>
                <div class="stat-value">{{ miniCount }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="search-bar">
        <el-input
          v-model="queryParams.hostname"
          placeholder="搜索主机名"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @keyup.enter="handleQuery"
        />
        <el-input
          v-model="queryParams.ip"
          placeholder="搜索 IP 地址"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @keyup.enter="handleQuery"
        />
        <el-select
          v-model="queryParams.terminal"
          placeholder="终端筛选"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined"/>
          <el-option label="Windows" :value="10"/>
          <el-option label="Linux" :value="20"/>
        </el-select>
        <el-select
          v-model="queryParams.platform"
          placeholder="平台筛选"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined"/>
          <el-option label="自动" :value="1"/>
          <el-option label="Web" :value="10"/>
          <el-option label="H5" :value="20"/>
          <el-option label="App" :value="30"/>
          <el-option label="微信小程序" :value="40"/>
        </el-select>
        <el-select
          v-model="queryParams.status"
          placeholder="状态筛选"
          clearable
          class="filter-select"
          @change="handleQuery"
        >
          <el-option label="全部" :value="undefined"/>
          <el-option label="启用" :value="0"/>
          <el-option label="停用" :value="1"/>
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

      <el-table v-loading="loading" :data="agentList" stripe>
        <el-table-column prop="uuid" label="UUID" min-width="120" show-overflow-tooltip fixed="left"/>
        <el-table-column prop="hostname" label="主机名" min-width="120" show-overflow-tooltip fixed="left"/>
        <el-table-column prop="ip" label="IP 地址" width="120"/>
        <el-table-column prop="os" label="操作系统" width="100" show-overflow-tooltip/>
        <el-table-column prop="terminal" label="终端类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small" round>
              {{ getTerminalName(row.terminal) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="platform" label="平台类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small" round>
              {{ getPlatformName(row.platform) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="80" align="center"/>
        <el-table-column prop="collectorStatus" label="采集器状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small" round>
              {{ getCollectorStatusName(row.collectorStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="online" label="在线状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.online ? 'success' : 'info'" size="small" round>
              {{ row.online ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          label="最后心跳时间"
          align="center"
          prop="lastHeartbeat"
          width="180"
          :formatter="dateFormatter"
        />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="0"
              :inactive-value="1"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column label="备注" align="center" prop="remark" :show-overflow-tooltip="true"/>
        <el-table-column
          label="创建时间"
          align="center"
          prop="createTime"
          width="180"
          :formatter="dateFormatter"
        />
        <el-table-column label="操作" width="260" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button type="info" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handlePushConfig(row)">推送配置</el-button>
            <el-button type="warning" link @click="handlePushCommand(row)">推送命令</el-button>
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

    <el-dialog v-model="detailDialogVisible" title="Agent 详情" width="700px" class="agent-detail-dialog">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="UUID">{{ detailData.uuid }}</el-descriptions-item>
        <el-descriptions-item label="主机名">{{ detailData.hostname }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ detailData.ip }}</el-descriptions-item>
        <el-descriptions-item label="操作系统">{{ detailData.os }}</el-descriptions-item>
        <el-descriptions-item label="终端类型">
          {{ getTerminalName(detailData.terminal) }}
        </el-descriptions-item>
        <el-descriptions-item label="平台类型">
          {{ getPlatformName(detailData.platform) }}
        </el-descriptions-item>
        <el-descriptions-item label="版本">{{ detailData.version }}</el-descriptions-item>
        <el-descriptions-item label="采集器状态">
          {{ getCollectorStatusName(detailData.collectorStatus) }}
        </el-descriptions-item>
        <el-descriptions-item label="在线状态">
          <el-tag :type="detailData.online ? 'success' : 'info'" size="small">
            {{ detailData.online ? '在线' : '离线' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="最后心跳时间">
          {{ formatDate(detailData.lastHeartbeat) }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDate(detailData.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detailData.remark }}</el-descriptions-item>
      </el-descriptions>

      <div class="config-section">
        <div class="config-title">Vector 配置</div>
        <pre class="config-content">{{ detailData.config || '无配置' }}</pre>
      </div>
    </el-dialog>

    <el-dialog v-model="commandDialogVisible" title="推送命令" width="500px">
      <el-form :model="commandForm" label-width="100px">
        <el-form-item label="Agent UUID">
          <el-input v-model="commandForm.uuid" disabled/>
        </el-form-item>
        <el-form-item label="命令动作">
          <el-select v-model="commandForm.action" placeholder="请选择命令动作" style="width: 100%">
            <el-option label="重启" value="restart"/>
            <el-option label="停止" value="stop"/>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="commandDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCommand">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editDialogVisible" title="编辑 Agent" width="700px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="平台类型">
          <el-select v-model="editForm.platform" placeholder="请选择平台类型" style="width: 100%">
            <el-option v-for="(name, key) in platformMap" :key="key" :label="name"
                       :value="Number(key)"/>
          </el-select>
        </el-form-item>
        <el-form-item label="Vector 配置">
          <el-input
            v-model="editForm.config"
            type="textarea"
            :rows="20"
            placeholder="请输入 Vector 配置（YAML 格式）"
            class="yaml-editor"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {dateFormatter} from "@/utils/formatTime";
import {
  type AgentCommandReqVO,
  type AgentPageReqVO,
  type AgentRespVO,
  type AgentUpdateReqVO,
  getAgent,
  getAgentPage,
  pushCommand,
  pushConfig,
  updateAgent,
  updateAgentStatus
} from '@/api/ubax/gather/agent'

defineOptions({name: 'AgentList'})

const loading = ref(false)
const agentList = ref<AgentRespVO[]>([])
const total = ref(0)
const detailDialogVisible = ref(false)
const detailData = ref<AgentRespVO>({} as AgentRespVO)
const commandDialogVisible = ref(false)
const commandForm = ref<AgentCommandReqVO>({uuid: '', action: ''})
const editDialogVisible = ref(false)
const editForm = ref<AgentUpdateReqVO>({id: 0, platform: undefined, config: '', remark: ''})

const queryParams = ref<AgentPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  hostname: undefined,
  ip: undefined,
  terminal: undefined,
  platform: undefined,
  status: undefined,
  createTime: undefined
})

const terminalMap: Record<number, string> = {
  10: 'Linux',
  20: 'Windows'
}

const platformMap: Record<number, string> = {
  0: '未知',
  1: '自动',
  10: 'Web',
  20: 'H5',
  30: 'App',
  40: '微信小程序',
}

const collectorStatusMap: Record<string, string> = {
  unknown: '未知',
  running: '运行中',
  stopped: '已停止'
}

const windowsCount = computed(() => agentList.value.filter((a) => a.terminal === 20).length)
const linuxCount = computed(() => agentList.value.filter((a) => a.terminal === 10).length)
const autoCount = computed(() => agentList.value.filter((a) => a.platform === 1).length)
const webCount = computed(() => agentList.value.filter((a) => a.platform === 10).length)
const h5Count = computed(() => agentList.value.filter((a) => a.platform === 20).length)
const appCount = computed(() => agentList.value.filter((a) => a.platform === 30).length)
const miniCount = computed(() => agentList.value.filter((a) => a.platform === 40).length)

const getList = async () => {
  loading.value = true
  try {
    const data = await getAgentPage(queryParams.value)
    agentList.value = data.list
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
    hostname: undefined,
    ip: undefined,
    terminal: undefined,
    platform: undefined,
    status: undefined,
    createTime: undefined
  }
  handleQuery()
}

const handleDetail = async (row: AgentRespVO) => {
  detailData.value = await getAgent(row.id)
  detailDialogVisible.value = true
}

const handlePushCommand = (row: AgentRespVO) => {
  commandForm.value = {uuid: row.uuid, action: ''}
  commandDialogVisible.value = true
}

const handlePushConfig = async (row: AgentRespVO) => {
  try {
    await ElMessageBox.confirm(`确定要向 Agent「${row.uuid}」推送 Vector 配置吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await pushConfig(row.uuid)
    ElMessage.success('配置已推送')
  } catch {
  }
}

const handleEdit = (row: AgentRespVO) => {
  editForm.value = {
    id: row.id,
    platform: row.platform,
    config: row.config || '',
    remark: row.remark || ''
  }
  editDialogVisible.value = true
}

const submitEdit = async () => {
  await updateAgent(editForm.value)
  ElMessage.success('更新成功')
  editDialogVisible.value = false
  getList()
}

const handleStatusChange = async (row: AgentRespVO) => {
  const actionText = row.status === 0 ? '开启' : '关闭'
  try {
    await ElMessageBox.confirm(`确定要${actionText}该 Agent 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateAgentStatus(row.id, row.status)
    ElMessage.success(`${actionText}成功`)
  } catch {
    row.status = row.status === 0 ? 1 : 0
  }
}

const submitCommand = async () => {
  if (!commandForm.value.action) {
    ElMessage.warning('请选择命令动作')
    return
  }
  await pushCommand(commandForm.value)
  ElMessage.success('命令已推送')
  commandDialogVisible.value = false
}

const getTerminalName = (terminal: number) => {
  return terminalMap[terminal] || '未知'
}

const getPlatformName = (platform: number) => {
  return platformMap[platform] || '未知'
}

const getCollectorStatusName = (status: string) => {
  return collectorStatusMap[status] || status
}

const formatDate = (date: Date | string | undefined) => {
  if (!date) return '-'
  return dateFormatter({ [date]: date }, '', date)
}

onMounted(() => {
  getList()
})
</script>

<style lang="scss" scoped>
.app-list-container {
  padding: 16px;
}

.dashboard-card {
  border-radius: 8px;

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--el-border-color-light);
    padding: 14px 20px;
  }
}

:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}

:deep(.agent-detail-dialog.el-dialog) {
  border-radius: 1px;
}

:deep(.agent-detail-dialog .el-dialog__header) {
  border-radius: 1px 1px 0 0;
}

.config-section {
  margin-top: 20px;
  padding: 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;

  .config-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
    color: var(--el-text-color-primary);
  }
}

:deep(.el-table__fixed) {
  background-color: var(--el-bg-color) !important;
  z-index: 100 !important;
  box-shadow: 2px 0 4px rgba(0, 0, 0, 0.05);

  &::before {
    background-color: transparent !important;
  }
}

:deep(.el-table__fixed-right) {
  background-color: var(--el-bg-color) !important;
  z-index: 100 !important;
  box-shadow: -2px 0 4px rgba(0, 0, 0, 0.05);

  &::before {
    background-color: transparent !important;
  }
}

:deep(.el-table__fixed-header-wrapper) {
  position: absolute !important;
  left: 0 !important;
  top: 0 !important;
  z-index: 1000 !important;
  background-color: #ffffff !important;
  overflow: hidden !important;

  .el-table__header {
    background-color: #ffffff !important;
  }

  th.el-table__cell {
    background-color: #ffffff !important;
  }
}

:deep(.el-table__fixed-right-header-wrapper) {
  position: absolute !important;
  right: 0 !important;
  top: 0 !important;
  z-index: 1000 !important;
  background-color: #ffffff !important;
  overflow: hidden !important;

  .el-table__header {
    background-color: #ffffff !important;
  }

  th.el-table__cell {
    background-color: #ffffff !important;
  }
}

:deep(.el-table__header-wrapper) {
  z-index: 1 !important;

  .el-table__header {
    background-color: var(--el-bg-color) !important;
  }

  th.el-table__cell {
    background-color: var(--el-bg-color) !important;
  }
}

:deep(.el-table__fixed .el-table__cell) {
  background-color: var(--el-bg-color) !important;
}

:deep(.el-table__fixed-right .el-table__cell) {
  background-color: var(--el-bg-color) !important;
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

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.stats-section.terminal-section {
  flex: 2;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .stats-cards {
    display: flex;
    gap: 12px;
  }
}

.stats-section.platform-section {
  flex: 5;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    margin-bottom: 12px;
  }

  .stats-cards {
    display: flex;
    gap: 12px;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: 8px;
  border: 1px solid var(--el-border-color-light);
  flex: 1;

  .stat-icon {
    width: 40px;
    height: 40px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;

    &.web {
      background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
      color: #fff;
    }

    &.h5 {
      background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
      color: #fff;
    }

    &.app {
      background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
      color: #fff;
    }

    &.mini {
      background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
      color: #fff;
    }

    &.auto {
      background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
      color: #fff;
    }

    &.windows {
      background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
      color: #fff;
    }

    &.linux {
      background: linear-gradient(135deg, #f97316 0%, #ea580c 100%);
      color: #fff;
    }
  }

  .stat-content {
    .stat-label {
      font-size: 12px;
      color: var(--el-text-color-secondary);
    }

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      margin-top: 2px;
    }
  }
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

.config-content {
  white-space: pre-wrap;
  word-break: break-all;
  margin: 0;
  font-family: 'Courier New', Courier, monospace;
  font-size: 12px;
  line-height: 1.5;
  background-color: var(--el-fill-color-light);
  padding: 12px;
  border-radius: 4px;
  max-height: 400px;
  overflow-y: auto;
}

:deep(.yaml-editor .el-textarea__inner) {
  font-family: 'Courier New', Courier, monospace;
  font-size: 13px;
  line-height: 1.5;
  background-color: var(--el-fill-color-light);
}
</style>
