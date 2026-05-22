<template>
  <div class="sdk-integration">
    <!-- UBAX-Pilot 简介 -->
    <el-card shadow="never" class="intro-card mb-4">
      <div class="intro-content">
        <div class="intro-header">
          <div class="intro-icon">
            <Icon icon="ep:cpu" :size="32" />
          </div>
          <div class="intro-text">
            <h2>UBAX-Pilot</h2>
            <p class="intro-desc">UBAX-Pilot 是 UBA-X 平台的轻量级数据采集探针，支持 Web、移动端（iOS/Android）、小程序等多端接入。通过极简的 SDK 集成与可视化配置，帮助业务团队快速实现用户行为追踪、事件上报与数据标准化处理。</p>
          </div>
        </div>
        <el-row :gutter="24" class="intro-features">
          <el-col :span="8">
            <div class="feature-item">
              <Icon icon="ep:lightning" :size="20" />
              <span>轻量无侵入，体积仅 8KB</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-item">
              <Icon icon="ep:monitor" :size="20" />
              <span>全端覆盖，Web / App / 小程序</span>
            </div>
          </el-col>
          <el-col :span="8">
            <div class="feature-item">
              <Icon icon="ep:setting" :size="20" />
              <span>可视化配置，零代码接入</span>
            </div>
          </el-col>
        </el-row>
      </div>
    </el-card>

    <!-- Pilot 概览 -->
    <el-row :gutter="16" class="overview-row">
      <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
        <el-card shadow="never" class="overview-card windows-sdk">
          <div class="sdk-header">
            <div class="sdk-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
              <Icon icon="ep:monitor" :size="28" />
            </div>
            <div class="sdk-info">
              <div class="sdk-name">Windows Pilot</div>
              <div class="sdk-version">v2.1.0</div>
            </div>
          </div>
          <div class="sdk-status">
            <el-tag type="success" size="small">
              <Icon icon="ep:success-filled" :size="12" /> 已接入
            </el-tag>
            <span class="update-time">更新于 2026-05-20</span>
          </div>
          <div class="sdk-stats">
            <div class="stat-item">
              <span class="stat-label">日调用量</span>
              <span class="stat-value">128,456</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">接入项目</span>
              <span class="stat-value">3</span>
            </div>
          </div>
          <div class="sdk-actions">
            <el-button type="primary" size="small" @click="handleDownloadSDK('windows')">
              <Icon icon="ep:download" /> 下载
            </el-button>
            <el-button size="small" @click="handleViewGuide('windows')">
              <Icon icon="ep:document" /> 接入指南
            </el-button>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="12" :md="12" :lg="12" :xl="12">
        <el-card shadow="never" class="overview-card linux-sdk">
          <div class="sdk-header">
            <div class="sdk-icon" style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%)">
              <Icon icon="ep:cpu" :size="28" />
            </div>
            <div class="sdk-info">
              <div class="sdk-name">Linux Pilot</div>
              <div class="sdk-version">v1.8.3</div>
            </div>
          </div>
          <div class="sdk-status">
            <el-tag type="success" size="small">
              <Icon icon="ep:success-filled" :size="12" /> 已接入
            </el-tag>
            <span class="update-time">更新于 2026-05-18</span>
          </div>
          <div class="sdk-stats">
            <div class="stat-item">
              <span class="stat-label">日调用量</span>
              <span class="stat-value">89,234</span>
            </div>
            <div class="stat-item">
              <span class="stat-label">接入项目</span>
              <span class="stat-value">2</span>
            </div>
          </div>
          <div class="sdk-actions">
            <el-button type="success" size="small" @click="handleDownloadSDK('linux')">
              <Icon icon="ep:download" /> 下载
            </el-button>
            <el-button size="small" @click="handleViewGuide('linux')">
              <Icon icon="ep:document" /> 接入指南
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Pilot 配置参数 -->
    <el-row :gutter="16" class="mt-4">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <span class="card-title">Pilot 配置参数</span>
          </template>
          <el-table :data="sdkConfigs" style="width: 100%">
            <el-table-column prop="name" label="配置名称" width="180" />
            <el-table-column prop="project" label="所属项目" width="150" />
            <el-table-column prop="appId" label="App ID" width="200" />
            <el-table-column prop="serverUrl" label="上报地址" />
            <el-table-column prop="autoTrack" label="自动采集" width="100">
              <template #default="{ row }">
                <el-switch v-model="row.autoTrack" disabled />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default>
                <el-button type="primary" link>编辑</el-button>
                <el-button type="primary" link>复制</el-button>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 接入指南对话框 -->
    <el-dialog v-model="guideDialogVisible" :title="guideTitle" width="800px">
      <div class="guide-content">
        <el-tabs v-model="activeGuideTab">
          <el-tab-pane label="快速开始" name="quickstart">
            <h4>1. 安装 UBAX-Pilot</h4>
            <pre><code>npm install @ubax/{{ guideSdkType }}-pilot</code></pre>
            <h4>2. 初始化</h4>
            <pre><code>import { init } from '@ubax/{{ guideSdkType }}-pilot'

init({
  appId: 'your-app-id',
  serverUrl: 'https://api.ubax.com/track',
  autoTrack: true
})</code></pre>
            <h4>3. 上报事件</h4>
            <pre><code>import { track } from '@ubax/{{ guideSdkType }}-pilot'

