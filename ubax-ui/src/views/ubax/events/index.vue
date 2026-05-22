<template>
  <div class="custom-events">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">自定义事件上报</span>
          <el-button type="primary" size="small" @click="handleAddEvent">
            <Icon icon="ep:plus" /> 新增事件
          </el-button>
        </div>
      </template>
      <el-table :data="paginatedEvents" style="width: 100%">
        <el-table-column prop="name" label="事件名称" min-width="160" />
        <el-table-column prop="code" label="事件标识" min-width="150" />
        <el-table-column prop="category" label="事件分类" width="100" />
        <el-table-column prop="properties" label="属性数量" width="90" align="center" />
        <el-table-column prop="status" label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 'enabled' ? 'success' : 'info'" size="small">
              {{ row.status === 'enabled' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default>
            <el-button type="primary" link size="small">编辑</el-button>
            <el-button type="primary" link size="small">属性配置</el-button>
            <el-button type="danger" link size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="customEvents.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        class="mt-4"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" title="新增自定义事件" width="600px">
      <el-form :model="eventForm" label-width="100px">
        <el-form-item label="事件名称">
          <el-input v-model="eventForm.name" placeholder="请输入事件名称" />
        </el-form-item>
        <el-form-item label="事件标识">
          <el-input v-model="eventForm.code" placeholder="请输入事件标识，如：user_register" />
        </el-form-item>
        <el-form-item label="事件分类">
          <el-select v-model="eventForm.category" placeholder="请选择事件分类">
            <el-option label="用户行为" value="user" />
            <el-option label="页面浏览" value="page" />
            <el-option label="点击事件" value="click" />
            <el-option label="转化事件" value="conversion" />
          </el-select>
        </el-form-item>
        <el-form-item label="事件描述">
          <el-input v-model="eventForm.desc" type="textarea" :rows="3" placeholder="请输入事件描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEvent">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'CustomEvents' })

const dialogVisible = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const eventForm = reactive({
  name: '',
  code: '',
  category: '',
  desc: ''
})

const customEvents = ref([
  {
    id: 1,
    name: '用户注册',
    code: 'user_register',
    category: '用户行为',
    properties: 5,
    status: 'enabled',
    updateTime: '2026-05-22 10:30'
  },
  {
    id: 2,
    name: '商品浏览',
    code: 'product_view',
    category: '页面浏览',
    properties: 8,
    status: 'enabled',
    updateTime: '2026-05-22 09:15'
  },
  {
    id: 3,
    name: '加入购物车',
    code: 'add_to_cart',
    category: '点击事件',
    properties: 6,
    status: 'enabled',
    updateTime: '2026-05-21 16:45'
  },
  {
    id: 4,
    name: '完成支付',
    code: 'payment_complete',
    category: '转化事件',
    properties: 10,
    status: 'enabled',
    updateTime: '2026-05-21 14:20'
  }
])

const paginatedEvents = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return customEvents.value.slice(start, start + pageSize.value)
})

const handleAddEvent = () => {
  dialogVisible.value = true
}

const handleSubmitEvent = () => {
  ElMessage.success('事件创建成功')
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.custom-events {
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
