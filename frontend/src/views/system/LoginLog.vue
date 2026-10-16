<template>
  <div class="page">
    <h2 class="page-title">登录日志</h2>
    <p class="page-sub">用户登录 / 登出记录，含 IP、归属地与成功状态</p>

    <RangeBar :initial-days="14" @change="onRange">
      <el-input v-model="query.kw" placeholder="搜索用户名/IP" clearable style="width: 200px" :prefix-icon="Search" @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.success" placeholder="状态" clearable style="width: 120px" @change="reload">
        <el-option label="成功" :value="true" />
        <el-option label="失败" :value="false" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
    </RangeBar>

    <div class="card">
      <el-table :data="rows" size="small" stripe v-loading="loading">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" width="130" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'LOGIN' ? 'primary' : 'info'" size="small" effect="plain">
              {{ row.type === 'LOGIN' ? '登录' : '登出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="150" />
        <el-table-column prop="location" label="归属地" width="120" />
        <el-table-column label="客户端" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ row.userAgent || '—' }}</template>
        </el-table-column>
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.success ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="detail" label="说明" min-width="160" show-overflow-tooltip />
      </el-table>
      <el-pagination
        style="margin-top:12px;justify-content:flex-end"
        layout="total, sizes, prev, pager, next"
        :total="total"
        :page-size="query.size"
        :current-page="query.page"
        :page-sizes="[20, 50, 100]"
        @current-change="onPage"
        @size-change="onSize"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search } from '@element-plus/icons-vue'
import RangeBar from '../../components/RangeBar.vue'
import { systemApi } from '../../api'
import { fmtDateTime, lastNDays } from '../../utils/date'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ kw: '', success: '', from: lastNDays(14)[0], to: lastNDays(14)[1], page: 1, size: 20 })

async function load() {
  loading.value = true
  try {
    const params = { ...query, page: query.page - 1 }
    Object.keys(params).forEach((k) => { if (params[k] === '' || params[k] == null) delete params[k] })
    const res = await systemApi.loginLog(params)
    rows.value = res.rows || []
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
function reload() { query.page = 1; load() }
function onRange(r) { query.from = r.from; query.to = r.to; reload() }
function onPage(p) { query.page = p; load() }
function onSize(s) { query.size = s; reload() }

onMounted(load)
</script>
