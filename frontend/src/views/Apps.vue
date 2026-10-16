<template>
  <div class="page">
    <h2 class="page-title">应用与数据接入</h2>
    <p class="page-sub">管理多端应用，获取 AppKey 与 SDK 接入代码</p>

    <div class="toolbar">
      <el-button type="primary" :icon="Plus" @click="openCreate">新建应用</el-button>
    </div>

    <div class="card">
      <el-table :data="apps" size="small" stripe>
        <el-table-column prop="name" label="应用名称" min-width="150" />
        <el-table-column prop="platform" label="平台" width="120">
          <template #default="{ row }"><el-tag size="small">{{ platformLabel(row.platform) }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="appKey" label="AppKey" min-width="240">
          <template #default="{ row }"><span class="mono">{{ row.appKey }}</span>
            <el-button link type="primary" size="small" :icon="CopyDocument" @click="copy(row.appKey)" />
          </template>
        </el-table-column>
        <el-table-column label="自动采集" width="90">
          <template #default="{ row }">{{ row.autoTrack ? '开启' : '关闭' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }"><el-tag size="small" :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showSdk(row)">接入代码</el-button>
            <el-button link type="warning" size="small" @click="rotate(row)">重置Key</el-button>
            <el-button link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="formVisible" :title="editing ? '编辑应用' : '新建应用'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="应用名称"><el-input v-model="form.name" placeholder="如：官网商城" /></el-form-item>
        <el-form-item label="平台">
          <el-select v-model="form.platform" style="width: 100%">
            <el-option label="Web" value="WEB" /><el-option label="H5" value="H5" />
            <el-option label="App" value="APP" /><el-option label="小程序" value="MINIPROGRAM" />
          </el-select>
        </el-form-item>
        <el-form-item label="自动采集"><el-switch v-model="form.autoTrack" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item v-if="editing" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" /><el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="sdkVisible" title="SDK 接入代码" width="680px">
      <p class="muted">将以下代码粘贴到目标站点 <code>&lt;/head&gt;</code> 前即可完成接入（同域通过 nginx 反代 <code>/api</code>）：</p>
      <pre class="snippet mono">{{ sdkSnippet }}</pre>
      <el-alert type="info" :closable="false" show-icon>
        采集示例页：<el-link type="primary" href="/demo/" target="_blank">/demo/</el-link>
        ，SDK 脚本：<el-link type="primary" :href="sdkUrl" target="_blank">{{ sdkUrl }}</el-link>
      </el-alert>
      <template #footer>
        <el-button @click="copy(sdkSnippet)" type="primary" :icon="CopyDocument">复制代码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Plus, CopyDocument } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { appApi } from '../api'
import { useAppStore } from '../store/app'

const appStore = useAppStore()
const apps = ref([])
const formVisible = ref(false)
const editing = ref(false)
const form = reactive({ id: null, name: '', platform: 'WEB', autoTrack: true, description: '', status: 'ACTIVE' })
const sdkVisible = ref(false)
const sdkSnippet = ref('')
const sdkUrl = ref('')

function platformLabel(p) { return { WEB: 'Web', H5: 'H5', APP: 'App', MINIPROGRAM: '小程序' }[p] || p }

async function load() { apps.value = await appApi.list() }
function openCreate() { editing.value = false; Object.assign(form, { id: null, name: '', platform: 'WEB', autoTrack: true, description: '', status: 'ACTIVE' }); formVisible.value = true }
function openEdit(row) { editing.value = true; Object.assign(form, row); formVisible.value = true }
async function save() {
  if (!form.name) { ElMessage.warning('请输入应用名称'); return }
  if (editing.value) await appApi.update(form.id, form)
  else await appApi.create(form)
  ElMessage.success('保存成功')
  formVisible.value = false
  await load(); await appStore.load(true)
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除应用「${row.name}」？`, '提示', { type: 'warning' })
  } catch (e) { return }
  await appApi.remove(row.id); ElMessage.success('已删除'); load(); appStore.load(true)
}
async function rotate(row) {
  try {
    await ElMessageBox.confirm('重置后旧 AppKey 立即失效，确认继续？', '提示', { type: 'warning' })
  } catch (e) { return }
  await appApi.rotate(row.id); ElMessage.success('已重置'); load()
}
async function showSdk(row) {
  const server = location.origin
  const res = await appApi.sdk(row.id, server)
  sdkSnippet.value = res.snippet
  sdkUrl.value = res.sdkUrl
  sdkVisible.value = true
}
async function copy(text) {
  try { await navigator.clipboard.writeText(text); ElMessage.success('已复制') }
  catch (e) { ElMessage.warning('复制失败，请手动选择') }
}

onMounted(load)
</script>

<style scoped>
.snippet { background: #111827; color: #d6e2ff; padding: 14px; border-radius: 8px; font-size: 12.5px; overflow-x: auto; white-space: pre; }
code { background: #f0f2f5; padding: 1px 5px; border-radius: 4px; }
</style>
