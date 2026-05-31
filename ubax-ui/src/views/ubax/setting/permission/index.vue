<template>
  <div class="account-permission">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">账号与权限</span>
          <el-button type="primary" size="small" @click="handleAddMember">
            <Icon icon="ep:plus" /> 添加成员
          </el-button>
        </div>
      </template>
      <el-table :data="members" style="width: 100%">
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" width="200" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)" size="small">{{ row.role }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="permissions" label="权限范围" width="250">
          <template #default="{ row }">
            <el-tag v-for="perm in row.permissions" :key="perm" size="small" class="mr-1">{{ perm }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLogin" label="最后登录" width="180" />
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="primary" link>权限配置</el-button>
            <el-button type="danger" link>移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="memberForm" label-width="100px">
        <el-form-item label="姓名">
          <el-input v-model="memberForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="memberForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="memberForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="管理员" />
            <el-option label="分析师" value="分析师" />
            <el-option label="开发者" value="开发者" />
            <el-option label="观察者" value="观察者" />
          </el-select>
        </el-form-item>
        <el-form-item label="权限范围">
          <el-checkbox-group v-model="memberForm.permissions">
            <el-checkbox label="数据总览" />
            <el-checkbox label="数据采集" />
            <el-checkbox label="数据清洗" />
            <el-checkbox label="可视化分析" />
            <el-checkbox label="AI 监测" />
            <el-checkbox label="系统设置" />
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitMember">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'AccountPermission' })

const dialogVisible = ref(false)
const dialogTitle = ref('添加成员')
const memberForm = reactive({
  name: '',
  email: '',
  role: '',
  permissions: [] as string[]
})

const members = ref([
  {
    id: 1,
    name: '张三',
    email: 'zhangsan@example.com',
    role: '管理员',
    permissions: ['数据总览', '数据采集', '数据清洗', '可视化分析', 'AI 监测', '系统设置'],
    lastLogin: '2026-05-22 14:30'
  },
  {
    id: 2,
    name: '李四',
    email: 'lisi@example.com',
    role: '分析师',
    permissions: ['数据总览', '可视化分析', 'AI 监测'],
    lastLogin: '2026-05-22 10:15'
  },
  {
    id: 3,
    name: '王五',
    email: 'wangwu@example.com',
    role: '开发者',
    permissions: ['数据采集', '数据清洗'],
    lastLogin: '2026-05-21 18:45'
  },
  {
    id: 4,
    name: '赵六',
    email: 'zhaoliu@example.com',
    role: '观察者',
    permissions: ['数据总览'],
    lastLogin: '2026-05-20 09:00'
  }
])

const getRoleType = (role: string) => {
  const map: Record<string, string> = {
    '管理员': 'danger',
    '分析师': 'primary',
    '开发者': 'success',
    '观察者': 'info'
  }
  return map[role] || ''
}

const handleAddMember = () => {
  dialogTitle.value = '添加成员'
  memberForm.name = ''
  memberForm.email = ''
  memberForm.role = ''
  memberForm.permissions = []
  dialogVisible.value = true
}

const handleSubmitMember = () => {
  ElMessage.success('成员添加成功')
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.account-permission {
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
