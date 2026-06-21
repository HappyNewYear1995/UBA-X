import request from '@/config/axios'

export interface DatabaseSourceRespVO {
  id: number
  name: string
  dbType: string
  dbTypeName?: string
  host: string
  port: number
  database: string
  username: string
  password?: string
  url?: string
  protocol?: string
  sslCertPath?: string
  sslKeyPath?: string
  sslCaPath?: string
  passwordEncryptType?: string
  connectionParams?: string
  maxPoolSize?: number
  connectionTimeout?: number
  status: number
  remark?: string
  createTime: Date
}

export interface DatabaseSourceSaveReqVO {
  id?: number
  name: string
  dbType: string
  host: string
  port: number
  database: string
  username: string
  password: string
  url?: string
  protocol?: string
  sslCertPath?: string
  sslKeyPath?: string
  sslCaPath?: string
  passwordEncryptType?: string
  connectionParams?: string
  maxPoolSize?: number
  connectionTimeout?: number
  status?: number
  remark?: string
}

export interface DatabaseSourcePageReqVO extends PageParam {
  name?: string
  dbType?: string
  status?: number
}

export interface SqlExecuteReqVO {
  databaseId: number
  sql: string
}

export interface SqlExecuteRespVO {
  success: boolean
  costTime?: number
  results?: Array<Record<string, any>>
  affectedRows?: number
  outputParams?: Record<string, any>
  errorMessage?: string
}

export interface ProcedureReqVO {
  databaseId: number
  procedureName: string
  inputParams?: any[]
  outputParamNames?: string[]
}

export interface DatabaseTestReqVO {
  id?: number
  dbType?: string
  host?: string
  port?: number
  database?: string
  username?: string
  password?: string
  protocol?: string
  sslCertPath?: string
  sslKeyPath?: string
  sslCaPath?: string
  connectionParams?: string
}

/** 获取数据库数据源分页 */
export const getDatabaseSourcePage = (params: DatabaseSourcePageReqVO) => {
  return request.get({ url: '/source/database/page', params })
}

/** 获取数据库数据源详情 */
export const getDatabaseSource = (id: number) => {
  return request.get({ url: '/source/database/get', params: { id } })
}

/** 创建数据库数据源 */
export const createDatabaseSource = (data: DatabaseSourceSaveReqVO) => {
  return request.post({ url: '/source/database/create', data })
}

/** 更新数据库数据源 */
export const updateDatabaseSource = (data: DatabaseSourceSaveReqVO) => {
  return request.put({ url: '/source/database/update', data })
}

/** 删除数据库数据源 */
export const deleteDatabaseSource = (id: number) => {
  return request.delete({ url: '/source/database/delete', params: { id } })
}

/** 测试数据源连接（通过 ID 或连接参数） */
export const testDatabaseSourceConnection = (data: DatabaseTestReqVO) => {
  return request.post({ url: '/source/database/test', data })
}

/** 执行 SQL 语句 */
export const executeSql = (data: SqlExecuteReqVO) => {
  return request.post({ url: '/source/database/sql/execute', data })
}

/** 批量执行 SQL */
export const executeBatchSql = (databaseId: number, sqlList: string[]) => {
  return request.post({ url: '/source/database/sql/execute-batch', params: { databaseId: databaseId }, data: sqlList })
}

/** 执行存储过程 */
export const executeProcedure = (data: ProcedureReqVO) => {
  return request.post({ url: '/source/database/sql/execute-procedure', data })
}

/** 执行视图查询 */
export const executeViewQuery = (databaseId: number, viewName: string) => {
  return request.post({ url: '/source/database/sql/execute-view', params: { databaseId: databaseId, viewName } })
}
