import request from '@/config/axios'

export interface EventRespVO {
  id: number
  agentUuid: string
  eventType: string
  eventLevel: number
  eventLevelName?: string
  eventSource?: string
  eventSourceName?: string
  title: string
  content?: string
  tags?: string
  eventTime: Date
  handled: boolean
  handler?: string
  handleTime?: Date
  handleRemark?: string
  createTime: Date
}

export interface EventPageReqVO extends PageParam {
  agentUuid?: string
  eventType?: string
  eventLevel?: number
  eventSource?: string
  title?: string
  handled?: boolean
  eventTime?: Date[]
  createTime?: Date[]
}

export interface EventSaveReqVO {
  agentUuid: string
  eventType: string
  eventLevel: number
  eventSource?: string
  title: string
  content?: string
  tags?: string
  eventTime?: Date
}

export interface EventHandleReqVO {
  id: number
  handler?: string
  handleRemark?: string
}

export const createEvent = (data: EventSaveReqVO) => {
  return request.post({ url: '/gather/event/create', data })
}

export const handleEvent = (data: EventHandleReqVO) => {
  return request.put({ url: '/gather/event/handle', data })
}

export const getEvent = (id: number) => {
  return request.get({ url: '/gather/event/get?id=' + id })
}

export const getEventPage = (params: EventPageReqVO) => {
  return request.get({ url: '/gather/event/page', params })
}

export const getEventListByAgentUuid = (agentUuid: string) => {
  return request.get({ url: '/gather/event/list-by-agent?agentUuid=' + agentUuid })
}

export const countEventByAgentUuidAndLevel = (agentUuid: string, eventLevel: number) => {
  return request.get({
    url: '/gather/event/count-by-agent-and-level',
    params: { agentUuid, eventLevel }
  })
}

export const countEventByAgentUuidAndHandled = (agentUuid: string, handled: boolean) => {
  return request.get({
    url: '/gather/event/count-by-agent-and-handled',
    params: { agentUuid, handled }
  })
}
