<template>
  <div class="retention-analysis" v-loading="loading">
    <el-row :gutter="16">
      <el-col :span="24">
        <el-card shadow="never" class="dashboard-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">留存分析</span>
              <div class="header-actions">
                <el-select v-model="selectedConfigId" placeholder="选择配置" style="width: 150px" @change="handleConfigChange">
                  <el-option v-for="config in retentionConfigs" :key="config.id" :label="config.name" :value="config.id" />
                </el-select>
                <el-select v-model="retentionType" placeholder="留存类型" style="width: 150px">
                  <el-option label="次日留存" value="next_day" />
                  <el-option label="7日留存" value="7_days" />
                  <el-option label="30日留存" value="30_days" />
                </el-select>
                <el-select v-model="dimension" placeholder="统计维度" style="width: 120px">
                  <el-option label="按日" value="day" />
                  <el-option label="按周" value="week" />
                  <el-option label="按月" value="month" />
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

          <!-- 留存趋势图 -->
          <Echart :options="retentionTrendOptions" :height="280" />

          <!-- 留存表格 -->
          <el-table :data="retentionTable" style="width: 100%; margin-top: 20px" border>
            <el-table-column prop="date" label="日期" min-width="120" fixed />
            <el-table-column prop="newUsers" label="新增用户" min-width="100" />
            <el-table-column
              v-for="day in retentionDays"
              :key="day"
              :label="`第${day}天`"
              min-width="90"
            >
              <template #default="{ row }">
                <span :style="{ color: getRetentionColor(row.retentionRates?.[day] || 0) }">
                  {{ row.retentionRates?.[day] || '-' }}%
                </span>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 留存配置管理 -->
    <el-dialog v-model="configDialogVisible" title="留存配置管理" width="950px" append-to-body destroy-on-close>
      <div class="config-dialog-content">
      <!-- 搜索栏 -->
      <el-form :model="configQueryParams" :inline="true" class="config-search-form">
        <el-form-item label="配置名称">
          <el-input v-model="configQueryParams.name" placeholder="请输入配置名称" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="留存类型">
          <el-select v-model="configQueryParams.retentionType" placeholder="请选择留存类型" clearable style="width: 180px">
            <el-option label="次日留存" value="next_day" />
            <el-option label="7日留存" value="7_days" />
            <el-option label="30日留存" value="30_days" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleConfigQuery">
            <Icon icon="ep:search" /> 搜索
          </el-button>
          <el-button @click="resetConfigQuery">
            <Icon icon="ep:refresh" /> 重置
          </el-button>
          <el-button type="primary" @click="handleConfigCreate">
            <Icon icon="ep:plus" /> 新增
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 配置列表 -->
      <el-table v-loading="configLoading" :data="configList" border style="width: 100%" max-height="360">
        <el-table-column prop="name" label="配置名称" min-width="150" />
        <el-table-column prop="retentionType" label="留存类型" min-width="120">
          <template #default="{ row }">
            {{ retentionTypeFormat(row.retentionType) }}
          </template>
        </el-table-column>
        <el-table-column prop="dimension" label="统计维度" min-width="100">
          <template #default="{ row }">
            {{ dimensionFormat(row.dimension) }}
          </template>
        </el-table-column>
        <el-table-column prop="startEvent" label="起始事件" min-width="130" show-overflow-tooltip />
        <el-table-column prop="returnEvent" label="回访事件" min-width="130" show-overflow-tooltip />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleConfigEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleConfigDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="config-pagination">
        <el-pagination
          v-model:current-page="configQueryParams.pageNo"
          v-model:page-size="configQueryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="configTotal"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="getConfigList"
          @current-change="getConfigList"
        />
      </div>
      </div>

      <!-- 新增/编辑对话框 -->
      <el-dialog v-model="configFormDialogVisible" :title="configFormDialogTitle" width="500px" append-to-body destroy-on-close>
        <el-form ref="configFormRef" :model="configFormData" :rules="configFormRules" label-width="100px">
          <el-form-item label="配置名称" prop="name">
            <el-input v-model="configFormData.name" placeholder="请输入配置名称" />
          </el-form-item>
          <el-form-item label="留存类型" prop="retentionType">
            <el-select v-model="configFormData.retentionType" placeholder="请选择留存类型" style="width: 100%">
              <el-option label="次日留存" value="next_day" />
              <el-option label="7日留存" value="7_days" />
              <el-option label="30日留存" value="30_days" />
            </el-select>
          </el-form-item>
          <el-form-item label="统计维度" prop="dimension">
            <el-select v-model="configFormData.dimension" placeholder="请选择统计维度" style="width: 100%">
              <el-option label="按日" value="day" />
              <el-option label="按周" value="week" />
              <el-option label="按月" value="month" />
            </el-select>
          </el-form-item>
          <el-form-item label="起始事件" prop="startEvent">
            <el-input v-model="configFormData.startEvent" placeholder="为空则统计所有新用户" />
          </el-form-item>
          <el-form-item label="回访事件" prop="returnEvent">
            <el-input v-model="configFormData.returnEvent" placeholder="为空则统计所有活跃用户" />
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input v-model="configFormData.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
  analyzeRetention,
  type RetentionAnalysisRespVO,
  type RetentionRow,
  type RetentionTrendItem,
  getRetentionConfigPage,
  getRetentionConfig,
  type RetentionConfigRespVO,
  type RetentionConfigSaveReqVO,
  createRetentionConfig,
  updateRetentionConfig,
  deleteRetentionConfig,
  getRetentionResults,
  type RetentionResultRespVO
} from '@/api/ubax/analysis'

