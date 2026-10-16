<template>
  <el-container class="layout">
    <el-aside :width="collapsed ? '64px' : '220px'" class="aside">
      <div class="logo">
        <span class="logo-badge">UBA</span>
        <span v-show="!collapsed" class="logo-text">UBA-X 行为分析</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :default-openeds="openedGroups"
        :collapse="collapsed"
        router
        background-color="#111827"
        text-color="#c7ccd6"
        active-text-color="#fff"
        class="menu"
      >
        <template v-for="m in menus" :key="m.key || m.path">
          <el-sub-menu v-if="m.type === 'group'" :index="m.key">
            <template #title>
              <el-icon><component :is="m.icon" /></el-icon>
              <span>{{ m.title }}</span>
            </template>
            <el-menu-item v-for="c in m.children" :key="c.path" :index="c.path">
              <el-icon><component :is="c.icon" /></el-icon>
              <template #title>{{ c.title }}</template>
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="m.path">
            <el-icon><component :is="m.icon" /></el-icon>
            <template #title>{{ m.title }}</template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="left">
          <el-icon class="collapse-btn" @click="collapsed = !collapsed">
            <Fold v-if="!collapsed" /><Expand v-else />
          </el-icon>
          <span class="crumb">{{ currentTitle }}</span>
        </div>
        <div class="right">
          <el-select
            v-model="appSelect"
            placeholder="选择应用"
            size="default"
            style="width: 180px"
          >
            <el-option label="全部应用" value="" />
            <el-option v-for="a in appStore.apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
          <el-button link type="primary" @click="openDemo">
            <el-icon><Position /></el-icon> 采集示例页
          </el-button>
          <el-popover placement="bottom-end" :width="340" trigger="click" popper-class="notice-pop">
            <template #reference>
              <el-badge :value="notices.length" :hidden="!notices.length" :max="99" class="bell-badge">
                <el-icon class="bell"><Bell /></el-icon>
              </el-badge>
            </template>
            <div class="notice-head">通知公告</div>
            <div v-if="!notices.length" class="notice-empty">暂无公告</div>
            <div v-else class="notice-list">
              <div v-for="n in notices" :key="n.id" class="notice-item" @click="openNotice(n)">
                <div class="ni-title">
                  <el-tag size="small" :type="n.type === 'ANNOUNCE' ? 'warning' : 'info'" effect="plain">{{ n.type === 'ANNOUNCE' ? '公告' : '通知' }}</el-tag>
                  <span v-if="n.top" class="ni-top">置顶</span>
                  <span class="ni-t">{{ n.title }}</span>
                </div>
                <div class="ni-time">{{ fmtDateTime(n.publishedAt) }}</div>
              </div>
            </div>
          </el-popover>
          <el-dropdown @command="onCommand">
            <span class="user">
              <el-avatar :size="28" class="avatar">{{ initials }}</el-avatar>
              <span class="uname">{{ auth.user?.nickname || auth.user?.username }}</span>
              <el-tag size="small" effect="plain" class="role-tag">{{ roleLabel }}</el-tag>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>{{ auth.user?.tenantName || '平台' }}</el-dropdown-item>
                <el-dropdown-item command="account" divided>
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout">
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main">
        <router-view v-slot="{ Component }">
          <keep-alive :max="6">
            <component :is="Component" :key="route.fullPath" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>

    <el-dialog v-model="noticeVisible" :title="currentNotice.title" width="520px">
      <div class="notice-meta">
        <el-tag size="small" :type="currentNotice.type === 'ANNOUNCE' ? 'warning' : 'info'" effect="plain">
          {{ currentNotice.type === 'ANNOUNCE' ? '公告' : '通知' }}
        </el-tag>
        <span class="muted">{{ currentNotice.publisher }} · {{ fmtDateTime(currentNotice.publishedAt) }}</span>
      </div>
      <p class="notice-body">{{ currentNotice.content }}</p>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { useAppStore } from '../store/app'
import { systemApi, accountApi } from '../api'
import { fmtDateTime } from '../utils/date'

const auth = useAuthStore()
const appStore = useAppStore()
const route = useRoute()
const router = useRouter()
const collapsed = ref(false)
const notices = ref([])
const noticeVisible = ref(false)
const currentNotice = ref({})

const roleLabels = { SUPER_ADMIN: '平台管理员', ADMIN: '租户管理员', ANALYST: '分析师', VIEWER: '访客' }
const roleLabel = computed(() => roleLabels[auth.role] || auth.role)
const initials = computed(() => (auth.user?.nickname || auth.user?.username || 'U').slice(0, 1))

// el-option cannot bind null, so proxy "全部应用" as '' while the store keeps null.
const appSelect = computed({
  get: () => (appStore.currentAppId == null ? '' : appStore.currentAppId),
  set: (v) => appStore.setCurrent(v === '' ? null : v)
})

const GROUPS = {
  behavior: { title: '行为分析', icon: 'TrendCharts' },
  insight: { title: '用户洞察', icon: 'Compass' },
  collect: { title: '数据采集', icon: 'Connection' },
  system: { title: '系统管理', icon: 'Setting' }
}

