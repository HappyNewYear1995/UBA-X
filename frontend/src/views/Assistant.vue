<template>
  <div class="ai-page">
    <!-- ============ 对话管理侧栏 ============ -->
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sb-head">
        <span v-show="!sidebarCollapsed" class="sb-title">对话记录</span>
        <el-button
          class="new-btn"
          type="primary"
          :icon="Plus"
          :circle="sidebarCollapsed"
          :text="sidebarCollapsed"
          @click="newChat"
        >
          <span v-show="!sidebarCollapsed">新建对话</span>
        </el-button>
      </div>

      <div v-show="!sidebarCollapsed" class="sb-list">
        <div v-if="!conversations.length" class="sb-empty">暂无历史对话</div>
        <div
          v-for="c in conversations"
          :key="c.id"
          class="conv"
          :class="{ active: c.id === activeId }"
          @click="switchConv(c.id)"
        >
          <el-icon class="conv-ico"><ChatLineRound /></el-icon>
          <div class="conv-body">
            <div class="conv-title">{{ c.title }}</div>
            <div class="conv-time">{{ relTime(c.updatedAt) }} · {{ c.messages.length }} 条</div>
          </div>
          <el-dropdown trigger="click" @command="(cmd) => onConvCmd(cmd, c)" @click.stop>
            <el-icon class="conv-more" @click.stop><MoreFilled /></el-icon>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :icon="EditPen" command="rename">重命名</el-dropdown-item>
                <el-dropdown-item :icon="Delete" command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <div v-show="!sidebarCollapsed" class="sb-foot">
        <el-button text :icon="Delete" size="small" :disabled="!conversations.length" @click="clearAll">
          清空全部
        </el-button>
      </div>
    </aside>

    <!-- ============ 对话主区 ============ -->
    <section class="main">
      <header class="chat-head">
        <div class="ch-left">
          <el-icon class="fold" @click="sidebarCollapsed = !sidebarCollapsed">
            <Expand v-if="sidebarCollapsed" /><Fold v-else />
          </el-icon>
          <div class="ch-title">
            <span class="t">{{ activeConv?.title || 'AI 数据助手' }}</span>
            <span class="s">自然语言查询行为数据 · 生成报表与图表 · 应用范围：{{ appStore.currentApp?.name || '全部应用' }}</span>
          </div>
        </div>
        <div class="ch-right">
          <el-tooltip content="重新生成上一条回答" placement="bottom">
            <el-button :icon="RefreshLeft" circle size="small" :disabled="!canRegen" @click="regenerate" />
          </el-tooltip>
          <el-tooltip content="清空当前对话" placement="bottom">
            <el-button :icon="Delete" circle size="small" :disabled="!activeConv?.messages.length" @click="clearCurrent" />
          </el-tooltip>
        </div>
      </header>

      <div ref="scroller" class="chat">
        <!-- 空状态引导 -->
        <div v-if="!activeConv || !activeConv.messages.length" class="hero">
          <div class="hero-badge"><el-icon><ChatDotRound /></el-icon></div>
          <h3>你好，我是 UBA-X 数据助手</h3>
          <p>用一句话查询数据、生成报表和图表。试试下面的示例：</p>
          <div class="hero-cards">
            <div v-for="(s, i) in starters" :key="i" class="hcard" @click="ask(s.text)">
              <el-icon class="hcard-ico" :style="{ color: s.color }"><component :is="s.icon" /></el-icon>
              <div class="hcard-t">{{ s.title }}</div>
              <div class="hcard-d">{{ s.text }}</div>
            </div>
          </div>
        </div>

        <!-- 消息 -->
        <div v-for="(m, i) in activeConv?.messages || []" :key="i" class="msg" :class="m.role">
          <div class="avatar">
            <el-icon v-if="m.role === 'ai'"><ChatDotRound /></el-icon>
            <span v-else>{{ userInitial }}</span>
          </div>
          <div class="bubble">
            <!-- AI 回复分区块渲染 -->
            <template v-if="m.role === 'ai'">
              <div v-if="m.loading" class="thinking">
                <span class="dot"></span><span class="dot"></span><span class="dot"></span>
                <em>正在分析数据…</em>
              </div>
              <template v-else>
                <div class="rich">
                  <template v-for="(b, bi) in parseReply(m.text)" :key="bi">
                    <div v-if="b.type === 'gap'" class="gap"></div>
                    <div v-else-if="b.type === 'h'" class="sec-h">{{ b.text }}</div>
                    <div v-else-if="b.type === 'li'" class="li"><span class="bullet"></span>{{ b.text }}</div>
                    <div v-else-if="b.type === 'ol'" class="li ol">{{ b.text }}</div>
                    <div v-else class="p">{{ b.text }}</div>
                  </template>
                </div>

                <div v-if="m.table && m.table.rows && m.table.rows.length" class="tbl">
                  <el-table :data="m.table.rows" size="small" border max-height="320">
                    <el-table-column
                      v-for="c in m.table.columns"
                      :key="c.key"
                      :prop="c.key"
                      :label="c.label"
                      :min-width="['date','path','metric','name'].includes(c.key) ? 130 : 90"
                      show-overflow-tooltip
                    />
                  </el-table>
                </div>

                <div v-if="m.chart" class="chart">
                  <BaseChart :option="m.chart" height="320px" />
                </div>
              </template>
            </template>

            <!-- 用户消息 -->
            <div v-else class="text">{{ m.text }}</div>

            <!-- 消息操作条 -->
            <div v-if="!m.loading" class="msg-foot">
              <span class="ts">{{ fmtTime(m.ts) }}</span>
              <span v-if="m.role === 'ai'" class="acts">
                <el-tooltip content="复制回答" placement="top">
                  <el-icon class="act" @click="copyMsg(m)"><CopyDocument /></el-icon>
                </el-tooltip>
                <el-tooltip v-if="m.suggestions && m.suggestions.length" content="收起/展开推荐" placement="top">
                  <el-icon class="act" @click="m._showSug = !m._showSug"><MagicStick /></el-icon>
                </el-tooltip>
              </span>
            </div>

            <div v-if="m.role === 'ai' && m._showSug && m.suggestions && m.suggestions.length" class="suggests">
              <el-tag v-for="(s, si) in m.suggestions" :key="si" class="sg" effect="plain" round @click="ask(s)">{{ s }}</el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="composer">
        <el-input
          v-model="input"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 8 }"
          resize="none"
          maxlength="500"
          show-word-limit
          placeholder="输入你的问题，例如：生成近一周的访问数据报表 / 近一周访问方式的饼图 / 今日的PV是多少（Enter 发送，Shift+Enter 换行）"
          @keydown.enter.exact.prevent="send"
        />
        <el-button class="send" type="primary" :icon="Promotion" :loading="busy" @click="send">发送</el-button>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Delete, EditPen, ChatDotRound, ChatLineRound, Promotion,
  Fold, Expand, CopyDocument, MoreFilled, RefreshLeft, MagicStick,
  Odometer, PieChart, TrendCharts, Document
} from '@element-plus/icons-vue'
import BaseChart from '../components/BaseChart.vue'
import { assistantApi } from '../api'
import { useAppStore } from '../store/app'
import { useAuthStore } from '../store/auth'

