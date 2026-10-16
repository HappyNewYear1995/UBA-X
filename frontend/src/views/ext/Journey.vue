<template>
  <div class="page">
    <h2 class="page-title">用户旅程时间线</h2>
    <p class="page-sub">按会话还原单个用户的完整行为时间线，用于用户故事级排查</p>

    <div class="toolbar">
      <el-select
        v-model="distinctId"
        filterable
        placeholder="选择用户"
        size="default"
        style="width: 300px"
        @change="loadJourney"
      >
        <el-option v-for="u in users" :key="u.distinctId" :label="u.distinctId + '（' + u.region + ' · ' + u.eventCount + ' 事件）'" :value="u.distinctId" />
      </el-select>
      <el-button :icon="Refresh" circle @click="loadUsers" />
    </div>

    <template v-if="profile.distinctId">
      <div class="card prof">
        <div class="prof-grid">
          <div class="pi"><span class="k">用户</span><span class="v">{{ profile.distinctId }}</span></div>
          <div class="pi"><span class="k">地域</span><span class="v">{{ profile.region }} {{ profile.city }}</span></div>
          <div class="pi"><span class="k">设备</span><span class="v">{{ profile.device }} · {{ profile.os }} · {{ profile.browser }}</span></div>
          <div class="pi"><span class="k">首次</span><span class="v">{{ fmt(profile.firstSeen) }}</span></div>
          <div class="pi"><span class="k">最近</span><span class="v">{{ fmt(profile.lastSeen) }}</span></div>
          <div class="pi"><span class="k">访问 / 事件</span><span class="v">{{ profile.visitCount }} / {{ profile.eventCount }}</span></div>
          <div class="pi"><span class="k">标签</span><span class="v"><el-tag v-for="t in tagList" :key="t" size="small" effect="plain" round style="margin-right:4px">{{ t }}</el-tag></span></div>
        </div>
      </div>

      <div class="stat-grid">
        <div class="stat-card"><div class="label">会话数</div><div class="value">{{ sessions.length }}</div></div>
        <div class="stat-card"><div class="label">事件总数</div><div class="value">{{ totalEvents }}</div></div>
      </div>

      <div v-for="(s, i) in sessions" :key="s.sessionId" class="card">
        <div class="card-title">
          会话 {{ sessions.length - i }} · {{ s.sessionId }}
          <span class="sess-meta">{{ fmt(s.start) }} → {{ fmt(s.end) }} · {{ s.events }} 事件</span>
        </div>
        <el-timeline class="tl">
          <el-timeline-item
            v-for="(st, j) in s.steps" :key="j"
            :timestamp="fmtTime(st.time)"
            :type="dotType(st.type)"
            placement="top"
          >
            <div class="step">
              <el-tag size="small" :type="tagType(st.type)" effect="plain">{{ st.type }}</el-tag>
              <span class="step-name">{{ st.name }}</span>
              <span v-if="st.url" class="step-url">{{ st.url }}</span>
              <span v-if="st.duration > 0" class="step-dur">停留 {{ (st.duration / 1000).toFixed(0) }}s</span>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
      <el-empty v-if="!sessions.length" description="该用户暂无行为记录" />
    </template>
    <el-empty v-else description="请选择一个用户查看旅程" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { extApi } from '../../api'
import { useAppStore } from '../../store/app'

const appStore = useAppStore()
const users = ref([])
const distinctId = ref('')
const profile = reactive({})
const sessions = ref([])
const totalEvents = ref(0)
const loading = ref(false)

const tagList = computed(() => (profile.tags ? String(profile.tags).split(',').filter(Boolean) : []))

function fmt(t) { return t && t !== '—' ? String(t).replace('T', ' ').slice(0, 16) : '—' }
function fmtTime(t) { return t && t !== '—' ? String(t).replace('T', ' ').slice(5, 19) : '—' }
function dotType(type) { return { pageview: 'primary', click: 'warning', event: 'success', custom: 'success', leave: 'info' }[type] || 'primary' }
function tagType(type) { return { pageview: '', click: 'warning', event: 'success', custom: 'success', leave: 'info' }[type] || 'info' }

function resetProfile() {
  for (const k of Object.keys(profile)) delete profile[k]
}

async function loadUsers() {
  loading.value = true
  try {
    const d = await extApi.journeyUsers({ appId: appStore.currentAppId })
    users.value = d.users || []
    if (users.value.length) {
      // Keep the current selection only if it still exists for this app.
      if (!distinctId.value || !users.value.some((u) => u.distinctId === distinctId.value)) {
        distinctId.value = users.value[0].distinctId
      }
      await loadJourney()
    } else {
      distinctId.value = ''
      resetProfile(); sessions.value = []; totalEvents.value = 0
    }
  } catch (e) {
    users.value = []
  } finally {
    loading.value = false
  }
}
async function loadJourney() {
  if (!distinctId.value) return
  try {
    const d = await extApi.journey({ distinctId: distinctId.value })
    // Replace rather than merge so a previous user's attributes never linger.
    resetProfile()
    Object.assign(profile, d.profile || {})
    sessions.value = d.sessions || []
    totalEvents.value = d.totalEvents || 0
  } catch (e) {
    resetProfile(); sessions.value = []; totalEvents.value = 0
  }
}

onMounted(loadUsers)
watch(() => appStore.currentAppId, () => { distinctId.value = ''; loadUsers() })
</script>

<style scoped>
.prof-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(240px, 1fr)); gap: 10px 20px; }
.pi { display: flex; gap: 8px; font-size: 13px; align-items: baseline; }
.pi .k { color: #8a94a6; flex: 0 0 70px; }
.pi .v { color: #1f2733; font-weight: 500; }
.sess-meta { color: #8a94a6; font-size: 12px; font-weight: 400; margin-left: 8px; }
.tl { padding-left: 4px; margin-top: 6px; }
.step { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.step-name { font-size: 13.5px; color: #1f2733; font-weight: 500; }
.step-url { color: #8a94a6; font-size: 12px; }
.step-dur { color: #00a870; font-size: 12px; }
</style>
