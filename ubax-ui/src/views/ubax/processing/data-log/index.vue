<template>
  <div class="data-log-container">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">数据日志</span>
          <div class="header-actions">
            <el-button size="small" @click="handleRefresh">
              <Icon icon="ep:refresh" /> 刷新
            </el-button>
            <el-button size="small" @click="handleExport">
              <Icon icon="ep:download" /> 导出
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索筛选 -->
      <el-form :model="searchForm" inline class="search-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="searchForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item label="事件类型">
          <el-select v-model="searchForm.eventType" placeholder="请选择" clearable style="width: 140px">
            <el-option label="页面浏览" value="page_view" />
            <el-option label="按钮点击" value="button_click" />
            <el-option label="表单提交" value="form_submit" />
            <el-option label="自定义事件" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备 ID">
          <el-input v-model="searchForm.deviceId" placeholder="请输入设备 ID" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="客户端">
          <el-select v-model="searchForm.appId" placeholder="请选择" clearable style="width: 160px">
            <el-option label="UBA-X 官网" value="app_ubax_web_001" />
            <el-option label="UBA-X Android" value="app_ubax_android_001" />
            <el-option label="UBA-X iOS" value="app_ubax_ios_001" />
            <el-option label="微信小程序" value="app_ubax_mini_001" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <Icon icon="ep:search" /> 查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志统计 -->
      <el-row :gutter="16" class="log-stats">
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon total">
              <Icon icon="ep:document" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">总日志数</div>
              <div class="stat-value">{{ totalLogs }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon today">
              <Icon icon="ep:calendar" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">今日日志</div>
              <div class="stat-value">{{ todayLogs }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon page">
              <Icon icon="ep:view" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">页面浏览</div>
              <div class="stat-value">{{ pageViewLogs }}</div>
            </div>
          </div>
        </el-col>
        <el-col :span="6">
          <div class="stat-card">
            <div class="stat-icon event">
              <Icon icon="ep:operation" :size="24" />
            </div>
            <div class="stat-content">
              <div class="stat-label">事件上报</div>
              <div class="stat-value">{{ eventLogs }}</div>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 日志列表 -->
      <el-table :data="logList" class="log-table mt-4" v-loading="loading">
        <el-table-column prop="id" label="日志 ID" min-width="160" show-overflow-tooltip />
        <el-table-column prop="appName" label="客户端" min-width="130" />
        <el-table-column prop="eventType" label="事件类型" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getEventTypeTag(row.eventType)" size="small">{{ row.eventTypeName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deviceId" label="设备 ID" min-width="150" show-overflow-tooltip />
        <el-table-column prop="userId" label="用户 ID" min-width="120" show-overflow-tooltip />
        <el-table-column prop="platform" label="平台" min-width="100" align="center" />
        <el-table-column prop="timestamp" label="上报时间" min-width="170" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ row.timestamp }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </el-card>

    <!-- 日志详情对话框 -->
    <el-dialog v-model="detailDialogVisible" title="日志详情" width="700px">
      <el-descriptions :column="2" border v-if="currentLog">
        <el-descriptions-item label="日志 ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="客户端">{{ currentLog.appName }}</el-descriptions-item>
        <el-descriptions-item label="事件类型">{{ currentLog.eventTypeName }}</el-descriptions-item>
        <el-descriptions-item label="设备 ID">{{ currentLog.deviceId }}</el-descriptions-item>
        <el-descriptions-item label="用户 ID">{{ currentLog.userId }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ currentLog.platform }}</el-descriptions-item>
        <el-descriptions-item label="上报时间">{{ currentLog.timestamp }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ currentLog.ip }}</el-descriptions-item>
        <el-descriptions-item label="User Agent" :span="2">{{ currentLog.userAgent }}</el-descriptions-item>
        <el-descriptions-item label="事件属性" :span="2">
          <pre class="json-block">{{ JSON.stringify(currentLog.properties, null, 2) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'DataLog' })

const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(156)
const detailDialogVisible = ref(false)
const currentLog = ref<any>(null)

const searchForm = reactive({
  dateRange: [],
  eventType: '',
  deviceId: '',
  appId: ''
})