const appStore = useAppStore()
const auth = useAuthStore()
const userInitial = computed(() => (auth.user?.nickname || auth.user?.username || 'U').slice(0, 1).toUpperCase())

const STORE_KEY = computed(() => `ubax_ai_convs_${auth.user?.username || 'anon'}`)

const conversations = ref([])
const activeId = ref(null)
const input = ref('')
const busy = ref(false)
const scroller = ref(null)
const sidebarCollapsed = ref(false)

const activeConv = computed(() => conversations.value.find((c) => c.id === activeId.value) || null)
const canRegen = computed(() => {
  const msgs = activeConv.value?.messages || []
  return msgs.length >= 2 && msgs[msgs.length - 2]?.role === 'user'
})

const starters = [
  { title: '今日核心指标', text: '今日的 PV 和 UV 是多少？', icon: Odometer, color: '#2b5cff' },
  { title: '生成访问报表', text: '生成近一周的访问数据报表', icon: Document, color: '#00c2a8' },
  { title: '访问方式占比', text: '近一周访问方式的饼图', icon: PieChart, color: '#ff9f43' },
  { title: '访问趋势', text: '最近14天的访问趋势', icon: TrendCharts, color: '#6a3cff' }
]

// ---------------- 持久化 ----------------
function persist() {
  try {
    const slim = conversations.value.map((c) => ({
      ...c,
      messages: c.messages.map((m) => ({ role: m.role, text: m.text, table: m.table || null, chart: m.chart || null, suggestions: m.suggestions || [], ts: m.ts, _showSug: false }))
    }))
    localStorage.setItem(STORE_KEY.value, JSON.stringify(slim.slice(0, 50)))
  } catch (e) { /* quota exceeded - ignore */ }
}
function loadStore() {
  try {
    const raw = localStorage.getItem(STORE_KEY.value)
    if (raw) {
      const arr = JSON.parse(raw)
      if (Array.isArray(arr)) { conversations.value = arr; return }
    }
  } catch (e) { /* ignore */ }
  conversations.value = []
}

