<template>
  <div class="page">
    <h2 class="page-title">用户画像</h2>
    <p class="page-sub">基于行为事件自动构建的访客画像与分群标签</p>

    <div class="stat-grid">
      <div class="stat-card"><div class="label">总用户数</div><div class="value">{{ compact(stats.total) }}</div></div>
      <div class="stat-card" v-for="(v, k) in stats.tags" :key="k">
        <div class="label">{{ k }}</div><div class="value">{{ compact(v) }}</div>
      </div>
    </div>

    <div class="card">
      <div class="toolbar">
        <el-input v-model="kw" placeholder="搜索访客ID / 用户ID / 地区" clearable style="width: 280px" :prefix-icon="Search" @keyup.enter="search" @clear="search" />
        <el-button type="primary" @click="search">查询</el-button>
      </div>
      <el-table :data="rows" size="small" stripe @row-click="openDetail">
        <el-table-column prop="distinctId" label="访客ID" width="130" />
        <el-table-column prop="userId" label="用户ID" width="130">
          <template #default="{ row }">{{ row.userId || '-' }}</template>
        </el-table-column>
        <el-table-column label="标签" min-width="180">
          <template #default="{ row }">
            <el-tag v-for="t in splitTags(row.tags)" :key="t" size="small" class="tag" effect="light">{{ t }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="visitCount" label="访问次数" width="90" />
        <el-table-column prop="eventCount" label="事件数" width="90" />
        <el-table-column prop="pageviewCount" label="浏览页数" width="90" />
        <el-table-column label="设备" width="150">
          <template #default="{ row }">{{ row.device }} / {{ row.os }}</template>
        </el-table-column>
        <el-table-column label="地区" width="140">
          <template #default="{ row }">{{ [row.region, row.city].filter(Boolean).join(' ') || '-' }}</template>
        </el-table-column>
        <el-table-column label="最近活跃" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.lastSeen) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        class="pager"
        layout="total, prev, pager, next"
        :total="total"
        :page-size="size"
        :current-page="page"
        @current-change="onPage"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { profileApi } from '../api'
import { compact, fmtDateTime } from '../utils/date'

const router = useRouter()
const rows = ref([])
const stats = reactive({ total: 0, tags: {} })
const kw = ref('')
const page = ref(1)
const size = ref(15)
const total = ref(0)

function splitTags(t) { return (t || '').split(',').filter(Boolean) }

async function load() {
  const res = await profileApi.list({ kw: kw.value || undefined, page: page.value, size: size.value })
  rows.value = res.records
  total.value = res.total
}
async function loadStats() { Object.assign(stats, await profileApi.stats()) }
function search() { page.value = 1; load() }
function onPage(p) { page.value = p; load() }
function openDetail(row) { router.push('/profiles/' + encodeURIComponent(row.distinctId)) }

onMounted(() => { load(); loadStats() })
</script>

<style scoped>
.tag { margin-right: 4px; }
.pager { margin-top: 14px; justify-content: flex-end; }
:deep(.el-table__row) { cursor: pointer; }
</style>
