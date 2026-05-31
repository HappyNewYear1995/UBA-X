<template>
  <div class="brand-page">
    <!-- Hero Section -->
    <section class="hero-section">
      <div class="hero-content">
        <div class="hero-badge">
          <Icon icon="ep:star" :size="16" />
          <span>新一代用户行为分析平台</span>
        </div>
        <h1 class="hero-title">
          <span class="brand-name">UBA-X</span>
          <span class="hero-subtitle">用户行为分析引擎</span>
        </h1>
        <p class="hero-desc">
          基于 AI 驱动的全链路用户行为分析平台，帮助企业深度洞察用户行为、优化产品体验、提升业务转化。
        </p>
        <div class="hero-actions">
          <el-button type="primary" size="large" @click="handleStart">
            <Icon icon="ep:rocket" /> 立即开始
          </el-button>
          <el-button size="large" @click="handleLearnMore">
            <Icon icon="ep:document" /> 了解更多
          </el-button>
        </div>
      </div>
      <div class="hero-visual">
        <div class="visual-card">
          <div class="visual-header">
            <div class="dot red"></div>
            <div class="dot yellow"></div>
            <div class="dot green"></div>
          </div>
          <div class="visual-body">
            <div class="chart-placeholder">
              <Echart :options="heroChartOptions" :height="280" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Core Features -->
    <section class="features-section">
      <div class="section-header">
        <h2 class="section-title">核心能力</h2>
        <p class="section-desc">六大核心模块，覆盖用户行为分析全链路</p>
      </div>
      <el-row :gutter="24">
        <el-col v-for="feature in features" :key="feature.title" :xs="12" :sm="8" :md="8" :lg="4" :xl="4">
          <div class="feature-card" @click="handleFeatureClick(feature)">
            <div class="feature-icon" :style="{ background: feature.iconBg }">
              <Icon :icon="feature.icon" :size="28" />
            </div>
            <h3 class="feature-title">{{ feature.title }}</h3>
            <p class="feature-desc">{{ feature.desc }}</p>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- Data Highlights -->
    <section class="highlights-section">
      <div class="section-header">
        <h2 class="section-title">数据亮点</h2>
        <p class="section-desc">用数据说话，见证增长</p>
      </div>
      <el-row :gutter="24">
        <el-col v-for="stat in stats" :key="stat.label" :xs="12" :sm="6" :md="6" :lg="6" :xl="6">
          <div class="stat-card">
            <div class="stat-value">
              <CountTo :start-val="0" :end-val="stat.value" :duration="2000" :suffix="stat.suffix" />
            </div>
            <div class="stat-label">{{ stat.label }}</div>
            <div class="stat-sub">{{ stat.sub }}</div>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- Product Modules -->
    <section class="modules-section">
      <div class="section-header">
        <h2 class="section-title">产品模块</h2>
        <p class="section-desc">模块化设计，灵活组合满足多样化需求</p>
      </div>
      <el-row :gutter="24">
        <el-col v-for="module in modules" :key="module.title" :xs="24" :sm="12" :md="12" :lg="8" :xl="8">
          <div class="module-card">
            <div class="module-header">
              <div class="module-icon" :style="{ background: module.iconBg }">
                <Icon :icon="module.icon" :size="24" />
              </div>
              <h3 class="module-title">{{ module.title }}</h3>
            </div>
            <ul class="module-list">
              <li v-for="item in module.items" :key="item">
                <Icon icon="ep:check" :size="14" />
                <span>{{ item }}</span>
              </li>
            </ul>
          </div>
        </el-col>
      </el-row>
    </section>

    <!-- Footer -->
    <section class="brand-footer">
      <div class="footer-content">
        <div class="footer-brand">
          <span class="footer-logo">UBA-X</span>
          <p class="footer-desc">新一代用户行为分析平台</p>
        </div>
        <div class="footer-links">
          <div class="footer-col">
            <h4>产品</h4>
            <a href="#">数据概览</a>
            <a href="#">应用管理</a>
            <a href="#">数据分析</a>
          </div>
          <div class="footer-col">
            <h4>支持</h4>
            <a href="#">接入文档</a>
            <a href="#">SDK 下载</a>
            <a href="#">常见问题</a>
          </div>
          <div class="footer-col">
            <h4>关于</h4>
            <a href="#">关于我们</a>
            <a href="#">隐私政策</a>
            <a href="#">服务条款</a>
          </div>
        </div>
      </div>
      <div class="footer-bottom">
        <span>© 2026 UBA-X. All rights reserved.</span>
      </div>
    </section>
  </div>
