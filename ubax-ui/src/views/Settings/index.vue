<template>
  <div class="settings-page">
    <el-tabs v-model="activeTab" class="custom-tabs">
      <el-tab-pane label="基础设置" name="basic">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>平台基础配置</span>
            </div>
          </template>
          <el-form label-width="120px" class="settings-form">
            <el-form-item label="平台名称">
              <el-input v-model="settings.name" placeholder="UBA-X" class="custom-input" />
            </el-form-item>
            <el-form-item label="平台描述">
              <el-input v-model="settings.desc" type="textarea" :rows="3" placeholder="用户行为分析平台" class="custom-input" />
            </el-form-item>
            <el-form-item label="数据保留">
              <el-select v-model="settings.retention" class="custom-select">
                <el-option label="30天" value="30" />
                <el-option label="90天" value="90" />
                <el-option label="180天" value="180" />
                <el-option label="1年" value="365" />
              </el-select>
            </el-form-item>
            <el-form-item label="时区设置">
              <el-select v-model="settings.timezone" class="custom-select">
                <el-option label="UTC+8 (北京时间)" value="UTC+8" />
                <el-option label="UTC+0 (格林威治)" value="UTC+0" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button class="btn-gradient">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="通知设置" name="notification">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>通知配置</span>
            </div>
          </template>
          <div class="notification-list">
            <div v-for="item in notifications" :key="item.id" class="notification-item">
              <div class="notification-info">
                <div class="notification-name">{{ item.name }}</div>
                <div class="notification-desc">{{ item.description }}</div>
              </div>
              <div class="notification-channels">
                <el-switch v-model="item.email" active-text="邮件" />
                <el-switch v-model="item.sms" active-text="短信" />
                <el-switch v-model="item.webhook" active-text="Webhook" />
              </div>
            </div>
          </div>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="团队管理" name="team">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>团队成员</span>
              </div>
              <el-button class="btn-gradient" size="small">
                <el-icon><Plus /></el-icon>
                邀请成员
              </el-button>
            </div>
          </template>
          <el-table :data="teamMembers" style="width: 100%" class="custom-table">
            <el-table-column prop="name" label="姓名" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column prop="role" label="角色" width="120">
              <template #default="{ row }">
                <span class="role-badge" :class="row.role">{{ row.role }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <span class="status-dot" :class="row.status"></span>
                {{ row.status === 'active' ? '活跃' : '离线' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default>
                <el-button class="link-btn primary" size="small">编辑</el-button>
                <el-button class="link-btn danger" size="small">移除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="API 密钥" name="api">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>API 密钥管理</span>
              </div>
              <el-button class="btn-gradient" size="small">
                <el-icon><Plus /></el-icon>
                创建密钥
              </el-button>
            </div>
          </template>
          <el-table :data="apiKeys" style="width: 100%" class="custom-table">
            <el-table-column prop="name" label="密钥名称" />
            <el-table-column prop="key" label="密钥" width="200">
              <template #default="{ row }">
                <span class="key-mask">{{ row.key }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="created" label="创建时间" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small" class="custom-tag">
                  {{ row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150">
              <template #default>
                <el-button class="link-btn primary" size="small">复制</el-button>
                <el-button class="link-btn danger" size="small">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('basic')

const settings = ref({
  name: 'UBA-X',
  desc: '面向未来的开源用户行为分析平台',
  retention: '90',
  timezone: 'UTC+8',
})

const notifications = ref([
  { id: 1, name: '异常告警通知', description: '当检测到数据异常时发送通知', email: true, sms: true, webhook: false },
  { id: 2, name: '日报推送', description: '每日数据报告定时推送', email: true, sms: false, webhook: false },
  { id: 3, name: '系统维护通知', description: '系统升级或维护时发送通知', email: true, sms: true, webhook: true },
])

const teamMembers = ref([
  { name: '张三', email: 'zhangsan@example.com', role: 'admin', status: 'active' },
  { name: '李四', email: 'lisi@example.com', role: 'analyst', status: 'active' },
  { name: '王五', email: 'wangwu@example.com', role: 'viewer', status: 'offline' },
])

const apiKeys = ref([
  { name: '生产环境密钥', key: 'sk-xxxx-xxxx-xxxx-1234', created: '2026-05-01 10:00:00', status: 'active' },
  { name: '测试环境密钥', key: 'sk-xxxx-xxxx-xxxx-5678', created: '2026-05-10 14:30:00', status: 'active' },
])
</script>

<style scoped lang="scss">
.settings-page {
  .custom-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 24px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      height: 1px;
      background: var(--border-light);
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 500;
      color: var(--text-secondary);
      transition: all var(--transition-base);

      &.is-active {
        color: var(--primary-color);
        font-weight: 600;
      }

      &:hover {
        color: var(--primary-color);
      }
    }

    :deep(.el-tabs__active-bar) {
      background: linear-gradient(90deg, var(--primary-color), var(--primary-light));
      height: 3px;
      border-radius: 2px;
    }
  }

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

  .settings-form {
    max-width: 600px;
  }

  .custom-input,
  .custom-select {
    width: 100%;
  }

  .notification-list {
    display: flex;
    flex-direction: column;
    gap: 16px;

    .notification-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px;
      background: var(--bg-color);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
      transition: all var(--transition-base);

      &:hover {
        box-shadow: var(--shadow-sm);
      }

      .notification-info {
        .notification-name {
          font-size: 15px;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 4px;
        }

        .notification-desc {
          font-size: 13px;
          color: var(--text-secondary);
        }
      }

      .notification-channels {
        display: flex;
        gap: 16px;
      }
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

  .role-badge {
    display: inline-block;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;

    &.admin { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
    &.analyst { background: rgba(99, 102, 241, 0.1); color: #6366f1; }
    &.viewer { background: rgba(100, 116, 139, 0.1); color: #64748b; }
  }

  .status-dot {
    display: inline-block;
    width: 8px;
    height: 8px;
    border-radius: 50%;
    margin-right: 6px;

    &.active {
      background: #10b981;
      box-shadow: 0 0 6px rgba(16, 185, 129, 0.5);
    }

    &.offline { background: #cbd5e1; }
  }

  .key-mask {
    font-family: 'JetBrains Mono', monospace;
    font-size: 13px;
    color: var(--text-secondary);
  }

  .link-btn {
    &.primary { color: var(--primary-color); }
    &.danger { color: var(--danger-color); }
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
}
</style>
