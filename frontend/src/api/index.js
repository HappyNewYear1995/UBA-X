import http from './http'

export const authApi = {
  login: (data) => http.post('/auth/login', data),
  me: () => http.get('/auth/me')
}

export const analyticsApi = {
  overview: (params) => http.get('/analytics/overview', { params }),
  trend: (params) => http.get('/analytics/trend', { params }),
  realtime: (params, cfg) => http.get('/analytics/realtime', { params, ...cfg }),
  live: (params, cfg) => http.get('/analytics/live', { params, ...cfg }),
  events: (params) => http.get('/analytics/events', { params }),
  pages: (params) => http.get('/analytics/pages', { params }),
  distributions: (params) => http.get('/analytics/distributions', { params }),
  funnel: (data) => http.post('/analytics/funnel', data),
  retention: (params) => http.get('/analytics/retention', { params }),
  heatmap: (params) => http.get('/analytics/heatmap', { params }),
  sessions: (params) => http.get('/analytics/sessions', { params }),
  sessionDetail: (id) => http.get('/analytics/sessions/' + encodeURIComponent(id)),
  streamMetrics: (cfg) => http.get('/analytics/stream-metrics', { ...cfg })
}

export const appApi = {
  list: () => http.get('/apps'),
  get: (id) => http.get('/apps/' + id),
  create: (data) => http.post('/apps', data),
  update: (id, data) => http.put('/apps/' + id, data),
  remove: (id) => http.delete('/apps/' + id),
  rotate: (id) => http.post('/apps/' + id + '/rotate-key'),
  sdk: (id, server) => http.get('/apps/' + id + '/sdk', { params: { server } })
}

export const profileApi = {
  list: (params) => http.get('/profiles', { params }),
  stats: () => http.get('/profiles/stats'),
  detail: (distinctId) => http.get('/profiles/' + encodeURIComponent(distinctId)),
  insight: (distinctId) => http.get('/profiles/' + encodeURIComponent(distinctId) + '/insight')
}

export const pilotApi = {
  overview: (cfg) => http.get('/pilot/manage/overview', { ...cfg }),
  agents: (cfg) => http.get('/pilot/manage/agents', { ...cfg }),
  agent: (id) => http.get('/pilot/manage/agents/' + id),
  create: (data) => http.post('/pilot/manage/agents', data),
  updateConfig: (id, config) => http.put('/pilot/manage/agents/' + id + '/config', config),
  command: (id, data) => http.post('/pilot/manage/agents/' + id + '/command', data),
  commands: (id) => http.get('/pilot/manage/agents/' + id + '/commands'),
  remove: (id) => http.delete('/pilot/manage/agents/' + id)
}

export const tenantApi = {
  list: () => http.get('/tenants'),
  current: () => http.get('/tenants/current'),
  create: (data) => http.post('/tenants', data),
  update: (id, data) => http.put('/tenants/' + id, data)
}

export const userApi = {
  list: () => http.get('/users'),
  create: (data) => http.post('/users', data),
  update: (id, data) => http.put('/users/' + id, data),
  resetPassword: (id, data) => http.post('/users/' + id + '/reset-password', data),
  remove: (id) => http.delete('/users/' + id)
}

export const roleApi = {
  list: () => http.get('/roles'),
  permissions: () => http.get('/roles/permissions'),
  create: (data) => http.post('/roles', data),
  update: (code, data) => http.put('/roles/' + encodeURIComponent(code), data),
  remove: (code) => http.delete('/roles/' + encodeURIComponent(code))
}

export const assistantApi = {
  chat: (data) => http.post('/assistant/chat', data),
  suggest: () => http.get('/assistant/suggest')
}

export const extApi = {
  security: (params) => http.get('/ext/security', { params }),
  geo: (params) => http.get('/ext/geo', { params }),
  fingerprint: (params) => http.get('/ext/fingerprint', { params }),
  conversionPath: (params) => http.get('/ext/conversion-path', { params }),
  funnelCompare: (params) => http.get('/ext/funnel-compare', { params }),
  retentionSegments: (params) => http.get('/ext/retention-segments', { params }),
  churnHealth: (params) => http.get('/ext/churn-health', { params }),
  rfm: (params) => http.get('/ext/rfm', { params }),
  journeyUsers: (params) => http.get('/ext/journey-users', { params }),
  journey: (params) => http.get('/ext/journey', { params }),
  anomaly: (params) => http.get('/ext/anomaly', { params }),
  pageExperience: (params) => http.get('/ext/page-experience', { params }),
  eventMeta: (params) => http.get('/ext/event-meta', { params }),
  eventExplore: (params) => http.get('/ext/event-explore', { params }),
  attribution: (params) => http.get('/ext/attribution', { params }),
  abTest: (params) => http.get('/ext/ab-test', { params })
}

export const systemApi = {
  audit: (params) => http.get('/system/audit', { params }),
  auditModules: () => http.get('/system/audit/modules'),
  loginLog: (params) => http.get('/system/login-log', { params }),
  config: () => http.get('/system/config'),
  saveConfig: (edits) => http.put('/system/config', edits),
  dictTypes: () => http.get('/system/dict/types'),
  dict: (type) => http.get('/system/dict', { params: { type } }),
  saveDict: (data) => http.post('/system/dict', data),
  removeDict: (id) => http.delete('/system/dict/' + id),
  notices: () => http.get('/system/notice'),
  publishedNotices: () => http.get('/system/notice/published'),
  saveNotice: (data) => http.post('/system/notice', data),
  removeNotice: (id) => http.delete('/system/notice/' + id),
  monitor: () => http.get('/system/monitor'),
  tasks: () => http.get('/system/task'),
  taskLogs: (params) => http.get('/system/task/logs', { params }),
  toggleTask: (id, enabled) => http.put('/system/task/' + id + '/toggle', null, { params: { enabled } }),
  runTask: (id) => http.post('/system/task/' + id + '/run')
}

export const accountApi = {
  me: () => http.get('/account/me'),
  updateProfile: (data) => http.put('/account/profile', data),
  changePassword: (data) => http.put('/account/password', data),
  logins: (params) => http.get('/account/logins', { params }),
  logout: () => http.post('/auth/logout')
}

export const governanceApi = {
  events: (params) => http.get('/governance/events', { params }),
  createEvent: (data) => http.post('/governance/events', data),
  updateEvent: (id, data) => http.put('/governance/events/' + id, data),
  toggleEvent: (id, enabled) => http.patch('/governance/events/' + id + '/toggle', { enabled }),
  removeEvent: (id) => http.delete('/governance/events/' + id),
  rules: (params) => http.get('/governance/rules', { params }),
  createRule: (data) => http.post('/governance/rules', data),
  updateRule: (id, data) => http.put('/governance/rules/' + id, data),
  toggleRule: (id, enabled) => http.patch('/governance/rules/' + id + '/toggle', { enabled }),
  removeRule: (id) => http.delete('/governance/rules/' + id),
  identity: (params) => http.get('/governance/identity', { params }),
  saveIdentity: (data) => http.post('/governance/identity', data),
  removeIdentity: (id) => http.delete('/governance/identity/' + id),
  alias: (params) => http.get('/governance/alias', { params })
}