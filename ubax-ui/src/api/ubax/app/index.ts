import request from '@/config/axios'

// ===== 告警规则 =====

export interface AlertRuleSaveReqVO {
  id?: number
  name: string
  alertType: number
  metricName?: string
  conditionOperator?: string
  thresholdValue?: string
  durationMinutes?: number
  notificationType?: string
  notificationConfig?: string
  enabled?: boolean
  remark?: string
}

export interface AlertRuleRespVO {
  id: number
  name: string
  alertType: number
  metricName?: string
  conditionOperator?: string
  thresholdValue?: string
  durationMinutes?: number
  notificationType?: string
  notificationConfig?: string
  enabled: boolean
  lastTriggeredTime?: Date
  triggerCount: number
  remark?: string
  createTime: Date
}

export interface AlertRulePageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  alertType?: number
  enabled?: boolean
  notificationType?: string
}

/** 创建告警规则 */
export const createAlertRule = (data: AlertRuleSaveReqVO) => {
  return request.post({ url: '/app/alert-rule/create', data })
}

/** 更新告警规则 */
export const updateAlertRule = (data: AlertRuleSaveReqVO) => {
  return request.put({ url: '/app/alert-rule/update', data })
}

/** 删除告警规则 */
export const deleteAlertRule = (id: number) => {
  return request.delete({ url: '/app/alert-rule/delete', params: { id } })
}

/** 获取告警规则详情 */
export const getAlertRule = (id: number) => {
  return request.get({ url: '/app/alert-rule/get', params: { id } })
}

/** 获取告警规则分页 */
export const getAlertRulePage = (params: AlertRulePageReqVO) => {
  return request.get({ url: '/app/alert-rule/page', params })
}

/** 更新告警规则启用状态 */
export const updateAlertRuleEnabled = (id: number, enabled: boolean) => {
  return request.put({ url: '/app/alert-rule/update-enabled', params: { id, enabled } })
}

// ===== 告警记录 =====

export interface AlertRecordRespVO {
  id: number
  ruleId: number
  ruleName?: string
  alertType?: number
  alertLevel: number
  metricValue?: string
  thresholdValue?: string
  message?: string
  notificationStatus: number
  acknowledged: boolean
  acknowledgedBy?: string
  acknowledgedTime?: Date
  remark?: string
  createTime: Date
}

export interface AlertRecordPageReqVO {
  pageNo: number
  pageSize: number
  ruleId?: number
  alertType?: number
  alertLevel?: number
  notificationStatus?: number
  acknowledged?: boolean
  createTime?: Date[]
}

export interface AlertRecordAckReqVO {
  id: number
  remark?: string
}

/** 获取告警记录详情 */
export const getAlertRecord = (id: number) => {
  return request.get({ url: '/app/alert-record/get', params: { id } })
}

/** 获取告警记录分页 */
export const getAlertRecordPage = (params: AlertRecordPageReqVO) => {
  return request.get({ url: '/app/alert-record/page', params })
}

/** 确认告警记录 */
export const ackAlertRecord = (data: AlertRecordAckReqVO) => {
  return request.put({ url: '/app/alert-record/ack', data })
}

// ===== 安全检测规则 =====

export interface SecurityRuleSaveReqVO {
  id?: number
  name: string
  detectionType: number
  pattern?: string
  severity: number
  action?: string
  enabled?: boolean
  remark?: string
}

export interface SecurityRuleRespVO {
  id: number
  name: string
  detectionType: number
  pattern?: string
  severity: number
  action?: string
  enabled: boolean
  triggerCount: number
  lastTriggeredTime?: Date
  remark?: string
  createTime: Date
}

export interface SecurityRulePageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  detectionType?: number
  severity?: number
  enabled?: boolean
}

/** 创建安全检测规则 */
export const createSecurityRule = (data: SecurityRuleSaveReqVO) => {
  return request.post({ url: '/app/security-rule/create', data })
}

