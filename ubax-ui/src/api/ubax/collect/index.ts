import request from '@/config/axios'

// ===== 事件配置 =====

export interface EventConfigSaveReqVO {
  id?: number
  name: string
  code: string
  dataSourceIds?: number[]
  dataSourceType?: string
  eventType?: string
  properties?: string
  filterCondition?: string
  fieldMapping?: string
  status?: number
  remark?: string
}

export interface EventConfigRespVO {
  id: number
  name: string
  code: string
  dataSourceIds?: number[]
  dataSourceType?: string
  eventType?: string
  properties?: string
  filterCondition?: string
  fieldMapping?: string
  status: number
  remark?: string
  createTime: Date
}

export interface EventConfigPageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  code?: string
  eventType?: string
  status?: number
}

/** 创建事件配置 */
export const createEventConfig = (data: EventConfigSaveReqVO) => {
  return request.post({ url: '/processing/event-config/create', data })
}

/** 更新事件配置 */
export const updateEventConfig = (data: EventConfigSaveReqVO) => {
  return request.put({ url: '/processing/event-config/update', data })
}

/** 删除事件配置 */
export const deleteEventConfig = (id: number) => {
  return request.delete({ url: '/processing/event-config/delete', params: { id } })
}

/** 获取事件配置详情 */
export const getEventConfig = (id: number) => {
  return request.get({ url: '/processing/event-config/get', params: { id } })
}

/** 获取事件配置分页 */
export const getEventConfigPage = (params: EventConfigPageReqVO) => {
  return request.get({ url: '/processing/event-config/page', params })
}

/** 获取事件配置精简列表 */
export const getSimpleEventConfigList = () => {
  return request.get({ url: '/processing/event-config/list' })
}

// ===== 事件 =====

export interface EventSaveReqVO {
  eventId: string
  eventType: string
  eventCategory?: string
  sourceId?: string
  pipelineId?: number
  actorId?: string
  actorType?: string
  actorAttributes?: string
  eventTime?: Date
  ingestTime?: Date
  processTime?: Date
  eventTimeZone?: string
  location?: string
  action?: string
  targetResource?: string
  targetResourceType?: string
  result?: string
  severity?: string
  rawEvent?: string
  enrichments?: string
  tags?: string
  sessionId?: string
  correlationId?: string
  remark?: string
}

export interface EventRespVO {
  id: number
  eventId: string
  eventType: string
  eventCategory?: string
  sourceId?: string
  pipelineId?: number
  actorId?: string
  actorType?: string
  actorAttributes?: string
  eventTime: Date
  ingestTime?: Date
  processTime?: Date
  eventTimeZone?: string
  location?: string
  action?: string
  targetResource?: string
  targetResourceType?: string
  result?: string
  severity?: string
  rawEvent?: string
  enrichments?: string
  tags?: string
  sessionId?: string
  correlationId?: string
  remark?: string
  createTime: Date
}

export interface EventPageReqVO {
  pageNo: number
  pageSize: number
  eventType?: string
  eventCategory?: string
  actorType?: string
  actorId?: string
  action?: string
  result?: string
  severity?: string
  sourceId?: string
}

/** 创建事件 */
export const createEvent = (data: EventSaveReqVO) => {
  return request.post({ url: '/processing/event/create', data })
}

/** 删除事件 */
export const deleteEvent = (id: number) => {
  return request.delete({ url: '/processing/event/delete', params: { id } })
}

/** 获取事件详情 */
export const getEvent = (id: number) => {
  return request.get({ url: '/processing/event/get', params: { id } })
}

/** 获取事件分页 */
export const getEventPage = (params: EventPageReqVO) => {
  return request.get({ url: '/processing/event/page', params })
}

// ===== 清洗管道 =====

export interface CleanPipelineSaveReqVO {
  id?: number
  name: string
  type?: string
  description?: string
  config?: string
  eventId?: number
  status?: number
  sort?: number
  remark?: string
}