track('button_click', {
  buttonId: 'submit',
  page: 'login'
})</code></pre>
          </el-tab-pane>
          <el-tab-pane label="API 文档" name="api">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="init(config)">初始化 UBAX-Pilot，config 包含 appId、serverUrl 等参数</el-descriptions-item>
              <el-descriptions-item label="track(event, properties)">上报自定义事件</el-descriptions-item>
              <el-descriptions-item label="identify(userId)">标识用户身份</el-descriptions-item>
              <el-descriptions-item label="pageView(pageName)">上报页面浏览</el-descriptions-item>
              <el-descriptions-item label="flush()">立即上报缓存数据</el-descriptions-item>
            </el-descriptions>
          </el-tab-pane>
          <el-tab-pane label="常见问题" name="faq">
            <el-collapse>
              <el-collapse-item title="如何获取 App ID？" name="1">
                在「项目管理」页面创建项目后，系统会自动分配唯一的 App ID
              </el-collapse-item>
              <el-collapse-item title="UBAX-Pilot 是否支持 SSR？" name="2">
                Web Pilot 支持 SSR 模式，初始化时设置 ssr: true 即可
              </el-collapse-item>
              <el-collapse-item title="数据上报频率限制？" name="3">
                默认单用户每秒最多上报 10 个事件，超出部分将自动丢弃
              </el-collapse-item>
            </el-collapse>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'SDKIntegration' })

const guideDialogVisible = ref(false)
const guideTitle = ref('')
const guideSdkType = ref('web')
const activeGuideTab = ref('quickstart')

const sdkConfigs = ref([
  {
    id: 1,
    name: 'UBA-X 主项目配置',
    project: 'UBA-X 主项目',
    appId: 'ubax_prod_001',
    serverUrl: 'https://api.ubax.com/track',
    autoTrack: true
  },
  {
    id: 2,
    name: '移动端生产配置',
    project: '移动端项目',
    appId: 'ubax_mobile_prod',
    serverUrl: 'https://api.ubax.com/track',
    autoTrack: true
  },
  {
    id: 3,
    name: '测试环境配置',
    project: 'UBA-X 主项目',
    appId: 'ubax_test_001',
    serverUrl: 'https://test-api.ubax.com/track',
    autoTrack: false
  }
])

const handleViewGuide = (type: string) => {
  const typeMap: Record<string, string> = {
    windows: 'Windows',
    linux: 'Linux'
  }
  guideSdkType.value = type
  guideTitle.value = `${typeMap[type]} Pilot 接入指南`
  activeGuideTab.value = 'quickstart'
  guideDialogVisible.value = true
}

const handleDownloadSDK = (type: string) => {
  const typeMap: Record<string, string> = {
    windows: 'Windows',
    linux: 'Linux'
  }
  ElMessage.success(`${typeMap[type]} Pilot 下载中...`)
}

</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.sdk-integration {
  padding: 16px;
  background: var(--app-content-bg-color);
  min-height: 100%;
}

.intro-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;

  :deep(.el-card__body) {
    padding: 24px !important;
  }
}

.intro-content {
  .intro-header {
    display: flex;
    align-items: flex-start;
    gap: 20px;
    margin-bottom: 24px;

    .intro-icon {
      width: 64px;
      height: 64px;
      border-radius: var(--radius-lg);
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
    }

    .intro-text {
      h2 {
        margin: 0 0 8px;
        font-size: 22px;
        font-weight: 600;
        color: var(--app-content-text-color-primary);
      }

      .intro-desc {
        margin: 0;
        font-size: 14px;
        line-height: 1.6;
        color: var(--app-content-text-color-secondary);
      }
    }
  }

  .intro-features {
    .feature-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: var(--app-content-text-color-secondary);

      svg {
        color: #667eea;
      }
    }
  }
}

.overview-row {
  margin-bottom: 0;
}

.overview-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;
  transition: all 0.3s ease !important;

  &:hover {
    transform: translateY(-4px);
    box-shadow: var(--shadow-lg) !important;
  }

  :deep(.el-card__body) {
    padding: 20px !important;
  }
}

.sdk-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.sdk-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}

.sdk-info {
  flex: 1;
}

.sdk-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

.sdk-version {
  font-size: 13px;
  color: var(--app-content-text-color-secondary);
  margin-top: 4px;
}

.sdk-status {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.update-time {
  font-size: 12px;
  color: var(--app-content-text-color-secondary);
}

.sdk-stats {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid var(--app-content-card-border);
  border-bottom: 1px solid var(--app-content-card-border);
  margin-bottom: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: var(--app-content-text-color-secondary);
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

.sdk-actions {
  display: flex;
  gap: 8px;
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

.quick-start {
  padding: 16px 0;
}

.step-content {
  margin-top: 24px;
}

.code-block {
  background: #1e1e1e;
  border-radius: var(--radius-md);
  overflow: hidden;
}

.code-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 16px;
  background: #2d2d2d;
  color: #d4d4d4;
  font-size: 13px;
}

.code-block pre {
  margin: 0;
  padding: 16px;
  color: #d4d4d4;
  font-size: 13px;
  line-height: 1.6;
  overflow-x: auto;
}

.step-nav {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
}

.guide-content {
  h4 {
    margin: 16px 0 8px;
    color: var(--app-content-text-color-primary);
  }

  pre {
    background: #1e1e1e;
    padding: 16px;
    border-radius: var(--radius-md);
    color: #d4d4d4;
    font-size: 13px;
    line-height: 1.6;
    overflow-x: auto;
  }
}
</style>
