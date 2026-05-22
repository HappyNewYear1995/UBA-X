<template>
  <div class="dirty-data-log">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">脏数据处理日志</span>
          <el-badge :value="dirtyDataCount" :max="999" type="warning" />
        </div>
      </template>
      <el-table :data="dirtyDataLogs" style="width: 100%">
        <el-table-column prop="time" label="处理时间" min-width="180" />
        <el-table-column prop="type" label="异常类型" min-width="150" />
        <el-table-column prop="source" label="数据来源" min-width="150" />
        <el-table-column prop="count" label="异常记录数" min-width="120" />
        <el-table-column prop="action" label="处理动作" min-width="120" />
        <el-table-column label="操作" min-width="100" fixed="right">
          <template #default>
            <el-button type="primary" link>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'DirtyDataLog' })

const dirtyDataCount = ref(156)

const dirtyDataLogs = ref([
  {
    id: 1,
    time: '2026-05-22 14:32',
    type: '格式异常',
    source: 'Web SDK',
    count: 45,
    action: '自动过滤'
  },
  {
    id: 2,
    time: '2026-05-22 14:28',
    type: '重复数据',
    source: '移动端 SDK',
    count: 78,
    action: '自动去重'
  },
  {
    id: 3,
    time: '2026-05-22 14:15',
    type: '非法字符',
    source: 'API 上报',
    count: 23,
    action: '自动过滤'
  },
  {
    id: 4,
    time: '2026-05-22 14:05',
    type: '时间戳异常',
    source: 'Web SDK',
    count: 10,
    action: '标记待审'
  }
])
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.dirty-data-log {
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
