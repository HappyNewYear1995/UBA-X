<template>
  <div class="page">
    <h2 class="page-title">定时任务</h2>
    <p class="page-sub">系统内置维护任务的启停、手动执行与运行历史</p>

    <div class="card">
      <div class="card-title">任务列表</div>
      <el-table :data="tasks" size="small" stripe>
        <el-table-column prop="name" label="任务名称" min-width="150" />
        <el-table-column prop="code" label="任务标识" width="180">
          <template #default="{ row }"><span class="mono">{{ row.code }}</span></template>
        </el-table-column>
        <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="启用" width="90">
          <template #default="{ row }">
            <el-switch v-model="row.enabled" @change="(v) => onToggle(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="上次执行" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.lastRunAt) }}</template>
        </el-table-column>
        <el-table-column label="上次结果" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.lastStatus" :type="statusType(row.lastStatus)" size="small">{{ row.lastStatus }}</el-tag>
            <span v-else class="muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="runCount" label="执行次数" width="90" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" :loading="running === row.id" @click="onRun(row)">立即执行</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card">
      <div class="card-title">
        执行历史
        <el-select v-model="filterCode" placeholder="全部任务" clearable size="small" style="width:200px;margin-left:12px" @change="loadLogs">
          <el-option v-for="t in tasks" :key="t.code" :label="t.name" :value="t.code" />
        </el-select>
        <el-button :icon="Refresh" size="small" style="margin-left:8px" @click="loadLogs">刷新</el-button>
      </div>
      <el-table :data="logs" size="small" stripe>
        <el-table-column prop="taskName" label="任务" min-width="140" />
        <el-table-column label="开始时间" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.startAt) }}</template>
        </el-table-column>
        <el-table-column label="耗时" width="100">
          <template #default="{ row }">{{ dur(row.startAt, row.endAt) }}</template>
        </el-table-column>
        <el-table-column label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="affected" label="影响行数" width="100" />
        <el-table-column label="触发方式" width="100">
          <template #default="{ row }">
            <el-tag :type="row.triggerType === 'MANUAL' ? 'warning' : 'info'" size="small" effect="plain">
              {{ row.triggerType === 'MANUAL' ? '手动' : '自动' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="message" label="消息" min-width="240" show-overflow-tooltip />
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import { systemApi } from '../../api'
import { fmtDateTime } from '../../utils/date'

const tasks = ref([])
const logs = ref([])
const filterCode = ref('')
const running = ref(null)

function statusType(s) {
  if (s === 'SUCCESS') return 'success'
  if (s === 'FAILED' || s === 'ERROR') return 'danger'
  if (s === 'RUNNING') return 'warning'
  return 'info'
}
function dur(a, b) {
  if (!a || !b) return '—'
  const ms = new Date(b) - new Date(a)
  if (isNaN(ms) || ms < 0) return '—'
  if (ms < 1000) return ms + ' ms'
  return (ms / 1000).toFixed(2) + ' s'
}

async function loadTasks() { tasks.value = await systemApi.tasks() }
async function loadLogs() {
  const params = { limit: 50 }
  if (filterCode.value) params.code = filterCode.value
  logs.value = await systemApi.taskLogs(params)
}
async function onToggle(row, v) {
  try {
    await systemApi.toggleTask(row.id, v)
    ElMessage.success(v ? '已启用' : '已停用')
  } catch (e) {
    row.enabled = !v
  }
}
async function onRun(row) {
  running.value = row.id
  try {
    const res = await systemApi.runTask(row.id)
    ElMessage.success(res?.message || '执行完成')
    await Promise.all([loadTasks(), loadLogs()])
  } finally {
    running.value = null
  }
}

onMounted(async () => { await loadTasks(); await loadLogs() })
</script>
