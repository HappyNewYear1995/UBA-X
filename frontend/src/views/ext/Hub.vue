<template>
  <div class="ext-shell" :class="{ split: hasApp }">
    <!-- Left grouped app nav, shown once an app is open (gallery -> in-app transform) -->
    <aside v-if="hasApp" class="ext-nav">
      <div class="nav-head" @click="router.push('/ext')" title="返回应用列表">
        <el-icon class="nh-ico"><ArrowLeft /></el-icon>
        <span class="nh-txt">数据应用</span>
      </div>
      <div class="nav-scroll">
        <div v-for="g in GROUPS" :key="g.title" class="nav-group">
          <div class="nav-group-title">{{ g.title }}</div>
          <div
            v-for="a in g.apps" :key="a.path"
            class="nav-item" :class="{ active: isActive(a.path) }"
            @click="router.push(a.path)"
          >
            <span class="ni-ico" :style="{ background: isActive(a.path) ? a.color : a.grad }">
              <el-icon :size="14"><component :is="a.icon" /></el-icon>
            </span>
            <span class="ni-name">{{ a.name }}</span>
          </div>
        </div>
      </div>
    </aside>

    <!-- Right pane: gallery at /ext, embedded app view otherwise -->
    <div class="ext-main">
      <router-view v-slot="{ Component }">
        <keep-alive>
          <component :is="Component" />
        </keep-alive>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { GROUPS } from './apps'

const route = useRoute()
const router = useRouter()

// An app is open when we are on a nested /ext/<app> route (not the gallery index).
const hasApp = computed(() => route.path.startsWith('/ext/') && route.path.length > 5)
function isActive(path) { return route.path === path }
</script>

<style scoped>
.ext-shell { display: flex; height: 100%; min-height: 0; }
.ext-shell.split .ext-main { border-left: 1px solid #eef0f4; }

.ext-nav {
  flex: 0 0 208px; width: 208px; display: flex; flex-direction: column;
  background: #fbfcfe; min-height: 0;
}
.nav-head {
  display: flex; align-items: center; gap: 8px; padding: 14px 16px;
  font-size: 14px; font-weight: 700; color: #1f2733; cursor: pointer;
  border-bottom: 1px solid #eef0f4; user-select: none;
}
.nav-head:hover { color: #2b5cff; }
.nh-ico { font-size: 15px; }
.nav-scroll { flex: 1; overflow-y: auto; padding: 8px 10px 16px; }
.nav-group + .nav-group { margin-top: 14px; }
.nav-group-title {
  font-size: 11px; color: #9aa2af; font-weight: 600; letter-spacing: .04em;
  padding: 6px 8px; text-transform: none;
}
.nav-item {
  display: flex; align-items: center; gap: 9px; padding: 8px 9px; margin-top: 2px;
  border-radius: 8px; cursor: pointer; transition: background .15s, color .15s;
  color: #4a5262; font-size: 13.5px;
}
.nav-item:hover { background: #eef2fb; }
.nav-item.active { background: #e8efff; color: #2b5cff; font-weight: 600; }
.ni-ico {
  flex: 0 0 auto; width: 24px; height: 24px; border-radius: 7px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.ni-name { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

.ext-main { flex: 1; min-width: 0; overflow-y: auto; }
</style>
