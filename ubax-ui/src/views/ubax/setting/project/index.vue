<template>
  <div class="project-management">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">项目管理</span>
          <el-button type="primary" size="small" @click="handleAddProject">
            <Icon icon="ep:plus" /> 新增项目
          </el-button>
        </div>
      </template>
      <el-table :data="projects" style="width: 100%">
        <el-table-column prop="name" label="项目名称" width="200" />
        <el-table-column prop="code" label="项目标识" width="180" />
        <el-table-column prop="description" label="描述" />
        <el-table-column prop="events" label="事件数" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'active' ? 'success' : 'info'" size="small">
              {{ row.status === 'active' ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="primary" link>配置</el-button>
            <el-button type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="projectForm" label-width="100px">
        <el-form-item label="项目名称">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目标识">
          <el-input v-model="projectForm.code" placeholder="请输入项目标识，如：ubax_main" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="projectForm.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
        <el-form-item label="全局参数">
          <el-button size="small" @click="handleAddParam">
            <Icon icon="ep:plus" /> 添加参数
          </el-button>
          <div class="param-list" v-if="projectForm.params.length > 0">
            <div v-for="(param, index) in projectForm.params" :key="index" class="param-item">
              <el-input v-model="param.key" placeholder="参数名" style="width: 150px" />
              <el-input v-model="param.value" placeholder="参数值" style="width: 150px; margin-left: 8px" />
              <el-button type="danger" link @click="projectForm.params.splice(index, 1)" style="margin-left: 8px">
                <Icon icon="ep:delete" />
              </el-button>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitProject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
defineOptions({ name: 'ProjectManagement' })

const dialogVisible = ref(false)
const dialogTitle = ref('新增项目')
const projectForm = reactive({
  name: '',
  code: '',
  description: '',
  params: [] as { key: string; value: string }[]
})

const projects = ref([
  {
    id: 1,
    name: 'UBA-X 主项目',
    code: 'ubax_main',
    description: 'UBA-X 平台主项目，包含核心埋点与统计分析',
    events: 45,
    status: 'active',
    createTime: '2026-01-15 10:00'
  },
  {
    id: 2,
    name: '移动端项目',
    code: 'ubax_mobile',
    description: '移动端 SDK 埋点项目',
    events: 28,
    status: 'active',
    createTime: '2026-02-20 14:30'
  },
  {
    id: 3,
    name: '小程序项目',
    code: 'ubax_miniapp',
    description: '微信小程序埋点项目',
    events: 15,
    status: 'active',
    createTime: '2026-03-10 09:15'
  }
])

const handleAddProject = () => {
  dialogTitle.value = '新增项目'
  projectForm.name = ''
  projectForm.code = ''
  projectForm.description = ''
  projectForm.params = []
  dialogVisible.value = true
}

const handleAddParam = () => {
  projectForm.params.push({ key: '', value: '' })
}

const handleSubmitProject = () => {
  ElMessage.success('项目创建成功')
  dialogVisible.value = false
}
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.project-management {
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

.param-list {
  margin-top: 12px;
}

.param-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}
</style>