// ---------------- 会话管理 ----------------
function uid() { return Date.now().toString(36) + Math.random().toString(36).slice(2, 7) }

function newChat(silent) {
  const c = { id: uid(), title: '新对话', createdAt: Date.now(), updatedAt: Date.now(), messages: [] }
  conversations.value.unshift(c)
  activeId.value = c.id
  if (!silent) { persist(); scrollBottom() }
  return c
}
function switchConv(id) { activeId.value = id; scrollBottom() }

async function onConvCmd(cmd, c) {
  if (cmd === 'rename') {
    try {
      const { value } = await ElMessageBox.prompt('请输入新的对话名称', '重命名', {
        inputValue: c.title, inputPattern: /\S+/, inputErrorMessage: '名称不能为空', confirmButtonText: '保存', cancelButtonText: '取消'
      })
      c.title = value.trim().slice(0, 30)
      persist()
    } catch (e) { /* cancelled */ }
  } else if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm(`确定删除对话「${c.title}」？`, '删除对话', { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' })
      const idx = conversations.value.findIndex((x) => x.id === c.id)
      conversations.value.splice(idx, 1)
      if (activeId.value === c.id) activeId.value = conversations.value[0]?.id || null
      persist()
    } catch (e) { /* cancelled */ }
  }
}
async function clearCurrent() {
  if (!activeConv.value) return
  try {
    await ElMessageBox.confirm('清空当前对话的所有消息？', '清空', { type: 'warning', confirmButtonText: '清空', cancelButtonText: '取消' })
    activeConv.value.messages = []
    activeConv.value.title = '新对话'
    persist()
  } catch (e) { /* cancelled */ }
}
async function clearAll() {
  try {
    await ElMessageBox.confirm('删除全部历史对话？此操作不可恢复。', '清空全部', { type: 'warning', confirmButtonText: '全部删除', cancelButtonText: '取消' })
    conversations.value = []
    activeId.value = null
    persist()
  } catch (e) { /* cancelled */ }
}

// ---------------- 发送 ----------------
async function ask(text) { input.value = text; send() }

async function send() {
  const text = input.value.trim()
  if (!text || busy.value) return
  if (!activeConv.value) newChat(true)
  const conv = activeConv.value
  if (conv.title === '新对话') conv.title = text.slice(0, 20)

  conv.messages.push({ role: 'user', text, ts: Date.now() })
  const holder = { role: 'ai', loading: true, ts: Date.now() }
  conv.messages.push(holder)
  conv.updatedAt = Date.now()
  moveConvTop(conv)
  input.value = ''
  busy.value = true
  await scrollBottom()

  try {
    const data = await assistantApi.chat({ message: text, appId: appStore.currentAppId })
    Object.assign(holder, {
      loading: false,
      text: data.reply || '（无回答）',
      table: data.table || null,
      chart: data.chart || null,
      suggestions: data.suggestions || [],
      _showSug: false,
      ts: Date.now()
    })
  } catch (e) {
    Object.assign(holder, { loading: false, text: '请求失败：' + (e?.message || e), ts: Date.now() })
  } finally {
    conv.updatedAt = Date.now()
    moveConvTop(conv)
    busy.value = false
    persist()
    await scrollBottom()
  }
}

async function regenerate() {
  // Guard: send() early-returns while busy, which would leave the spliced
  // messages lost with nothing re-sent. Never mutate history mid-flight.
  if (busy.value) return
  const msgs = activeConv.value?.messages || []
  if (msgs.length < 2) return
  const userMsg = msgs[msgs.length - 2]
  const text = (userMsg?.text || '').trim()
  if (!text) return
  // drop the trailing user + ai pair; send() re-adds the user message
  msgs.splice(msgs.length - 2, 2)
  const saved = input.value
  input.value = text
  try {
    await send()
  } finally {
    // preserve anything the user had typed (send() clears input on success)
    if (saved && !busy.value) input.value = saved
  }
}

function moveConvTop(conv) {
  const i = conversations.value.findIndex((c) => c.id === conv.id)
  if (i > 0) { conversations.value.splice(i, 1); conversations.value.unshift(conv) }
}

function copyMsg(m) {
  const text = m.text || ''
  navigator.clipboard?.writeText(text)
    .then(() => ElMessage.success('已复制到剪贴板'))
    .catch(() => ElMessage.warning('复制失败，请手动选择文本'))
}

