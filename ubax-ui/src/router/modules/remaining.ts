import {Layout} from '@/utils/routerHelper'

const { t } = useI18n()

/**
 * redirect: noredirect        当设置 noredirect 的时候该路由在面包屑导航中不可被点击
 * name:'router-name'          设定路由的名字，一定要填写不然使用<keep-alive>时会出现各种问题
 * meta : {
 hidden: true              当设置 true 的时候该路由不会再侧边栏出现 如404，login等页面(默认 false)

 alwaysShow: true          当你一个路由下面的 children 声明的路由大于1个时，自动会变成嵌套的模式，
 只有一个时，会将那个子路由当做根路由显示在侧边栏，
 若你想不管路由下面的 children 声明的个数都显示你的根路由，
 你可以设置 alwaysShow: true，这样它就会忽略之前定义的规则，
 一直显示根路由(默认 false)

 title: 'title'            设置该路由在侧边栏和面包屑中展示的名字

 icon: 'svg-name'          设置该路由的图标

 noCache: true             如果设置为true，则不会被 <keep-alive> 缓存(默认 false)

 breadcrumb: false         如果设置为false，则不会在breadcrumb面包屑中显示(默认 true)

 affix: true               如果设置为true，则会一直固定在tag项中(默认 false)

 noTagsView: true          如果设置为true，则不会出现在tag中(默认 false)

 activeMenu: '/dashboard'  显示高亮的路由路径

 followAuth: '/dashboard'  跟随哪个路由进行权限过滤

 canTo: true               设置为true即使hidden为true，也依然可以进行路由跳转(默认 false)
 }
 **/
