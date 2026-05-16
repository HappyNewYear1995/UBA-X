import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '数据仪表盘', icon: 'DataAnalysis' },
      },
      {
        path: 'collection',
        name: 'Collection',
        component: () => import('@/views/Collection/index.vue'),
        meta: { title: '数据采集', icon: 'Download' },
      },
      {
        path: 'modeling',
        name: 'Modeling',
        component: () => import('@/views/Modeling/index.vue'),
        meta: { title: '数据建模', icon: 'Connection' },
      },
      {
        path: 'analysis',
        name: 'Analysis',
        component: () => import('@/views/Analysis/index.vue'),
        meta: { title: '行为分析', icon: 'TrendCharts' },
      },
      {
        path: 'anomaly',
        name: 'Anomaly',
        component: () => import('@/views/Anomaly/index.vue'),
        meta: { title: '异常检测', icon: 'Warning' },
      },
      {
        path: 'ai-insights',
        name: 'AIInsights',
        component: () => import('@/views/AIInsights/index.vue'),
        meta: { title: 'AI 智能洞察', icon: 'Cpu' },
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings/index.vue'),
        meta: { title: '系统设置', icon: 'Setting' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile/index.vue'),
        meta: { title: '个人中心', icon: 'User' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