const logList = ref([
  {
    id: 'log_20260522_001',
    appName: 'UBA-X 官网',
    eventType: 'page_view',
    eventTypeName: '页面浏览',
    deviceId: 'dev_web_8a7f6e5d',
    userId: 'user_12345',
    platform: 'Web',
    timestamp: '2026-05-22 10:35:22',
    ip: '192.168.1.100',
    userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
    properties: { page_url: '/home', page_title: '首页', referrer: 'https://google.com' }
  },
  {
    id: 'log_20260522_002',
    appName: 'UBA-X Android',
    eventType: 'button_click',
    eventTypeName: '按钮点击',
    deviceId: 'dev_android_3c4d5e6f',
    userId: 'user_67890',
    platform: 'Android',
    timestamp: '2026-05-22 10:34:15',
    ip: '10.0.0.55',
    userAgent: 'UBA-X-Android/1.8.3',
    properties: { button_id: 'btn_submit', page_name: 'checkout' }
  },
  {
    id: 'log_20260522_003',
    appName: 'UBA-X iOS',
    eventType: 'form_submit',
    eventTypeName: '表单提交',
    deviceId: 'dev_ios_7g8h9i0j',
    userId: 'user_11111',
    platform: 'iOS',
    timestamp: '2026-05-22 10:33:08',
    ip: '172.16.0.22',
    userAgent: 'UBA-X-iOS/1.8.3',
    properties: { form_id: 'login_form', fields: ['username', 'password'] }
  },
  {
    id: 'log_20260522_004',
    appName: '微信小程序',
    eventType: 'custom',
    eventTypeName: '自定义事件',
    deviceId: 'dev_mini_1k2l3m4n',
    userId: 'user_22222',
    platform: '微信小程序',
    timestamp: '2026-05-22 10:32:45',
    ip: '192.168.2.88',
    userAgent: 'WeChat/8.0.0',
    properties: { event_name: 'share_product', product_id: 'prod_001' }
  },
  {
    id: 'log_20260522_005',
    appName: 'UBA-X 官网',
    eventType: 'page_view',
    eventTypeName: '页面浏览',
    deviceId: 'dev_web_5o6p7q8r',
    userId: 'user_33333',
    platform: 'Web',
    timestamp: '2026-05-22 10:31:30',
    ip: '192.168.3.44',
    userAgent: 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7)',
    properties: { page_url: '/products', page_title: '产品列表' }
  },
  {
    id: 'log_20260522_006',
    appName: 'UBA-X Android',
    eventType: 'button_click',
    eventTypeName: '按钮点击',
    deviceId: 'dev_android_9s0t1u2v',
    userId: 'user_44444',
    platform: 'Android',
    timestamp: '2026-05-22 10:30:12',
    ip: '10.0.1.33',
    userAgent: 'UBA-X-Android/1.8.3',
    properties: { button_id: 'btn_add_cart', product_id: 'prod_002' }
  }
])

const totalLogs = computed(() => logList.value.length)
const todayLogs = computed(() => logList.value.filter(l => l.timestamp.includes('2026-05-22')).length)
const pageViewLogs = computed(() => logList.value.filter(l => l.eventType === 'page_view').length)
const eventLogs = computed(() => logList.value.filter(l => l.eventType !== 'page_view').length)

const getEventTypeTag = (type: string) => {
  const map: Record<string, string> = {
    'page_view': 'primary',
    'button_click': 'success',
    'form_submit': 'warning',
    'custom': 'info'
  }
  return map[type] || ''
}

const handleSearch = () => {
  loading.value = true
  setTimeout(() => {
    loading.value = false
    ElMessage.success('查询完成')
  }, 500)
}

const handleReset = () => {
  searchForm.dateRange = []
  searchForm.eventType = ''
  searchForm.deviceId = ''
  searchForm.appId = ''
}

const handleRefresh = () => {
  handleSearch()
}

const handleExport = () => {
  ElMessage.success('日志导出中，请稍候')
}

const handleViewDetail = (row: any) => {
  currentLog.value = row
  detailDialogVisible.value = true
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.data-log-container {
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
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

.search-form {
  margin-bottom: 16px;

  :deep(.el-form-item) {
    margin-bottom: 12px;
  }
}

.log-stats {
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: var(--radius-lg);
  background: var(--app-content-card-bg);
  border: 1px solid var(--app-content-card-border);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .stat-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;

    &.total {
      background: linear-gradient(135deg, #60a5fa 0%, #3b82f6 100%);
      color: #fff;
    }

    &.today {
      background: linear-gradient(135deg, #34d399 0%, #10b981 100%);
      color: #fff;
    }

    &.page {
      background: linear-gradient(135deg, #a78bfa 0%, #8b5cf6 100%);
      color: #fff;
    }

    &.event {
      background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
      color: #fff;
    }
  }

  .stat-content {
    flex: 1;

    .stat-label {
      font-size: 13px;
      color: var(--app-content-text-color-secondary);
    }

    .stat-value {
      font-size: 20px;
      font-weight: 600;
      color: var(--app-content-text-color-primary);
      margin-top: 4px;
    }
  }
}

.log-table {
  :deep(.el-table__header) {
    th {
      background: var(--border-color-light);
      font-weight: 600;
    }
  }

  :deep(.el-table__row) {
    &:hover {
      background: var(--border-color-light);
    }
  }
}

.time-text {
  font-family: 'SF Mono', 'Consolas', monospace;
  font-size: 12px;
  color: var(--app-content-text-color-secondary);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.json-block {
  background: var(--app-content-bg-color);
  padding: 12px;
  border-radius: var(--radius-md);
  font-size: 12px;
  max-height: 200px;
  overflow: auto;
  margin: 0;
  font-family: 'SF Mono', 'Consolas', monospace;
}
</style>