// ---------------- 渲染辅助 ----------------
function parseReply(text) {
  if (!text) return []
  return text.split('\n').flatMap((line) => {
    const trimmed = line.replace(/\s+$/, '').trim()
    if (!trimmed) return [{ type: 'gap' }]
    // 行首 【标题】，其后可能同行跟正文，拆成 标题 + 段落
    const lead = trimmed.match(/^【(.+?)】\s*(.*)$/)
    if (lead) {
      const blocks = [{ type: 'h', text: lead[1] }]
      if (lead[2]) blocks.push({ type: 'p', text: lead[2] })
      return blocks
    }
    if (/^[·•\-]\s*/.test(trimmed)) return [{ type: 'li', text: trimmed.replace(/^[·•\-]\s*/, '') }]
    if (/^\d+\.\s*/.test(trimmed)) return [{ type: 'ol', text: trimmed }]
    return [{ type: 'p', text: trimmed }]
  })
}
function fmtTime(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}
function relTime(ts) {
  if (!ts) return ''
  const diff = Date.now() - ts
  const min = Math.floor(diff / 60000)
  if (min < 1) return '刚刚'
  if (min < 60) return `${min} 分钟前`
  const hr = Math.floor(min / 60)
  if (hr < 24) return `${hr} 小时前`
  const day = Math.floor(hr / 24)
  if (day < 30) return `${day} 天前`
  return new Date(ts).toLocaleDateString('zh-CN')
}
async function scrollBottom() {
  await nextTick()
  if (scroller.value) scroller.value.scrollTop = scroller.value.scrollHeight
}

// 切换应用时不影响历史，但提示一下当前范围
watch(() => appStore.currentAppId, () => { /* appId is read at send time */ })

onMounted(async () => {
  await appStore.load()
  loadStore()
  if (!conversations.value.length) newChat(true)
  else activeId.value = conversations.value[0].id
  scrollBottom()
})
</script>

