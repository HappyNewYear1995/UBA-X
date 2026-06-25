<template>
  <div class="alert-config">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">检测配置</span>
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 告警规则 Tab -->
        <el-tab-pane label="告警规则" name="alert">
          <div class="mb-4 flex justify-between">
            <el-button type="primary" @click="openAlertForm()">
              <Icon icon="ep:plus" /> 新增告警规则
            </el-button>
          </div>
          <el-table v-loading="alertLoading" :data="alertList" style="width: 100%">
            <el-table-column prop="name" label="规则名称" min-width="180" />
            <el-table-column prop="alertType" label="告警类型" min-width="120">
              <template #default="{ row }">
                <el-tag :type="row.alertType === 1 ? 'primary' : row.alertType === 2 ? 'warning' : 'danger'">
                  {{ alertTypeMap[row.alertType] || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="metricName" label="监控指标" min-width="130" />
            <el-table-column prop="thresholdValue" label="阈值" min-width="100" />
            <el-table-column prop="notificationType" label="通知方式" min-width="130">
              <template #default="{ row }">
                <el-tag v-if="row.notificationType" size="small">
                  {{ notificationTypeMap[row.notificationType] || row.notificationType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" min-width="80">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="handleAlertEnabledChange(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openAlertForm(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteAlert(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-4 flex justify-end">
            <el-pagination
              v-model:current-page="alertPageNo"
              v-model:page-size="alertPageSize"
              :total="alertTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @change="getAlertList"
            />
          </div>
        </el-tab-pane>

        <!-- 安全检测 Tab -->
        <el-tab-pane label="安全检测" name="security">
          <div class="mb-4 flex justify-between">
            <el-button type="primary" @click="openSecurityForm()">
              <Icon icon="ep:plus" /> 新增安全规则
            </el-button>
          </div>
          <el-table v-loading="securityLoading" :data="securityList" style="width: 100%">
            <el-table-column prop="name" label="规则名称" min-width="180" />
            <el-table-column prop="detectionType" label="检测类型" min-width="120">
              <template #default="{ row }">
                <el-tag :type="row.detectionType === 5 ? 'info' : 'danger'">
                  {{ detectionTypeMap[row.detectionType] || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="severity" label="严重等级" min-width="100">
              <template #default="{ row }">
                <el-tag :type="(severityTypeMap[row.severity]) as any">{{ severityMap[row.severity] || '中' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="action" label="处置动作" min-width="100">
              <template #default="{ row }">
                {{ actionMap[row.action] || row.action }}
              </template>
            </el-table-column>
            <el-table-column prop="enabled" label="状态" min-width="80">
              <template #default="{ row }">
                <el-switch v-model="row.enabled" @change="handleSecurityEnabledChange(row)" />
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="140" fixed="right">
              <template #default="{ row }">
                <el-button type="primary" link @click="openSecurityForm(row)">编辑</el-button>
                <el-button type="danger" link @click="handleDeleteSecurity(row)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
          <div class="mt-4 flex justify-end">
            <el-pagination
              v-model:current-page="securityPageNo"
              v-model:page-size="securityPageSize"
              :total="securityTotal"
              :page-sizes="[10, 20, 50]"
              layout="total, sizes, prev, pager, next"
              @change="getSecurityList"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- 告警规则表单弹窗 -->
    <el-dialog v-model="alertFormVisible" :title="alertForm.id ? '编辑告警规则' : '新增告警规则'" width="600px">
      <el-form ref="alertFormRef" :model="alertForm" :rules="alertFormRules" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="alertForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="告警类型" prop="alertType">
          <el-select v-model="alertForm.alertType" placeholder="请选择告警类型">
            <el-option :label="1" :value="1">指标阈值</el-option>
            <el-option :label="2" :value="2">波动检测</el-option>
            <el-option :label="3" :value="3">异常检测</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="监控指标" prop="metricName">
          <el-input v-model="alertForm.metricName" placeholder="如 activeUsers, pageViews" />
        </el-form-item>
        <el-form-item label="条件运算符">
          <el-select v-model="alertForm.conditionOperator" placeholder="请选择">
            <el-option label="大于 (gt)" value="gt" />
            <el-option label="小于 (lt)" value="lt" />
            <el-option label="大于等于 (gte)" value="gte" />
            <el-option label="小于等于 (lte)" value="lte" />
            <el-option label="等于 (eq)" value="eq" />
            <el-option label="不等于 (neq)" value="neq" />
          </el-select>
        </el-form-item>
        <el-form-item label="阈值">
          <el-input v-model="alertForm.thresholdValue" placeholder="请输入阈值" />
        </el-form-item>
        <el-form-item label="持续时间(分)">
          <el-input-number v-model="alertForm.durationMinutes" :min="1" />
        </el-form-item>
        <el-form-item label="通知方式">
          <el-select v-model="alertForm.notificationType" placeholder="请选择通知方式">
            <el-option label="邮件" value="email" />
            <el-option label="钉钉" value="dingtalk" />
            <el-option label="企业微信" value="wechat" />
            <el-option label="Webhook" value="webhook" />
          </el-select>
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="alertForm.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="alertForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="alertFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAlertForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 安全检测规则表单弹窗 -->
    <el-dialog v-model="securityFormVisible" :title="securityForm.id ? '编辑安全规则' : '新增安全规则'" width="600px">
      <el-form ref="securityFormRef" :model="securityForm" :rules="securityFormRules" label-width="100px">
        <el-form-item label="规则名称" prop="name">
          <el-input v-model="securityForm.name" placeholder="请输入规则名称" />
        </el-form-item>
        <el-form-item label="检测类型" prop="detectionType">
          <el-select v-model="securityForm.detectionType" placeholder="请选择检测类型">
            <el-option :label="1" :value="1">SQL注入</el-option>
            <el-option :label="2" :value="2">XSS攻击</el-option>
            <el-option :label="3" :value="3">暴力破解</el-option>
            <el-option :label="4" :value="4">异常访问</el-option>
            <el-option :label="5" :value="5">自定义</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="严重等级" prop="severity">
          <el-select v-model="securityForm.severity" placeholder="请选择严重等级">
            <el-option :label="1" :value="1">低</el-option>
            <el-option :label="2" :value="2">中</el-option>
            <el-option :label="3" :value="3">高</el-option>
            <el-option :label="4" :value="4">严重</el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="处置动作">
          <el-select v-model="securityForm.action" placeholder="请选择处置动作">
            <el-option label="告警" value="alert" />
            <el-option label="阻断" value="block" />
            <el-option label="记录" value="log" />
          </el-select>
        </el-form-item>
        <el-form-item label="检测模式">
          <el-input v-model="securityForm.pattern" type="textarea" :rows="3" placeholder="JSON格式的检测模式配置" />
        </el-form-item>
        <el-form-item label="是否启用">
          <el-switch v-model="securityForm.enabled" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="securityForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="securityFormVisible = false">取消</el-button>
        <el-button type="primary" @click="submitSecurityForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getAlertRulePage, createAlertRule, updateAlertRule, deleteAlertRule, updateAlertRuleEnabled,
  getSecurityRulePage, createSecurityRule, updateSecurityRule, deleteSecurityRule, updateSecurityRuleEnabled
} from '@/api/ubax/app'
import type { AlertRuleRespVO, SecurityRuleRespVO } from '@/api/ubax/app'

defineOptions({ name: 'AlertConfig' })

// ===== 通用映射 =====
const alertTypeMap: Record<number, string> = { 1: '指标阈值', 2: '波动检测', 3: '异常检测' }
const notificationTypeMap: Record<string, string> = { email: '邮件', dingtalk: '钉钉', wechat: '企业微信', webhook: 'Webhook' }
const detectionTypeMap: Record<number, string> = { 1: 'SQL注入', 2: 'XSS攻击', 3: '暴力破解', 4: '异常访问', 5: '自定义' }
const severityMap: Record<number, string> = { 1: '低', 2: '中', 3: '高', 4: '严重' }
const severityTypeMap: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }
const actionMap: Record<string, string> = { alert: '告警', block: '阻断', log: '记录' }

const activeTab = ref('alert')
const message = useMessage()

// ===== 告警规则 =====
const alertLoading = ref(false)
const alertList = ref<AlertRuleRespVO[]>([])
const alertPageNo = ref(1)
const alertPageSize = ref(10)
const alertTotal = ref(0)

const alertFormVisible = ref(false)
const alertFormRef = ref()
const alertForm = ref<any>({})
const alertFormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  alertType: [{ required: true, message: '请选择告警类型', trigger: 'change' }]
}

/** 获取告警规则列表 */
const getAlertList = async () => {
  alertLoading.value = true
  try {
    const res = await getAlertRulePage({ pageNo: alertPageNo.value, pageSize: alertPageSize.value })
    const data = res as any
    alertList.value = data.list || []
    alertTotal.value = data.total || 0
  } catch (e) {
    console.error('获取告警规则失败', e)
  } finally {
    alertLoading.value = false
  }
}

/** 打开告警规则表单 */
const openAlertForm = (row?: AlertRuleRespVO) => {
  alertForm.value = row ? { ...row } : { enabled: true, alertType: 1 }
  alertFormVisible.value = true
}

/** 提交告警规则表单 */
const submitAlertForm = async () => {
  const valid = await alertFormRef.value?.validate()
  if (!valid) return
  try {
    if (alertForm.value.id) {
      await updateAlertRule(alertForm.value)
      message.success('更新成功')
    } else {
      await createAlertRule(alertForm.value)
      message.success('创建成功')
    }
    alertFormVisible.value = false
    getAlertList()
  } catch (e) {
    console.error('提交告警规则失败', e)
  }
}

/** 删除告警规则 */
const handleDeleteAlert = async (row: AlertRuleRespVO) => {
  try {
    await message.delConfirm()
    await deleteAlertRule(row.id)
    message.success('删除成功')
    getAlertList()
  } catch (e) {
    // 用户取消或删除失败
  }
}

/** 切换告警规则启用状态 */
const handleAlertEnabledChange = async (row: AlertRuleRespVO) => {
  try {
    await updateAlertRuleEnabled(row.id, row.enabled)
    message.success('状态更新成功')
  } catch (e) {
    row.enabled = !row.enabled
    console.error('更新状态失败', e)
  }
}

// ===== 安全检测规则 =====
const securityLoading = ref(false)
const securityList = ref<SecurityRuleRespVO[]>([])
const securityPageNo = ref(1)
const securityPageSize = ref(10)
const securityTotal = ref(0)

const securityFormVisible = ref(false)
const securityFormRef = ref()
const securityForm = ref<any>({})
const securityFormRules = {
  name: [{ required: true, message: '请输入规则名称', trigger: 'blur' }],
  detectionType: [{ required: true, message: '请选择检测类型', trigger: 'change' }],
  severity: [{ required: true, message: '请选择严重等级', trigger: 'change' }]
}

/** 获取安全检测规则列表 */
const getSecurityList = async () => {
  securityLoading.value = true
  try {
    const res = await getSecurityRulePage({ pageNo: securityPageNo.value, pageSize: securityPageSize.value })
    const data = res as any
    securityList.value = data.list || []
    securityTotal.value = data.total || 0
  } catch (e) {
    console.error('获取安全检测规则失败', e)
  } finally {
    securityLoading.value = false
  }
}

/** 打开安全检测规则表单 */
const openSecurityForm = (row?: SecurityRuleRespVO) => {
  securityForm.value = row ? { ...row } : { enabled: true, detectionType: 1, severity: 2, action: 'alert' }
  securityFormVisible.value = true
}

/** 提交安全检测规则表单 */
const submitSecurityForm = async () => {
  const valid = await securityFormRef.value?.validate()
  if (!valid) return
  try {
    if (securityForm.value.id) {
      await updateSecurityRule(securityForm.value)
      message.success('更新成功')
    } else {
      await createSecurityRule(securityForm.value)
      message.success('创建成功')
    }
    securityFormVisible.value = false
    getSecurityList()
  } catch (e) {
    console.error('提交安全检测规则失败', e)
  }
}

/** 删除安全检测规则 */
const handleDeleteSecurity = async (row: SecurityRuleRespVO) => {
  try {
    await message.delConfirm()
    await deleteSecurityRule(row.id)
    message.success('删除成功')
    getSecurityList()
  } catch (e) {
    // 用户取消或删除失败
  }
}

/** 切换安全检测规则启用状态 */
const handleSecurityEnabledChange = async (row: SecurityRuleRespVO) => {
  try {
    await updateSecurityRuleEnabled(row.id, row.enabled)
    message.success('状态更新成功')
  } catch (e) {
    row.enabled = !row.enabled
    console.error('更新状态失败', e)
  }
}

// ===== 初始化 =====
onMounted(() => {
  getAlertList()
  getSecurityList()
})
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
