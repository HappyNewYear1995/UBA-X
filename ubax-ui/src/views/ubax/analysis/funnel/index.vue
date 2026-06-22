<template>
  <div class="funnel-analysis" v-loading="loading">
    <!-- 筛选栏 -->
    <el-card shadow="never" class="filter-card">
      <div class="filter-bar">
        <div class="filter-left">
          <el-select v-model="selectedFunnel" placeholder="选择漏斗" class="filter-select" @change="handleQuery">
            <el-option v-for="config in funnelConfigs" :key="config.id" :label="config.name" :value="String(config.id)" />
          </el-select>
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="filter-date"
            @change="handleQuery"
          />
        </div>
        <div style="display: flex; gap: 8px">
          <el-button type="primary" @click="handleQuery">
            <Icon icon="ep:search" /> 查询
          </el-button>
          <el-button @click="openConfigDialog">
            <Icon icon="ep:setting" /> 配置管理
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- 核心指标卡片 -->
    <el-row :gutter="16" class="metrics-row">
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%)">
            <Icon icon="ep:user" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">总用户数</div>
            <div class="metric-value">{{ formatNumber(totalUsers) }}</div>
            <div class="metric-trend">
              <span class="trend-up">
                <Icon icon="ep:top" :size="12" /> 12.5%
              </span>
              <span class="trend-label">较上期</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)">
            <Icon icon="ep:check" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">最终转化</div>
            <div class="metric-value">{{ finalConversion }}%</div>
            <div class="metric-trend">
              <span class="trend-up">
                <Icon icon="ep:top" :size="12" /> 3.2%
              </span>
              <span class="trend-label">较上期</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #fa709a 0%, #fee140 100%)">
            <Icon icon="ep:switch" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">平均转化率</div>
            <div class="metric-value">{{ avgConversion }}%</div>
            <div class="metric-trend">
              <span class="trend-down">
                <Icon icon="ep:bottom" :size="12" /> 1.8%
              </span>
              <span class="trend-label">较上期</span>
            </div>
          </div>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <div class="metric-card">
          <div class="metric-icon" style="background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)">
            <Icon icon="ep:timer" :size="24" />
          </div>
          <div class="metric-content">
            <div class="metric-label">平均耗时</div>
            <div class="metric-value">{{ avgTime }}</div>
            <div class="metric-trend">
              <span class="trend-label">全程时长</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 漏斗图表 -->
    <el-card shadow="never" class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">转化漏斗</span>
          <div class="header-actions">
            <el-radio-group v-model="chartView" size="small">
              <el-radio-button label="funnel">漏斗图</el-radio-button>
              <el-radio-button label="bar">柱状图</el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>
      <Echart :options="funnelOptions" :height="320" />
    </el-card>

    <!-- 漏斗步骤详情 -->
    <el-card shadow="never" class="table-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">步骤详情</span>
        </div>
      </template>
      <el-table :data="funnelSteps" class="funnel-table" stripe>
        <el-table-column prop="stepName" label="步骤" min-width="150">
          <template #default="{ row, $index }">
            <div class="step-cell">
              <div class="step-badge" :style="{ background: stepColors[$index] }">{{ $index + 1 }}</div>
              <span class="step-name">{{ row.stepName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="users" label="用户数" min-width="120" align="right">
          <template #default="{ row }">
            <span class="users-value">{{ formatNumber(row.users) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="conversionRate" label="步骤转化率" min-width="130" align="center">
          <template #default="{ row, $index }">
            <div class="conversion-cell">
              <div class="conversion-bar">
                <div class="conversion-fill" :style="{ width: row.conversionRate + '%', background: stepColors[$index] }"></div>
              </div>
              <span :class="row.conversionRate < 50 ? 'text-danger' : 'text-success'">{{ row.conversionRate }}%</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="overallRate" label="总转化率" min-width="120" align="center">
          <template #default="{ row }">
            <span class="overall-rate">{{ row.overallRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="lossRate" label="流失率" min-width="120" align="center">
          <template #default="{ row }">
            <span class="text-danger">{{ row.lossRate }}%</span>
          </template>
        </el-table-column>
        <el-table-column prop="avgTime" label="平均耗时" min-width="120" align="center">
          <template #default="{ row }">
            <span class="time-text">{{ formatDuration(row.avgTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="100" fixed="right" align="center">
          <template #default>
            <el-button type="primary" link>详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 漏斗配置管理 -->
    <el-dialog v-model="configDialogVisible" title="漏斗配置管理" width="900px" append-to-body destroy-on-close>
      <div class="config-dialog-content">
      <!-- 搜索栏 -->
      <div class="config-search-bar">
        <el-input
          v-model="configQueryParams.name"
          placeholder="搜索漏斗名称"
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
        <el-table-column prop="name" label="漏斗名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="steps" label="步骤事件" min-width="250" show-overflow-tooltip />
        <el-table-column prop="windowTime" label="窗口时间(秒)" width="140" align="center" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" align="center">
          <template #default="{ row }">
            <span>{{ formatDateTime(row.createTime) }}</span>
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
        <el-form ref="configFormRef" :model="configFormData" :rules="configFormRules" label-width="110px">
          <el-form-item label="漏斗名称" prop="name">
            <el-input v-model="configFormData.name" placeholder="请输入漏斗名称" />
          </el-form-item>
          <el-form-item label="步骤事件" prop="steps">
            <el-input
              v-model="configFormData.steps"
              type="textarea"
              :rows="4"
              placeholder='请输入步骤事件JSON数组，如 ["app_open","home_view","product_detail"]'
            />
          </el-form-item>
          <el-form-item label="窗口时间(秒)" prop="windowTime">
            <el-input-number v-model="configFormData.windowTime" :min="0" :step="3600" />
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
  analyzeFunnel,
  type FunnelAnalysisRespVO,
  type FunnelStep,
  getFunnelConfigPage,
  type FunnelConfigRespVO,
  type FunnelConfigPageReqVO,
  type FunnelConfigSaveReqVO,
  createFunnelConfig,
  updateFunnelConfig,
  deleteFunnelConfig
} from '@/api/ubax/analysis'

defineOptions({ name: 'FunnelAnalysis' })

const message = useMessage()

const loading = ref(false)
const selectedFunnel = ref('')
const dateRange = ref<[Date, Date]>([new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), new Date()])
const chartView = ref('funnel')

const stepColors = ['#667eea', '#764ba2', '#4facfe', '#43e97b', '#fa709a']

const funnelSteps = ref<FunnelStep[]>([])
const funnelData = ref<FunnelAnalysisRespVO | null>(null)

const funnelConfigs = ref<FunnelConfigRespVO[]>([])

const loadFunnelConfigs = async () => {
  try {
    const data = await getFunnelConfigPage({ pageNo: 1, pageSize: 100 })
    funnelConfigs.value = data.list || []
  } catch { /* ignore */ }
}

const funnelStepMap = computed(() => {
  const map: Record<string, string[]> = {}
  funnelConfigs.value.forEach(config => {
    try {
      map[String(config.id)] = JSON.parse(config.steps)
    } catch { /* ignore parse error */ }
  })
  return map
})

const totalUsers = computed(() => funnelData.value?.totalUsers || 0)
const finalConversion = computed(() => funnelData.value?.finalConversionRate || 0)
const avgConversion = computed(() => funnelData.value?.avgConversionRate || 0)
const avgTime = computed(() => formatDuration(funnelData.value?.avgDuration || 0))

const formatDate = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

const formatDateTime = (date: Date) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

const formatDuration = (seconds: number) => {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return m > 0 ? `${m}m ${s}s` : `${s}s`
}

const funnelOptions = reactive<EChartsOption>({
  tooltip: { trigger: 'item', formatter: '{b} : {c}人 ({d}%)' },
  series: [
    {
      name: '转化漏斗',
      type: 'funnel',
      left: '20%',
      top: 20,
      bottom: 20,
      width: '60%',
      min: 0,
      max: 100,
      minSize: '15%',
      maxSize: '85%',
      sort: 'descending',
      gap: 6,
      label: {
        show: true,
        position: 'inside',
        formatter: '{b}\n{c}人',
        fontSize: 12,
        color: '#fff'
      },
      labelLine: { length: 8, lineStyle: { width: 1, type: 'solid' } },
      itemStyle: { borderColor: '#fff', borderWidth: 1, shadowBlur: 8, shadowColor: 'rgba(0,0,0,0.1)' },
      emphasis: {
        itemStyle: { shadowBlur: 15, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' }
      },
      data: []
    }
  ]
})

const updateChart = () => {
  const data = funnelSteps.value.map((step, index) => ({
    value: step.users,
    name: step.stepName,
    itemStyle: { color: stepColors[index % stepColors.length] }
  }))
  funnelOptions.series = [{
    name: '转化漏斗',
    type: chartView.value === 'funnel' ? 'funnel' : 'bar',
    left: '20%',
    top: 20,
    bottom: 20,
    width: '60%',
    min: 0,
    max: 100,
    minSize: '15%',
    maxSize: '85%',
    sort: 'descending',
    gap: 6,
    label: { show: true, position: 'inside', formatter: '{b}\n{c}人', fontSize: 12, color: '#fff' },
    labelLine: { length: 8, lineStyle: { width: 1, type: 'solid' } },
    itemStyle: { borderColor: '#fff', borderWidth: 1, shadowBlur: 8, shadowColor: 'rgba(0,0,0,0.1)' },
    emphasis: { itemStyle: { shadowBlur: 15, shadowOffsetX: 0, shadowColor: 'rgba(0,0,0,0.2)' } },
    data
  }]
}

const formatNumber = (num: number) => {
  return num.toLocaleString()
}

const handleQuery = async () => {
  loading.value = true
  try {
    const steps = funnelStepMap.value[selectedFunnel.value]
    if (!steps || steps.length === 0) {
      ElMessage.warning('请先配置漏斗步骤')
      loading.value = false
      return
    }
    const data = await analyzeFunnel({
      steps,
      startTime: `${formatDate(dateRange.value[0])} 00:00:00`,
      endTime: `${formatDate(dateRange.value[1])} 23:59:59`
    })
    funnelData.value = data
    funnelSteps.value = data.steps || []
    updateChart()
  } catch {
    ElMessage.error('查询失败')
  } finally {
    loading.value = false
  }
}

// ===== 漏斗配置管理 =====
const configDialogVisible = ref(false)
const configLoading = ref(false)
const configList = ref<FunnelConfigRespVO[]>([])
const configTotal = ref(0)
const configQueryParams = ref<FunnelConfigPageReqVO>({
  pageNo: 1,
  pageSize: 10,
  name: undefined
})

const configFormDialogVisible = ref(false)
const configFormDialogTitle = ref('')
const configFormRef = ref()
const configFormData = ref<FunnelConfigSaveReqVO>({
  name: '',
  steps: '',
  windowTime: 86400,
  remark: ''
})

const configFormRules = {
  name: [{ required: true, message: '漏斗名称不能为空', trigger: 'blur' }],
  steps: [{ required: true, message: '步骤事件不能为空', trigger: 'blur' }]
}

const openConfigDialog = () => {
  configDialogVisible.value = true
  getConfigList()
}

/** 获取配置列表 */
const getConfigList = async () => {
  configLoading.value = true
  try {
    const data = await getFunnelConfigPage(configQueryParams.value)
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
  configFormDialogTitle.value = '新增漏斗配置'
  configFormData.value = { name: '', steps: '', windowTime: 86400, remark: '' }
  configFormDialogVisible.value = true
}

/** 编辑配置 */
const handleConfigEdit = (row: FunnelConfigRespVO) => {
  configFormDialogTitle.value = '编辑漏斗配置'
  configFormData.value = {
    id: row.id,
    name: row.name,
    steps: row.steps,
    windowTime: row.windowTime || 86400,
    remark: row.remark || ''
  }
  configFormDialogVisible.value = true
}

/** 删除配置 */
const handleConfigDelete = async (row: FunnelConfigRespVO) => {
  await message.delConfirm(`确定要删除漏斗配置「${row.name}」吗？`)
  await deleteFunnelConfig(row.id)
  message.success('删除成功')
  getConfigList()
  await loadFunnelConfigs()
}

/** 提交配置表单 */
const submitConfigForm = async () => {
  await configFormRef.value?.validate()
  if (configFormData.value.id) {
    await updateFunnelConfig(configFormData.value)
    message.success('更新成功')
  } else {
    await createFunnelConfig(configFormData.value)
    message.success('创建成功')
  }
  configFormDialogVisible.value = false
  getConfigList()
  await loadFunnelConfigs()
}

watch(chartView, () => {
  updateChart()
})

onMounted(async () => {
  await loadFunnelConfigs()
  if (funnelConfigs.value.length > 0) {
    selectedFunnel.value = String(funnelConfigs.value[0].id)
  }
  handleQuery()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.funnel-analysis {
  padding: 16px;
  background: var(--app-content-bg-color);
  min-height: 100%;
}

// 筛选栏
.filter-card {
  margin-bottom: 16px;
  border-radius: var(--radius-lg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  background: var(--app-content-card-bg) !important;

  :deep(.el-card__body) {
    padding: 12px 20px;
  }
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.filter-select {
  width: 180px;
}

.filter-date {
  width: 260px;
}

// 指标卡片
.metrics-row {
  margin-bottom: 16px;
}

.metric-card {
  display: flex;
  align-items: center;
  padding: 20px;
  border-radius: var(--radius-lg);
  background: var(--app-content-card-bg);
  border: 1px solid var(--app-content-card-border);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  .metric-icon {
    width: 48px;
    height: 48px;
    border-radius: var(--radius-md);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 16px;
    color: #fff;
    flex-shrink: 0;
  }

  .metric-content {
    flex: 1;
    min-width: 0;

    .metric-label {
      font-size: 13px;
      color: var(--app-content-text-color-secondary);
      margin-bottom: 4px;
    }

    .metric-value {
      font-size: 24px;
      font-weight: 600;
      color: var(--app-content-text-color-primary);
      line-height: 1.2;
    }

    .metric-trend {
      display: flex;
      align-items: center;
      gap: 6px;
      margin-top: 4px;
      font-size: 12px;

      .trend-up {
        color: #10b981;
        display: flex;
        align-items: center;
        gap: 2px;
      }

      .trend-down {
        color: #ef4444;
        display: flex;
        align-items: center;
        gap: 2px;
      }

      .trend-label {
        color: var(--app-content-text-color-secondary);
      }
    }
  }
}

// 图表卡片
.chart-card {
  margin-bottom: 16px;
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
}

// 表格卡片
.table-card {
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
    --el-table-border-color: var(--app-content-card-border);
    --el-table-header-bg-color: var(--app-content-card-bg);
  }
}

// 通用卡片头部
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

// 步骤单元格
.step-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.step-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.step-name {
  font-weight: 500;
  color: var(--app-content-text-color-primary);
}

// 用户数
.users-value {
  font-weight: 600;
  color: var(--app-content-text-color-primary);
}

// 转化率单元格
.conversion-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.conversion-bar {
  width: 60px;
  height: 6px;
  background: var(--app-content-card-border);
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}

.conversion-fill {
  height: 100%;
  border-radius: 3px;
  transition: width 0.3s ease;
}

// 总转化率
.overall-rate {
  font-weight: 500;
  color: var(--app-content-text-color-primary);
}

// 时间文本
.time-text {
  color: var(--app-content-text-color-secondary);
  font-size: 13px;
}

// 文字颜色
.text-danger {
  color: #ef4444;
}

.text-success {
  color: #10b981;
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