<style scoped>
.ai-page { display: flex; height: calc(100vh - 58px); background: #f4f6fa; overflow: hidden; }

/* ---------- sidebar ---------- */
.sidebar { width: 248px; flex: 0 0 248px; background: #fff; border-right: 1px solid #eef0f4; display: flex; flex-direction: column; transition: width .18s, flex-basis .18s; }
.sidebar.collapsed { width: 56px; flex-basis: 56px; }
.sb-head { display: flex; align-items: center; justify-content: space-between; gap: 8px; padding: 12px; border-bottom: 1px solid #f0f2f5; }
.sb-title { font-size: 13px; font-weight: 600; color: #6b7280; }
.new-btn { flex: 1; }
.sidebar.collapsed .new-btn { flex: none; }
.sb-list { flex: 1; overflow-y: auto; padding: 8px; }
.sb-empty { text-align: center; color: #b0b6c0; font-size: 12.5px; padding: 24px 0; }
.conv { display: flex; align-items: center; gap: 8px; padding: 9px 10px; border-radius: 9px; cursor: pointer; margin-bottom: 4px; border: 1px solid transparent; }
.conv:hover { background: #f5f7fb; }
.conv.active { background: #eef3ff; border-color: #d7e2ff; }
.conv-ico { color: #8a94a6; flex: 0 0 auto; }
.conv.active .conv-ico { color: #2b5cff; }
.conv-body { flex: 1; min-width: 0; }
.conv-title { font-size: 13.5px; color: #1f2733; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.conv.active .conv-title { color: #2b5cff; font-weight: 600; }
.conv-time { font-size: 11.5px; color: #a2a9b5; margin-top: 2px; }
.conv-more { color: #c2c8d2; opacity: 0; transition: opacity .15s; flex: 0 0 auto; }
.conv:hover .conv-more { opacity: 1; }
.conv-more:hover { color: #2b5cff; }
.sb-foot { padding: 8px 12px; border-top: 1px solid #f0f2f5; }

/* ---------- main ---------- */
.main { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.chat-head { display: flex; align-items: center; justify-content: space-between; padding: 10px 18px; background: #fff; border-bottom: 1px solid #eef0f4; }
.ch-left { display: flex; align-items: center; gap: 12px; min-width: 0; }
.fold { font-size: 18px; color: #6b7280; cursor: pointer; }
.fold:hover { color: #2b5cff; }
.ch-title { min-width: 0; }
.ch-title .t { display: block; font-size: 15px; font-weight: 600; color: #1f2733; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ch-title .s { display: block; font-size: 12px; color: #9aa2af; margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.ch-right { display: flex; gap: 8px; }

.chat { flex: 1; overflow-y: auto; padding: 22px 24px; }

/* hero / empty */
.hero { max-width: 760px; margin: 6vh auto 0; text-align: center; }
.hero-badge { width: 56px; height: 56px; margin: 0 auto 14px; border-radius: 16px; display: flex; align-items: center; justify-content: center; font-size: 26px; color: #fff; background: linear-gradient(135deg, #2b5cff, #6a3cff); box-shadow: 0 8px 20px rgba(43,92,255,.28); }
.hero h3 { margin: 0 0 6px; font-size: 20px; color: #1f2733; }
.hero p { margin: 0 0 22px; color: #8a94a6; font-size: 13.5px; }
.hero-cards { display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px; }
.hcard { text-align: left; background: #fff; border: 1px solid #eef0f4; border-radius: 12px; padding: 14px; cursor: pointer; transition: all .16s; }
.hcard:hover { border-color: #c7d6ff; box-shadow: 0 6px 18px rgba(43,92,255,.1); transform: translateY(-2px); }
.hcard-ico { font-size: 20px; }
.hcard-t { font-size: 14px; font-weight: 600; color: #1f2733; margin: 8px 0 3px; }
.hcard-d { font-size: 12.5px; color: #8a94a6; }

/* messages */
.msg { display: flex; gap: 12px; margin-bottom: 22px; max-width: 900px; }
.msg.user { flex-direction: row-reverse; margin-left: auto; }
.avatar { flex: 0 0 34px; width: 34px; height: 34px; border-radius: 10px; display: flex; align-items: center; justify-content: center; color: #fff; font-weight: 600; font-size: 14px; background: linear-gradient(135deg, #2b5cff, #6a3cff); }
.msg.user .avatar { background: linear-gradient(135deg, #00c2a8, #2b5cff); }
.bubble { max-width: min(760px, 84%); background: #fff; border: 1px solid #eef0f4; border-radius: 4px 14px 14px 14px; padding: 12px 15px; box-shadow: 0 1px 3px rgba(16,24,40,.05); }
.msg.user .bubble { background: linear-gradient(135deg, #2b5cff, #4a72ff); border: none; color: #fff; border-radius: 14px 4px 14px 14px; }
.msg.user .text { white-space: pre-wrap; word-break: break-word; line-height: 1.6; font-size: 14px; }

.rich { font-size: 14px; color: #1f2733; line-height: 1.75; }
.rich .gap { height: 7px; }
.rich .sec-h { font-weight: 700; color: #2b5cff; margin: 8px 0 4px; font-size: 13.5px; letter-spacing: .3px; }
.rich .sec-h:first-child { margin-top: 0; }
.rich .p { word-break: break-word; }
.rich .li { display: flex; gap: 8px; align-items: baseline; padding: 1px 0; word-break: break-word; }
.rich .li .bullet { flex: 0 0 5px; width: 5px; height: 5px; border-radius: 50%; background: #2b5cff; transform: translateY(-2px); }
.rich .li.ol { display: block; }

.tbl { margin-top: 12px; }
.chart { margin-top: 12px; }

.msg-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 9px; padding-top: 7px; border-top: 1px dashed #eef0f4; }
.msg.user .msg-foot { border-top-color: rgba(255,255,255,.25); }
.ts { font-size: 11px; color: #b0b6c0; }
.msg.user .ts { color: rgba(255,255,255,.75); }
.acts { display: flex; gap: 10px; }
.act { font-size: 14px; color: #b0b6c0; cursor: pointer; }
.act:hover { color: #2b5cff; }
.msg.user .act { color: rgba(255,255,255,.8); }
.msg.user .act:hover { color: #fff; }

.suggests { margin-top: 10px; display: flex; flex-wrap: wrap; gap: 8px; }
.sg { cursor: pointer; }
.sg:hover { color: #2b5cff; border-color: #2b5cff; }

.thinking { display: flex; align-items: center; gap: 4px; color: #8a94a6; font-size: 13px; }
.thinking em { margin-left: 6px; font-style: normal; }
.thinking .dot { width: 6px; height: 6px; border-radius: 50%; background: #b6bfcc; animation: blink 1.2s infinite ease-in-out; }
.thinking .dot:nth-child(2) { animation-delay: .2s; }
.thinking .dot:nth-child(3) { animation-delay: .4s; }
@keyframes blink { 0%, 80%, 100% { opacity: .3; } 40% { opacity: 1; } }

/* composer */
.composer { display: flex; gap: 10px; align-items: flex-end; padding: 12px 24px 16px; background: #f4f6fa; width: 100%; max-width: 880px; margin: 0 auto; box-sizing: border-box; }
.composer :deep(.el-textarea__inner) { border-radius: 12px; padding: 12px 14px; box-shadow: 0 1px 3px rgba(16,24,40,.06); font-size: 14px; line-height: 1.6; }
.send { height: 40px; border-radius: 10px; }
</style>
