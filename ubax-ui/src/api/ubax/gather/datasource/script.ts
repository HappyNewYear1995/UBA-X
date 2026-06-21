import request from '@/config/axios'

export interface ScriptParamDef {
  name: string
  type: string
  required: boolean
  defaultValue?: any
  description?: string
}

export interface ScriptConfigRespVO {
  id: number
  name: string
  code: string
  databaseId: number
  dataSourceName?: string
  scriptType: string
  scriptTypeName?: string
  scriptContent: string
  description?: string
  resultTableName?: string
  resultFieldMapping?: string
  inputParams?: string
  outputParams?: string
  cronExpression?: string
  executeCount: number
  lastExecuteTime?: Date
  lastExecuteStatus?: number
  status: number
  remark?: string
  createTime: Date
}

export interface ScriptConfigSaveReqVO {
  id?: number
  name: string
  code: string
  databaseId: number
  scriptType: string
  scriptContent: string
  description?: string
  resultTableName?: string
  resultFieldMapping?: string
  inputParams?: string
  outputParams?: string
  cronExpression?: string
  status?: number
  remark?: string
}

export interface ScriptConfigPageReqVO extends PageParam {
  name?: string
  code?: string
  databaseId?: number
  scriptType?: string
  status?: number
}

export interface ScriptExecuteReqVO {
  scriptId: number
  persistResult?: number
  inputParams?: Record<string, any>
}

export interface ScriptExecuteRespVO {
  success: boolean
  costTime?: number
  results?: Array<Record<string, any>>
  affectedRows?: number
  outputParams?: Record<string, any>
  errorMessage?: string
  persisted?: boolean
  persistRecordCount?: number
}

export interface ScriptExecutionLogRespVO {
  id: number
  scriptId: number
  scriptName: string
  scriptCode: string
  databaseId: number
  executeType: string
  scriptContent?: string
  executeResult?: string
  affectedRows?: number
  outputParams?: string
  inputParams?: string
  status: number
  errorMessage?: string
  costTime?: number
  resultRecordCount?: number
  persisted: number
  persistError?: string
  createTime: Date
}

export interface ScriptExecutionLogPageReqVO extends PageParam {
  scriptId?: number
  databaseId?: number
  status?: number
  executeType?: string
  beginTime?: Date
  endTime?: Date
}

/** 创建脚本 */
export const createScriptConfig = (data: ScriptConfigSaveReqVO) => {
  return request.post({ url: '/source/database-script/create', data })
}

/** 更新脚本 */
export const updateScriptConfig = (data: ScriptConfigSaveReqVO) => {
  return request.put({ url: '/source/database-script/update', data })
}

/** 删除脚本 */
export const deleteScriptConfig = (id: number) => {
  return request.delete({ url: '/source/database-script/delete', params: { id } })
}

/** 获取脚本详情 */
export const getScriptConfig = (id: number) => {
  return request.get({ url: '/source/database-script/get', params: { id } })
}

/** 获取脚本分页 */
export const getScriptConfigPage = (params: ScriptConfigPageReqVO) => {
  return request.get({ url: '/source/database-script/page', params })
}

/** 执行脚本 */
export const executeScript = (data: ScriptExecuteReqVO) => {
  return request.post({ url: '/source/database-script/execute', data })
}

/** 获取执行日志分页 */
export const getExecutionLogPage = (params: ScriptExecutionLogPageReqVO) => {
  return request.get({ url: '/source/database-script/log/page', params })
}

/** 获取执行日志详情 */
export const getExecutionLog = (id: number) => {
  return request.get({ url: '/source/database-script/log/get', params: { id } })
}

/** 删除执行日志 */
export const deleteExecutionLog = (id: number) => {
  return request.delete({ url: '/source/database-script/log/delete', params: { id } })
}
