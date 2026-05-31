<template>
  <div class="app-list-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">Agent 列表</span>
        </div>
      </template>

      <!-- 统计视图 -->
      <div class="stats-row">
        <!-- 左侧：终端类型 -->
        <div class="stats-section terminal-section">
          <div class="section-title">终端类型</div>
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon windows">
                <Icon icon="ep:monitor" :size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-label">Windows</div>
                <div class="stat-value">{{ windowsCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon linux">
                <Icon icon="ep:cpu" :size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-label">Linux</div>
                <div class="stat-value">{{ linuxCount }}</div>
              </div>
            </div>
          </div>
        </div>
        <!-- 右侧：平台类型 -->
        <div class="stats-section platform-section">
          <div class="section-title">平台类型</div>
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon auto">
                <Icon icon="ep:refresh" :size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-label">自动</div>
                <div class="stat-value">{{ autoCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon web">
                <Icon icon="ep:monitor" :size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-label">Web</div>
                <div class="stat-value">{{ webCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon h5">
                <Icon icon="ep:cellphone" :size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-label">H5</div>
                <div class="stat-value">{{ h5Count }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon app">
                <Icon icon="ep:iphone" :size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-label">App</div>
                <div class="stat-value">{{ appCount }}</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon mini">
                <Icon icon="ep:chat-dot-round" :size="24" />
              </div>
              <div class="stat-content">
                <div class="stat-label">微信小程序</div>
                <div class="stat-value">{{ miniCount }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 搜索筛选 -->
      <div class="search-bar">
        <el-input
          v-model="searchText"
          placeholder="搜索客户端名称或 App ID"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @input="handleSearch"
        />
        <el-select
          v-model="filterTerminal"
          placeholder="终端筛选"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="全部" value="" />
          <el-option label="Windows" value="Windows" />
          <el-option label="Linux" value="Linux" />
        </el-select>
        <el-select
          v-model="filterPlatform"
          placeholder="平台筛选"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="全部" value="" />
          <el-option label="自动" value="自动" />
          <el-option label="Web" value="Web" />
          <el-option label="H5" value="H5" />
          <el-option label="App" value="App" />
          <el-option label="微信小程序" value="微信小程序" />
        </el-select>
        <el-select
          v-model="filterStatus"
          placeholder="状态筛选"
          clearable
          class="filter-select"
          @change="handleSearch"
        >
          <el-option label="全部" value="" />
          <el-option label="启用" value="active" />
          <el-option label="停用" value="inactive" />
        </el-select>
      </div>

      <!-- 客户端列表 -->
      <el-table :data="paginatedAppList" style="width: 100%" class="app-table mt-4" stripe>
        <el-table-column prop="name" label="客户端名称" min-width="180">
          <template #default="{ row }">
            <div class="app-name-cell">
              <div class="app-icon" :class="getPlatformIconClass(row.platform)">
                <Icon :icon="getPlatformIcon(row.platform)" :size="18" />
              </div>
              <div class="app-info">
                <span class="app-name">{{ row.name }}</span>
                <span class="app-id">{{ row.appId }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="terminal" label="终端类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.terminal === 'Windows' ? '' : 'info'"
              size="small"
              effect="light"
              round
              >{{ row.terminal }}</el-tag
            >
          </template>
        </el-table-column>
        <el-table-column prop="platform" label="平台类型" width="130" align="center">
          <template #default="{ row }">
            <el-tag :type="getPlatformTag(row.platform)" size="small" effect="light" round>{{
              row.platform
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sdkVersion" label="SDK 版本" width="110" align="center">
          <template #default="{ row }">
            <span class="sdk-version">{{ row.sdkVersion }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="active"
              inactive-value="inactive"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="healthStatus" label="健康状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag
              :type="row.healthStatus === '正常' ? 'success' : 'danger'"
              size="small"
              effect="light"
              round
            >
              {{ row.healthStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="dailyEvents" label="今日事件数" width="130" align="right">
          <template #default="{ row }">
            <span class="events-value" :class="row.dailyEvents > 0 ? 'active' : 'zero'">
              {{ formatNumber(row.dailyEvents) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.createdAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-button type="primary" link @click="handleEditApp(row)">
                <Icon icon="ep:edit" /> 编辑
              </el-button>
              <el-button type="primary" link @click="handleViewConfig(row)">
                <Icon icon="ep:setting" /> 配置
              </el-button>
              <el-button type="danger" link @click="handleDeleteApp(row)">
                <Icon icon="ep:delete" /> 删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredAppList.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 编辑客户端对话框 -->
    <el-dialog v-model="appDialogVisible" :title="appDialogTitle" width="600px">
      <el-form :model="appForm" label-width="100px">
        <el-form-item label="客户端名称">
          <el-input v-model="appForm.name" placeholder="请输入客户端名称" />
        </el-form-item>
        <el-form-item label="客户端描述">
          <el-input
            v-model="appForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入客户端描述"
          />
        </el-form-item>
        <el-form-item label="平台类型">
          <el-select v-model="appForm.platform" placeholder="请选择平台类型" style="width: 100%">
            <el-option label="自动" value="自动" />
            <el-option label="Web" value="Web" />
            <el-option label="H5" value="H5" />
            <el-option label="App" value="App" />
            <el-option label="微信小程序" value="微信小程序" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="appDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitApp">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'ClientList' })

const appDialogVisible = ref(false)
const appDialogTitle = ref('')
const appForm = reactive({
  id: null as number | null,
  name: '',
  description: '',
  platform: ''
})

// 搜索筛选
const searchText = ref('')
const filterTerminal = ref('')
const filterPlatform = ref('')
const filterStatus = ref('')

const appList = ref([
  {
    id: 1,
    name: 'UBA-X 官网',
    appId: 'app_ubax_web_001',
    terminal: 'Windows',
    platform: 'Web',
    sdkVersion: 'v2.1.0',
    status: 'active',
    healthStatus: '正常',
    dailyEvents: 45230,
    createdAt: '2025-01-15 09:30:00'
  }
])

const webApps = computed(() => appList.value.filter((a) => a.platform === 'Web').length)
const h5Apps = computed(() => appList.value.filter((a) => a.platform === 'H5').length)
const appApps = computed(() => appList.value.filter((a) => a.platform === 'App').length)
const miniApps = computed(() => appList.value.filter((a) => a.platform === '微信小程序').length)
const autoApps = computed(() => appList.value.filter((a) => a.platform === '自动').length)
const windowsApps = computed(() => appList.value.filter((a) => a.terminal === 'Windows').length)
const linuxApps = computed(() => appList.value.filter((a) => a.terminal === 'Linux').length)

const webCount = computed(() => webApps.value)
const h5Count = computed(() => h5Apps.value)
const appCount = computed(() => appApps.value)
const miniCount = computed(() => miniApps.value)
const autoCount = computed(() => autoApps.value)
const windowsCount = computed(() => windowsApps.value)
const linuxCount = computed(() => linuxApps.value)

// 筛选后的列表
const filteredAppList = computed(() => {
  let result = appList.value
  if (searchText.value) {
    const keyword = searchText.value.toLowerCase()
    result = result.filter(
      (a) => a.name.toLowerCase().includes(keyword) || a.appId.toLowerCase().includes(keyword)
    )
  }
  if (filterTerminal.value) {
    result = result.filter((a) => a.terminal === filterTerminal.value)
  }
  if (filterPlatform.value) {
    result = result.filter((a) => a.platform === filterPlatform.value)
  }
  if (filterStatus.value) {
    result = result.filter((a) => a.status === filterStatus.value)
  }
  return result
})

// 分页
const currentPage = ref(1)
const pageSize = ref(10)

const paginatedAppList = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredAppList.value.slice(start, start + pageSize.value)
})

const handleSizeChange = () => {
  currentPage.value = 1
}

const handleCurrentChange = () => {
  // 页码变化时自动滚动到表格顶部（可选）
}

const getPlatformTag = (platform: string) => {
  const map: Record<string, string> = {
    Web: 'primary',
    H5: 'success',
    App: 'warning',
    微信小程序: 'info',
    自动: 'success'
  }
  return map[platform] || ''
}

const getPlatformIcon = (platform: string) => {
  const map: Record<string, string> = {
    Web: 'ep:monitor',
    H5: 'ep:cellphone',
    App: 'ep:iphone',
    微信小程序: 'ep:chat-dot-round',
    自动: 'ep:refresh'
  }
  return map[platform] || 'ep:platform'
}

const getPlatformIconClass = (platform: string) => {
  const map: Record<string, string> = {
    Web: 'web',
    H5: 'h5',
    App: 'app',
    微信小程序: 'mini',
    自动: 'auto'
  }
  return map[platform] || ''
}

const formatNumber = (num: number) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + 'w'
  }
  if (num >= 1000) {
    return (num / 1000).toFixed(1) + 'k'
  }
  return num.toString()
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleStatusChange = (row: any) => {
  const action = row.status === 'active' ? '启用' : '停用'
  ElMessageBox.confirm(`确定要${action}客户端「${row.name}」吗？`, '确认操作', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      ElMessage.success(`已${action}客户端「${row.name}」`)
    })
    .catch(() => {
      // 取消操作，恢复原状态
      row.status = row.status === 'active' ? 'inactive' : 'active'
    })
}

const handleEditApp = (row: any) => {
  appDialogTitle.value = '编辑客户端'
  appForm.id = row.id
  appForm.name = row.name
  appForm.description = ''
  appForm.platform = row.platform
  appDialogVisible.value = true
}

const handleViewConfig = (row: any) => {
  ElMessage.info('配置功能开发中')
}

const handleDeleteApp = (row: any) => {
  ElMessageBox.confirm(`确定要删除客户端「${row.name}」吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      const index = appList.value.findIndex((a) => a.id === row.id)
      if (index > -1) {
        appList.value.splice(index, 1)
        ElMessage.success('客户端已删除，请前往客户端卸载 SDK')
      }
    })
    .catch(() => {})
}

const handleSubmitApp = () => {
  if (appForm.id) {
    const index = appList.value.findIndex((a) => a.id === appForm.id)
    if (index > -1) {
      appList.value[index].name = appForm.name
      appList.value[index].platform = appForm.platform
    }
  }
  ElMessage.success('客户端信息已更新')
  appDialogVisible.value = false
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.app-list-container {
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
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

.app-stats {
  margin-bottom: 16px;
}

.stats-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
  width: 100%;
}

.stats-section.terminal-section {
  flex: 2;
  min-width: 0;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--app-content-text-color-primary);
    margin-bottom: 12px;
  }

  .stats-cards {
    display: flex;
    gap: 12px;
    flex-wrap: nowrap;
  }
}

.stats-section.platform-section {
  flex: 5;
  min-width: 0;

  .section-title {
    font-size: 14px;
    font-weight: 600;
    color: var(--app-content-text-color-primary);
    margin-bottom: 12px;
  }

  .stats-cards {
    display: flex;
    gap: 12px;
    flex-wrap: nowrap;
  }
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-radius: var(--radius-lg);
  background: var(--app-content-card-bg);
  border: 1px solid var(--app-content-card-border);
  transition: all 0.3s ease;
  flex: 1;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 12px;
    flex-shrink: 0;

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
    flex: 1;
    min-width: 0;

    .stat-label {
      font-size: 12px;
      color: var(--app-content-text-color-secondary);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }

    .stat-value {
      font-size: 18px;
      font-weight: 600;
      color: var(--app-content-text-color-primary);
      margin-top: 2px;
    }
  }
}

// 搜索筛选栏
.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.search-input {
  width: 280px;
}

.filter-select {
  width: 130px;
}

// 表格样式
.app-table {
  :deep(.el-table__header) {
    th {
      background: var(--app-content-card-bg);
      color: var(--app-content-text-color-secondary);
      font-weight: 600;
      font-size: 13px;
    }
  }

  :deep(.el-table__row) {
    &:hover {
      background: rgba(102, 126, 234, 0.04);
    }
  }
}

// 客户端名称单元格
.app-name-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-icon {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.web {
    background: linear-gradient(135deg, rgba(96, 165, 250, 0.15) 0%, rgba(59, 130, 246, 0.15) 100%);
    color: #3b82f6;
  }

  &.h5 {
    background: linear-gradient(135deg, rgba(52, 211, 153, 0.15) 0%, rgba(16, 185, 129, 0.15) 100%);
    color: #10b981;
  }

  &.app {
    background: linear-gradient(135deg, rgba(251, 191, 36, 0.15) 0%, rgba(245, 158, 11, 0.15) 100%);
    color: #f59e0b;
  }

  &.mini {
    background: linear-gradient(135deg, rgba(7, 193, 96, 0.15) 0%, rgba(6, 173, 86, 0.15) 100%);
    color: #07c160;
  }

  &.windows {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, rgba(37, 99, 235, 0.15) 100%);
    color: #2563eb;
  }

  &.linux {
    background: linear-gradient(135deg, rgba(251, 146, 60, 0.15) 0%, rgba(234, 88, 12, 0.15) 100%);
    color: #ea580c;
  }

  &.auto {
    background: linear-gradient(135deg, rgba(16, 185, 129, 0.15) 0%, rgba(5, 150, 105, 0.15) 100%);
    color: #059669;
  }
}

.app-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.app-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-content-text-color-primary);
}

.app-id {
  font-size: 12px;
  color: var(--app-content-text-color-secondary);
}

// SDK 版本
.sdk-version {
  font-family: monospace;
  font-size: 13px;
  color: var(--app-content-text-color-primary);
  background: rgba(102, 126, 234, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
}

// 状态开关
:deep(.el-switch) {
  --el-switch-on-color: #10b981;
  --el-switch-off-color: #9ca3af;
}

// 分页
.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 0 0;
}

// 健康状态标签
.health-tag {
  font-size: 12px;
}

// 事件数
.events-value {
  font-weight: 500;
  font-size: 14px;

  &.active {
    color: var(--app-content-text-color-primary);
  }

  &.zero {
    color: var(--app-content-text-color-secondary);
  }
}

// 操作按钮样式
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 4px 8px;
  border: 1px solid var(--app-content-card-border);
  border-radius: 6px;
  background: var(--app-content-card-bg);
  width: fit-content;
  margin: 0 auto;

  .el-button {
    font-size: 12px;
    padding: 4px 6px;
  }
}

.time-text {
  font-size: 13px;
  color: var(--app-content-text-color-secondary);
  font-family: monospace;
}

// 对话框样式
:deep(.el-dialog) {
  border-radius: 8px;
  overflow: hidden;
}
</style>
