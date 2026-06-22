import request from '@/config/axios'

// ===== 漏斗分析 =====

export interface FunnelAnalysisReqVO {
  steps: string[]
  startTime: string
  endTime: string
}

export interface FunnelStep {
  stepName: string
  users: number
  conversionRate: number
  overallRate: number
  lossRate: number
  avgTime: number
}

export interface FunnelAnalysisRespVO {
  steps: FunnelStep[]
  totalUsers: number
  finalConversionRate: number
  avgConversionRate: number
  avgDuration: number
}

/** 执行漏斗分析 */
export const analyzeFunnel = (data: FunnelAnalysisReqVO) => {
  return request.post({ url: '/analysis/funnel/analyze', data })
}

// ===== 留存分析 =====

export interface RetentionAnalysisReqVO {
  retentionType: string
  dimension?: string
  startTime: string
  endTime: string
}

export interface RetentionTrendItem {
  date: string
  nextDayRate: number
  day7Rate: number
  day30Rate: number
}

export interface RetentionRow {
  date: string
  newUsers: number
  retentionRates: Record<number, number>
}

export interface RetentionAnalysisRespVO {
  trend: RetentionTrendItem[]
  table: RetentionRow[]
}

/** 执行留存分析 */
export const analyzeRetention = (data: RetentionAnalysisReqVO) => {
  return request.post({ url: '/analysis/retention/analyze', data })
}

// ===== 路径分析 =====

export interface PathAnalysisReqVO {
  startEvent?: string
  startTime?: string
  endTime?: string
  maxDepth?: number
}

export interface SankeyNode {
  name: string
}

export interface SankeyLink {
  source: string
  target: string
  value: number
}

export interface PathStat {
  path: string
  users: number
  percentage: number
  avgDuration: number
}

export interface PathAnalysisRespVO {
  nodes: SankeyNode[]
  links: SankeyLink[]
  pathStats: PathStat[]
}

/** 执行路径分析 */
export const analyzePath = (data: PathAnalysisReqVO) => {
  return request.post({ url: '/analysis/path/analyze', data })
}

// ===== 漏斗分析配置 =====

export interface FunnelConfigSaveReqVO {
  id?: number
  name: string
  steps: string
  windowTime?: number
  remark?: string
}

export interface FunnelConfigRespVO {
  id: number
  name: string
  steps: string
  windowTime?: number
  remark?: string
  createTime: Date
}

export interface FunnelConfigPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
}

/** 获取漏斗配置分页 */
export const getFunnelConfigPage = (params: FunnelConfigPageReqVO) => {
  return request.get({ url: '/analysis/funnel-config/page', params })
}

/** 获取漏斗配置详情 */
export const getFunnelConfig = (id: number) => {
  return request.get({ url: '/analysis/funnel-config/get', params: { id } })
}

/** 创建漏斗配置 */
export const createFunnelConfig = (data: FunnelConfigSaveReqVO) => {
  return request.post({ url: '/analysis/funnel-config/create', data })
}

/** 更新漏斗配置 */
export const updateFunnelConfig = (data: FunnelConfigSaveReqVO) => {
  return request.put({ url: '/analysis/funnel-config/update', data })
}

/** 删除漏斗配置 */
export const deleteFunnelConfig = (id: number) => {
  return request.delete({ url: '/analysis/funnel-config/delete', params: { id } })
}

// ===== 留存分析配置 =====

export interface RetentionConfigSaveReqVO {
  id?: number
  name: string
  retentionType: string
  dimension?: string
  startEvent?: string
  returnEvent?: string
  remark?: string
}

export interface RetentionConfigRespVO {
  id: number
  name: string
  retentionType: string
  dimension?: string
  startEvent?: string
  returnEvent?: string
  remark?: string
  createTime: Date
}

export interface RetentionConfigPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  retentionType?: string
}

export interface RetentionResultRespVO {
  id: number
  configId: number
  statDate: string
  newUsers: number
  retentionUsers: Record<number, number>
  retentionRates: Record<number, number>
}

/** 获取留存配置分页 */
export const getRetentionConfigPage = (params: RetentionConfigPageReqVO) => {
  return request.get({ url: '/analysis/retention-config/page', params })
}

/** 获取留存配置详情 */
export const getRetentionConfig = (id: number) => {
  return request.get({ url: '/analysis/retention-config/get', params: { id } })
}

/** 创建留存配置 */
export const createRetentionConfig = (data: RetentionConfigSaveReqVO) => {
  return request.post({ url: '/analysis/retention-config/create', data })
}

/** 更新留存配置 */
export const updateRetentionConfig = (data: RetentionConfigSaveReqVO) => {
  return request.put({ url: '/analysis/retention-config/update', data })
}

/** 删除留存配置 */
export const deleteRetentionConfig = (id: number) => {
  return request.delete({ url: '/analysis/retention-config/delete', params: { id } })
}

/** 查询留存分析结果 */
export const getRetentionResults = (configId: number, startDate: string, endDate: string) => {
  return request.get({ url: '/analysis/retention-config/results', params: { configId, startDate, endDate } })
}

// ===== 漏斗分析结果 =====

export interface FunnelResultStepRespVO {
  stepName: string
  users: number
  conversionRate: number
  overallRate: number
  lossRate: number
  avgTime: number
}

export interface FunnelResultRespVO {
  id: number
  configId: number
  statDate: string
  totalUsers: number
  finalConversionRate: number
  avgConversionRate: number
  avgDuration: number
  steps: FunnelResultStepRespVO[]
}

/** 查询漏斗分析结果 */
export const getFunnelResults = (configId: number, startDate: string, endDate: string) => {
  return request.get({ url: '/analysis/funnel-config/results', params: { configId, startDate, endDate } })
}

// ===== 路径分析配置 =====

export interface PathConfigSaveReqVO {
  id?: number
  name: string
  startEvent?: string
  maxDepth?: number
  windowTime?: number
  remark?: string
}

export interface PathConfigRespVO {
  id: number
  name: string
  startEvent?: string
  maxDepth?: number
  windowTime?: number
  remark?: string
  createTime: Date
}

export interface PathConfigPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
}

export interface PathResultRespVO {
  id: number
  configId: number
  statDate: string
  nodes: SankeyNode[]
  links: SankeyLink[]
  pathStats: PathStat[]
}

/** 获取路径配置分页 */
export const getPathConfigPage = (params: PathConfigPageReqVO) => {
  return request.get({ url: '/analysis/path-config/page', params })
}

/** 获取路径配置详情 */
export const getPathConfig = (id: number) => {
  return request.get({ url: '/analysis/path-config/get', params: { id } })
}

/** 创建路径配置 */
export const createPathConfig = (data: PathConfigSaveReqVO) => {
  return request.post({ url: '/analysis/path-config/create', data })
}

/** 更新路径配置 */
export const updatePathConfig = (data: PathConfigSaveReqVO) => {
  return request.put({ url: '/analysis/path-config/update', data })
}

/** 删除路径配置 */
export const deletePathConfig = (id: number) => {
  return request.delete({ url: '/analysis/path-config/delete', params: { id } })
}

/** 查询路径分析结果 */
export const getPathResults = (configId: number, startDate: string, endDate: string) => {
  return request.get({ url: '/analysis/path-config/results', params: { configId, startDate, endDate } })
}