const layoutChildren = () => {
  const layout = router.options.routes.find((r) => r.path === '/')
  const tenantless = auth.user && auth.user.tenantId == null
  return (layout?.children || [])
    .filter((c) => !c.meta?.hidden)
    .filter((c) => !c.meta?.superAdmin || auth.isSuperAdmin)
    .filter((c) => !c.meta?.perm || auth.has(c.meta.perm))
    // Platform super admins have no tenant, so tenant-scoped pages are hidden.
    .filter((c) => !c.meta?.tenant || !tenantless)
}

// Build a two-level menu tree: routes with meta.group nest under a parent sub-menu.
const menus = computed(() => {
  const tree = []
  const index = {}
  for (const c of layoutChildren()) {
    const item = { path: '/' + c.path, title: c.meta.title, icon: c.meta.icon }
    const g = c.meta?.group
    if (!g || !GROUPS[g]) { tree.push({ type: 'item', ...item }); continue }
    if (!index[g]) {
      index[g] = { type: 'group', key: g, title: GROUPS[g].title, icon: GROUPS[g].icon, children: [] }
      tree.push(index[g])
    }
    index[g].children.push(item)
  }
  return tree
})

const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/profiles')) return '/profiles'
  if (p.startsWith('/ext/')) return '/ext'
  return p
})

// Auto-expand the group that owns the current route.
const openedGroups = computed(() => {
  const active = activeMenu.value.replace(/^\//, '')
  const cur = layoutChildren().find((c) => c.path === active)
  const g = cur?.meta?.group
  return g && GROUPS[g] ? [g] : []
})
const currentTitle = computed(() => route.meta?.title || '')

function openDemo() {
  window.open('/demo/', '_blank')
}
function openNotice(n) {
  currentNotice.value = n
  noticeVisible.value = true
}
async function loadNotices() {
  try { notices.value = await systemApi.publishedNotices() } catch { notices.value = [] }
}
async function onCommand(cmd) {
  if (cmd === 'account') {
    router.push('/account')
  } else if (cmd === 'logout') {
    try { await accountApi.logout() } catch { /* best-effort server-side log */ }
    auth.logout()
    router.push('/login')
  }
}

onMounted(() => {
  appStore.load()
  loadNotices()
})
</script>

<style scoped>
.layout { height: 100vh; }
.aside { background: #111827; transition: width .2s; overflow-x: hidden; }
.logo { height: 58px; display: flex; align-items: center; gap: 10px; padding: 0 16px; color: #fff; border-bottom: 1px solid #1f2937; }
.logo-badge { background: linear-gradient(135deg,#2b5cff,#6a3cff); border-radius: 7px; padding: 3px 7px; font-weight: 700; font-size: 13px; }
.logo-text { font-size: 15px; font-weight: 600; white-space: nowrap; }
.menu { border-right: none; }
.menu :deep(.el-sub-menu__title) { font-weight: 600; }
.menu :deep(.el-sub-menu .el-menu-item) { background-color: #0d1421 !important; height: 44px; line-height: 44px; font-size: 13.5px; }
.menu :deep(.el-sub-menu .el-menu-item:hover) { background-color: #1a2438 !important; }
.menu :deep(.el-sub-menu .el-menu-item.is-active) { background-color: #22304f !important; color: #fff !important; }
.menu :deep(.el-menu-item.is-active) { background-color: #22304f !important; }
.header { background: #fff; border-bottom: 1px solid #eef0f4; display: flex; align-items: center; justify-content: space-between; height: 58px; }
.header .left { display: flex; align-items: center; gap: 14px; }
.collapse-btn { cursor: pointer; font-size: 19px; color: #5a6270; }
.crumb { font-size: 15px; font-weight: 600; }
.header .right { display: flex; align-items: center; gap: 16px; }
.user { display: flex; align-items: center; gap: 8px; cursor: pointer; }
.avatar { background: linear-gradient(135deg,#2b5cff,#6a3cff); color: #fff; }
.uname { font-size: 14px; }
.role-tag { margin-left: 2px; }
.main { background: #f4f6fa; padding: 0; }

.bell-badge { display: flex; align-items: center; }
.bell { font-size: 19px; color: #5a6270; cursor: pointer; }
.bell:hover { color: #2b5cff; }
.notice-head { font-size: 14px; font-weight: 600; padding-bottom: 8px; border-bottom: 1px solid #eef0f4; margin-bottom: 6px; }
.notice-empty { color: #9aa2af; font-size: 13px; text-align: center; padding: 18px 0; }
.notice-list { max-height: 320px; overflow-y: auto; }
.notice-item { padding: 9px 6px; border-radius: 8px; cursor: pointer; border-bottom: 1px solid #f4f6fa; }
.notice-item:hover { background: #f2f6ff; }
.ni-title { display: flex; align-items: center; gap: 6px; }
.ni-top { font-size: 11px; color: #e6453d; border: 1px solid #ffd6d3; border-radius: 4px; padding: 0 4px; }
.ni-t { font-size: 13.5px; color: #1f2733; font-weight: 500; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.ni-time { font-size: 11.5px; color: #9aa2af; margin-top: 4px; }
.notice-meta { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.notice-body { font-size: 14px; line-height: 1.8; color: #485061; margin: 0; white-space: pre-wrap; }
</style>
