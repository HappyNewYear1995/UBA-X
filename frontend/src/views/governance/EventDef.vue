<template>
  <div class="page" v-loading="loading">
    <h2 class="page-title">事件字典</h2>
    <p class="page-sub">定义允许采集的事件与属性规范；上报事件命中字典则标准化命名/类型，未命中按策略处理（默认放行打标，不丢数据）</p>

    <div class="card">
      <div class="toolbar">
        <el-select v-model="appId" placeholder="应用范围" style="width: 200px" @change="reload">
          <el-option label="全部应用 (appId=0)" :value="0" />
          <el-option v-for="a in appStore.apps" :key="a.id" :label="a.name" :value="a.id" />
        </el-select>
        <el-button type="primary" :icon="Plus" @click="openCreate">新增事件定义</el-button>
      </div>

      <el-table :data="rows" size="small" stripe>
        <el-table-column prop="eventKey" label="事件键" min-width="150" show-overflow-tooltip />
        <el-table-column prop="eventName" label="显示名" min-width="130" show-overflow-tooltip />
        <el-table-column label="类型" width="110">
          <template #default="{ row }"><el-tag size="small" effect="plain">{{ typeLabel(row.type) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="作用域" width="130">
          <template #default="{ row }">{{ row.appId === 0 ? '全部应用' : appName(row.appId) }}</template>
        </el-table-column>
        <el-table-column label="未命中" width="110">
          <template #default="{ row }"><el-tag size="small" :type="missTag(row.onMiss)">{{ missLabel(row.onMiss) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="70" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled" size="small" @change="(v) => toggle(row, v)" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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

    <el-dialog v-model="visible" :title="editing ? '编辑事件定义' : '新增事件定义'" width="560px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="事件键"><el-input v-model="form.eventKey" :disabled="editing" placeholder="如 order_paid / $pageview" /></el-form-item>
        <el-form-item label="显示名"><el-input v-model="form.eventName" placeholder="如 订单支付" /></el-form-item>
        <el-form-item label="类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option label="页面浏览 pageview" value="pageview" />
            <el-option label="点击 click" value="click" />
            <el-option label="离开 leave" value="leave" />
            <el-option label="自定义 custom" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="作用应用">
          <el-select v-model="form.appId" style="width:100%">
            <el-option label="全部应用 (appId=0)" :value="0" />
            <el-option v-for="a in appStore.apps" :key="a.id" :label="a.name" :value="a.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="属性 Schema">
          <el-input v-model="form.propertiesSchema" type="textarea" :rows="4"
            placeholder='JSON，如 {"amount":{"type":"number","required":true}}' />
        </el-form-item>
        <el-form-item label="未命中策略">
          <el-select v-model="form.onMiss" style="width:100%">
            <el-option label="放行并打标 (MARK)" value="MARK" />
            <el-option label="丢弃 (DROP)" value="DROP" />
            <el-option label="降级为 $unknown (UNKNOWN)" value="UNKNOWN" />
          </el-select>
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
const form = reactive({ id: null, eventKey: '', eventName: '', type: 'custom', appId: 0, propertiesSchema: '', onMiss: 'MARK', remark: '' })

function typeLabel(t) { return { pageview: '页面浏览', click: '点击', leave: '离开', custom: '自定义' }[t] || t }
function missLabel(m) { return { MARK: '放行打标', DROP: '丢弃', UNKNOWN: '降级' }[m] || m }
function missTag(m) { return { MARK: 'success', DROP: 'danger', UNKNOWN: 'warning' }[m] || 'info' }
function appName(id) { return appStore.apps.find((a) => a.id === id)?.name || ('#' + id) }

async function load() {
  loading.value = true
  try {
    const res = await governanceApi.events({ appId: appId.value, page: query.page - 1, size: query.size })
    rows.value = res.rows || []
    total.value = res.total || 0
  } catch (e) { rows.value = []; total.value = 0 } finally { loading.value = false }
}
function reload() { query.page = 1; load() }
function onPage(p) { query.page = p; load() }

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, eventKey: '', eventName: '', type: 'custom', appId: appId.value, propertiesSchema: '', onMiss: 'MARK', remark: '' })
  visible.value = true
}
function openEdit(row) { editing.value = true; Object.assign(form, row); visible.value = true }

async function save() {
  if (!form.eventKey) { ElMessage.warning('事件键必填'); return }
  if (form.propertiesSchema && form.propertiesSchema.trim()) {
    try { JSON.parse(form.propertiesSchema) } catch (e) { ElMessage.warning('属性 Schema 不是合法 JSON'); return }
  }
  const payload = { ...form }
  if (editing.value) await governanceApi.updateEvent(form.id, payload)
  else await governanceApi.createEvent(payload)
  ElMessage.success('已保存'); visible.value = false; await load()
}

async function toggle(row, v) {
  await governanceApi.toggleEvent(row.id, v)
  row.enabled = v
  ElMessage.success(v ? '已启用' : '已停用')
}

async function remove(row) {
  try { await ElMessageBox.confirm(`确认删除事件定义「${row.eventKey}」？`, '提示', { type: 'warning' }) } catch (e) { return }
  await governanceApi.removeEvent(row.id); ElMessage.success('已删除'); await load()
}

watch(() => appStore.currentAppId, () => { appId.value = appStore.currentAppId || 0; reload() })
onMounted(async () => { await appStore.load(); appId.value = appStore.currentAppId || 0; await load() })
</script>

<style scoped>
.toolbar { display: flex; gap: 12px; margin-bottom: 14px; align-items: center; }
</style>
