<template>
  <div class="page">
    <h2 class="page-title">租户管理</h2>
    <p class="page-sub">平台级多租户开通、套餐、用量与状态管理（仅平台管理员）</p>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索名称 / 编码 / 联系方式"
        :prefix-icon="Search"
        clearable
        style="width: 260px"
      />
      <el-button type="primary" :icon="Plus" @click="openCreate">新建租户</el-button>
    </div>

    <div class="card">
      <el-table :data="filtered" size="small" stripe v-loading="loading">
        <el-table-column label="租户" min-width="200">
          <template #default="{ row }">
            <div class="t-name">{{ row.name }}</div>
            <div class="t-code mono">{{ row.code }}</div>
          </template>
        </el-table-column>
        <el-table-column label="套餐" width="100">
          <template #default="{ row }"><el-tag size="small" :type="planType(row.plan)">{{ planLabel(row.plan) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="用户" width="80" align="center">
          <template #default="{ row }">{{ row.userCount }}</template>
        </el-table-column>
        <el-table-column label="应用" width="80" align="center">
          <template #default="{ row }">{{ row.appCount }}</template>
        </el-table-column>
        <el-table-column label="事件量" width="100" align="center">
          <template #default="{ row }">{{ fmtNum(row.eventCount) }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 'ACTIVE' ? 'success' : 'info'">
              {{ row.status === 'ACTIVE' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="到期时间" width="120">
          <template #default="{ row }">
            <span :class="{ expired: isExpired(row) }">{{ row.expireAt ? fmtDate(row.expireAt) : '不限' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="联系方式" min-width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.contact || '—' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button
              link size="small"
              :type="row.status === 'ACTIVE' ? 'warning' : 'success'"
              @click="toggleStatus(row)"
            >{{ row.status === 'ACTIVE' ? '停用' : '启用' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑租户' : '新建租户'" width="520px">
      <el-form :model="form" label-width="92px">
        <el-form-item label="名称"><el-input v-model="form.name" placeholder="如：Acme 科技" /></el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" :disabled="editing" placeholder="唯一，小写字母/数字/下划线，如 acme" />
          <span class="form-tip" v-if="!editing">登录时作为租户编码使用，创建后不可修改</span>
        </el-form-item>
        <el-form-item label="套餐">
          <el-select v-model="form.plan" style="width: 100%">
            <el-option label="免费版" value="FREE" /><el-option label="标准版" value="STANDARD" /><el-option label="企业版" value="ENTERPRISE" />
          </el-select>
        </el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.contact" placeholder="邮箱或电话" /></el-form-item>
        <el-form-item label="到期时间">
          <el-date-picker
            v-model="form.expireAt"
            type="date"
            value-format="YYYY-MM-DDTHH:mm:ss"
            placeholder="留空表示不限期"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-if="editing" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" value="ACTIVE" /><el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>

        <template v-if="!editing">
          <el-divider content-position="left">初始管理员（可选）</el-divider>
          <el-form-item label="管理员账号">
            <el-input v-model="form.adminUsername" placeholder="留空则稍后在用户管理中创建" />
          </el-form-item>
          <el-form-item label="管理员密码">
            <el-input v-model="form.adminPassword" type="password" show-password placeholder="该租户首个 ADMIN 账号的密码" />
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { tenantApi } from '../api'

const tenants = ref([])
const loading = ref(false)
const keyword = ref('')
const visible = ref(false)
const editing = ref(false)
const form = reactive({
  id: null, name: '', code: '', plan: 'STANDARD', contact: '', status: 'ACTIVE',
  expireAt: null, adminUsername: '', adminPassword: ''
})

const filtered = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return tenants.value
  return tenants.value.filter((t) =>
    [t.name, t.code, t.contact].some((v) => (v || '').toLowerCase().includes(k)))
})

function planLabel(p) { return { FREE: '免费版', STANDARD: '标准版', ENTERPRISE: '企业版' }[p] || p }
function planType(p) { return { FREE: 'info', STANDARD: 'primary', ENTERPRISE: 'warning' }[p] || 'info' }
function fmtNum(n) { return n >= 10000 ? (n / 10000).toFixed(1) + ' 万' : n }
function fmtDate(s) { return (s || '').slice(0, 10) }
function isExpired(row) { return row.expireAt && new Date(row.expireAt) < new Date() }

async function load() {
  loading.value = true
  try { tenants.value = await tenantApi.list() } finally { loading.value = false }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { id: null, name: '', code: '', plan: 'STANDARD', contact: '', status: 'ACTIVE', expireAt: null, adminUsername: '', adminPassword: '' })
  visible.value = true
}
function openEdit(row) {
  editing.value = true
  Object.assign(form, { ...row, expireAt: row.expireAt || null })
  visible.value = true
}

async function save() {
  if (!form.name || !form.code) { ElMessage.warning('名称与编码必填'); return }
  if (!editing.value && form.adminUsername && !form.adminPassword) { ElMessage.warning('请为初始管理员设置密码'); return }
  if (editing.value) {
    await tenantApi.update(form.id, {
      name: form.name, contact: form.contact, plan: form.plan,
      status: form.status, expireAt: form.expireAt
    })
  } else {
    await tenantApi.create({
      name: form.name, code: form.code.trim(), plan: form.plan, contact: form.contact,
      expireAt: form.expireAt, adminUsername: form.adminUsername, adminPassword: form.adminPassword
    })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function toggleStatus(row) {
  const to = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  const verb = to === 'DISABLED' ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${verb}租户「${row.name}」？${to === 'DISABLED' ? '停用后该租户成员将无法登录。' : ''}`, '提示', { type: 'warning' })
  } catch (e) { return }
  await tenantApi.update(row.id, { status: to })
  ElMessage.success(`已${verb}`)
  load()
}

onMounted(load)
</script>

<style scoped>
.page { padding: 20px 24px; }
.page-title { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
.page-sub { color: #8a94a6; font-size: 13px; margin: 0 0 16px; }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.card { background: #fff; border-radius: 10px; padding: 8px 12px; box-shadow: 0 1px 3px rgba(0,0,0,.05); }
.t-name { font-weight: 600; }
.t-code { color: #94a0b3; font-size: 12px; margin-top: 2px; }
.mono { font-family: monospace; }
.expired { color: #e6a23c; }
.form-tip { color: #a0a7b4; font-size: 12px; margin-top: 2px; display: block; }
</style>
