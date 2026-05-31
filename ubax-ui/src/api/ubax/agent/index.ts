import request from '@/config/axios'

export interface PilotAgentVO {
  id: number
  name: string
  status: number
  version: string
  platform: string
  terminal: string
  events: number
  createTime: Date
}

export interface PilotAgentPageReqVO extends PageParam {
  name?: string
  status?: number
  platform?: string
  terminal?: string
}

export interface PilotCommandReqVO {
  id: number
  command: string
  params?: Record<string, any>
}

export const getPilotAgentPage = (params: PilotAgentPageReqVO) => {
  return request.get({ url: '/pilot/agent/page', params })
}

export const getPilotAgent = (id: number) => {
  return request.get({ url: '/pilot/agent/get?id=' + id })
}

export const pushPilotCommand = (data: PilotCommandReqVO) => {
  return request.post({ url: '/pilot/agent/push-comand', data })
}
