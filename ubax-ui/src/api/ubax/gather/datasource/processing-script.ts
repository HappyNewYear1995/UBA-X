import request from '@/config/axios'

export interface ProcessingScriptRespVO {
  id: number
  name: string
  code: string
  scriptContent: string
  description?: string
  inputParams?: string
  resultTableName?: string
  resultFieldMapping?: string
  cronExpression?: string
  executeCount: number
  lastExecuteTime?: Date
  lastExecuteStatus?: number
  status: number
  remark?: string
  createTime: Date
}

export interface ProcessingScriptSaveReqVO {
  id?: number
  name: string
  code: string
  scriptContent: string
  description?: string
  inputParams?: string
  resultTableName?: string
  resultFieldMapping?: string
  cronExpression?: string
  status?: number
  remark?: string
}

export interface ProcessingScriptPageReqVO extends PageParam {
  name?: string
  code?: string
  status?: number
}

export interface ProcessingScriptExecuteReqVO {
  scriptId: number
  persistResult?: number
  inputParams?: Record<string, any>
}

export interface ProcessingScriptExecuteRespVO {
  success: boolean
  costTime?: number
  results?: Array<Record<string, any>>
  resultRecordCount?: number
  persisted?: boolean
  persistRecordCount?: number
  errorMessage?: string
}

export const getProcessingScriptPage = (params: ProcessingScriptPageReqVO) => {
  return request.get({ url: '/source/processing-script/page', params })
}

export const getProcessingScript = (id: number) => {
  return request.get({ url: '/source/processing-script/get', params: { id } })
}

export const createProcessingScript = (data: ProcessingScriptSaveReqVO) => {
  return request.post({ url: '/source/processing-script/create', data })
}

export const updateProcessingScript = (data: ProcessingScriptSaveReqVO) => {
  return request.put({ url: '/source/processing-script/update', data })
}

export const deleteProcessingScript = (id: number) => {
  return request.delete({ url: '/source/processing-script/delete', params: { id } })
}

export const executeProcessingScript = (data: ProcessingScriptExecuteReqVO) => {
  return request.post({ url: '/source/processing-script/execute', data })
}
