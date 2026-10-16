// Shared catalog for the 数据应用 (extension apps) hub.
// Grouped so both the gallery and the left in-app nav render consistently.
export const GROUPS = [
  {
    title: '安全风控',
    apps: [
      {
        path: '/ext/security', key: 'security', icon: 'Lock',
        color: '#f56c6c', grad: 'linear-gradient(135deg,#f56c6c,#e6a23c)',
        name: '访问安全分析',
        desc: '识别高频访问、爬虫 UA 与突发脚本会话等异常流量，评估整体访问风险',
        tags: ['风控', '异常检测']
      }
    ]
  },
  {
    title: '流量与转化',
    apps: [
      {
        path: '/ext/geo', key: 'geo', icon: 'MapLocation',
        color: '#2b5cff', grad: 'linear-gradient(135deg,#2b5cff,#00c2a8)',
        name: '地域分析',
        desc: '按地域 / 城市下钻访问量，观察地域结构、设备差异与每日趋势',
        tags: ['地域', '下钻']
      },
      {
        path: '/ext/conversion', key: 'conversion', icon: 'Guide',
        color: '#00a870', grad: 'linear-gradient(135deg,#00c2a8,#00a870)',
        name: '转化路径分析',
        desc: '还原会话内访问路径，统计高频路径与到目标的转化，桑基图呈现流向',
        tags: ['路径', '桑基图']
      },
      {
        path: '/ext/funnel-compare', key: 'funnelCompare', icon: 'Sort',
        color: '#e6a23c', grad: 'linear-gradient(135deg,#f0a020,#e6a23c)',
        name: '漏斗对比',
        desc: '按设备 / 渠道等维度对比转化漏斗，量化每一步的分组差异',
        tags: ['漏斗', '对比']
      }
    ]
  },
  {
    title: '用户与留存',
    apps: [
      {
        path: '/ext/fingerprint', key: 'fingerprint', icon: 'Postcard',
        color: '#6a3cff', grad: 'linear-gradient(135deg,#6a3cff,#2b5cff)',
        name: '客户端指纹',
        desc: '基于 设备 / 系统 / 浏览器 / 分辨率 组合识别客户端环境分布',
        tags: ['设备', '环境识别']
      },
      {
        path: '/ext/retention', key: 'retention', icon: 'Refresh',
        color: '#00c2a8', grad: 'linear-gradient(135deg,#00c2a8,#2b5cff)',
        name: '留存分群',
        desc: '按维度分群对比前后周期留存率，定位高留存与易流失人群',
        tags: ['留存', '分群']
      },
      {
        path: '/ext/churn', key: 'churn', icon: 'FirstAidKit',
        color: '#f56c6c', grad: 'linear-gradient(135deg,#ff7a7a,#f56c6c)',
        name: '流失预警 / 健康分',
        desc: '基于最近活跃与访问频次计算用户健康分，输出高流失风险名单',
        tags: ['流失', '健康分']
      },
      {
        path: '/ext/rfm', key: 'rfm', icon: 'Medal',
        color: '#b8860b', grad: 'linear-gradient(135deg,#e6a23c,#b8860b)',
        name: 'RFM 价值分层',
        desc: '按最近消费 / 频次 / 金额三维打分，自动划分用户价值客群',
        tags: ['RFM', '价值分层']
      },
      {
        path: '/ext/journey', key: 'journey', icon: 'Clock',
        color: '#2b5cff', grad: 'linear-gradient(135deg,#4b7bff,#2b5cff)',
        name: '用户旅程时间线',
        desc: '按会话还原单个用户的完整行为时间线，用于用户故事级排查',
        tags: ['旅程', '时间线']
      }
    ]
  },
  {
    title: '体验与监控',
    apps: [
      {
        path: '/ext/page-experience', key: 'pageExperience', icon: 'Monitor',
        color: '#00a0e9', grad: 'linear-gradient(135deg,#36c5f0,#00a0e9)',
        name: '页面性能与体验',
        desc: '统计各页面停留时长、跳出率与退出情况，定位体验薄弱环节',
        tags: ['体验', '停留']
      },
      {
        path: '/ext/anomaly', key: 'anomaly', icon: 'Bell',
        color: '#e6453d', grad: 'linear-gradient(135deg,#ff6b6b,#e6453d)',
        name: '异常预警',
        desc: '对 PV / UV / 事件量做基线学习，自动标记突增突降异常日',
        tags: ['监控', '异常检测']
      }
    ]
  },
  {
    title: '自助分析',
    apps: [
      {
        path: '/ext/explorer', key: 'explorer', icon: 'Search',
        color: '#6a3cff', grad: 'linear-gradient(135deg,#8b5cf6,#6a3cff)',
        name: '事件探索',
        desc: '自由选择事件 + 维度 + 指标即时出图，搭建个性化自助看板',
        tags: ['自助', '探索']
      }
    ]
  },
  {
    title: '增长与实验',
    apps: [
      {
        path: '/ext/attribution', key: 'attribution', icon: 'Share',
        color: '#00a870', grad: 'linear-gradient(135deg,#00c2a8,#2b5cff)',
        name: '归因分析',
        desc: '多触点渠道归因，对比首次 / 末次 / 线性 / 位置模型的贡献分配',
        tags: ['归因', '渠道']
      },
      {
        path: '/ext/ab-test', key: 'abTest', icon: 'DataLine',
        color: '#e6703c', grad: 'linear-gradient(135deg,#f0a020,#e6453d)',
        name: 'A/B 实验',
        desc: '分组对比转化率 / 客单价 / 活跃度，附显著性检验与结论建议',
        tags: ['实验', '显著性']
      }
    ]
  }
]

export const SOON = []

// Flat lookup of all navigable apps.
export const ALL_APPS = GROUPS.flatMap((g) => g.apps)
