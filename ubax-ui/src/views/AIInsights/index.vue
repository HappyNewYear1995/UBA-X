<template>
  <div class="ai-insights-page">
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-for="(stat, index) in stats" :key="index">
        <div class="stat-card-wrapper" :style="{ animationDelay: `${index * 0.1}s` }">
          <div class="stat-card">
            <div class="stat-bg" :class="stat.color"></div>
            <div class="stat-content">
              <div class="stat-icon" :class="stat.color">
                <el-icon><component :is="stat.icon" /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ stat.value }}</div>
                <div class="stat-label">{{ stat.label }}</div>
              </div>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card shadow="never" class="glass-card ai-chat-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot ai-dot"></div>
                <span>AI 智能问答</span>
              </div>
              <el-tag class="ai-badge" effect="dark">
                <el-icon><MagicStick /></el-icon>
                AI Powered
              </el-tag>
            </div>
          </template>
          <div class="chat-container">
            <div v-for="(msg, index) in chatMessages" :key="index" class="chat-message" :class="msg.role">
              <div class="message-avatar">
                <el-icon v-if="msg.role === 'ai'"><MagicStick /></el-icon>
                <el-icon v-else><User /></el-icon>
              </div>
              <div class="message-content">
                <div class="message-text">{{ msg.content }}</div>
                <div class="message-time">{{ msg.time }}</div>
              </div>
            </div>
          </div>
          <div class="chat-input-wrapper">
            <el-input
              v-model="chatInput"
              placeholder="向 AI 提问，例如：为什么昨天的 DAU 下降了？"
              class="chat-input"
              @keyup.enter="sendMessage"
            >
              <template #append>
                <el-button class="btn-gradient" @click="sendMessage">
                  <el-icon><Promotion /></el-icon>
                </el-button>
              </template>
            </el-input>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>快捷问题</span>
            </div>
          </template>
          <div class="quick-questions">
            <div
              v-for="q in quickQuestions"
              :key="q"
              class="quick-question"
              @click="askQuestion(q)"
            >
              <el-icon><ChatDotRound /></el-icon>
              <span>{{ q }}</span>
            </div>
          </div>
        </el-card>

        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>AI 洞察</span>
            </div>
          </template>
          <div class="insights-list">
            <div v-for="insight in insights" :key="insight.id" class="insight-item">
              <div class="insight-icon" :class="insight.type">
                <el-icon><component :is="insight.icon" /></el-icon>
              </div>
              <div class="insight-content">
                <div class="insight-title">{{ insight.title }}</div>
                <div class="insight-desc">{{ insight.description }}</div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="glass-card prediction-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-dot"></div>
            <span>趋势预测</span>
          </div>
          <el-radio-group v-model="predictionRange" class="custom-radio-group">
            <el-radio-button value="7d">7天</el-radio-button>
            <el-radio-button value="30d">30天</el-radio-button>
            <el-radio-button value="90d">90天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <v-chart class="chart" :option="predictionOption" autoresize />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, LineChart, GridComponent, TooltipComponent, LegendComponent])

const chatInput = ref('')
const predictionRange = ref('7d')

const stats = ref([
  { value: '128', label: 'AI 问答次数', icon: 'ChatLineSquare', color: 'primary' },
  { value: '96.5%', label: '回答准确率', icon: 'CircleCheck', color: 'success' },
  { value: '12', label: 'AI 洞察', icon: 'Lightning', color: 'warning' },
  { value: '3', label: '趋势预测', icon: 'TrendCharts', color: 'danger' },
])

const chatMessages = ref([
  { role: 'ai', content: '你好！我是 UBA-X AI 助手，可以帮你分析用户行为数据。请问有什么可以帮你的？', time: '10:00' },
  { role: 'user', content: '为什么昨天的 DAU 下降了 15%？', time: '10:05' },
  { role: 'ai', content: '根据数据分析，昨天 DAU 下降的主要原因：1) 新用户获取渠道转化率下降 25%；2) 某核心功能改版导致老用户活跃度降低；3) 周末效应影响。建议关注新用户引导流程优化。', time: '10:05' },
])

const quickQuestions = ref([
  '本周哪些页面跳出率最高？',
  '用户留存率趋势如何？',
  '哪些功能使用率下降明显？',
  '预测下个月的 DAU 趋势',
  '用户转化漏斗的瓶颈在哪里？',
])

