import request from '@/config/axios'

export interface EventConfigRespVO {
  id: number
  configName: string
  configDesc?: string
  matchPosition: string
  matchPositionName?: string
  matchType: string
  matchTypeName?: string
  matchValue: string
  matchLogic?: string
  matchLogicName?: string
  eventType?: string
  eventLevel?: number
  eventLevelName?: string
  eventTitleTemplate?: string
  enabled: boolean
  sort: number
  remark?: string
  createTime: Date
}

export interface EventConfigPageReqVO extends PageParam {
  configName?: string
  matchPosition?: string
  matchType?: string
  eventType?: string
  eventLevel?: number
  enabled?: boolean
  createTime?: Date[]
}

export interface EventConfigSaveReqVO {
  id?: number
  configName: string
  configDesc?: string
  matchPosition: string
  matchType: string
  matchValue: string
  matchLogic?: string
  eventType?: string
  eventLevel?: number
  eventTitleTemplate?: string
  enabled?: boolean
  sort?: number
  remark?: string
}

export const createEventConfig = (data: EventConfigSaveReqVO) => {
  return request.post({ url: '/gather/event-config/create', data })
}

export const updateEventConfig = (data: EventConfigSaveReqVO) => {
  return request.put({ url: '/gather/event-config/update', data })
}

export const deleteEventConfig = (id: number) => {
  return request.delete({ url: '/gather/event-config/delete?id=' + id })
}

export const getEventConfig = (id: number) => {
  return request.get({ url: '/gather/event-config/get?id=' + id })
}

export const getEventConfigPage = (params: EventConfigPageReqVO) => {
  return request.get({ url: '/gather/event-config/page', params })
}

export const getEnabledEventConfigList = () => {
  return request.get({ url: '/gather/event-config/list-enabled' })
}

export const getEnabledEventConfigListByPosition = (matchPosition: string) => {
  return request.get({
    url: '/gather/event-config/list-by-position?matchPosition=' + matchPosition
  })
}

export const matchEvent = (position: string, content: string) => {
  return request.post({
    url: '/gather/event-config/match',
    params: { position, content }
  })
}
