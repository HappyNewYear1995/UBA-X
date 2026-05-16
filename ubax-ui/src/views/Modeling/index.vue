<template>
  <div class="modeling-page">
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>事件模型</span>
              </div>
              <el-button class="btn-gradient" size="small" @click="showEventDialog = true">
                <el-icon><Plus /></el-icon>
                新建事件
              </el-button>
            </div>
          </template>
          <el-table :data="events" style="width: 100%" class="custom-table">
            <el-table-column prop="name" label="事件名称" />
            <el-table-column prop="code" label="事件代码" width="150" />
            <el-table-column prop="properties" label="属性数" width="80" />
            <el-table-column label="操作" width="80">
              <template #default>
                <el-button class="link-btn primary" size="small">编辑</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="card-header">
              <div class="header-title">
                <div class="title-dot"></div>
                <span>用户属性</span>
              </div>
              <el-button class="btn-gradient" size="small">
                <el-icon><Plus /></el-icon>
                新建属性
              </el-button>
            </div>
          </template>
          <el-table :data="userProperties" style="width: 100%" class="custom-table">
            <el-table-column prop="name" label="属性名称" />
            <el-table-column prop="type" label="类型" width="80">
              <template #default="{ row }">
                <span class="type-badge">{{ row.type }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="desc" label="描述" />
          </el-table>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card shadow="never" class="glass-card">
          <template #header>
            <div class="header-title">
              <div class="title-dot"></div>
              <span>转化漏斗模型</span>
            </div>
          </template>
          <div class="funnel-preview">
            <div v-for="(step, index) in funnelSteps" :key="index" class="funnel-step">
              <div class="funnel-bar" :style="{ width: step.percent + '%' }">
                <span class="funnel-label">{{ step.name }}</span>
                <span class="funnel-value">{{ step.value }} ({{ step.percent }}%)</span>
              </div>
            </div>
          </div>
          <el-button class="btn-gradient" style="width: 100%; margin-top: 16px;">
            配置漏斗
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" class="glass-card sql-card">
      <template #header>
        <div class="card-header">
          <div class="header-title">
            <div class="title-dot"></div>
            <span>SQL 查询编辑器</span>
          </div>
          <el-button class="btn-gradient" size="small">
            <el-icon><CaretRight /></el-icon>
            执行查询
          </el-button>
        </div>
      </template>
      <el-input
        v-model="sqlQuery"
        type="textarea"
        :rows="6"
        placeholder="输入 SQL 查询语句..."
        class="sql-editor"
      />
      <div class="sql-result">
        <el-empty description="执行查询后显示结果" :image-size="80" />
      </div>
    </el-card>

    <el-dialog v-model="showEventDialog" title="新建事件" width="600px" class="custom-dialog">
      <el-form label-width="100px">
        <el-form-item label="事件名称">
          <el-input placeholder="例如：用户登录" />
        </el-form-item>
        <el-form-item label="事件代码">
          <el-input placeholder="例如：user_login" />
        </el-form-item>
        <el-form-item label="事件分类">
          <el-select placeholder="请选择分类">
            <el-option label="用户行为" value="behavior" />
            <el-option label="业务事件" value="business" />
            <el-option label="系统事件" value="system" />
          </el-select>
        </el-form-item>
        <el-form-item label="属性定义">
          <el-table :data="[]" border size="small">
            <el-table-column prop="name" label="属性名" width="150" />
            <el-table-column prop="type" label="类型" width="100" />
            <el-table-column prop="desc" label="描述" />
          </el-table>
          <el-button class="link-btn primary" size="small">+ 添加属性</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEventDialog = false" class="btn-outline">取消</el-button>
        <el-button class="btn-gradient" @click="showEventDialog = false">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const showEventDialog = ref(false)

const sqlQuery = ref(`SELECT 
  date,
  COUNT(DISTINCT user_id) AS dau,
  COUNT(event_id) AS total_events
FROM events
WHERE date >= '2026-05-01'
GROUP BY date
ORDER BY date DESC
LIMIT 30;`)

const events = ref([
  { name: '用户登录', code: 'user_login', properties: 5 },
  { name: '页面浏览', code: 'page_view', properties: 8 },
  { name: '按钮点击', code: 'button_click', properties: 6 },
  { name: '表单提交', code: 'form_submit', properties: 12 },
  { name: '视频播放', code: 'video_play', properties: 7 },
])

const userProperties = ref([
  { name: '用户等级', type: 'String', desc: 'VIP/普通' },
  { name: '注册时间', type: 'Date', desc: '首次注册' },
  { name: '累计消费', type: 'Number', desc: '总金额' },
  { name: '最后登录', type: 'Date', desc: '最近活跃' },
])

const funnelSteps = ref([
  { name: '访问首页', value: 10000, percent: 100 },
  { name: '浏览商品', value: 6500, percent: 65 },
  { name: '加入购物车', value: 2800, percent: 28 },
  { name: '提交订单', value: 1200, percent: 12 },
  { name: '完成支付', value: 890, percent: 8.9 },
])
</script>

<style scoped lang="scss">
.modeling-page {
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
    }
  }

  .custom-table {
    :deep(.el-table__header-wrapper) {
      th {
        background: var(--bg-color);
        color: var(--text-secondary);
        font-weight: 600;
        font-size: 13px;
        border-bottom: 1px solid var(--border-light);
      }
    }

    :deep(.el-table__row) {
      transition: all var(--transition-fast);

      &:hover {
        background: rgba(99, 102, 241, 0.03) !important;
      }

      td {
        border-bottom: 1px solid var(--border-light);
        color: var(--text-regular);
      }
    }
  }

  .type-badge {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 600;
    background: rgba(99, 102, 241, 0.1);
    color: #6366f1;
  }

  .funnel-preview {
    padding: 16px 0;

    .funnel-step {
      margin-bottom: 10px;

      .funnel-bar {
        background: linear-gradient(90deg, #6366f1, #818cf8);
        border-radius: 8px;
        padding: 10px 14px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: #fff;
        font-size: 13px;
        font-weight: 500;
        transition: all var(--transition-base);
        box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2);

        &:hover {
          transform: translateX(4px);
          box-shadow: 0 6px 16px rgba(99, 102, 241, 0.3);
        }
      }
    }
  }

  .sql-card {
    .sql-editor {
      :deep(textarea) {
        font-family: 'JetBrains Mono', 'Fira Code', 'Consolas', monospace;
        font-size: 14px;
        background: linear-gradient(135deg, #1e1e2e 0%, #2d2d3f 100%);
        color: #cdd6f4;
        border: 1px solid rgba(255, 255, 255, 0.05);
        border-radius: var(--radius-md);
        padding: 16px;
      }
    }

    .sql-result {
      margin-top: 16px;
      min-height: 200px;
      border: 2px dashed var(--border-color);
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      background: var(--bg-color);
      transition: all var(--transition-base);

      &:hover {
        border-color: var(--primary-color);
        background: rgba(99, 102, 241, 0.02);
      }
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

  .btn-outline {
    background: transparent;
    border: 1px solid var(--border-color);
    color: var(--text-regular);
    transition: all var(--transition-fast);

    &:hover {
      border-color: var(--primary-color);
      color: var(--primary-color);
    }
  }

  .link-btn {
    &.primary { color: var(--primary-color); }
  }
}
</style>
