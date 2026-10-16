<template>
  <div class="page">
    <h2 class="page-title">会话回放</h2>
    <p class="page-sub">按会话还原用户完整行为轨迹（页面浏览、点击、业务事件时序重放）</p>

    <RangeBar @change="onRange" />

    <div class="session-layout">
      <div class="card session-list">
        <div class="card-title">会话列表 <span class="muted">({{ sessions.length }})</span></div>
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: current && current.sessionId === s.sessionId }"
          @click="selectSession(s)"
        >
          <div class="si-top">
            <span class="mono sid">{{ s.sessionId }}</span>
            <el-tag size="small" effect="plain">{{ s.events }} 事件</el-tag>
          </div>
          <div class="muted si-sub">
            {{ fmtDateTime(s.start) }} · 时长 {{ fmtDuration(s.duration) }}
          </div>
          <div class="muted si-sub">访客 {{ s.distinctId }} · 入口 {{ s.entry || '-' }}</div>
        </div>
        <el-empty v-if="!sessions.length" description="暂无会话" :image-size="70" />
      </div>

      <div class="card player" v-if="current">
        <div class="card-title">
          回放 · {{ current.sessionId }}
          <div class="controls">
            <el-button size="small" :icon="RefreshLeft" @click="step(-1)" :disabled="idx <= 0" />
            <el-button size="small" :type="playing ? 'warning' : 'primary'" :icon="playing ? VideoPause : VideoPlay" @click="togglePlay">{{ playing ? '暂停' : '播放' }}</el-button>
            <el-button size="small" :icon="RefreshRight" @click="step(1)" :disabled="idx >= frames.length - 1" />
          </div>
        </div>

        <div class="viewport" ref="vpRef">
          <div class="vp-bar">
            <span class="dot r"></span><span class="dot y"></span><span class="dot g"></span>
            <span class="vp-url mono">{{ currentFrame.url || ('https://shop.demo.com' + (currentFrame.path || '')) }}</span>
          </div>
          <div class="vp-body">
            <div class="vp-title">{{ currentFrame.title || currentFrame.path || '页面' }}</div>
            <div class="vp-hint muted">事件 {{ idx + 1 }} / {{ frames.length }} · {{ currentFrame.type }} · {{ currentFrame.event }}</div>
            <transition name="fade">
              <div v-if="currentFrame.type === 'click' && currentFrame.x != null" class="cursor"
                   :style="{ left: cursorX + 'px', top: cursorY + 'px' }">
                <div class="cursor-ring"></div>
                <div class="cursor-label mono">{{ currentFrame.element }}</div>
              </div>
            </transition>
          </div>
        </div>

        <el-slider v-model="idx" :max="Math.max(0, frames.length - 1)" :show-tooltip="false" class="scrub" />

        <el-table :data="frames" size="small" height="260" highlight-current-row @current-change="onRowPick" ref="tblRef">
          <el-table-column type="index" width="50" />
          <el-table-column label="时间" width="160">
            <template #default="{ row }">{{ fmtDateTime(row.time) }}</template>
          </el-table-column>
          <el-table-column prop="type" label="类型" width="90">
            <template #default="{ row }"><el-tag size="small" :type="tagType(row.type)">{{ row.type }}</el-tag></template>
          </el-table-column>
          <el-table-column prop="event" label="事件" width="130" />
          <el-table-column prop="path" label="页面" min-width="120" show-overflow-tooltip />
          <el-table-column prop="element" label="元素/详情" min-width="120" show-overflow-tooltip />
        </el-table>
      </div>
      <div class="card player" v-else>
        <el-empty description="从左侧选择一个会话开始回放" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { VideoPlay, VideoPause, RefreshLeft, RefreshRight } from '@element-plus/icons-vue'
import RangeBar from '../components/RangeBar.vue'
import { analyticsApi } from '../api'
import { useAppStore } from '../store/app'
import { fmtDateTime, fmtDuration, lastNDays } from '../utils/date'

const appStore = useAppStore()
const sessions = ref([])
const current = ref(null)
const frames = ref([])
const idx = ref(0)
const playing = ref(false)
const vpRef = ref(null)
const loading = ref(false)
let timer = null
let detailSeq = 0
const range = reactive({ from: lastNDays(7)[0], to: lastNDays(7)[1] })