const insights = ref([
  { id: 1, title: '新用户流失预警', description: '注册后24小时内未完成核心操作的用户，7日留存率仅 12%', type: 'warning', icon: 'Warning' },
  { id: 2, title: '功能使用热点', description: '搜索功能使用量增长 45%，建议优化搜索体验', type: 'success', icon: 'TrendCharts' },
  { id: 3, title: '异常行为检测', description: '检测到 3 个 IP 存在刷量行为，已自动标记', type: 'danger', icon: 'CircleClose' },
])

const predictionOption = {
  tooltip: { trigger: 'axis' },
  legend: { data: ['实际 DAU', '预测 DAU', '预测区间'], top: 0 },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['05-10', '05-11', '05-12', '05-13', '05-14', '05-15', '05-16', '05-17', '05-18', '05-19', '05-20', '05-21', '05-22', '05-23'],
    axisLine: { lineStyle: { color: '#e2e8f0' } },
    axisLabel: { color: '#64748b' },
  },
  yAxis: { 
    type: 'value',
    splitLine: { lineStyle: { color: '#f1f5f9', type: 'dashed' } },
    axisLabel: { color: '#64748b' },
  },
  series: [
    { name: '实际 DAU', type: 'line', data: [21000, 22500, 21800, 23200, 24100, 22800, 23500, null, null, null, null, null, null, null], itemStyle: { color: '#6366f1' }, lineStyle: { width: 3 } },
    { name: '预测 DAU', type: 'line', data: [null, null, null, null, null, null, 23500, 24200, 24800, 25100, 25600, 26200, 26800, 27500], itemStyle: { color: '#10b981' }, lineStyle: { width: 3, type: 'dashed' } },
    { name: '预测区间', type: 'line', data: [null, null, null, null, null, null, 23500, 25500, 26200, 26800, 27500, 28200, 29000, 29800], itemStyle: { color: 'rgba(16, 185, 129, 0.2)' }, areaStyle: { color: 'rgba(16, 185, 129, 0.1)' }, lineStyle: { width: 0 } },
  ],
}