export interface CleanPipelineRespVO {
  id: number
  name: string
  type?: string
  description?: string
  config?: string
  eventId?: number
  status: number
  sort?: number
  processedCount?: number
  remark?: string
  updateTime: Date
  createTime: Date
}

export interface CleanPipelinePageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  type?: string
  status?: number
}

/** 创建清洗管道 */
export const createCleanPipeline = (data: CleanPipelineSaveReqVO) => {
  return request.post({ url: '/processing/clean-pipeline/create', data })
}

/** 更新清洗管道 */
export const updateCleanPipeline = (data: CleanPipelineSaveReqVO) => {
  return request.put({ url: '/processing/clean-pipeline/update', data })
}

/** 删除清洗管道 */
export const deleteCleanPipeline = (id: number) => {
  return request.delete({ url: '/processing/clean-pipeline/delete', params: { id } })
}

/** 获取清洗管道详情 */
export const getCleanPipeline = (id: number) => {
  return request.get({ url: '/processing/clean-pipeline/get', params: { id } })
}

/** 获取清洗管道分页 */
export const getCleanPipelinePage = (params: CleanPipelinePageReqVO) => {
  return request.get({ url: '/processing/clean-pipeline/page', params })
}

/** 获取清洗管道精简列表 */
export const getSimpleCleanPipelineList = () => {
  return request.get({ url: '/processing/clean-pipeline/list-all-simple' })
}

// ===== 异常日志 =====

export interface ErrorLogSaveReqVO {
  id?: number
  errorType?: string
  source?: string
  errorCount?: number
  action?: string
  status?: number
  detail?: string
  pipelineId?: number
  remark?: string
}

export interface ErrorLogRespVO {
  id: number
  errorType: string
  source: string
  errorCount: number
  action: string
  status: number
  detail?: string
  pipelineId?: number
  remark?: string
  createTime: Date
}

export interface ErrorLogPageReqVO {
  pageNo: number
  pageSize: number
  errorType?: string
  source?: string
  status?: number
  pipelineId?: number
}

/** 创建异常日志 */
export const createErrorLog = (data: ErrorLogSaveReqVO) => {
  return request.post({ url: '/processing/error-log/create', data })
}

/** 更新异常日志 */
export const updateErrorLog = (data: ErrorLogSaveReqVO) => {
  return request.put({ url: '/processing/error-log/update', data })
}

/** 删除异常日志 */
export const deleteErrorLog = (id: number) => {
  return request.delete({ url: '/processing/error-log/delete', params: { id } })
}

/** 获取异常日志详情 */
export const getErrorLog = (id: number) => {
  return request.get({ url: '/processing/error-log/get', params: { id } })
}

/** 获取异常日志分页 */
export const getErrorLogPage = (params: ErrorLogPageReqVO) => {
  return request.get({ url: '/processing/error-log/page', params })
}

// ===== 数据日志 =====

export interface DataLogSaveReqVO {
  id?: number
  logId?: string
  appId?: string
  eventType?: string
  deviceId?: string
  userId?: string
  platform?: string
  collectTime?: Date
  properties?: string
  remark?: string
}

export interface DataLogRespVO {
  id: number
  logId: string
  appId: string
  eventType: string
  deviceId: string
  userId: string
  platform: string
  collectTime: Date
  properties?: string
  remark?: string
  createTime: Date
}

export interface DataLogPageReqVO {
  pageNo: number
  pageSize: number
  eventType?: string
  appId?: string
  deviceId?: string
  userId?: string
}

/** 创建数据日志 */
export const createDataLog = (data: DataLogSaveReqVO) => {
  return request.post({ url: '/processing/data-log/create', data })
}

/** 删除数据日志 */
export const deleteDataLog = (id: number) => {
  return request.delete({ url: '/processing/data-log/delete', params: { id } })
}

/** 获取数据日志详情 */
export const getDataLog = (id: number) => {
  return request.get({ url: '/processing/data-log/get', params: { id } })
}

/** 获取数据日志分页 */
export const getDataLogPage = (params: DataLogPageReqVO) => {
  return request.get({ url: '/processing/data-log/page', params })
}
