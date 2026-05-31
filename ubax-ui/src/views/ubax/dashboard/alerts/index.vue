<template>
  <div class="alerts-center">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">异常告警中心</span>
          <el-badge :value="alertCount" :max="99" type="danger" />
        </div>
      </template>
      <el-table :data="alerts" style="width: 100%">
        <el-table-column prop="time" label="告警时间" width="180" />
        <el-table-column prop="title" label="告警内容" />
        <el-table-column prop="level" label="告警级别" width="120">
          <template #default="{ row }">
            <el-tag :type="row.level === 'danger' ? 'danger' : 'warning'" size="small">
              {{ row.levelText }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="impact" label="影响范围" width="150" />
        <el-table-column label="操作" width="100">
          <template #default>
            <el-button type="primary" link>查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'AlertsCenter' })

const alertCount = ref(7)

const alerts = ref([
  {
    id: 1,
    title: '异常登录频率激增',
    desc: '检测到来自 IP 192.168.1.100 的异常登录尝试，频率超过阈值 300%',
    time: '2026-05-22 14:32',
    level: 'danger',
    levelText: '严重',
    impact: '全局'
  },
  {
    id: 2,
    title: '用户行为模式异常',
    desc: '用户组 A 的访问路径偏离正常模式，可能存在数据爬取行为',
    time: '2026-05-22 14:15',
    level: 'warning',
    levelText: '警告',
    impact: '用户组 A'
  },
  {
    id: 3,
    title: '转化率异常下降',
    desc: '购买转化率较昨日同期下降 45%，建议检查支付流程',
    time: '2026-05-22 13:58',
    level: 'warning',
    levelText: '警告',
    impact: '转化漏斗'
  },
  {
    id: 4,
    title: '页面加载超时告警',
    desc: '首页平均加载时间超过 5 秒，影响用户体验',
    time: '2026-05-22 13:45',
    level: 'warning',
    levelText: '警告',
    impact: '首页'
  },
  {
    id: 5,
    title: '数据上报异常',
    desc: 'Web SDK 数据上报量骤降 60%，可能存在接入问题',
    time: '2026-05-22 13:30',
    level: 'danger',
    levelText: '严重',
    impact: '数据采集'
  }
])
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.alerts-center {
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
</style>
