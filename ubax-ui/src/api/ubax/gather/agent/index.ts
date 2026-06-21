import request from '@/config/axios'

export interface AgentRespVO {
  id: number
  uuid: string
  hostname: string
  version?: string
  terminal?: number
  platform?: number
  status?: number
  collectorStatus?: string
  online?: boolean
  lastHeartbeat: Date
  ip?: string
  os?: string
  config?: string
  remark?: string
  createTime: Date
}

export interface AgentPageReqVO extends PageParam {
  uuid?: string
  hostname?: string
  ip?: string
  terminal?: number
  platform?: number
  status?: number
  createTime?: Date[]
}

export interface AgentCommandReqVO {
  uuid: string
  action: string
}

export interface AgentUpdateReqVO {
  id: number
  platform?: number
  config?: string
  remark?: string
}

export const getAgentPage = (params: AgentPageReqVO) => {
  return request.get({url: '/gather/agent/page', params})
}

export const getAgent = (id: number) => {
  return request.get({url: '/gather/agent/get?id=' + id})
}

export const pushCommand = (data: AgentCommandReqVO) => {
  return request.post({url: '/gather/agent/push-command', data})
}

export const updateAgentStatus = (id: number, status: number) => {
  return request.put({url: '/gather/agent/update-status', params: {id, status}})
}

export const pushConfig = (uuid: string) => {
  return request.post({url: '/gather/agent/push-config', params: {uuid}})
}

export const updateAgent = (data: AgentUpdateReqVO) => {
  return request.put({url: '/gather/agent/update', data})
}
