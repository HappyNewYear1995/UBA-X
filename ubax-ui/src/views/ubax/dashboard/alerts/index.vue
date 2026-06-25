<template>
  <div class="alerts-center">
    <el-card shadow="never" class="dashboard-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">异常告警中心</span>
          <el-badge v-if="unackCount > 0" :value="unackCount" :max="99" type="danger" />
        </div>
      </template>

      <el-tabs v-model="activeTab">
        <!-- 告警记录 Tab -->
        <el-tab-pane label="告警记录" name="alert">
          <el-table v-loading="alertLoading" :data="alertList" style="width: 100%">
            <el-table-column prop="createTime" label="告警时间" min-width="170">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="ruleName" label="规则名称" min-width="150" />
            <el-table-column prop="message" label="告警内容" min-width="200" show-overflow-tooltip />
            <el-table-column prop="alertLevel" label="告警级别" min-width="100">
              <template #default="{ row }">
                <el-tag :type="(alertLevelTypeMap[row.alertLevel] || 'info') as any" size="small">
                  {{ alertLevelMap[row.alertLevel] || '未知' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="notificationStatus" label="通知状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.notificationStatus === 2 ? 'success' : row.notificationStatus === 1 ? 'warning' : 'info'" size="small">
                  {{ notifyStatusMap[row.notificationStatus] || '未发送' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="acknowledged" label="确认状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.acknowledged ? 'success' : 'danger'" size="small">
                  {{ row.acknowledged ? '已确认' : '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="!row.acknowledged" type="primary" link @click="handleAck(row)">确认</el-button>
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

        <!-- 安全事件 Tab -->
        <el-tab-pane label="安全事件" name="security">
          <el-table v-loading="securityLoading" :data="securityList" style="width: 100%">
            <el-table-column prop="createTime" label="发生时间" min-width="170">
              <template #default="{ row }">
                {{ formatDate(row.createTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="ruleName" label="规则名称" min-width="150" />
            <el-table-column prop="eventDetail" label="事件详情" min-width="200" show-overflow-tooltip />
            <el-table-column prop="severity" label="严重等级" min-width="100">
              <template #default="{ row }">
                <el-tag :type="(severityTypeMap[row.severity] || 'info') as any" size="small">
                  {{ severityMap[row.severity] || '中' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceIp" label="来源IP" min-width="130" />
            <el-table-column prop="actionTaken" label="处置动作" min-width="100">
              <template #default="{ row }">
                <el-tag size="small">{{ actionMap[row.actionTaken] || row.actionTaken || '告警' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="handled" label="处理状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.handled ? 'success' : 'danger'" size="small">
                  {{ row.handled ? '已处理' : '待处理' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="100" fixed="right">
              <template #default="{ row }">
                <el-button v-if="!row.handled" type="primary" link @click="openHandleDialog(row)">处理</el-button>
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

    <!-- 确认告警弹窗 -->
    <el-dialog v-model="ackDialogVisible" title="确认告警" width="400px">
      <el-form label-width="80px">
        <el-form-item label="备注">
          <el-input v-model="ackRemark" type="textarea" :rows="2" placeholder="请输入确认备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ackDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAck">确定</el-button>
      </template>
    </el-dialog>

    <!-- 处理安全事件弹窗 -->
    <el-dialog v-model="handleDialogVisible" title="处理安全事件" width="400px">
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="handleRemark" type="textarea" :rows="2" placeholder="请输入处理备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitHandle">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script lang="ts" setup>
import {
  getAlertRecordPage, ackAlertRecord,
  getSecurityEventPage, handleSecurityEvent
} from '@/api/ubax/app'
import type { AlertRecordRespVO, SecurityEventRespVO } from '@/api/ubax/app'

defineOptions({ name: 'AlertsCenter' })

const message = useMessage()

// ===== 映射 =====
const alertLevelMap: Record<number, string> = { 1: '提示', 2: '警告', 3: '重要', 4: '紧急' }
const alertLevelTypeMap: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }
const notifyStatusMap: Record<number, string> = { 0: '未发送', 1: '发送中', 2: '已发送', 3: '发送失败' }
const severityMap: Record<number, string> = { 1: '低', 2: '中', 3: '高', 4: '严重' }
const severityTypeMap: Record<number, string> = { 1: 'info', 2: 'warning', 3: 'danger', 4: 'danger' }
const actionMap: Record<string, string> = { alert: '告警', block: '阻断', log: '记录' }

const activeTab = ref('alert')
const unackCount = ref(0)

// ===== 告警记录 =====
const alertLoading = ref(false)
const alertList = ref<AlertRecordRespVO[]>([])
const alertPageNo = ref(1)
const alertPageSize = ref(10)
const alertTotal = ref(0)

const ackDialogVisible = ref(false)
const ackRecordId = ref(0)
const ackRemark = ref('')

/** 获取告警记录列表 */
const getAlertList = async () => {
  alertLoading.value = true
  try {
    const res = await getAlertRecordPage({ pageNo: alertPageNo.value, pageSize: alertPageSize.value })
    const data = res as any
    alertList.value = data.list || []
    alertTotal.value = data.total || 0
    // 统计未确认数量
    unackCount.value = alertList.value.filter((r: AlertRecordRespVO) => !r.acknowledged).length
  } catch (e) {
    console.error('获取告警记录失败', e)
  } finally {
    alertLoading.value = false
  }
}

/** 打开确认弹窗 */
const handleAck = (row: AlertRecordRespVO) => {
  ackRecordId.value = row.id
  ackRemark.value = ''
  ackDialogVisible.value = true
}

/** 提交确认 */
const submitAck = async () => {
  try {
    await ackAlertRecord({ id: ackRecordId.value, remark: ackRemark.value })
    message.success('确认成功')
    ackDialogVisible.value = false
    getAlertList()
  } catch (e) {
    console.error('确认告警失败', e)
  }
}

// ===== 安全事件 =====
const securityLoading = ref(false)
const securityList = ref<SecurityEventRespVO[]>([])
const securityPageNo = ref(1)
const securityPageSize = ref(10)
const securityTotal = ref(0)

const handleDialogVisible = ref(false)
const handleEventId = ref(0)
const handleRemark = ref('')

/** 获取安全事件列表 */
const getSecurityList = async () => {
  securityLoading.value = true
  try {
    const res = await getSecurityEventPage({ pageNo: securityPageNo.value, pageSize: securityPageSize.value })
    const data = res as any
    securityList.value = data.list || []
    securityTotal.value = data.total || 0
  } catch (e) {
    console.error('获取安全事件失败', e)
  } finally {
    securityLoading.value = false
  }
}

/** 打开处理弹窗 */
const openHandleDialog = (row: SecurityEventRespVO) => {
  handleEventId.value = row.id
  handleRemark.value = ''
  handleDialogVisible.value = true
}

/** 提交处理 */
const submitHandle = async () => {
  try {
    await handleSecurityEvent({ id: handleEventId.value, handleRemark: handleRemark.value })
    message.success('处理成功')
    handleDialogVisible.value = false
    getSecurityList()
  } catch (e) {
    console.error('处理安全事件失败', e)
  }
}

/** 格式化日期 */
const formatDate = (date: Date | string) => {
  if (!date) return ''
  const d = new Date(date)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

// ===== 初始化 =====
onMounted(() => {
  getAlertList()
  getSecurityList()
})
</script>

<style lang="scss" scoped>
@use '@/styles/variables.scss' as *;

.alerts-center {
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
