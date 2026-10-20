<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">用户标识</h2>
    <p class="page-sub">配置身份归并策略，并查询 distinctId → 统一身份 的绑定关系。SDK 调用 identify(userId) 或 POST /api/collect/identify 后，登录前后事件归并到同一画像</p>

    <div class="card">
      <div class="card-title">
        <span>归并策略</span>
        <el-button type="primary" size="small" :icon="Plus" @click="openCreate">新增策略</el-button>
      </div>
      <el-table :data="rules" size="small" stripe>
        <el-table-column label="策略" width="200">
          <template #default="{ row }"><el-tag size="small" effect="plain">{{ strategyLabel(row.strategy) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="作用域" width="150">
          <template #default="{ row }">{{ row.appId === 0 ? '全部应用' : appName(row.appId) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.enabled ? 'success' : 'info'">{{ row.enabled ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="removeRule(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div class="card" style="margin-top:16px">
      <div class="card-title">
        <span>身份绑定关系 (alias)</span>
        <el-select v-model="aliasAppId" placeholder="应用范围" size="small" style="width: 180px" @change="reloadAlias">
          <el-option label="全部应用" :value="0" />
          <el-option v-for="a in appStore.apps" :key="a.id" :label="a.name" :value="a.id" />
        </el-select>
      </div>
      <el-table :data="alias" size="small" stripe>
        <el-table-column prop="distinctId" label="匿名 distinctId" min-width="200" show-overflow-tooltip />
        <el-table-column prop="unifiedId" label="统一身份 unifiedId" min-width="160" show-overflow-tooltip />
        <el-table-column label="作用域" width="140">
          <template #default="{ row }">{{ row.appId === 0 ? '全部应用' : appName(row.appId) }}</template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="110" />
        <el-table-column label="绑定时间" width="180">
          <template #default="{ row }">{{ fmtDateTime(row.mergedAt) }}</template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top:12px;justify-content:flex-end"
        layout="total, prev, pager, next"
        :total="aliasTotal" :page-size="aliasQuery.size" :current-page="aliasQuery.page"
        @current-change="onAliasPage"
      />
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑归并策略' : '新增归并策略'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="策略">
          <el-select v-model="form.strategy" style="width:100%">
            <el-option label="distinctId 优先 (DISTINCT_FIRST)" value="DISTINCT_FIRST" />
            <el-option label="userId 优先 (USER_FIRST)" value="USER_FIRST" />
            <el-option label="仅显式绑定 (EXPLICIT)" value="EXPLICIT" />
          </el-select>
        </el-form-item>
        <el-form-item label="作用应用">
          <el-select v-model="form.appId" style="width:100%">
            <el-option label="全部应用 (appId=0)" :value="0" />
            <el-option v-for="a in appStore.apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用"><el-switch v-model="form.enabled" /></el-form-item>
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
import { fmtDateTime } from '../../utils/date'

const appStore = useAppStore()
const loading = ref(false)
const rules = ref([])
const alias = ref([])
const aliasTotal = ref(0)
const aliasAppId = ref(0)
const aliasQuery = reactive({ page: 1, size: 20 })
const visible = ref(false)
const editing = ref(false)
const form = reactive({ id: null, strategy: 'DISTINCT_FIRST', appId: 0, enabled: true, remark: '' })

function strategyLabel(s) { return { DISTINCT_FIRST: 'distinctId 优先', USER_FIRST: 'userId 优先', EXPLICIT: '仅显式绑定' }[s] || s }
function appName(id) { return appStore.apps.find((a) => a.id === id)?.name || ('#' + id) }

async function loadRules() {
  try { rules.value = await governanceApi.identity({}) } catch (e) { rules.value = [] }
}
async function loadAlias() {
  try {
    const res = await governanceApi.alias({ appId: aliasAppId.value, page: aliasQuery.page - 1, size: aliasQuery.size })
    alias.value = res.rows || []
    aliasTotal.value = res.total || 0
  } catch (e) { alias.value = []; aliasTotal.value = 0 }
}
async function loadAll() {
  loading.value = true
  try { await Promise.all([loadRules(), loadAlias()]) } finally { loading.value = false }
}
function reloadAlias() { aliasQuery.page = 1; loadAlias() }
function onAliasPage(p) { aliasQuery.page = p; loadAlias() }

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, strategy: 'DISTINCT_FIRST', appId: aliasAppId.value, enabled: true, remark: '' })
  visible.value = true
}
function openEdit(row) { editing.value = true; Object.assign(form, row); visible.value = true }

async function save() {
  await governanceApi.saveIdentity({ ...form })
  ElMessage.success('已保存'); visible.value = false; await loadRules()
}
async function removeRule(row) {
  try { await ElMessageBox.confirm('确认删除该归并策略？', '提示', { type: 'warning' }) } catch (e) { return }
  await governanceApi.removeIdentity(row.id); ElMessage.success('已删除'); await loadRules()
}

watch(() => appStore.currentAppId, () => { aliasAppId.value = appStore.currentAppId || 0; reloadAlias() })
onMounted(async () => { await appStore.load(); aliasAppId.value = appStore.currentAppId || 0; await loadAll() })
</script>
