<template>
  <div class="profile-page">
    <el-row :gutter="24">
      <el-col :span="8">
        <el-card shadow="never" class="glass-card profile-card">
          <div class="profile-header">
            <div class="avatar-wrapper">
              <el-avatar :size="100" class="custom-avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="avatar-badge">
                <el-icon><Edit /></el-icon>
              </div>
            </div>
            <h2 class="username">管理员</h2>
            <p class="user-role">超级管理员</p>
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">128</div>
                <div class="stat-label">分析报告</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-value">56</div>
                <div class="stat-label">数据模型</div>
              </div>
              <div class="stat-divider"></div>
              <div class="stat-item">
                <div class="stat-value">12</div>
                <div class="stat-label">告警规则</div>
              </div>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="glass-card info-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>基本信息</span>
            </div>
          </template>
          <div class="info-list">
            <div class="info-item">
              <span class="info-label">用户名</span>
              <span class="info-value">admin</span>
            </div>
            <div class="info-item">
              <span class="info-label">邮箱</span>
              <span class="info-value">admin@uba-x.com</span>
            </div>
            <div class="info-item">
              <span class="info-label">手机号</span>
              <span class="info-value">138****8888</span>
            </div>
            <div class="info-item">
              <span class="info-label">部门</span>
              <span class="info-value">数据分析部</span>
            </div>
            <div class="info-item">
              <span class="info-label">注册时间</span>
              <span class="info-value">2026-01-15</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后登录</span>
              <span class="info-value">2026-05-17 10:30</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="16">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>编辑资料</span>
              </div>
            </div>
          </template>
          <el-form :model="profileForm" label-width="100px" class="profile-form">
            <el-form-item label="用户名">
              <el-input v-model="profileForm.username" class="custom-input" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="profileForm.nickname" class="custom-input" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" class="custom-input" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" class="custom-input" />
            </el-form-item>
            <el-form-item label="部门">
              <el-select v-model="profileForm.department" class="custom-select">
                <el-option label="数据分析部" value="data" />
                <el-option label="产品部" value="product" />
                <el-option label="运营部" value="operation" />
                <el-option label="技术部" value="tech" />
              </el-select>
            </el-form-item>
            <el-form-item label="个人简介">
              <el-input v-model="profileForm.bio" type="textarea" :rows="4" class="custom-input" />
            </el-form-item>
            <el-form-item>
              <el-button class="btn-gradient">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>修改密码</span>
              </div>
            </div>
          </template>
          <el-form :model="passwordForm" label-width="120px" class="password-form">
            <el-form-item label="当前密码">
              <el-input v-model="passwordForm.current" type="password" show-password class="custom-input" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="passwordForm.new" type="password" show-password class="custom-input" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="passwordForm.confirm" type="password" show-password class="custom-input" />
            </el-form-item>
            <el-form-item>
              <el-button class="btn-gradient">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>操作日志</span>
            </div>
          </template>
          <el-timeline class="custom-timeline">
            <el-timeline-item
              v-for="log in logs"
              :key="log.id"
              :timestamp="log.time"
              placement="top"
              color="#6366f1"
            >
              <div class="log-item">
                <span class="log-action">{{ log.action }}</span>
                <span class="log-ip">IP: {{ log.ip }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { User, Edit } from '@element-plus/icons-vue'

const profileForm = reactive({
  username: 'admin',
  nickname: '管理员',
  email: 'admin@uba-x.com',
  phone: '13888888888',
  department: 'data',
  bio: 'UBA-X 平台超级管理员，负责数据分析和系统运维。',
})

const passwordForm = reactive({
  current: '',
  new: '',
  confirm: '',
})

const logs = ref([
  { id: 1, time: '2026-05-17 10:30:00', action: '登录系统', ip: '192.168.1.100' },
  { id: 2, time: '2026-05-16 18:45:00', action: '修改数据模型配置', ip: '192.168.1.100' },
  { id: 3, time: '2026-05-16 14:20:00', action: '创建异常检测规则', ip: '192.168.1.100' },
  { id: 4, time: '2026-05-16 09:15:00', action: '登录系统', ip: '192.168.1.100' },
  { id: 5, time: '2026-05-15 17:30:00', action: '导出分析报告', ip: '192.168.1.100' },
])
</script>

<style scoped lang="scss">
.profile-page {
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

  .profile-card {
    .profile-header {
      text-align: center;
      padding: 20px 0;

      .avatar-wrapper {
        position: relative;
        display: inline-block;
        margin-bottom: 16px;

        .custom-avatar {
          background: linear-gradient(135deg, #6366f1, #818cf8);
          font-size: 40px;
          color: #fff;
          box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
        }

        .avatar-badge {
          position: absolute;
          bottom: 0;
          right: 0;
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: linear-gradient(135deg, #6366f1, #818cf8);
          border: 3px solid #fff;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 14px;
          color: #fff;
          cursor: pointer;
          transition: all var(--transition-base);
          box-shadow: var(--shadow-sm);

          &:hover {
            transform: scale(1.1);
          }
        }
      }

      .username {
        font-size: 22px;
        font-weight: 700;
        color: var(--text-primary);
        margin-bottom: 4px;
      }

      .user-role {
        font-size: 14px;
        color: var(--text-secondary);
        margin-bottom: 24px;
      }

      .user-stats {
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 24px;
        padding: 16px;
        background: var(--bg-color);
        border-radius: var(--radius-md);

        .stat-item {
          text-align: center;

          .stat-value {
            font-size: 20px;
            font-weight: 700;
            color: var(--primary-color);
          }

          .stat-label {
            font-size: 12px;
            color: var(--text-secondary);
            margin-top: 4px;
          }
        }

        .stat-divider {
          width: 1px;
          height: 32px;
          background: var(--border-color);
        }
      }
    }
  }

  .info-card {
    .info-list {
      .info-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px 0;
        border-bottom: 1px solid var(--border-light);

        &:last-child {
          border-bottom: none;
        }

        .info-label {
          font-size: 13px;
          color: var(--text-secondary);
          font-weight: 500;
        }

        .info-value {
          font-size: 14px;
          color: var(--text-primary);
          font-weight: 500;
        }
      }
    }
  }

  .profile-form,
  .password-form {
    max-width: 500px;
  }

  .custom-input,
  .custom-select {
    width: 100%;
  }

  .custom-timeline {
    :deep(.el-timeline-item__timestamp) {
      font-size: 12px;
      color: var(--text-secondary);
    }

    .log-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 8px 12px;
      background: var(--bg-color);
      border-radius: var(--radius-sm);

      .log-action {
        font-size: 14px;
        color: var(--text-primary);
        font-weight: 500;
      }

      .log-ip {
        font-size: 12px;
        color: var(--text-secondary);
        font-family: 'JetBrains Mono', monospace;
      }
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
}
</style>
