<template>
  <div class="page">
    <el-page-header @back="$router.back()" class="ph">
      <template #content>
        <span class="ph-title">用户画像 · {{ distinctId }}</span>
        <el-tag v-for="t in splitTags(profile?.tags)" :key="t" size="small" class="ph-tag" effect="dark">{{ t }}</el-tag>
      </template>
    </el-page-header>

    <template v-if="profile">
      <!-- overview stat cards -->
      <div class="stat-grid">
        <div class="stat-card"><div class="label">事件总数</div><div class="value">{{ compact(stats.eventTotal) }}</div></div>
        <div class="stat-card"><div class="label">访问次数</div><div class="value">{{ compact(stats.visitCount) }}</div></div>
        <div class="stat-card"><div class="label">浏览页数</div><div class="value">{{ compact(stats.pageviewCount) }}</div></div>
        <div class="stat-card"><div class="label">会话数</div><div class="value">{{ compact(stats.sessionCount) }}</div></div>
        <div class="stat-card"><div class="label">活跃天数</div><div class="value">{{ stats.activeDays }}<small> 天</small></div></div>
        <div class="stat-card"><div class="label">日均事件</div><div class="value">{{ stats.avgEventsPerDay }}</div></div>
        <div class="stat-card"><div class="label">平均停留</div><div class="value" style="font-size:22px">{{ fmtDuration(Math.round(stats.avgDwellMs / 1000)) }}</div></div>
        <div class="stat-card"><div class="label">生命周期</div><div class="value">{{ stats.lifetimeDays }}<small> 天</small></div><div class="sub-label" v-if="stats.daysSinceLastSeen != null">已 {{ stats.daysSinceLastSeen }} 天未活跃</div></div>
      </div>

      <!-- AI insight -->
      <div class="card ai-card">
        <div class="card-title">
          <span class="ai-title"><span class="ai-badge">AI</span>用户画像智能洞察</span>
          <el-button size="small" :type="insight ? 'default' : 'primary'" :loading="aiLoading" :icon="MagicStick" @click="genInsight">
            {{ insight ? '重新分析' : '生成 AI 分析' }}
          </el-button>
        </div>

        <!-- empty prompt -->
        <div v-if="!insight && !aiLoading" class="ai-empty">
          <el-icon class="ai-empty-icon"><MagicStick /></el-icon>
          <div class="ai-empty-text">
            基于该用户的 <b>{{ stats.eventTotal }}</b> 条真实行为事件，自动生成活跃度评分、人群画像、关键发现、流失风险与运营建议。
          </div>
          <el-button type="primary" :icon="MagicStick" @click="genInsight">立即分析</el-button>
        </div>

        <!-- loading -->
        <div v-else-if="aiLoading" class="ai-loading">
          <el-icon class="is-loading ai-spin"><Loading /></el-icon>
          <span>正在分析用户行为特征…</span>
        </div>

        <!-- result -->
        <div v-else-if="insight" class="ai-body">
          <div class="ai-top">
            <div class="ai-score" :style="{ '--c': scoreColor(insight.score) }">
              <el-progress type="dashboard" :percentage="insight.score" :width="118" :stroke-width="9"
                :color="scoreColor(insight.score)">
                <template #default="{ percentage }">
                  <div class="score-num">{{ percentage }}</div>
                  <div class="score-cap">活跃评分</div>
                </template>
              </el-progress>
            </div>
            <div class="ai-meta">
              <div class="ai-persona">{{ insight.persona }}</div>
              <div class="ai-tags">
                <el-tag :type="insight.levelTone" effect="dark" size="small">{{ insight.level }}</el-tag>
                <el-tag :type="insight.churnTone" effect="plain" size="small">流失风险：{{ insight.churn }}</el-tag>
                <span class="ai-time">基于 {{ insight.basedOn }} 条事件 · {{ fmtTime(insight.generatedAt) }}</span>
              </div>
              <p class="ai-summary">{{ insight.summary }}</p>
            </div>
          </div>

          <div class="ai-cols">
            <div class="ai-col">
              <div class="ai-col-title"><el-icon><Star /></el-icon>关键发现</div>
              <ul class="ai-list">
                <li v-for="(h, i) in insight.highlights" :key="i">
                  <span class="dot" :class="h.tone"></span>
                  <span class="li-label">{{ h.label }}</span>
                  <span class="li-text">{{ h.text }}</span>
                </li>
              </ul>
            </div>
            <div class="ai-col">
              <div class="ai-col-title"><el-icon><WarnTriangleFilled /></el-icon>风险提示</div>
              <ul class="ai-list">
                <li v-for="(r, i) in insight.risks" :key="i">
                  <span class="dot" :class="r.tone"></span>
                  <span class="li-label">{{ r.label }}</span>
                  <span class="li-text">{{ r.text }}</span>
                </li>
              </ul>
            </div>
          </div>

          <div class="ai-recs">
            <div class="ai-col-title"><el-icon><Opportunity /></el-icon>运营建议</div>
            <ol class="ai-rec-list">
              <li v-for="(rec, i) in insight.recommendations" :key="i">{{ rec }}</li>
            </ol>
          </div>

          <div class="ai-foot">
            <el-icon><InfoFilled /></el-icon>
            洞察由规则引擎基于该用户的真实行为数据生成，用于辅助运营决策；评分与建议为启发式判断，非绝对结论。
          </div>
        </div>
      </div>

      <!-- charts row: event type + active hours -->
      <div class="row2">
        <div class="card">
          <div class="card-title">事件类型分布</div>
          <BaseChart :option="typeOption" height="280px" />
        </div>
        <div class="card">
          <div class="card-title">活跃时段分布（24 小时）</div>
          <BaseChart :option="hourOption" height="280px" />
        </div>
      </div>

      <!-- activity trend -->
      <div class="card">
        <div class="card-title">活跃趋势（近 30 天，按事件数）</div>
        <BaseChart :option="trendOption" height="280px" />
      </div>

      <!-- top pages + top events -->
      <div class="row2">
        <div class="card">
          <div class="card-title">常访页面 TOP</div>
          <div v-if="topPages.length" class="rank-list">
            <div class="rank-item" v-for="(r, i) in topPages" :key="r.key">
              <span class="rank-no" :class="{ top: i < 3 }">{{ i + 1 }}</span>
              <div class="rank-body">
                <div class="rank-name mono">{{ r.key }}</div>
                <div class="rank-sub" v-if="r.title">{{ r.title }}</div>
                <el-progress :percentage="pct(r.count, maxPageCount)" :show-text="false" :stroke-width="6" color="#2b5cff" />
              </div>
              <span class="rank-count">{{ r.count }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无页面浏览" :image-size="60" />
        </div>
        <div class="card">
          <div class="card-title">常做事件 TOP</div>
          <div v-if="topEvents.length" class="rank-list">
            <div class="rank-item" v-for="(r, i) in topEvents" :key="r.key">
              <span class="rank-no" :class="{ top: i < 3 }">{{ i + 1 }}</span>
              <div class="rank-body">
                <div class="rank-name">{{ r.key }}</div>
                <el-progress :percentage="pct(r.count, maxEventCount)" :show-text="false" :stroke-width="6" color="#00c2a8" />
              </div>
              <span class="rank-count">{{ r.count }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无自定义事件" :image-size="60" />
        </div>
      </div>

      <!-- commerce & behaviour highlights (mined from event properties) -->
      <div class="card" v-if="hasDerived">
        <div class="card-title">消费与行为特征</div>
        <div class="derived-grid">
          <div class="derived-item">
            <div class="d-label">订单数</div>
            <div class="d-value">{{ derived.orderCount }}</div>
          </div>
          <div class="derived-item">
            <div class="d-label">累计消费</div>
            <div class="d-value accent">{{ formatMoney(derived.totalAmount) }}<small> {{ derived.currency }}</small></div>
          </div>
          <div class="derived-item">
            <div class="d-label">购买商品件数</div>
            <div class="d-value">{{ derived.totalItems }}</div>
          </div>
          <div class="derived-item">
            <div class="d-label">客单价</div>
            <div class="d-value">{{ derived.orderCount ? formatMoney(Math.round(derived.totalAmount / derived.orderCount)) : '0' }}<small> {{ derived.currency }}</small></div>
          </div>
        </div>
        <div class="kw-block" v-if="derived.keywords?.length">
          <div class="d-label">搜索关键词</div>
          <div class="kw-list">
            <el-tag v-for="k in derived.keywords" :key="k.keyword" size="small" class="kw-tag" effect="plain">
              {{ k.keyword }}<span class="kw-count">{{ k.count }}</span>
            </el-tag>
          </div>
        </div>
      </div>

      <!-- basic attributes -->
      <div class="card">
        <div class="card-title">基础属性</div>
        <el-descriptions :column="3" border size="small">
          <el-descriptions-item label="访客ID">{{ profile.distinctId }}</el-descriptions-item>
          <el-descriptions-item label="用户ID">{{ profile.userId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="首次访问">{{ fmtDateTime(profile.firstSeen) }}</el-descriptions-item>
          <el-descriptions-item label="最近活跃">{{ fmtDateTime(profile.lastSeen) }}</el-descriptions-item>
          <el-descriptions-item label="访问次数">{{ profile.visitCount }}</el-descriptions-item>
          <el-descriptions-item label="事件总数">{{ profile.eventCount }}</el-descriptions-item>
          <el-descriptions-item label="浏览页数">{{ profile.pageviewCount }}</el-descriptions-item>
          <el-descriptions-item label="设备">{{ profile.device || '-' }} / {{ profile.os || '-' }}</el-descriptions-item>
          <el-descriptions-item label="浏览器">{{ profile.browser || '-' }}</el-descriptions-item>
          <el-descriptions-item label="地区">{{ [profile.region, profile.city].filter(Boolean).join(' ') || '-' }}</el-descriptions-item>
          <el-descriptions-item label="最近页面" :span="2">{{ profile.lastUrl || '-' }}</el-descriptions-item>
          <el-descriptions-item label="标签" :span="3">
            <el-tag v-for="t in splitTags(profile.tags)" :key="t" size="small" class="tag">{{ t }}</el-tag>
            <span v-if="!splitTags(profile.tags).length" class="muted">-</span>
          </el-descriptions-item>
          <el-descriptions-item v-if="propsList.length" label="自定义属性" :span="3">
            <div class="kv-grid">
              <div class="kv" v-for="(v, k) in propsObj" :key="k"><span class="kv-k mono">{{ k }}</span><span class="kv-v">{{ v }}</span></div>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- sessions -->
      <div class="card">
        <div class="card-title">会话记录（最近 {{ sessions.length }} 个）</div>
        <el-table v-if="sessions.length" :data="sessions" size="small" stripe>
          <el-table-column type="index" label="#" width="50" />
          <el-table-column label="会话ID" min-width="150">
            <template #default="{ row }"><span class="mono">{{ shortId(row.sessionId) }}</span></template>
          </el-table-column>
          <el-table-column label="开始时间" width="170">
            <template #default="{ row }">{{ fmtDateTime(row.start) }}</template>
          </el-table-column>
          <el-table-column label="时长" width="110">
            <template #default="{ row }">{{ fmtDuration(Math.round(row.durationMs / 1000)) }}</template>
          </el-table-column>
          <el-table-column prop="events" label="事件数" width="90" />
          <el-table-column prop="pages" label="浏览页数" width="90" />
          <el-table-column label="入口页" min-width="160">
            <template #default="{ row }"><span class="mono">{{ row.entry || '-' }}</span></template>
          </el-table-column>
        </el-table>
        <el-empty v-else description="暂无会话记录" :image-size="60" />
      </div>

      <!-- behavior timeline -->
      <div class="card">
        <div class="card-title">
          <span>最近行为轨迹</span>
          <span class="muted" style="font-weight:400;font-size:12px">最近 {{ events.length }} 条</span>
        </div>
        <el-timeline v-if="events.length">
          <el-timeline-item
            v-for="(e, i) in events"
            :key="i"
            :timestamp="fmtDateTime(e.eventTime)"
            :type="timelineType(e.eventType)"
            :hollow="e.eventType !== 'pageview'"
            placement="top"
          >
            <div class="tl-head">
              <el-tag size="small" :type="tagType(e.eventType)" effect="light">{{ typeLabel(e.eventType) }}</el-tag>
              <b>{{ e.eventName || typeLabel(e.eventType) }}</b>
            </div>
            <div class="muted tl-detail" v-if="e.urlPath">
              <span class="mono">{{ e.urlPath }}</span>
              <span v-if="e.element"> → {{ e.element }}</span>
              <span v-if="e.duration"> · 停留 {{ fmtDuration(Math.round(e.duration / 1000)) }}</span>
            </div>
          </el-timeline-item>
        </el-timeline>
        <el-empty v-else description="暂无行为记录" />
      </div>
    </template>

    <div v-else-if="loadError" class="card">
      <el-result icon="warning" title="加载失败" sub-title="无法获取该用户画像，可能不存在或已被删除">
        <template #extra>
          <el-button type="primary" @click="loadAll">重试</el-button>
          <el-button @click="$router.back()">返回</el-button>
        </template>
      </el-result>
    </div>

    <el-skeleton v-else :rows="8" animated />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { MagicStick, Loading, Star, WarnTriangleFilled, Opportunity, InfoFilled } from '@element-plus/icons-vue'
import BaseChart from '../components/BaseChart.vue'
import { profileApi } from '../api'
import { fmtDateTime, fmtDuration, compact } from '../utils/date'

const route = useRoute()
const distinctId = route.params.distinctId
const profile = ref(null)
const events = ref([])
const stats = reactive({
  eventTotal: 0, visitCount: 0, pageviewCount: 0, sessionCount: 0,
  activeDays: 0, avgEventsPerDay: 0, avgDwellMs: 0, lifetimeDays: 0, daysSinceLastSeen: null
})
const eventTypeDist = ref([])
const topPages = ref([])
const topEvents = ref([])
const dailyTrend = ref([])
const hourDist = ref([])
const sessions = ref([])
const derived = reactive({ orderCount: 0, totalAmount: 0, totalItems: 0, currency: 'CNY', keywords: [] })

// AI insight (on-demand)
const insight = ref(null)
const aiLoading = ref(false)
async function genInsight() {
  aiLoading.value = true
  try {
    insight.value = await profileApi.insight(distinctId)
  } catch (e) {
    insight.value = null
  } finally {
    aiLoading.value = false
  }
}
function scoreColor(s) {
  if (s >= 80) return '#00c2a8'
  if (s >= 60) return '#2b5cff'
  if (s >= 40) return '#ff9f43'
  return '#f5222d'
}
function fmtTime(iso) { return iso ? iso.replace('T', ' ').slice(0, 16) : '' }

const TYPE_LABEL = { pageview: '浏览', click: '点击', event: '事件', custom: '自定义', leave: '离开' }
const TYPE_COLOR = { pageview: '#2b5cff', click: '#ff9f43', event: '#00c2a8', custom: '#a855f7', leave: '#8a9099' }

function splitTags(t) { return (t || '').split(',').map((s) => s.trim()).filter(Boolean) }
function timelineType(t) { return { pageview: 'primary', click: 'warning', event: 'success', leave: 'info', custom: 'success' }[t] || 'info' }
function tagType(t) { return { pageview: 'primary', click: 'warning', event: 'success', custom: 'success', leave: 'info' }[t] || 'info' }
function typeLabel(t) { return TYPE_LABEL[t] || t || '-' }
function shortId(id) { return id && id.length > 16 ? id.slice(0, 8) + '…' + id.slice(-4) : (id || '-') }
function pct(v, max) { return max > 0 ? Math.round((v / max) * 100) : 0 }

const maxPageCount = computed(() => Math.max(1, ...topPages.value.map((r) => r.count)))
const maxEventCount = computed(() => Math.max(1, ...topEvents.value.map((r) => r.count)))

const propsObj = computed(() => {
  const raw = profile.value?.properties
  if (!raw) return {}
  try { const o = JSON.parse(raw); return (o && typeof o === 'object') ? o : {} } catch { return {} }
})
const propsList = computed(() => Object.keys(propsObj.value))

// show the commerce card only when there is at least one meaningful signal
const hasDerived = computed(() => derived.orderCount > 0 || (derived.keywords && derived.keywords.length > 0))
function formatMoney(v) { return Number(v || 0).toLocaleString('zh-CN') }

const typeOption = computed(() => {
  const data = eventTypeDist.value.map((d) => ({
    name: TYPE_LABEL[d.type] || d.type, value: d.count,
    itemStyle: { color: TYPE_COLOR[d.type] || '#8a9099' }
  }))
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, icon: 'circle', itemWidth: 8, itemHeight: 8, textStyle: { color: '#8a9099', fontSize: 12 } },
    series: [{
      type: 'pie', radius: ['46%', '70%'], center: ['50%', '44%'], avoidLabelOverlap: true,
      itemStyle: { borderColor: '#fff', borderWidth: 2 },
      label: { show: true, formatter: '{b}\n{d}%', fontSize: 11, color: '#6b7280' },
      labelLine: { length: 8, length2: 8 },
      data: data.length ? data : [{ name: '暂无数据', value: 1, itemStyle: { color: '#e5e7eb' } }]
    }]
  }
})

const hourOption = computed(() => {
  const counts = hourDist.value.map((h) => h.count)
  const max = Math.max(1, ...counts)
  return {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' }, formatter: (p) => `${p[0].name}时：${p[0].value} 次` },
    grid: { left: 40, right: 16, top: 16, bottom: 30 },
    xAxis: { type: 'category', data: hourDist.value.map((h) => h.hour), axisTick: { show: false },
      axisLabel: { color: '#8a9099', fontSize: 11, interval: 2 }, axisLine: { lineStyle: { color: '#e5e7eb' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f5' } }, axisLabel: { color: '#8a9099', fontSize: 11 } },
    series: [{
      type: 'bar', data: counts, barWidth: '62%',
      itemStyle: {
        borderRadius: [3, 3, 0, 0],
        color: (p) => {
          const t = p.value / max
          return t > 0.75 ? '#f5222d' : t > 0.45 ? '#ff9f43' : '#2b5cff'
        }
      }
    }]
  }
})

const trendOption = computed(() => {
  const d = dailyTrend.value
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: 44, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', boundaryGap: false, data: d.map((x) => x.date.slice(5)),
      axisLabel: { color: '#8a9099', fontSize: 11 }, axisLine: { lineStyle: { color: '#e5e7eb' } } },
    yAxis: { type: 'value', splitLine: { lineStyle: { color: '#f0f2f5' } }, axisLabel: { color: '#8a9099', fontSize: 11 } },
    series: [{
      type: 'line', smooth: true, showSymbol: d.length <= 40, symbolSize: 5, data: d.map((x) => x.count),
      lineStyle: { color: '#2b5cff', width: 2 }, itemStyle: { color: '#2b5cff' },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [{ offset: 0, color: 'rgba(43,92,255,0.25)' }, { offset: 1, color: 'rgba(43,92,255,0.02)' }] } }
    }]
  }
})

const loading = ref(true)
const loadError = ref(false)

async function loadAll() {
  loading.value = true
  loadError.value = false
  try {
    const res = await profileApi.detail(distinctId)
    profile.value = res.profile
    events.value = res.recentEvents || []
    Object.assign(stats, res.stats || {})
    eventTypeDist.value = res.eventTypeDist || []
    topPages.value = res.topPages || []
    topEvents.value = res.topEvents || []
    dailyTrend.value = res.dailyTrend || []
    hourDist.value = res.hourDist || []
    sessions.value = res.sessions || []
    Object.assign(derived, res.derived || {})
    genInsight()
  } catch (e) {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(loadAll)
</script>

<style scoped>
.ph { margin-bottom: 12px; }
.ph-title { font-weight: 600; }
.ph-tag { margin-left: 8px; vertical-align: middle; }
.tag { margin-right: 6px; }
.sub-label { color: #8a9099; font-size: 12px; margin-top: 4px; }

/* ranked list (top pages / top events) */
.rank-list { display: flex; flex-direction: column; gap: 12px; }
.rank-item { display: flex; align-items: center; gap: 10px; }
.rank-no {
  flex: 0 0 auto; width: 20px; height: 20px; border-radius: 5px; font-size: 12px; font-weight: 600;
  display: flex; align-items: center; justify-content: center; background: #eef0f4; color: #8a9099;
}
.rank-no.top { background: #2b5cff; color: #fff; }
.rank-body { flex: 1; min-width: 0; }
.rank-name { font-size: 13px; color: #1f2329; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.rank-sub { font-size: 11px; color: #a0a6af; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; margin-bottom: 3px; }
.rank-body :deep(.el-progress) { margin-top: 4px; }
.rank-count { flex: 0 0 auto; min-width: 34px; text-align: right; font-size: 14px; font-weight: 600; color: #2b5cff; font-variant-numeric: tabular-nums; }

/* custom properties key/value grid */
.kv-grid { display: flex; flex-wrap: wrap; gap: 8px 22px; }
.kv { display: inline-flex; align-items: baseline; gap: 6px; font-size: 13px; }
.kv-k { color: #8a9099; }
.kv-v { color: #1f2329; font-weight: 500; }

/* commerce & behaviour highlights */
.derived-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(150px, 1fr)); gap: 14px; }
.derived-item { background: #f8fafc; border: 1px solid #eef0f4; border-radius: 10px; padding: 14px 16px; }
.d-label { color: #8a9099; font-size: 12px; margin-bottom: 6px; }
.d-value { font-size: 24px; font-weight: 700; color: #1f2329; font-variant-numeric: tabular-nums; }
.d-value small { font-size: 13px; font-weight: 500; color: #8a9099; margin-left: 2px; }
.d-value.accent { color: #f5222d; }
.kw-block { margin-top: 16px; }
.kw-list { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.kw-tag { display: inline-flex; align-items: center; gap: 5px; }
.kw-count {
  display: inline-block; min-width: 16px; padding: 0 5px; height: 16px; line-height: 16px;
  border-radius: 8px; background: #2b5cff; color: #fff; font-size: 11px; text-align: center;
}

/* timeline entry */
.tl-head { display: flex; align-items: center; gap: 8px; }
.tl-detail { font-size: 12px; margin-top: 3px; }

/* ---- AI insight card ---- */
.ai-card { border: 1px solid #e6ebff; background: linear-gradient(180deg, #f7f9ff 0%, #ffffff 42%); }
.ai-title { display: inline-flex; align-items: center; gap: 8px; }
.ai-badge {
  display: inline-block; padding: 1px 7px; border-radius: 6px; font-size: 11px; font-weight: 700;
  color: #fff; background: linear-gradient(135deg, #6a3cff, #2b5cff); letter-spacing: .5px;
}

.ai-empty { display: flex; flex-direction: column; align-items: center; gap: 14px; padding: 26px 20px; text-align: center; }
.ai-empty-icon { font-size: 40px; color: #2b5cff; opacity: .8; }
.ai-empty-text { color: #6b7280; font-size: 13px; line-height: 1.7; max-width: 560px; }
.ai-empty-text b { color: #2b5cff; }

.ai-loading { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 40px 0; color: #2b5cff; font-size: 14px; }
.ai-spin { font-size: 20px; }

.ai-body { padding-top: 4px; }
.ai-top { display: flex; gap: 22px; align-items: center; }
.ai-score :deep(.el-progress__text) { margin: 0; }
.score-num { font-size: 30px; font-weight: 800; line-height: 1; color: #1f2329; }
.score-cap { font-size: 11px; color: #8a9099; margin-top: 4px; }
.ai-meta { flex: 1; min-width: 0; }
.ai-persona { font-size: 17px; font-weight: 700; color: #1f2329; margin-bottom: 8px; }
.ai-tags { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 10px; }
.ai-time { font-size: 12px; color: #a0a6af; }
.ai-summary { margin: 0; font-size: 13px; line-height: 1.85; color: #4b5563; }

.ai-cols { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-top: 20px; }
@media (max-width: 900px) { .ai-cols { grid-template-columns: 1fr; } .ai-top { flex-direction: column; align-items: flex-start; } }
.ai-col-title { display: flex; align-items: center; gap: 6px; font-size: 13px; font-weight: 600; color: #1f2329; margin-bottom: 10px; }
.ai-col-title .el-icon { color: #2b5cff; }
.ai-list { list-style: none; margin: 0; padding: 0; display: flex; flex-direction: column; gap: 10px; }
.ai-list li { display: flex; align-items: baseline; gap: 8px; font-size: 13px; line-height: 1.6; }
.ai-list .dot { flex: 0 0 auto; width: 7px; height: 7px; border-radius: 50%; margin-top: 6px; background: #8a9099; }
.ai-list .dot.positive, .ai-list .dot.success { background: #00c2a8; }
.ai-list .dot.info { background: #2b5cff; }
.ai-list .dot.neutral { background: #a0a6af; }
.ai-list .dot.warning { background: #ff9f43; }
.ai-list .dot.danger { background: #f5222d; }
.li-label { flex: 0 0 auto; font-weight: 600; color: #1f2329; }
.li-label::after { content: '·'; margin-left: 6px; color: #c4c9d2; }
.li-text { color: #4b5563; }

.ai-recs { margin-top: 20px; padding-top: 18px; border-top: 1px dashed #e5e7eb; }
.ai-rec-list { margin: 0; padding-left: 20px; display: flex; flex-direction: column; gap: 9px; }
.ai-rec-list li { font-size: 13px; line-height: 1.7; color: #374151; }
.ai-rec-list li::marker { color: #2b5cff; font-weight: 700; }

.ai-foot { display: flex; align-items: center; gap: 6px; margin-top: 18px; padding: 10px 12px;
  background: #f4f6fa; border-radius: 8px; font-size: 12px; color: #8a9099; line-height: 1.6; }
.ai-foot .el-icon { flex: 0 0 auto; }
</style>