defineOptions({ name: 'RetentionAnalysis' })

const message = useMessage()

const loading = ref(false)
const retentionType = ref('next_day')
const dimension = ref('day')
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), new Date()])

const retentionData = ref<RetentionAnalysisRespVO | null>(null)
const retentionTable = ref<RetentionRow[]>([])

const retentionConfigs = ref<RetentionConfigRespVO[]>([])
const selectedConfigId = ref<number | undefined>(undefined)

const loadRetentionConfigs = async () => {
  try {
    const data = await getRetentionConfigPage({ pageNo: 1, pageSize: 100 })
    retentionConfigs.value = data.list || []
  } catch { /* ignore */ }
}

const handleConfigChange = (configId: number) => {
  const config = retentionConfigs.value.find(c => c.id === configId)
  if (config) {
    retentionType.value = config.retentionType
    if (config.dimension) dimension.value = config.dimension
  }
  handleQuery()
}

const formatDate = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const retentionDays = computed(() => {
  const type = retentionType.value
  if (type === '30_days') return Array.from({ length: 30 }, (_, i) => i + 1)
  if (type === '7_days') return Array.from({ length: 7 }, (_, i) => i + 1)
  return Array.from({ length: 7 }, (_, i) => i + 1)
})

const retentionTrendOptions = ref<EChartsOption>({
  tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
  legend: { data: ['次日留存', '7日留存', '30日留存'], top: 10 },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: []
  },
  yAxis: { type: 'value', axisLabel: { formatter: '{value}%' } },
  series: []
})

const updateChart = (trend: RetentionTrendItem[]) => {
  retentionTrendOptions.value.xAxis = {
    type: 'category',
    data: trend.map(t => t.date)
  }
  retentionTrendOptions.value.series = [
    {
      name: '次日留存',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#667eea' },
      data: trend.map(t => t.nextDayRate)
    },
    {
      name: '7日留存',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#4facfe' },
      data: trend.map(t => t.day7Rate)
    },
    {
      name: '30日留存',
      type: 'line',
      smooth: true,
      itemStyle: { color: '#43e97b' },
      data: trend.map(t => t.day30Rate)
    }
  ]
}

const getRetentionColor = (value: number) => {
  if (value >= 40) return '#43e97b'
  if (value >= 25) return '#4facfe'
  return '#fa709a'
}

