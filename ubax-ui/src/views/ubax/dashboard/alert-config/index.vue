<template>
  <div class="alert-config">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">异常检测配置</span>
          <el-button type="primary" size="small" @click="handleAddRule">
            <Icon icon="ep:plus" /> 新增规则
          </el-button>
        </div>
      </template>
      <el-table :data="alertRules" style="width: 100%">
        <el-table-column prop="name" label="规则名称" min-width="200" />
        <el-table-column prop="metric" label="监控指标" min-width="150" />
        <el-table-column prop="threshold" label="波动阈值" min-width="120" />
        <el-table-column prop="triggerCondition" label="触发条件" min-width="150" />
        <el-table-column prop="notifyMethod" label="通知方式" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="method in row.notifyMethod" :key="method" size="small" class="mr-1">
              {{ method }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" />
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'AlertConfig' })

const alertRules = ref([
  {
    id: 1,
    name: '活跃用户骤降告警',
    metric: '活跃用户数',
    threshold: '30%',
    triggerCondition: '5分钟内',
    notifyMethod: ['站内信', '邮件'],
    enabled: true
  },
  {
    id: 2,
    name: '转化率异常波动',
    metric: '转化率',
    threshold: '20%',
    triggerCondition: '10分钟内',
    notifyMethod: ['站内信', '短信'],
    enabled: true
  },
  {
    id: 3,
    name: '异常事件激增',
    metric: '异常事件',
    threshold: '50%',
    triggerCondition: '3分钟内',
    notifyMethod: ['站内信', '邮件', '短信'],
    enabled: true
  },
  {
    id: 4,
    name: '页面加载超时',
    metric: '页面加载时长',
    threshold: '5s',
    triggerCondition: '持续1分钟',
    notifyMethod: ['站内信'],
    enabled: false
  }
])

const handleAddRule = () => {
  ElMessage.info('新增告警规则功能开发中')
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.alert-config {
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