const sendMessage = () => {
  if (!chatInput.value.trim()) return
  chatMessages.value.push({ role: 'user', content: chatInput.value, time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
  chatInput.value = ''
  setTimeout(() => {
    chatMessages.value.push({ role: 'ai', content: '正在分析数据，请稍候...', time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) })
  }, 500)
}

const askQuestion = (q: string) => {
  chatInput.value = q
  sendMessage()
}
</script>

<style scoped lang="scss">
.ai-insights-page {
  .stats-row {
    margin-bottom: 24px;
  }

  .stat-card-wrapper {
    animation: fadeInUp 0.5s ease-out forwards;
    opacity: 0;
  }

  .stat-card {
    position: relative;
    padding: 24px;
    background: var(--card-bg);
    border-radius: var(--radius-lg);
    border: 1px solid var(--border-light);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-slow);
    overflow: hidden;
    cursor: pointer;

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-xl);
      border-color: transparent;

      .stat-bg {
        opacity: 0.15;
        transform: scale(1.1);
      }

      .stat-icon {
        transform: scale(1.1) rotate(5deg);
      }
    }

    .stat-bg {
      position: absolute;
      top: -20px;
      right: -20px;
      width: 120px;
      height: 120px;
      border-radius: 50%;
      opacity: 0.08;
      transition: all var(--transition-slow);
      filter: blur(30px);

      &.primary { background: linear-gradient(135deg, #6366f1, #818cf8); }
      &.success { background: linear-gradient(135deg, #10b981, #34d399); }
      &.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
      &.danger { background: linear-gradient(135deg, #ef4444, #f87171); }
    }

    .stat-content {
      display: flex;
      align-items: center;
      position: relative;
      z-index: 1;

      .stat-icon {
        width: 52px;
        height: 52px;
        border-radius: var(--radius-md);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        color: #fff;
        margin-right: 16px;
        transition: all var(--transition-base);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

        &.primary { background: linear-gradient(135deg, #6366f1, #818cf8); }
        &.success { background: linear-gradient(135deg, #10b981, #34d399); }
        &.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
        &.danger { background: linear-gradient(135deg, #ef4444, #f87171); }
      }

      .stat-info {
        .stat-value {
          font-size: 28px;
          font-weight: 700;
          color: var(--text-primary);
          letter-spacing: -0.5px;
          line-height: 1.2;
        }

        .stat-label {
          font-size: 13px;
          color: var(--text-secondary);
          margin-top: 4px;
          font-weight: 500;
        }
      }
    }
  }

  .glass-card {
    background: var(--card-bg);
    border: 1px solid var(--border-light);
    border-radius: var(--radius-lg);
    box-shadow: var(--shadow-md);
    transition: all var(--transition-slow);
    margin-bottom: 24px;

    &:hover {
      box-shadow: var(--shadow-lg);
    }

    :deep(.el-card__header) {
      padding: 20px 24px;
      border-bottom: 1px solid var(--border-light);
      background: transparent;
    }

    :deep(.el-card__body) {
      padding: 24px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .header-title {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);

    .title-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
      box-shadow: 0 0 8px rgba(99, 102, 241, 0.4);

      &.ai-dot {
        background: linear-gradient(135deg, #8b5cf6, #a78bfa);
        box-shadow: 0 0 8px rgba(139, 92, 246, 0.4);
      }
    }
  }

  .ai-badge {
    background: linear-gradient(135deg, #8b5cf6, #a78bfa);
    border: none;
    font-size: 12px;
    padding: 4px 12px;
    border-radius: 20px;
  }

  .ai-chat-card {
    .chat-container {
      height: 400px;
      overflow-y: auto;
      padding: 16px 0;
      margin-bottom: 16px;

      .chat-message {
        display: flex;
        margin-bottom: 20px;
        animation: fadeInUp 0.3s ease-out;

        &.user { flex-direction: row-reverse; }

        .message-avatar {
          width: 36px;
          height: 36px;
          border-radius: 50%;
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          flex-shrink: 0;
          box-shadow: var(--shadow-sm);

          .ai-insights-page &.ai {
            background: linear-gradient(135deg, #8b5cf6, #a78bfa);
            color: #fff;
          }

          &.user {
            background: linear-gradient(135deg, #6366f1, #818cf8);
            color: #fff;
          }
        }

        .message-content {
          max-width: 70%;
          margin: 0 12px;

          .message-text {
            padding: 12px 16px;
            border-radius: 16px;
            font-size: 14px;
            line-height: 1.6;
            box-shadow: var(--shadow-sm);
          }

          .message-time {
            font-size: 11px;
            color: var(--text-secondary);
            margin-top: 4px;
          }
        }

        &.ai {
          .message-text {
            background: var(--bg-color);
            color: var(--text-regular);
            border-bottom-left-radius: 4px;
          }
        }

        &.user {
          .message-text {
            background: linear-gradient(135deg, #6366f1, #818cf8);
            color: #fff;
            border-bottom-right-radius: 4px;
          }

          .message-time {
            text-align: right;
          }
        }
      }
    }

    .chat-input-wrapper {
      :deep(.el-input-group__append) {
        background: transparent;
        border: none;
        padding: 0;
      }
    }
  }

  .quick-questions {
    display: flex;
    flex-direction: column;
    gap: 10px;

    .quick-question {
      padding: 12px 16px;
      background: var(--bg-color);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
      display: flex;
      align-items: center;
      gap: 10px;
      font-size: 13px;
      color: var(--text-regular);
      cursor: pointer;
      transition: all var(--transition-base);

      &:hover {
        background: rgba(99, 102, 241, 0.05);
        border-color: var(--primary-color);
        color: var(--primary-color);
        transform: translateX(4px);
      }
    }
  }

  .insights-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .insight-item {
      display: flex;
      gap: 12px;
      padding: 12px;
      background: var(--bg-color);
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
      transition: all var(--transition-base);

      &:hover {
        box-shadow: var(--shadow-sm);
      }

      .insight-icon {
        width: 36px;
        height: 36px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 16px;
        color: #fff;
        flex-shrink: 0;

        &.warning { background: linear-gradient(135deg, #f59e0b, #fbbf24); }
        &.success { background: linear-gradient(135deg, #10b981, #34d399); }
        &.danger { background: linear-gradient(135deg, #ef4444, #f87171); }
      }

      .insight-content {
        .insight-title {
          font-size: 14px;
          font-weight: 600;
          color: var(--text-primary);
          margin-bottom: 4px;
        }

        .insight-desc {
          font-size: 12px;
          color: var(--text-secondary);
          line-height: 1.5;
        }
      }
    }
  }

  .prediction-card {
    .chart {
      height: 350px;
    }
  }

  .btn-gradient {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
    border: none;
    color: #fff;
    font-weight: 500;
    box-shadow: 0 4px 12px rgba(99, 102, 241, 0.3);
    transition: all var(--transition-base);

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 6px 16px rgba(99, 102, 241, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}
</style>