const currentFrame = computed(() => frames.value[idx.value] || {})
const cursorX = computed(() => {
  const w = vpRef.value?.clientWidth || 640
  return Math.min(w - 20, (currentFrame.value.x || 0) * (w / 1440))
})
const cursorY = computed(() => {
  const raw = currentFrame.value.y || 0
  return 40 + Math.min(240, (raw % 900) * (280 / 900))
})

function tagType(t) { return { pageview: 'primary', click: 'warning', event: 'success', leave: 'info' }[t] || 'info' }

async function loadSessions() {
  loading.value = true
  try {
    const rows = await analyticsApi.sessions({ appId: appStore.currentAppId, from: range.from, to: range.to, limit: 40 })
    sessions.value = Array.isArray(rows) ? rows : (rows?.rows || [])
    if (sessions.value.length) selectSession(sessions.value[0])
    else { stop(); current.value = null; frames.value = []; idx.value = 0 }
  } catch (e) {
    sessions.value = []
  } finally {
    loading.value = false
  }
}
async function selectSession(s) {
  stop()
  current.value = s
  const seq = ++detailSeq
  try {
    const framesData = await analyticsApi.sessionDetail(s.sessionId)
    if (seq !== detailSeq) return // a newer selection superseded this request
    frames.value = Array.isArray(framesData) ? framesData : (framesData?.rows || [])
    idx.value = 0
  } catch (e) {
    if (seq === detailSeq) { frames.value = []; idx.value = 0 }
  }
}
function onRange(r) { range.from = r.from; range.to = r.to; loadSessions() }
function step(d) { idx.value = Math.max(0, Math.min(frames.value.length - 1, idx.value + d)) }
function togglePlay() { playing.value ? stop() : play() }
function play() {
  if (!frames.value.length) return
  playing.value = true
  timer = setInterval(() => {
    if (idx.value >= frames.value.length - 1) { stop(); return }
    idx.value++
  }, 900)
}
function stop() { playing.value = false; timer && clearInterval(timer); timer = null }
function onRowPick(row) { if (row) idx.value = frames.value.indexOf(row) }

onMounted(async () => { await appStore.load(); loadSessions() })
watch(() => appStore.currentAppId, loadSessions)
onBeforeUnmount(stop)
</script>

<style scoped>
.session-layout { display: grid; grid-template-columns: 340px 1fr; gap: 16px; align-items: start; }
@media (max-width: 1000px) { .session-layout { grid-template-columns: 1fr; } }
.session-list { max-height: 720px; overflow-y: auto; }
.session-item { padding: 10px 12px; border-radius: 10px; cursor: pointer; border: 1px solid transparent; margin-bottom: 6px; }
.session-item:hover { background: #f5f7ff; }
.session-item.active { background: #eef2ff; border-color: #c7d6ff; }
.si-top { display: flex; justify-content: space-between; align-items: center; }
.sid { font-size: 13px; font-weight: 600; }
.si-sub { font-size: 12px; margin-top: 3px; }
.controls { display: flex; gap: 6px; }
.viewport { border: 1px solid #e5e8ef; border-radius: 10px; overflow: hidden; }
.vp-bar { background: #f0f2f5; padding: 8px 12px; display: flex; align-items: center; gap: 6px; }
.dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }
.dot.r { background: #ff5f57; } .dot.y { background: #febc2e; } .dot.g { background: #28c840; }
.vp-url { margin-left: 10px; font-size: 12px; color: #5a6270; background: #fff; padding: 2px 10px; border-radius: 6px; }
.vp-body { position: relative; height: 300px; background: linear-gradient(180deg,#fff,#f7f9fc); }
.vp-title { font-size: 22px; font-weight: 600; padding: 40px 30px 6px; }
.vp-hint { padding: 0 30px; font-size: 13px; }
.cursor { position: absolute; transform: translate(-6px, -6px); }
.cursor-ring { width: 26px; height: 26px; border-radius: 50%; border: 3px solid #f5222d; background: rgba(245,34,45,.18); }
.cursor-label { position: absolute; left: 28px; top: 0; background: #111; color: #fff; font-size: 11px; padding: 2px 7px; border-radius: 5px; white-space: nowrap; }
.scrub { margin: 16px 6px; }
.fade-enter-active, .fade-leave-active { transition: opacity .25s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>
