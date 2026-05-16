<template>
  <div class="anomaly-page">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <div class="stat-card-wrapper" :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="stat-card">
            <div class="stat-bg" :class="stat.color"></div>
            <div class="stat-content">
              <div class="stat-icon" :class="stat.color">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="glass-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-dot"></div>
            <span>异常检测规则</span>
          </div>
          <el-button class="btn-gradient" size="small" @click="showRuleDialog = true">
            <el-icon><Plus /></el-icon>
            新建规则
          </el-button>
        </div>
      </template>
      <el-table :data="rules" style="width: 100%" class="custom-table">
        <el-table-column prop="name" label="规则名称" />
        <el-table-column prop="metric" label="监控指标" />
        <el-table-column prop="condition" label="触发条件" />
        <el-table-column prop="level" label="告警级别" width="100">
          <template #default="{ row }">
            <span class="level-badge" :class="row.level">{{ row.level }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" class="custom-switch" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default>
            <el-button class="link-btn primary" size="small">编辑</el-button>
            <el-button class="link-btn danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-card shadow="never" class="glass-card alert-card">
      <template #header>
        <div class="header-title">
          <div class="title-dot"></div>
          <span>告警记录</span>
        </div>
      </template>
      <el-timeline class="custom-timeline">
        <el-timeline-item
          v-for="alert in alerts"
          :key="alert.id"
          :timestamp="alert.time"
          placement="top"
          :color="alert.level === 'critical' ? '#ef4444' : alert.level === 'warning' ? '#f59e0b' : '#64748b'"
        >
          <div class="alert-item">
            <div class="alert-header">
              <span class="alert-title">{{ alert.title }}</span>
              <span class="alert-level" :class="alert.level">{{ alert.level === 'critical' ? '严重' : alert.level === 'warning' ? '警告' : '提示' }}</span>
            </div>
            <p class="alert-desc">{{ alert.description }}</p>
            <div class="alert-actions">
              <el-button class="btn-gradient" size="small">处理</el-button>
              <el-button class="btn-outline" size="small">忽略</el-button>
            </div>
          </div>
        </el-timeline-item>
      </el-timeline>
    </el-card>

    <el-dialog v-model="showRuleDialog" title="新建检测规则" width="600px" class="custom-dialog">
      <el-form label-width="120px">
        <el-form-item label="规则名称">
          <el-input placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="监控指标">
          <el-select placeholder="请选择指标">
            <el-option label="DAU 异常波动" value="dau" />
            <el-option label="事件量骤降" value="event_drop" />
            <el-option label="转化率异常" value="conversion" />
            <el-option label="响应时间超标" value="latency" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发条件">
          <el-input placeholder="例如：较昨日下降超过 30%" />
        </el-form-item>
        <el-form-item label="告警级别">
          <el-radio-group class="custom-radio-group">
            <el-radio value="critical">严重</el-radio>
            <el-radio value="warning">警告</el-radio>
            <el-radio value="info">提示</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="通知方式">
          <el-checkbox-group>
            <el-checkbox value="email">邮件</el-checkbox>
            <el-checkbox value="sms">短信</el-checkbox>
            <el-checkbox value="webhook">Webhook</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRuleDialog = false" class="btn-outline">取消</el-button>
        <el-button class="btn-gradient" @click="showRuleDialog = false">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showRuleDialog = ref(false)

const stats = ref([
  { value: '3', label: '活跃告警', icon: 'Warning', color: 'danger' },
  { value: '12', label: '今日检测', icon: 'Bell', color: 'warning' },
  { value: '98.5%', label: '数据健康度', icon: 'CircleCheck', color: 'success' },
  { value: '2.3s', label: '平均响应时间', icon: 'Timer', color: 'primary' },
])

const rules = ref([
  { name: 'DAU 异常波动检测', metric: '日活跃用户', condition: '较7日均值偏离 > 2σ', level: 'critical', enabled: true },
  { name: '事件量骤降告警', metric: '事件总数', condition: '较昨日下降 > 30%', level: 'warning', enabled: true },
  { name: '转化率异常监控', metric: '支付转化率', condition: '低于 5%', level: 'warning', enabled: true },
  { name: 'API 响应时间监控', metric: '接口延迟', condition: 'P99 > 2000ms', level: 'info', enabled: false },
])

const alerts = ref([
  { id: 1, time: '2026-05-16 14:25:00', title: 'DAU 异常下降', description: '当前 DAU 较7日均值下降 35%，已触发严重告警。', level: 'critical' },
  { id: 2, time: '2026-05-16 12:10:00', title: '事件量波动', description: '过去1小时事件量较预期下降 28%，请关注。', level: 'warning' },
  { id: 3, time: '2026-05-16 09:45:00', title: '转化率偏低', description: '今日支付转化率 4.2%，低于阈值 5%。', level: 'warning' },
])
</script>

<style scoped lang="scss">
.anomaly-page {
  .stats-row {
    margin-bottom: 24px;
  }

  .stat-card-wrapper {
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
  }

  .stat-card {
    position: relative;
    padding: 24px;
    background: var(--card-bg);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-slow);
    overflow: hidden;
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-xl);
      border-color: transparent;

      .stat-bg {
        opacity: 0.15;
        transform: scale(1.1);
      }

      .stat-icon {
        transform: scale(1.1) rotate(5deg);
      }
    }

    .stat-bg {
      position: absolute;
      top: -20px;
      right: -20px;
      width: 120px;
      height: 120px;
      border-radius: 50%;
      opacity: 0.08;
      transition: all var(--transition-slow);
      filter: blur(30px);

      &.primary { background: linear-gradient(135deg, #6366f1, #818cf8); }
      &.success { background: linear-gradient(135deg, #10b981, #34d399); }
      &.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
      &.danger { background: linear-gradient(135deg, #ef4444, #f87171); }
    }

    .stat-content {
      display: flex;
      align-items: center;
      position: relative;
      z-index: 1;

      .stat-icon {
        width: 52px;
        height: 52px;
        border-radius: var(--radius-md);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #fff;
        margin-right: 16px;
        transition: all var(--transition-base);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

        &.primary { background: linear-gradient(135deg, #6366f1, #818cf8); }
        &.success { background: linear-gradient(135deg, #10b981, #34d399); }
        &.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
        &.danger { background: linear-gradient(135deg, #ef4444, #f87171); }
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: var(--text-primary);
          letter-spacing: -0.5px;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: var(--text-secondary);
          margin-top: 4px;
          font-weight: 500;
        }
      }
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

  .level-badge {
    display: inline-block;
    padding: 4px 10px;
    border-radius: 20px;
    font-size: 12px;
    font-weight: 600;

    &.critical { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
    &.warning { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
    &.info { background: rgba(100, 116, 139, 0.1); color: #64748b; }
  }

  .link-btn {
    &.primary { color: var(--primary-color); }
    &.danger { color: var(--danger-color); }
  }

  .alert-card {
    .custom-timeline {
      :deep(.el-timeline-item__timestamp) {
        font-size: 12px;
        color: var(--text-secondary);
      }
    }

    .alert-item {
      padding: 16px;
      background: var(--bg-color);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
      transition: all var(--transition-base);

      &:hover {
        box-shadow: var(--shadow-sm);
      }

      .alert-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 8px;

        .alert-title {
          font-weight: 600;
          color: var(--text-primary);
        }

        .alert-level {
          padding: 2px 8px;
          border-radius: 12px;
          font-size: 11px;
          font-weight: 600;

          &.critical { background: rgba(239, 68, 68, 0.1); color: #ef4444; }
          &.warning { background: rgba(245, 158, 11, 0.1); color: #f59e0b; }
        }
      }

      .alert-desc {
        font-size: 13px;
        color: var(--text-regular);
        margin-bottom: 12px;
        line-height: 1.5;
      }

      .alert-actions {
        display: flex;
        gap: 8px;
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
</style>