/** 更新安全检测规则 */
export const updateSecurityRule = (data: SecurityRuleSaveReqVO) => {
  return request.put({ url: '/app/security-rule/update', data })
}

/** 删除安全检测规则 */
export const deleteSecurityRule = (id: number) => {
  return request.delete({ url: '/app/security-rule/delete', params: { id } })
}

/** 获取安全检测规则详情 */
export const getSecurityRule = (id: number) => {
  return request.get({ url: '/app/security-rule/get', params: { id } })
}

/** 获取安全检测规则分页 */
export const getSecurityRulePage = (params: SecurityRulePageReqVO) => {
  return request.get({ url: '/app/security-rule/page', params })
}

/** 更新安全检测规则启用状态 */
export const updateSecurityRuleEnabled = (id: number, enabled: boolean) => {
  return request.put({ url: '/app/security-rule/update-enabled', params: { id, enabled } })
}

// ===== 安全检测事件 =====

export interface SecurityEventRespVO {
  id: number
  ruleId?: number
  ruleName?: string
  detectionType?: number
  sourceIp?: string
  targetResource?: string
  eventDetail?: string
  severity: number
  actionTaken?: string
  handled: boolean
  handler?: string
  handleTime?: Date
  handleRemark?: string
  createTime: Date
}

export interface SecurityEventPageReqVO {
  pageNo: number
  pageSize: number
  ruleId?: number
  detectionType?: number
  severity?: number
  handled?: boolean
  sourceIp?: string
  createTime?: Date[]
}

export interface SecurityEventHandleReqVO {
  id: number
  handleRemark?: string
}

/** 获取安全检测事件详情 */
export const getSecurityEvent = (id: number) => {
  return request.get({ url: '/app/security-event/get', params: { id } })
}

/** 获取安全检测事件分页 */
export const getSecurityEventPage = (params: SecurityEventPageReqVO) => {
  return request.get({ url: '/app/security-event/page', params })
}

/** 处理安全检测事件 */
export const handleSecurityEvent = (data: SecurityEventHandleReqVO) => {
  return request.put({ url: '/app/security-event/handle', data })
}

// ===== 数据看板 =====

export interface DashboardSaveReqVO {
  id?: number
  name: string
  description?: string
  layoutConfig?: string
  enabled?: boolean
  remark?: string
}

export interface DashboardRespVO {
  id: number
  name: string
  description?: string
  layoutConfig?: string
  enabled: boolean
  remark?: string
  createTime: Date
}

export interface DashboardPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  enabled?: boolean
}

/** 创建数据看板 */
export const createDashboard = (data: DashboardSaveReqVO) => {
  return request.post({ url: '/app/dashboard/create', data })
}

/** 更新数据看板 */
export const updateDashboard = (data: DashboardSaveReqVO) => {
  return request.put({ url: '/app/dashboard/update', data })
}

/** 删除数据看板 */
export const deleteDashboard = (id: number) => {
  return request.delete({ url: '/app/dashboard/delete', params: { id } })
}

/** 获取数据看板详情 */
export const getDashboard = (id: number) => {
  return request.get({ url: '/app/dashboard/get', params: { id } })
}

/** 获取数据看板分页 */
export const getDashboardPage = (params: DashboardPageReqVO) => {
  return request.get({ url: '/app/dashboard/page', params })
}

// ===== 实时监控 =====

export interface RealtimeMetricsRespVO {
  activeUsers: number
  pageViews: number
  conversionRate: number
  anomalyEvents: number
}

export interface MetricsTrendReqVO {
  metricName: string
  startTime?: string
  endTime?: string
  interval?: string
}

export interface MetricsTrendRespVO {
  time: string
  value: number
}

/** 获取实时指标 */
export const getRealtimeMetrics = () => {
  return request.get({ url: '/app/monitor/realtime-metrics' })
}

/** 获取指标趋势 */
export const getMetricsTrend = (params: MetricsTrendReqVO) => {
  return request.get({ url: '/app/monitor/trend', params })
}
