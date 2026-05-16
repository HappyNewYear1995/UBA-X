<template>
  <div class="collection-page">
    <el-card shadow="never" class="glass-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-dot"></div>
            <span>数据采集配置</span>
          </div>
          <el-button class="btn-gradient" @click="showAddDialog = true">
            <el-icon><Plus /></el-icon>
            新增采集源
          </el-button>
        </div>
      </template>

      <el-table :data="collectionSources" style="width: 100%" class="custom-table">
        <el-table-column prop="name" label="采集源名称" width="200" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <span class="type-badge" :class="row.type.toLowerCase()">{{ row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="endpoint" label="接入端点" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="status-dot" :class="row.status === 'active' ? 'active' : 'inactive'"></span>
            <span>{{ row.status === 'active' ? '运行中' : '已停用' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="events" label="今日事件" width="120" />
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button class="link-btn primary" size="small">配置</el-button>
            <el-button class="link-btn primary" size="small">测试</el-button>
            <el-button class="link-btn danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="glass-card sdk-card">
      <template #header>
        <div class="header-title">
          <div class="title-dot"></div>
          <span>SDK 集成指南</span>
        </div>
      </template>
      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="Web SDK" name="web">
          <pre class="code-block"><code>{{ webSdkCode }}</code></pre>
        </el-tab-pane>
        <el-tab-pane label="小程序 SDK" name="miniapp">
          <pre class="code-block"><code>{{ miniappSdkCode }}</code></pre>
        </el-tab-pane>
        <el-tab-pane label="API 接入" name="api">
          <pre class="code-block"><code>{{ apiCode }}</code></pre>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog v-model="showAddDialog" title="新增采集源" width="500px" class="custom-dialog">
      <el-form :model="newSource" label-width="100px">
        <el-form-item label="采集源名称">
          <el-input v-model="newSource.name" placeholder="请输入名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="newSource.type" placeholder="请选择类型" class="custom-select">
            <el-option label="Web" value="Web" />
            <el-option label="App" value="App" />
            <el-option label="小程序" value="MiniApp" />
            <el-option label="Server" value="Server" />
          </el-select>
        </el-form-item>
        <el-form-item label="接入端点">
          <el-input v-model="newSource.endpoint" placeholder="/api/v1/track" class="custom-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false" class="btn-outline">取消</el-button>
        <el-button class="btn-gradient" @click="showAddDialog = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showAddDialog = ref(false)
const activeTab = ref('web')

const newSource = ref({
  name: '',
  type: '',
  endpoint: '',
})

const collectionSources = ref([
  { name: '官网 Web 端', type: 'Web', endpoint: '/api/v1/track/web', status: 'active', events: '458,392' },
  { name: 'iOS App', type: 'App', endpoint: '/api/v1/track/ios', status: 'active', events: '234,156' },
  { name: 'Android App', type: 'App', endpoint: '/api/v1/track/android', status: 'active', events: '198,743' },
  { name: '微信小程序', type: 'MiniApp', endpoint: '/api/v1/track/wx', status: 'active', events: '156,892' },
  { name: '服务端埋点', type: 'Server', endpoint: '/api/v1/track/server', status: 'inactive', events: '89,234' },
])

const webSdkCode = `// 安装 SDK
npm install @uba-x/web-sdk

// 初始化
// import { UbaX } from '@uba-x/web-sdk'

const uba = new UbaX({
  appId: 'your-app-id',
  serverUrl: 'https://your-domain.com/api/v1/track',
  autoTrack: true,
})

// 手动埋点
uba.track('button_click', {
  button_id: 'submit_btn',
  page: '/checkout',
})`

const miniappSdkCode = `// 安装 SDK
npm install @uba-x/miniapp-sdk

// 初始化 (app.js)
// import { UbaX } from '@uba-x/miniapp-sdk'

App({
  onLaunch() {
    this.uba = new UbaX({
      appId: 'your-app-id',
      serverUrl: 'https://your-domain.com/api/v1/track',
    })
  }
})

// 页面中使用
getApp().uba.track('page_view', { page: 'home' })`

const apiCode = `// REST API 接入
POST /api/v1/track
Content-Type: application/json

{
  "appId": "your-app-id",
  "distinctId": "user_12345",
  "event": "purchase",
  "properties": {
    "product_id": "SKU_001",
    "amount": 99.9,
    "currency": "CNY"
  },
  "timestamp": 1715846400000
}

// 批量上报
POST /api/v1/track/batch
Content-Type: application/json

{
  "appId": "your-app-id",
  "events": [...]
}`
</script>

<style scoped lang="scss">
.collection-page {
  .glass-card {
    background: var(--card-bg);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-slow);
    margin-bottom: 24px;

    &:hover {
      box-shadow: var(--shadow-lg);
    }

    :deep(.el-card__header) {
      padding: 20px 24px;
      border-bottom: 1px solid var(--border-light);
      background: transparent;
    }

    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);

    .title-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
      box-shadow: 0 0 8px rgba(99, 102, 241, 0.4);
    }
  }

  .custom-table {
    :deep(.el-table__header-wrapper) {
      th {
        background: var(--bg-color);
        color: var(--text-secondary);
        font-weight: 600;
        font-size: 13px;
        border-bottom: 1px solid var(--border-light);
      }
    }

    :deep(.el-table__row) {
      transition: all var(--transition-fast);

      &:hover {
        background: rgba(99, 102, 241, 0.03) !important;
      }

      td {
        border-bottom: 1px solid var(--border-light);
        color: var(--text-regular);
      }
    }
  }

  .type-badge {
    display: inline-block;
    padding: 4px 12px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;

    &.web { background: rgba(99, 102, 241, 0.1); color: #6366f1; }
    &.app { background: rgba(16, 185, 129, 0.1); color: #10b981; }
    &.miniapp { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
    &.server { background: rgba(100, 116, 139, 0.1); color: #64748b; }
  }

  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 8px;

    &.active {
      background: var(--success-color);
      box-shadow: 0 0 8px rgba(16, 185, 129, 0.4);
    }

    &.inactive {
      background: var(--text-secondary);
    }
  }

  .link-btn {
    &.primary { color: var(--primary-color); }
    &.danger { color: var(--danger-color); }
  }

  .sdk-card {
    .code-block {
      background: linear-gradient(135deg, #1e1e2e 0%, #2d2d3f 100%);
      color: #cdd6f4;
      padding: 20px;
      border-radius: var(--radius-md);
      overflow-x: auto;
      font-size: 13px;
      line-height: 1.7;
      font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
      border: 1px solid rgba(255, 255, 255, 0.05);
      box-shadow: inset 0 2px 8px rgba(0, 0, 0, 0.2);
    }
  }

  .btn-gradient {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
    border: none;
    color: #fff;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    transition: all var(--transition-base);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .btn-outline {
    background: transparent;
    border: 1px solid var(--border-color);
    color: var(--text-regular);
    transition: all var(--transition-fast);

    &:hover {
      border-color: var(--primary-color);
      color: var(--primary-color);
    }
  }
}

:deep(.custom-tabs) {
  .el-tabs__header {
    margin-bottom: 20px;
  }

  .el-tabs__item {
    font-weight: 500;
    color: var(--text-secondary);
    transition: color var(--transition-fast);

    &.is-active {
      color: var(--primary-color);
    }

    &:hover {
      color: var(--primary-color);
    }
  }

  .el-tabs__active-bar {
    background: linear-gradient(90deg, var(--primary-color), var(--primary-light));
    height: 3px;
    border-radius: 2px;
  }
}

:deep(.custom-dialog) {
  .el-dialog__header {
    border-bottom: 1px solid var(--border-light);
    padding-bottom: 16px;
  }

  .el-dialog__title {
    font-weight: 600;
    color: var(--text-primary);
  }
}
</style>
