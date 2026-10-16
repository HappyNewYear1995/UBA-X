import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../store/auth'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/Login.vue'), meta: { public: true } },
  {
    path: '/',
    component: () => import('../layouts/MainLayout.vue'),
    redirect: () => {
      const u = JSON.parse(localStorage.getItem('ubax_user') || 'null')
      // Platform-level super admins have no tenant, so land on system management.
      return u && u.tenantId == null ? '/users' : '/dashboard'
    },
    children: [
      { path: 'assistant', name: 'assistant', component: () => import('../views/Assistant.vue'), meta: { title: 'AI 数据助手', icon: 'ChatDotRound', perm: 'assistant:use', tenant: true } },
      { path: 'dashboard', name: 'dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '概览看板', icon: 'Odometer', perm: 'analytics:view', tenant: true } },
      { path: 'realtime', name: 'realtime', component: () => import('../views/Realtime.vue'), meta: { title: '实时分析', icon: 'Lightning', perm: 'analytics:view', tenant: true } },
      { path: 'events', name: 'events', component: () => import('../views/Events.vue'), meta: { title: '事件分析', icon: 'Aim', group: 'behavior', perm: 'analytics:view', tenant: true } },
      { path: 'pages', name: 'pages', component: () => import('../views/Pages.vue'), meta: { title: '页面分析', icon: 'Document', group: 'behavior', perm: 'analytics:view', tenant: true } },
      { path: 'funnel', name: 'funnel', component: () => import('../views/Funnel.vue'), meta: { title: '漏斗分析', icon: 'Filter', group: 'behavior', perm: 'analytics:view', tenant: true } },
      { path: 'retention', name: 'retention', component: () => import('../views/Retention.vue'), meta: { title: '留存分析', icon: 'RefreshRight', group: 'behavior', perm: 'analytics:view', tenant: true } },
      { path: 'profiles', name: 'profiles', component: () => import('../views/Profiles.vue'), meta: { title: '用户画像', icon: 'User', group: 'insight', perm: 'data:view', tenant: true } },
      { path: 'profiles/:distinctId', name: 'profileDetail', component: () => import('../views/ProfileDetail.vue'), meta: { hidden: true, group: 'insight', perm: 'data:view', tenant: true } },
      { path: 'heatmap', name: 'heatmap', component: () => import('../views/Heatmap.vue'), meta: { title: '点击热力图', icon: 'Sunny', group: 'insight', perm: 'data:view', tenant: true } },
      { path: 'sessions', name: 'sessions', component: () => import('../views/Sessions.vue'), meta: { title: '会话回放', icon: 'VideoPlay', group: 'insight', perm: 'data:view', tenant: true } },
      { path: 'apps', name: 'apps', component: () => import('../views/Apps.vue'), meta: { title: '应用与接入', icon: 'Grid', group: 'collect', perm: 'app:manage', tenant: true } },
      { path: 'agents', name: 'agents', component: () => import('../views/Agents.vue'), meta: { title: 'Agent 探针', icon: 'Cpu', group: 'collect', perm: 'agent:manage', tenant: true } },
      {
        path: 'ext',
        component: () => import('../views/ext/Hub.vue'),
        meta: { title: '数据应用', icon: 'DataAnalysis', perm: 'analytics:view', tenant: true },
        children: [
          { path: '', name: 'extHub', component: () => import('../views/ext/Gallery.vue'), meta: { title: '数据应用', perm: 'analytics:view', tenant: true } },
          { path: 'security', name: 'extSecurity', component: () => import('../views/ext/Security.vue'), meta: { title: '访问安全分析', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'geo', name: 'extGeo', component: () => import('../views/ext/Geo.vue'), meta: { title: '地域分析', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'fingerprint', name: 'extFingerprint', component: () => import('../views/ext/Fingerprint.vue'), meta: { title: '客户端指纹', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'conversion', name: 'extConversion', component: () => import('../views/ext/ConversionPath.vue'), meta: { title: '转化路径分析', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'funnel-compare', name: 'extFunnelCompare', component: () => import('../views/ext/FunnelCompare.vue'), meta: { title: '漏斗对比', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'retention', name: 'extRetention', component: () => import('../views/ext/RetentionSegments.vue'), meta: { title: '留存分群', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'churn', name: 'extChurn', component: () => import('../views/ext/ChurnHealth.vue'), meta: { title: '流失预警 / 健康分', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'rfm', name: 'extRfm', component: () => import('../views/ext/Rfm.vue'), meta: { title: 'RFM 价值分层', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'journey', name: 'extJourney', component: () => import('../views/ext/Journey.vue'), meta: { title: '用户旅程时间线', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'page-experience', name: 'extPageExperience', component: () => import('../views/ext/PageExperience.vue'), meta: { title: '页面性能与体验', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'anomaly', name: 'extAnomaly', component: () => import('../views/ext/Anomaly.vue'), meta: { title: '异常预警', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'explorer', name: 'extExplorer', component: () => import('../views/ext/EventExplorer.vue'), meta: { title: '事件探索', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'attribution', name: 'extAttribution', component: () => import('../views/ext/Attribution.vue'), meta: { title: '归因分析', hidden: true, perm: 'analytics:view', tenant: true } },
          { path: 'ab-test', name: 'extAbTest', component: () => import('../views/ext/AbTest.vue'), meta: { title: 'A/B 实验', hidden: true, perm: 'analytics:view', tenant: true } }
        ]
      },
      { path: 'users', name: 'users', component: () => import('../views/Users.vue'), meta: { title: '用户管理', icon: 'UserFilled', group: 'system', perm: 'user:manage' } },
      { path: 'roles', name: 'roles', component: () => import('../views/Roles.vue'), meta: { title: '角色权限', icon: 'Key', group: 'system', perm: 'role:manage' } },
      { path: 'config', name: 'sysConfig', component: () => import('../views/system/Config.vue'), meta: { title: '参数设置', icon: 'Tools', group: 'system', perm: 'system:manage' } },
      { path: 'dict', name: 'sysDict', component: () => import('../views/system/Dict.vue'), meta: { title: '数据字典', icon: 'Collection', group: 'system', perm: 'system:manage' } },
      { path: 'notice', name: 'sysNotice', component: () => import('../views/system/Notice.vue'), meta: { title: '通知公告', icon: 'Bell', group: 'system', perm: 'system:manage' } },
      { path: 'monitor', name: 'sysMonitor', component: () => import('../views/system/Monitor.vue'), meta: { title: '系统监控', icon: 'Monitor', group: 'system', perm: 'system:manage' } },
      { path: 'task', name: 'sysTask', component: () => import('../views/system/Task.vue'), meta: { title: '定时任务', icon: 'AlarmClock', group: 'system', perm: 'system:manage' } },
      { path: 'audit', name: 'sysAudit', component: () => import('../views/system/Audit.vue'), meta: { title: '操作日志', icon: 'Tickets', group: 'system', perm: 'system:manage' } },
      { path: 'login-log', name: 'sysLoginLog', component: () => import('../views/system/LoginLog.vue'), meta: { title: '登录日志', icon: 'Clock', group: 'system', perm: 'system:manage' } },
      { path: 'account', name: 'account', component: () => import('../views/system/Account.vue'), meta: { title: '个人中心', hidden: true } },
      { path: 'tenants', name: 'tenants', component: () => import('../views/Tenants.vue'), meta: { title: '租户管理', icon: 'OfficeBuilding', superAdmin: true, group: 'system', perm: 'tenant:manage' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: () => {
      const u = JSON.parse(localStorage.getItem('ubax_user') || 'null')
      return u && u.tenantId == null ? '/users' : '/dashboard'
    } }
]

const router = createRouter({ history: createWebHistory(), routes })

const homeFor = () => {
  const u = JSON.parse(localStorage.getItem('ubax_user') || 'null')
  return u && u.tenantId == null ? '/users' : '/dashboard'
}

// Preferred landing routes in order; the first the user can actually access wins.
const HOME_CANDIDATES = [
  { path: '/dashboard', perm: 'analytics:view', tenant: true },
  { path: '/realtime', perm: 'analytics:view', tenant: true },
  { path: '/profiles', perm: 'data:view', tenant: true },
  { path: '/apps', perm: 'app:manage', tenant: true },
  { path: '/users', perm: 'user:manage' },
  { path: '/roles', perm: 'role:manage' },
  { path: '/config', perm: 'system:manage' },
  { path: '/tenants', perm: 'tenant:manage' }
]
function accessibleHome(auth, superAdminNoTenant) {
  for (const c of HOME_CANDIDATES) {
    if (c.tenant && superAdminNoTenant) continue
    if (c.perm && !auth.has(c.perm)) continue
    return c.path
  }
  return null
}

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('ubax_token')
  if (to.meta.public) {
    if (token && to.name === 'login') return next(homeFor())
    return next()
  }
  if (!token) return next('/login')
  const auth = useAuthStore()
  const superAdminNoTenant = !!(auth.user && auth.user.tenantId == null)
  // Tenant-scoped pages need a tenant context; platform super admins have none.
  const denied =
    (to.meta.perm && !auth.has(to.meta.perm)) ||
    (to.meta.tenant && superAdminNoTenant)
  if (denied) {
    const fallback = accessibleHome(auth, superAdminNoTenant) || homeFor()
    // Prevent an infinite redirect loop when no accessible home exists,
    // or when the fallback is the very route we were denied.
    if (!fallback || fallback === to.path) return next(false)
    return next(fallback)
  }
  next()
})

export default router
