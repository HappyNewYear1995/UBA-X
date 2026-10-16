<template>
  <div class="page">
    <h2 class="page-title">操作日志</h2>
    <p class="page-sub">系统内增删改与授权等敏感操作的审计记录</p>

    <RangeBar :initial-days="14" @change="onRange">
      <el-input v-model="query.kw" placeholder="搜索用户/操作/详情" clearable style="width: 220px" :prefix-icon="Search" @keyup.enter="reload" @clear="reload" />
      <el-select v-model="query.type" placeholder="操作类型" clearable style="width: 140px" @change="reload">
        <el-option label="新增" value="CREATE" />
        <el-option label="修改" value="UPDATE" />
        <el-option label="删除" value="DELETE" />
        <el-option label="授权" value="GRANT" />
        <el-option label="其他" value="OTHER" />
      </el-select>
      <el-select v-model="query.module" placeholder="模块" clearable style="width: 160px" @change="reload">
        <el-option v-for="m in modules" :key="m" :label="m" :value="m" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="reload">查询</el-button>
    </RangeBar>

    <div class="card">
      <el-table :data="rows" size="small" stripe v-loading="loading">
        <el-table-column label="时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column prop="username" label="操作人" width="120" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="action" label="操作" min-width="150" show-overflow-tooltip />
        <el-table-column prop="detail" label="详情" min-width="220" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="location" label="归属" width="110" />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small" effect="plain">{{ row.success ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
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
const modules = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ kw: '', type: '', module: '', from: lastNDays(14)[0], to: lastNDays(14)[1], page: 1, size: 20 })

function typeTag(t) {
  return { CREATE: 'success', UPDATE: 'warning', DELETE: 'danger', GRANT: 'primary', OTHER: 'info' }[t] || 'info'
}
function typeLabel(t) {
  return { CREATE: '新增', UPDATE: '修改', DELETE: '删除', GRANT: '授权', LOGIN: '登录', LOGOUT: '登出', OTHER: '其他' }[t] || t
}

async function load() {
  loading.value = true
  try {
    const params = { ...query, page: query.page - 1 }
    Object.keys(params).forEach((k) => { if (params[k] === '' || params[k] == null) delete params[k] })
    const res = await systemApi.audit(params)
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

onMounted(async () => {
  try { modules.value = await systemApi.auditModules() } catch (e) { modules.value = [] }
  await load()
})
</script>
