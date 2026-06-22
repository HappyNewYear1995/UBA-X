<template>
  <div class="path-analysis" v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">用户路径分析</span>
              <div class="header-actions">
                <el-select v-model="selectedConfigId" placeholder="选择配置" style="width: 150px" @change="handleConfigChange">
                  <el-option v-for="config in pathConfigs" :key="config.id" :label="config.name" :value="config.id" />
                </el-select>
                <el-select v-model="chartType" placeholder="图表类型" style="width: 150px">
                  <el-option label="桑基图" value="sankey" />
                  <el-option label="旅程图" value="journey" />
                </el-select>
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  style="width: 240px"
                />
                <el-button type="primary" @click="handleQuery">
                  <Icon icon="ep:search" /> 查询
                </el-button>
                <el-button @click="openConfigDialog">
                  <Icon icon="ep:setting" /> 配置管理
                </el-button>
              </div>
            </div>
          </template>

          <!-- 桑基图 -->
          <Echart :options="sankeyOptions" :height="350" />

          <!-- 路径统计 -->
          <el-table :data="pathStats" style="width: 100%; margin-top: 20px">
            <el-table-column prop="path" label="用户路径" min-width="300" />
            <el-table-column prop="users" label="用户数" min-width="120" />
            <el-table-column prop="percentage" label="占比" min-width="100">
              <template #default="{ row }">
                {{ row.percentage }}%
              </template>
            </el-table-column>
            <el-table-column prop="avgDuration" label="平均耗时" min-width="120">
              <template #default="{ row }">
                {{ formatDuration(row.avgDuration) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 路径配置管理 -->
    <el-dialog v-model="configDialogVisible" title="路径配置管理" width="950px" append-to-body destroy-on-close>
      <div class="config-dialog-content">
      <!-- 搜索栏 -->
      <div class="config-search-bar">
        <el-input
          v-model="configQueryParams.name"
          placeholder="搜索配置名称"
          prefix-icon="ep:search"
          clearable
          class="search-input"
          @keyup.enter="handleConfigQuery"
        />
        <el-button type="primary" @click="handleConfigQuery">
          <Icon icon="ep:search" /> 搜索
        </el-button>
        <el-button @click="resetConfigQuery">
          <Icon icon="ep:refresh" /> 重置
        </el-button>
        <el-button type="primary" @click="handleConfigCreate">
          <Icon icon="ep:plus" /> 新增
        </el-button>
      </div>

      <!-- 配置列表 -->
      <el-table v-loading="configLoading" :data="configList" stripe max-height="360">
        <el-table-column prop="name" label="配置名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="startEvent" label="起始事件" min-width="140" show-overflow-tooltip />
        <el-table-column prop="maxDepth" label="最大深度" width="100" align="center" />
        <el-table-column prop="windowTime" label="时间窗口" width="120" align="center">
          <template #default="{ row }">
            {{ row.windowTime != null ? `${row.windowTime}秒` : '' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleConfigEdit(row)">
              <Icon icon="ep:edit" /> 编辑
            </el-button>
            <el-button type="danger" link @click="handleConfigDelete(row)">
              <Icon icon="ep:delete" /> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <Pagination
        v-model:page="configQueryParams.pageNo"
        v-model:limit="configQueryParams.pageSize"
        :total="configTotal"
        @pagination="getConfigList"
      />
      </div>

      <!-- 新增/编辑对话框 -->
      <el-dialog v-model="configFormDialogVisible" :title="configFormDialogTitle" width="600px" append-to-body destroy-on-close>
        <el-form ref="configFormRef" :model="configFormData" :rules="configFormRules" label-width="100px">
          <el-form-item label="配置名称" prop="name">
            <el-input v-model="configFormData.name" placeholder="请输入配置名称" />
          </el-form-item>
          <el-form-item label="起始事件" prop="startEvent">
            <el-input v-model="configFormData.startEvent" placeholder="请输入起始事件" />
          </el-form-item>
          <el-form-item label="最大深度" prop="maxDepth">
            <el-input-number v-model="configFormData.maxDepth" :min="1" :max="20" />
          </el-form-item>
          <el-form-item label="时间窗口" prop="windowTime">
            <el-input-number v-model="configFormData.windowTime" :min="0" :step="300" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="configFormData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="configFormDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitConfigForm">确定</el-button>
        </template>
      </el-dialog>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'
import {
  analyzePath,
  type PathAnalysisRespVO,
  type PathStat,
  type SankeyNode,
  type SankeyLink,
  getPathConfigPage,
  getPathConfig,
  type PathConfigRespVO,
  type PathConfigPageReqVO,
  type PathConfigSaveReqVO,
  createPathConfig,
  updatePathConfig,
  deletePathConfig
} from '@/api/ubax/analysis'

defineOptions({ name: 'PathAnalysis' })

const message = useMessage()

const loading = ref(false)
const chartType = ref('sankey')
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()])

const pathData = ref<PathAnalysisRespVO | null>(null)
const pathStats = ref<PathStat[]>([])

// 配置选择
const pathConfigs = ref<PathConfigRespVO[]>([])
const selectedConfigId = ref<number | undefined>(undefined)

const formatDate = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => n.toString().padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const formatDuration = (seconds: number) => {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return m > 0 ? `${m}m ${s}s` : `${s}s`
}

const loadPathConfigs = async () => {
  try {
    const data = await getPathConfigPage({ pageNo: 1, pageSize: 100 })
    pathConfigs.value = data.list || []
  } catch { /* ignore */ }
}

const handleConfigChange = (configId: number) => {
  handleQuery()
}

const sankeyOptions = ref<EChartsOption>({
  tooltip: { trigger: 'item', triggerOn: 'mousemove' },
  series: {
    type: 'sankey',
    layout: 'none',
    emphasis: { focus: 'adjacency' },
    data: [],
    links: [],
    lineStyle: {
      color: 'source',
      curveness: 0.5
    }
  }
})

const updateChart = (nodes: SankeyNode[], links: SankeyLink[]) => {
  sankeyOptions.value = {
    tooltip: { trigger: 'item', triggerOn: 'mousemove' },
    series: {
      type: 'sankey',
      layout: 'none',
      emphasis: { focus: 'adjacency' },
      data: nodes,
      links: links.map(l => ({ source: l.source, target: l.target, value: l.value })),
      lineStyle: { color: 'source', curveness: 0.5 }
    }
  }
}

const handleQuery = async () => {
  loading.value = true
  try {
    const config = pathConfigs.value.find(c => c.id === selectedConfigId.value)
    const data = await analyzePath({
      startEvent: config?.startEvent || undefined,
      startTime: dateRange.value ? `${formatDate(dateRange.value[0])} 00:00:00` : undefined,
      endTime: dateRange.value ? `${formatDate(dateRange.value[1])} 23:59:59` : undefined,
      maxDepth: config?.maxDepth || 7
    })
    pathData.value = data
    pathStats.value = data.pathStats || []
    updateChart(data.nodes || [], data.links || [])
  } catch {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// ===== 路径配置管理 =====
const configDialogVisible = ref(false)
const configLoading = ref(false)
const configList = ref<PathConfigRespVO[]>([])
const configTotal = ref(0)
const configQueryParams = ref<PathConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined
})

const configFormDialogVisible = ref(false)
const configFormDialogTitle = ref('')
const configFormRef = ref()
const configFormData = ref<PathConfigSaveReqVO>({
  name: '',
  startEvent: undefined,
  maxDepth: 7,
  windowTime: 1800,
  remark: undefined
})

const configFormRules = {
  name: [{ required: true, message: '配置名称不能为空', trigger: 'blur' }]
}

const openConfigDialog = () => {
  configDialogVisible.value = true
  getConfigList()
}

/** 获取配置列表 */
const getConfigList = async () => {
  configLoading.value = true
  try {
    const data = await getPathConfigPage(configQueryParams.value)
    configList.value = data.list
    configTotal.value = data.total
  } finally {
    configLoading.value = false
  }
}

/** 搜索配置 */
const handleConfigQuery = () => {
  configQueryParams.value.pageNo = 1
  getConfigList()
}

/** 重置搜索 */
const resetConfigQuery = () => {
  configQueryParams.value = { pageNo: 1, pageSize: 10, name: undefined }
  getConfigList()
}

/** 新增配置 */
const handleConfigCreate = () => {
  configFormDialogTitle.value = '新增配置'
  configFormData.value = {
    name: '',
    startEvent: undefined,
    maxDepth: 7,
    windowTime: 1800,
    remark: undefined
  }
  configFormDialogVisible.value = true
}

/** 编辑配置 */
const handleConfigEdit = async (row: PathConfigRespVO) => {
  configFormDialogTitle.value = '编辑配置'
  try {
    const data = await getPathConfig(row.id)
    configFormData.value = {
      id: data.id,
      name: data.name,
      startEvent: data.startEvent,
      maxDepth: data.maxDepth,
      windowTime: data.windowTime,
      remark: data.remark
    }
  } catch {
    ElMessage.error('获取配置详情失败')
    return
  }
  configFormDialogVisible.value = true
}

/** 删除配置 */
const handleConfigDelete = async (row: PathConfigRespVO) => {
  await message.delConfirm(`确定要删除配置「${row.name}」吗？`)
  await deletePathConfig(row.id)
  message.success('删除成功')
  getConfigList()
  await loadPathConfigs()
}

/** 提交配置表单 */
const submitConfigForm = async () => {
  await configFormRef.value?.validate()
  if (configFormData.value.id) {
    await updatePathConfig(configFormData.value)
    message.success('更新成功')
  } else {
    await createPathConfig(configFormData.value)
    message.success('创建成功')
  }
  configFormDialogVisible.value = false
  getConfigList()
  await loadPathConfigs()
}

onMounted(async () => {
  await loadPathConfigs()
  if (pathConfigs.value.length > 0) {
    selectedConfigId.value = pathConfigs.value[0].id
  }
  handleQuery()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.path-analysis {
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

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

// 配置弹窗内容区
.config-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

// 配置管理搜索栏
.config-search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;

  .search-input {
    width: 200px;
  }
}
</style>
