<template>
  <div class="gal">
    <h2 class="gal-title">数据应用</h2>
    <p class="gal-sub">面向不同场景的数据扩展应用，点击进入使用</p>

    <div v-for="g in GROUPS" :key="g.title" class="gal-group">
      <div class="gal-group-title">{{ g.title }}</div>
      <div class="gal-grid">
        <div v-for="a in g.apps" :key="a.path" class="gcard" @click="router.push(a.path)">
          <span class="gcard-ico" :style="{ background: a.grad }">
            <el-icon :size="16"><component :is="a.icon" /></el-icon>
          </span>
          <div class="gcard-body">
            <div class="gcard-name">{{ a.name }}</div>
            <div class="gcard-desc">{{ a.desc }}</div>
          </div>
          <el-icon class="gcard-arrow"><ArrowRight /></el-icon>
        </div>
      </div>
    </div>

    <div v-if="SOON.length" class="gal-group">
      <div class="gal-group-title">规划中</div>
      <div class="gal-grid">
        <div v-for="s in SOON" :key="s.name" class="gcard soon">
          <span class="gcard-ico" style="background:#eef1f6;color:#9aa2af">
            <el-icon :size="16"><Plus /></el-icon>
          </span>
          <div class="gcard-body">
            <div class="gcard-name">{{ s.name }}</div>
            <div class="gcard-desc">{{ s.desc }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ArrowRight, Plus } from '@element-plus/icons-vue'
import { GROUPS, SOON } from './apps'

const router = useRouter()
</script>

<style scoped>
.gal { padding: 20px 24px; }
.gal-title { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
.gal-sub { color: #8a94a6; font-size: 13px; margin: 0 0 20px; }

.gal-group + .gal-group { margin-top: 22px; }
.gal-group-title {
  font-size: 13px; font-weight: 600; color: #5a6270; margin-bottom: 10px;
  padding-left: 9px; border-left: 3px solid #2b5cff; line-height: 1.2;
}
.gal-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(248px, 1fr)); gap: 12px; }

.gcard {
  position: relative; background: #fff; border: 1px solid #eef0f4; border-radius: 10px;
  padding: 13px 14px; cursor: pointer; display: flex; align-items: flex-start; gap: 11px;
  transition: transform .16s, box-shadow .16s, border-color .16s;
}
.gcard:hover { transform: translateY(-2px); box-shadow: 0 8px 18px rgba(31,45,61,.09); border-color: #d6e2ff; }
.gcard.soon { cursor: default; opacity: .72; }
.gcard.soon:hover { transform: none; box-shadow: none; border-color: #eef0f4; }

.gcard-ico {
  flex: 0 0 auto; width: 32px; height: 32px; border-radius: 9px;
  display: flex; align-items: center; justify-content: center; color: #fff;
}
.gcard-body { flex: 1; min-width: 0; }
.gcard-name { font-size: 14px; font-weight: 600; color: #1f2733; }
.gcard-desc {
  color: #8a94a6; font-size: 12px; line-height: 1.5; margin-top: 4px;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden;
}
.gcard-arrow {
  position: absolute; right: 12px; top: 15px; color: #2b5cff; font-size: 14px;
  opacity: 0; transition: opacity .16s;
}
.gcard:hover .gcard-arrow { opacity: 1; }
</style>