</template>

<script lang="ts" setup>
import { EChartsOption } from 'echarts'

defineOptions({ name: 'BrandPage' })

const heroChartOptions = reactive<EChartsOption>({
  grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['1月', '2月', '3月', '4月', '5月', '6月'],
    axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
    axisLabel: { color: 'rgba(255,255,255,0.5)' }
  },
  yAxis: {
    type: 'value',
    splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
    axisLabel: { color: 'rgba(255,255,255,0.5)' }
  },
  series: [
    {
      type: 'line',
      smooth: true,
      areaStyle: {
        color: {
          type: 'linear',
          x: 0, y: 0, x2: 0, y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(102, 126, 234, 0.4)' },
            { offset: 1, color: 'rgba(102, 126, 234, 0)' }
          ]
        }
      },
      itemStyle: { color: '#667eea' },
      data: [820, 932, 1201, 1434, 1890, 2346]
    }
  ]
})

const features = ref([
  {
    title: '数据概览',
    desc: '核心指标实时看板',
    icon: 'ep:monitor',
    iconBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    path: '/ubax/dashboard'
  },
  {
    title: '采集管理',
    desc: '多端接入统一管理',
    icon: 'ep:grid',
    iconBg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    path: '/ubax/gather'
  },
  {
    title: '数据处理',
    desc: '智能清洗标准化',
    icon: 'ep:filter',
    iconBg: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    path: '/ubax/processing'
  },
  {
    title: '数据分析',
    desc: '漏斗留存路径分析',
    icon: 'ep:trend-charts',
    iconBg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    path: '/ubax/analysis'
  },
  {
    title: '数据应用',
    desc: '智能异常检测预警',
    icon: 'ep:cpu',
    iconBg: 'linear-gradient(135deg, #fa709a 0%, #fee140 100%)',
    path: '/ubax/app'
  },
  {
    title: '系统设置',
    desc: '项目权限灵活配置',
    icon: 'ep:setting',
    iconBg: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)',
    path: '/ubax/setting'
  }
])

const stats = ref([
  { label: '日均处理事件', value: 1000, suffix: '万+', sub: '高性能数据采集' },
  { label: '接入应用数', value: 500, suffix: '+', sub: '覆盖多端平台' },
  { label: '平均响应时间', value: 50, suffix: 'ms', sub: '毫秒级实时分析' },
  { label: '数据准确率', value: 99, suffix: '.9%', sub: 'AI 智能清洗保障' }
])

const modules = ref([
  {
    title: '数据采集',
    icon: 'ep:download',
    iconBg: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    items: ['Web/移动端/小程序 SDK', '自定义事件配置', '客户端运行监控', '原始数据日志查询']
  },
  {
    title: '数据分析',
    icon: 'ep:trend-charts',
    iconBg: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    items: ['漏斗转化分析', '用户留存分析', '用户路径桑基图', '多维交叉分析']
  },
  {
    title: 'AI 智能',
    icon: 'ep:cpu',
    iconBg: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    items: ['分钟级实时监控', '智能异常检测', '自动告警通知', '波动归因分析']
  }
])

const handleStart = () => {
  window.location.href = '/ubax/dashboard'
}

const handleLearnMore = () => {
  ElMessage.info('更多功能介绍开发中')
}

const handleFeatureClick = (feature: any) => {
  window.location.href = feature.path
}
</script>

<style lang="scss" scoped>
.brand-page {
  background: linear-gradient(180deg, #0f0c29 0%, #1a1a3e 50%, #24243e 100%);
  min-height: 100vh;
  color: #fff;
}

// Hero Section
.hero-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 80px 48px;
  max-width: 1400px;
  margin: 0 auto;
  gap: 48px;

  @media (max-width: 960px) {
    flex-direction: column;
    padding: 48px 24px;
  }
}

