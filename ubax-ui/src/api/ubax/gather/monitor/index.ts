import request from '@/config/axios'

export interface MonitorMetricRespVO {
  id: number
  agentUuid: string
  metricType: string
  metricTypeName?: string
  metricName: string
  metricValue: number
  metricUnit?: string
  timeGranularity?: string
  timeGranularityName?: string
  startTime?: Date
  endTime?: Date
  metricTime: Date
  extraData?: string
  createTime: Date
}

export interface MonitorMetricPageReqVO extends PageParam {
  agentUuid?: string
  metricType?: string
  metricName?: string
  timeGranularity?: string
  metricTime?: Date[]
  createTime?: Date[]
}

export interface MonitorMetricSaveReqVO {
  agentUuid: string
  metricType: string
  metricName: string
  metricValue: number
  metricUnit?: string
  timeGranularity?: string
  startTime?: Date
  endTime?: Date
  metricTime?: Date
  extraData?: string
}

export interface MonitorStatisticsRespVO {
  agentUuid: string
  totalEventCount?: number
  successEventCount?: number
  failedEventCount?: number
  successRate?: number
  errorRate?: number
  avgLatency?: number
  throughput?: number
  bandwidthUsage?: number
  eventLevelCountMap?: Record<number, number>
}

export const createMetric = (data: MonitorMetricSaveReqVO) => {
  return request.post({ url: '/gather/monitor/metric/create', data })
}

export const getMetric = (id: number) => {
  return request.get({ url: '/gather/monitor/metric/get?id=' + id })
}

export const getMetricPage = (params: MonitorMetricPageReqVO) => {
  return request.get({ url: '/gather/monitor/metric/page', params })
}

export const getMetricListByAgentUuidAndType = (agentUuid: string, metricType: string) => {
  return request.get({
    url: '/gather/monitor/metric/list-by-agent-and-type',
    params: { agentUuid, metricType }
  })
}

export const getMetricListByAgentUuidAndTimeRange = (
  agentUuid: string,
  startTime: Date,
  endTime: Date
) => {
  return request.get({
    url: '/gather/monitor/metric/list-by-agent-and-time',
    params: { agentUuid, startTime, endTime }
  })
}

export const getAgentStatistics = (agentUuid: string, startTime: Date, endTime: Date) => {
  return request.get({
    url: '/gather/monitor/statistics',
    params: { agentUuid, startTime, endTime }
  })
}
