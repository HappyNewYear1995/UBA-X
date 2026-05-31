<template>
  <div class="data-cleaning">
    <el-row :gutter="16">
      <!-- 自动化预处理 -->
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">自动化预处理</span>
              <el-button type="primary" size="small" @click="handleAddRule">
                <Icon icon="ep:plus" /> 新增规则
              </el-button>
            </div>
          </template>
          <el-table :data="cleaningRules" style="width: 100%">
            <el-table-column prop="name" label="规则名称" min-width="200" />
            <el-table-column prop="type" label="规则类型" min-width="150" />
            <el-table-column prop="status" label="状态" min-width="100">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" />
              </template>
            </el-table-column>
            <el-table-column prop="processedCount" label="已处理数据量" min-width="150" />
            <el-table-column prop="updateTime" label="更新时间" min-width="180" />
            <el-table-column label="操作" min-width="180" fixed="right">
              <template #default>
                <el-button type="primary" link>编辑</el-button>
                <el-button type="primary" link>查看日志</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mt-4">
      <!-- 处理进度 -->
      <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="8">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <span class="card-title">数据标准化进度</span>
          </template>
          <div class="progress-list">
            <div v-for="item in progressList" :key="item.name" class="progress-item">
              <div class="progress-header">
                <span>{{ item.name }}</span>
                <span class="progress-percent">{{ item.percent }}%</span>
              </div>
              <el-progress :percentage="item.percent" :color="item.color" />
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 脏数据处理日志 -->
      <el-col :xs="24" :sm="12" :md="16" :lg="16" :xl="16">
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
      </el-col>
    </el-row>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'DataCleaning' })

const dirtyDataCount = ref(156)

const cleaningRules = ref([
  {
    id: 1,
    name: '时间格式标准化',
    type: '格式转换',
    enabled: true,
    processedCount: '128,456',
    updateTime: '2026-05-22 14:30'
  },
  {
    id: 2,
    name: 'IP 地址归一化',
    type: '数据清洗',
    enabled: true,
    processedCount: '98,234',
    updateTime: '2026-05-22 14:25'
  },
  {
    id: 3,
    name: '用户 ID 映射',
    type: '数据映射',
    enabled: true,
    processedCount: '87,123',
    updateTime: '2026-05-22 14:20'
  },
  {
    id: 4,
    name: '空值过滤',
    type: '数据过滤',
    enabled: false,
    processedCount: '45,678',
    updateTime: '2026-05-22 13:50'
  }
])

const progressList = ref([
  { name: '时间标准化', percent: 95, color: '#667eea' },
  { name: 'IP 归一化', percent: 88, color: '#4facfe' },
  { name: '用户映射', percent: 76, color: '#43e97b' },
  { name: '空值过滤', percent: 62, color: '#fa709a' }
])

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

const handleAddRule = () => {
  ElMessage.info('新增清洗规则功能开发中')
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.data-cleaning {
  padding: 16px;
  background: var(--app-content-bg-color);
  min-height: 100%;
}

.dashboard-card {
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;
  height: 100%;

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

.progress-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.progress-item {
  .progress-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 8px;
    font-size: 13px;
    color: var(--app-content-text-color-primary);
  }

  .progress-percent {
    font-weight: 600;
    color: #667eea;
  }
}
</style>