.hero-content {
  flex: 1;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border-radius: 20px;
  background: rgba(102, 126, 234, 0.15);
  border: 1px solid rgba(102, 126, 234, 0.3);
  font-size: 13px;
  color: #a5b4fc;
  margin-bottom: 24px;
}

.hero-title {
  display: flex;
  flex-direction: column;
  margin: 0 0 20px;
}

.brand-name {
  font-size: 56px;
  font-weight: 800;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #4facfe 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1.1;
}

.hero-subtitle {
  font-size: 24px;
  font-weight: 400;
  color: rgba(255, 255, 255, 0.7);
  margin-top: 8px;
}

.hero-desc {
  font-size: 16px;
  line-height: 1.8;
  color: rgba(255, 255, 255, 0.6);
  max-width: 520px;
  margin-bottom: 32px;
}

.hero-actions {
  display: flex;
  gap: 16px;
}

.hero-visual {
  flex: 1;
  max-width: 560px;
}

.visual-card {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 16px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.visual-header {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  background: rgba(0, 0, 0, 0.2);
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;

  &.red { background: #ff5f57; }
  &.yellow { background: #ffbd2e; }
  &.green { background: #28ca41; }
}

.visual-body {
  padding: 20px;
}

// Section Common
.section-header {
  text-align: center;
  margin-bottom: 48px;
}

.section-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px;
  background: linear-gradient(135deg, #fff 0%, rgba(255,255,255,0.7) 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.section-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

// Features Section
.features-section {
  padding: 64px 48px;
  max-width: 1400px;
  margin: 0 auto;
}

.feature-card {
  text-align: center;
  padding: 32px 20px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  transition: all 0.3s ease;
  cursor: pointer;

  &:hover {
    transform: translateY(-8px);
    background: rgba(255, 255, 255, 0.06);
    border-color: rgba(102, 126, 234, 0.3);
    box-shadow: 0 20px 40px rgba(102, 126, 234, 0.15);
  }
}

.feature-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin: 0 auto 16px;
}

.feature-title {
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  margin: 0 0 8px;
}

.feature-desc {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.5);
  margin: 0;
}

// Highlights Section
.highlights-section {
  padding: 64px 48px;
  max-width: 1400px;
  margin: 0 auto;
}

.stat-card {
  text-align: center;
  padding: 32px 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  border: 1px solid rgba(102, 126, 234, 0.2);
}

.stat-value {
  font-size: 40px;
  font-weight: 700;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 4px;
}

.stat-sub {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.4);
}

// Modules Section
.modules-section {
  padding: 64px 48px;
  max-width: 1400px;
  margin: 0 auto;
}

.module-card {
  padding: 28px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
  height: 100%;
  transition: all 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.05);
    border-color: rgba(102, 126, 234, 0.2);
  }
}

.module-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.module-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.module-title {
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  margin: 0;
}

.module-list {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 0;
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    font-size: 14px;
    color: rgba(255, 255, 255, 0.7);

    &:last-child {
      border-bottom: none;
    }

    .el-icon {
      color: #43e97b;
    }
  }
}

// Footer
.brand-footer {
  padding: 64px 48px 32px;
  max-width: 1400px;
  margin: 0 auto;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.footer-content {
  display: flex;
  justify-content: space-between;
  gap: 48px;
  margin-bottom: 48px;

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.footer-brand {
  .footer-logo {
    font-size: 28px;
    font-weight: 800;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .footer-desc {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.5);
    margin: 8px 0 0;
  }
}

.footer-links {
  display: flex;
  gap: 64px;
}

.footer-col {
  h4 {
    font-size: 14px;
    font-weight: 600;
    color: #fff;
    margin: 0 0 16px;
  }

  a {
    display: block;
    font-size: 13px;
    color: rgba(255, 255, 255, 0.5);
    text-decoration: none;
    margin-bottom: 10px;
    transition: color 0.2s;

    &:hover {
      color: #667eea;
    }
  }
}

.footer-bottom {
  text-align: center;
  padding-top: 24px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
  font-size: 13px;
  color: rgba(255, 255, 255, 0.3);
}
</style>
