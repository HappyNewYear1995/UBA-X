<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">解析规则</h2>
    <p class="page-sub">事件入库前的配置化处理：字段映射 / 属性抽取 / 过滤丢弃 / 脱敏。按优先级（小者先）顺序应用；全局开关、采样率、IP 脱敏由「参数设置」中的 collect.* 项驱动</p>

    <div class="card">
      <div class="toolbar">
        <el-select v-model="appId" placeholder="应用范围" style="width: 200px" @change="reload">
          <el-option label="全部应用 (appId=0)" :value="0" />
          <el-option v-for="a in appStore.apps" :key="a.id" :label="a.name" :value="a.id" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增规则</el-button>
        <div class="hint">match/action 为 JSON；action.op ∈ set / rename / extract / drop / anonymize</div>
      </div>

      <el-table :data="rows" size="small" stripe>
        <el-table-column prop="priority" label="优先级" width="80" />
        <el-table-column prop="name" label="名称" min-width="130" show-overflow-tooltip />
        <el-table-column label="作用域" width="130">
          <template #default="{ row }">{{ row.appId === 0 ? '全部应用' : appName(row.appId) }}</template>
        </el-table-column>
        <el-table-column label="匹配 (match)" min-width="180">
          <template #default="{ row }"><code class="json">{{ row.matchExpr || '{}' }}</code></template>
        </el-table-column>
        <el-table-column label="动作 (action)" min-width="180">
          <template #default="{ row }"><code class="json">{{ row.action || '{}' }}</code></template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled" size="small" @change="(v) => toggle(row, v)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        style="margin-top:12px;justify-content:flex-end"
        layout="total, prev, pager, next"
        :total="total" :page-size="query.size" :current-page="query.page"
        @current-change="onPage"
      />
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑解析规则' : '新增解析规则'" width="600px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如 drop-heartbeat" /></el-form-item>
        <el-form-item label="优先级"><el-input-number v-model="form.priority" :min="0" :max="9999" /></el-form-item>
        <el-form-item label="作用应用">
          <el-select v-model="form.appId" style="width:100%">
            <el-option label="全部应用 (appId=0)" :value="0" />
            <el-option v-for="a in appStore.apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="匹配 match">
          <el-input v-model="form.matchExpr" type="textarea" :rows="3" placeholder='JSON，如 {"type":"heartbeat"} 或 {"path":{"prefix":"/admin"}}' />
        </el-form-item>
        <el-form-item label="动作 action">
          <el-input v-model="form.action" type="textarea" :rows="3" placeholder='JSON，如 {"op":"drop"} / {"op":"set","field":"type","value":"custom"} / {"op":"extract","from":"properties.plan","to":"title"}' />
        </el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { governanceApi } from '../../api'
import { useAppStore } from '../../store/app'

const appStore = useAppStore()
const rows = ref([])
const total = ref(0)
const loading = ref(false)
const appId = ref(0)
const query = reactive({ page: 1, size: 20 })
const visible = ref(false)
const editing = ref(false)
const form = reactive({ id: null, name: '', priority: 100, appId: 0, matchExpr: '{}', action: '{}', remark: '' })

function appName(id) { return appStore.apps.find((a) => a.id === id)?.name || ('#' + id) }

async function load() {
  loading.value = true
  try {
    const res = await governanceApi.rules({ appId: appId.value, page: query.page - 1, size: query.size })
    rows.value = res.rows || []
    total.value = res.total || 0
  } catch (e) { rows.value = []; total.value = 0 } finally { loading.value = false }
}
function reload() { query.page = 1; load() }
function onPage(p) { query.page = p; load() }

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, name: '', priority: 100, appId: appId.value, matchExpr: '{}', action: '{}', remark: '' })
  visible.value = true
}
function openEdit(row) {
  editing.value = true
  Object.assign(form, { ...row, matchExpr: row.matchExpr || '{}', action: row.action || '{}' })
  visible.value = true
}

function validJson(s, label) {
  if (!s || !s.trim()) return true
  try { JSON.parse(s); return true } catch (e) { ElMessage.warning(label + ' 不是合法 JSON'); return false }
}

async function save() {
  if (!form.name) { ElMessage.warning('名称必填'); return }
  if (!validJson(form.matchExpr, 'match')) return
  if (!validJson(form.action, 'action')) return
  const payload = { ...form }
  if (editing.value) await governanceApi.updateRule(form.id, payload)
  else await governanceApi.createRule(payload)
  ElMessage.success('已保存'); visible.value = false; await load()
}

async function toggle(row, v) {
  await governanceApi.toggleRule(row.id, v)
  row.enabled = v
  ElMessage.success(v ? '已启用' : '已停用')
}

async function remove(row) {
  try { await ElMessageBox.confirm(`确认删除规则「${row.name}」？`, '提示', { type: 'warning' }) } catch (e) { return }
  await governanceApi.removeRule(row.id); ElMessage.success('已删除'); await load()
}

watch(() => appStore.currentAppId, () => { appId.value = appStore.currentAppId || 0; reload() })
onMounted(async () => { await appStore.load(); appId.value = appStore.currentAppId || 0; await load() })
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 14px; align-items: center; flex-wrap: wrap; }
.hint { font-size: 12px; color: #9aa2af; }
.json { font-size: 12px; color: #4a5262; background: #f5f7fa; padding: 2px 6px; border-radius: 4px; display: inline-block; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