const handleQuery = async () => {
  loading.value = true
  try {
    const data = await analyzeRetention({
      retentionType: retentionType.value,
      dimension: dimension.value,
      startTime: `${formatDate(dateRange.value[0])} 00:00:00`,
      endTime: `${formatDate(dateRange.value[1])} 23:59:59`
    })
    retentionData.value = data
    retentionTable.value = data.table || []
    updateChart(data.trend || [])
  } catch {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// ===== 留存配置管理 =====
const configDialogVisible = ref(false)
const configLoading = ref(false)
const configList = ref<RetentionConfigRespVO[]>([])
const configTotal = ref(0)
const configQueryParams = reactive({
  pageNo: 1,
  pageSize: 10,
  name: '',
  retentionType: ''
})

const configFormDialogVisible = ref(false)
const configFormDialogTitle = ref('')
const configFormRef = ref()
const configFormData = ref<RetentionConfigSaveReqVO>({
  name: '',
  retentionType: '',
  dimension: '',
  startEvent: '',
  returnEvent: '',
  remark: ''
})

const configFormRules = {
  name: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  retentionType: [{ required: true, message: '请选择留存类型', trigger: 'change' }]
}

/** 留存类型格式化 */
const retentionTypeFormat = (type: string) => {
  const map: Record<string, string> = {
    next_day: '次日留存',
    '7_days': '7日留存',
    '30_days': '30日留存'
  }
  return map[type] || type
}

/** 统计维度格式化 */
const dimensionFormat = (dim?: string) => {
  const map: Record<string, string> = {
    day: '按日',
    week: '按周',
    month: '按月'
  }
  return dim ? (map[dim] || dim) : '-'
}

const openConfigDialog = () => {
  configDialogVisible.value = true
  getConfigList()
}

/** 获取配置列表 */
const getConfigList = async () => {
  configLoading.value = true
  try {
    const data = await getRetentionConfigPage(configQueryParams)
    configList.value = data.list || []
    configTotal.value = data.total || 0
  } catch {
    ElMessage.error('获取列表失败')
  } finally {
    configLoading.value = false
  }
}

/** 搜索配置 */
const handleConfigQuery = () => {
  configQueryParams.pageNo = 1
  getConfigList()
}

/** 重置搜索 */
const resetConfigQuery = () => {
  configQueryParams.name = ''
  configQueryParams.retentionType = ''
  configQueryParams.pageNo = 1
  getConfigList()
}

/** 新增配置 */
const handleConfigCreate = () => {
  configFormDialogTitle.value = '新增留存配置'
  configFormData.value = {
    name: '',
    retentionType: '',
    dimension: '',
    startEvent: '',
    returnEvent: '',
    remark: ''
  }
  configFormDialogVisible.value = true
}

/** 编辑配置 */
const handleConfigEdit = async (row: RetentionConfigRespVO) => {
  configFormDialogTitle.value = '编辑留存配置'
  try {
    const data = await getRetentionConfig(row.id)
    configFormData.value = {
      id: data.id,
      name: data.name,
      retentionType: data.retentionType,
      dimension: data.dimension || '',
      startEvent: data.startEvent || '',
      returnEvent: data.returnEvent || '',
      remark: data.remark || ''
    }
  } catch {
    ElMessage.error('获取配置详情失败')
    return
  }
  configFormDialogVisible.value = true
}

/** 删除配置 */
const handleConfigDelete = async (row: RetentionConfigRespVO) => {
  await message.delConfirm(`确定要删除留存配置「${row.name}」吗？`)
  await deleteRetentionConfig(row.id)
  message.success('删除成功')
  getConfigList()
  await loadRetentionConfigs()
}

/** 提交配置表单 */
const submitConfigForm = async () => {
  const valid = await configFormRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (configFormData.value.id) {
      await updateRetentionConfig(configFormData.value)
      message.success('更新成功')
    } else {
      await createRetentionConfig(configFormData.value)
      message.success('创建成功')
    }
    configFormDialogVisible.value = false
    getConfigList()
    await loadRetentionConfigs()
  } catch {
    ElMessage.error(configFormData.value.id ? '更新失败' : '创建失败')
  }
}

onMounted(async () => {
  await loadRetentionConfigs()
  if (retentionConfigs.value.length > 0) {
    selectedConfigId.value = retentionConfigs.value[0].id
    const config = retentionConfigs.value[0]
    retentionType.value = config.retentionType
    if (config.dimension) dimension.value = config.dimension
  }
  handleQuery()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.retention-analysis {
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
.config-search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  margin-bottom: 16px;
}

.config-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
