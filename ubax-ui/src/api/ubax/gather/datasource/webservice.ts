import request from '@/config/axios'

export interface WebServiceSourceRespVO {
  id: number
  name: string
  url: string
  method: string
  headers: string
  body: string
  authType: string
  authToken?: string
  soapNamespace: string
  soapAction: string
  responsePath: string
  syncInterval: number
  status: number
  remark: string
  createTime: Date
}

export interface WebServiceSourceSaveReqVO {
  id?: number
  name: string
  url: string
  method?: string
  headers?: string
  body?: string
  authType?: string
  authToken?: string
  soapNamespace?: string
  soapAction?: string
  responsePath?: string
  syncInterval?: number
  remark?: string
}

export interface WebServicePageReqVO {
  pageNo: number
  pageSize: number
  name?: string
  status?: number
}

/** 获取 WebService 数据源分页 */
export const getWebServiceSourcePage = (params: WebServicePageReqVO) => {
  return request.get({ url: '/source/webservice/page', params })
}

/** 获取 WebService 数据源详情 */
export const getWebServiceSource = (id: number) => {
  return request.get({ url: '/source/webservice/get', params: { id } })
}

/** 创建 WebService 数据源 */
export const createWebServiceSource = (data: WebServiceSourceSaveReqVO) => {
  return request.post({ url: '/source/webservice/create', data })
}

/** 更新 WebService 数据源 */
export const updateWebServiceSource = (data: WebServiceSourceSaveReqVO) => {
  return request.put({ url: '/source/webservice/update', data })
}

/** 删除 WebService 数据源 */
export const deleteWebServiceSource = (id: number) => {
  return request.delete({ url: '/source/webservice/delete', params: { id } })
}

/** 测试 WebService 连接 */
export const testWebServiceConnection = (id: number) => {
  return request.post({ url: '/source/webservice/test', params: { id } })
}

/** 执行 WebService 请求 */
export const executeWebService = (data: any) => {
  return request.post({ url: '/source/webservice/execute', data })
}