const remainingRouter: AppRouteRecordRaw[] = [
  {
    path: '/redirect',
    component: Layout,
    name: 'Redirect',
    children: [
      {
        path: '/redirect/:path(.*)',
        name: 'Redirect',
        component: () => import('@/views/Redirect/Redirect.vue'),
        meta: {}
      }
    ],
    meta: {
      hidden: true,
      noTagsView: true
    }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/ubax/dashboard/metrics',
    name: 'Home',
    meta: {
      hidden: true
    },
    children: []
  },
  {
    path: '/index',
    component: Layout,
    redirect: '/ubax/dashboard/metrics',
    name: 'Index',
    meta: {
      hidden: true
    },
    children: []
  },
  {
    path: '/ubax/dashboard',
    component: Layout,
    redirect: '/ubax/dashboard/metrics',
    name: 'DataDashboard',
    meta: {
      title: '数据概览',
      icon: 'ep:monitor'
    },
    children: [
      {
        path: 'metrics',
        component: () => import('@/views/ubax/analysis/metrics/index.vue'),
        name: 'MetricsDashboard',
        meta: {
          title: '核心看板',
          icon: 'ep:data-board',
          noCache: false
        }
      },
      {
        path: 'alerts',
        component: () => import('@/views/ubax/dashboard/alerts/index.vue'),
        name: 'AlertsCenter',
        meta: {
          title: '告警中心',
          icon: 'ep:warning',
          noCache: false
        }
      }
    ]
  },
  {
    path: '/ubax/source',
    component: Layout,
    redirect: '/ubax/source/datasource',
    name: 'SourceManagement',
    meta: {
      title: '数据源管理',
      icon: 'ep:grid'
    },
    children: [
      {
        path: 'datasource',
        name: 'Datasource',
        meta: {
          title: '数据库',
          icon: 'ep:database',
          noCache: false
        },
        children: [
          {
            path: 'datasource',
            component: () => import('@/views/ubax/source/datasource/index.vue'),
            name: 'Datasource',
            meta: {
              title: '数据源管理',
              noCache: false
            }
          },
          {
            path: 'execution',
            component: () => import('@/views/ubax/source/datasource/execution.vue'),
            name: 'DatabaseExecute',
            meta: {
              title: 'SQL测试',
              noCache: false
            }
          },
          {
            path: 'script',
            component: () => import('@/views/ubax/source/datasource/script.vue'),
            name: 'DatabaseScript',
            meta: {
              title: '脚本管理',
              noCache: false
            }
          }
        ]
      },
      {
        path: 'webservice',
        name: 'WebService',
        meta: {
          title: 'WebService',
          icon: 'ep:connection',
          noCache: false
        },
        children: [
          {
            path: 'list',
            component: () => import('@/views/ubax/source/datasource/webservice.vue'),
            name: 'WebServiceList',
            meta: {
              title: '服务管理',
              noCache: false
            }
          }
        ]
      },
      {
        path: 'agent',
        name: 'Agent',
        meta: {
          title: '探针管理',
          icon: 'ep:menu',
          noCache: false
        },
        children: [
          {
            path: 'agent',
            component: () => import('@/views/ubax/source/agent/index.vue'),
            name: 'ClientList',
            meta: {
              title: 'Agent管理',
              icon: 'ep:menu',
              noCache: false
            }
          },
          {
            path: 'events',
            component: () => import('@/views/ubax/source/events/index.vue'),
            name: 'EventConfig',
            meta: {
              title: '事件管理',
              icon: 'ep:edit',
              noCache: false
            }
          },
          {
            path: 'monitor',
            component: () => import('@/views/ubax/source/monitor/index.vue'),
            name: 'AppMonitor',
            meta: {
              title: '运行监控',
              icon: 'ep:monitor',
              noCache: false
            }
          }
        ]
      }
    ]
  },
  {
    path: '/ubax/processing',
    component: Layout,
    redirect: '/ubax/processing/cleaning',
    name: 'DataProcessing',
    meta: {
      title: '数据处理',
      icon: 'ep:filter'
    },
    children: [
      {
        path: 'cleaning',
        component: () => import('@/views/ubax/processing/cleaning/index.vue'),
        name: 'CleaningPipeline',
        meta: {
          title: '清洗管道',
          icon: 'ep:magic-stick',
          noCache: false
        }
      },
      {
        path: 'dirty-data',
        component: () => import('@/views/ubax/processing/dirty-data/index.vue'),
        name: 'DirtyDataLog',
        meta: {
          title: '异常日志',
          icon: 'ep:document-remove',
          noCache: false
        }
      },
      {
        path: 'data-log',
        component: () => import('@/views/ubax/processing/data-log/index.vue'),
        name: 'DataLog',
        meta: {
          title: '数据日志',
          icon: 'ep:document',
          noCache: false
        }
      }
    ]
  },
  {
    path: '/ubax/analysis',
    component: Layout,
    redirect: '/ubax/analysis/funnel',
    name: 'DataAnalysis',
    meta: {
      title: '数据分析',
      icon: 'ep:trend-charts'
    },
    children: [
      {
        path: 'funnel',
        component: () => import('@/views/ubax/analysis/funnel/index.vue'),
        name: 'FunnelAnalysis',
        meta: {
          title: '漏斗分析',
          icon: 'ep:trend-charts',
          noCache: false
        }
      },
      {
        path: 'retention',
        component: () => import('@/views/ubax/analysis/retention/index.vue'),
        name: 'RetentionAnalysis',
        meta: {
          title: '留存分析',
          icon: 'ep:calendar',
          noCache: false
        }
      },
      {
        path: 'path',
        component: () => import('@/views/ubax/analysis/path/index.vue'),
        name: 'PathAnalysis',
        meta: {
          title: '路径分析',
          icon: 'ep:connection',
          noCache: false
        }
      }
    ]
  },
  {
    path: '/ubax/app',
    component: Layout,
    redirect: '/ubax/app/realtime',
    name: 'DataApp',
    meta: {
      title: '数据应用',
      icon: 'ep:cpu'
    },
    children: [
      {
        path: 'realtime',
        component: () => import('@/views/ubax/dashboard/realtime-monitor/index.vue'),
        name: 'RealtimeMonitor',
        meta: {
          title: '实时监控',
          icon: 'ep:monitor',
          noCache: false
        }
      },
      {
        path: 'alert-config',
        component: () => import('@/views/ubax/dashboard/alert-config/index.vue'),
        name: 'AlertConfig',
        meta: {
          title: '检测配置',
          icon: 'ep:setting',
          noCache: false
        }
      }
    ]
  },
  {
    path: '/user',
    component: Layout,
    name: 'UserInfo',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'profile',
        component: () => import('@/views/Profile/index.vue'),
        name: 'Profile',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:user',
          title: t('common.profile')
        }
      },
      {
        path: 'notify-message',
        component: () => import('@/views/system/notify/my/index.vue'),
        name: 'MyNotifyMessage',
        meta: {
          canTo: true,
          hidden: true,
          noTagsView: false,
          icon: 'ep:message',
          title: '我的站内信'
        }
      }
    ]
  },
  {
    path: '/dict',
    component: Layout,
    name: 'dict',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'type/data/:dictType',
        component: () => import('@/views/system/dict/data/index.vue'),
        name: 'SystemDictData',
        meta: {
          title: '字典数据',
          noCache: true,
          hidden: true,
          canTo: true,
          icon: '',
          activeMenu: '/system/dict'
        }
      }
    ]
  },
  {
    path: '/job',
    component: Layout,
    name: 'JobL',
    meta: {
      hidden: true
    },
    children: [
      {
        path: 'job-log',
        component: () => import('@/views/infra/job/logger/index.vue'),
        name: 'InfraJobLog',
        meta: {
          noCache: true,
          hidden: true,
          canTo: true,
          icon: 'ep:edit',
          title: '调度日志',
          activeMenu: 'infra/job/index'
        }
      }
    ]
  },
  {
    path: '/login',
    component: () => import('@/views/Login/Login.vue'),
    name: 'Login',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/sso',
    component: () => import('@/views/Login/Login.vue'),
    name: 'SSOLogin',
    meta: {
      hidden: true,
      title: t('router.login'),
      noTagsView: true
    }
  },
  {
    path: '/social-login',
    component: () => import('@/views/Login/SocialLogin.vue'),
    name: 'SocialLogin',
    meta: {
      hidden: true,
      title: t('router.socialLogin'),
      noTagsView: true
    }
  },
  {
    path: '/403',
    component: () => import('@/views/Error/403.vue'),
    name: 'NoAccess',
    meta: {
      hidden: true,
      title: '403',
      noTagsView: true
    }
  },
  {
    path: '/404',
    component: () => import('@/views/Error/404.vue'),
    name: 'NoFound',
    meta: {
      hidden: true,
      title: '404',
      noTagsView: true
    }
  },
  {
    path: '/500',
    component: () => import('@/views/Error/500.vue'),
    name: 'Error',
    meta: {
      hidden: true,
      title: '500',
      noTagsView: true
    }
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/Error/404.vue'),
    name: '',
    meta: {
      title: '404',
      hidden: true,
      breadcrumb: false
    }
  }
]

export default remainingRouter
