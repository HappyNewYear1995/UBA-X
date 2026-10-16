<template>
  <div class="page">
    <h2 class="page-title">用户管理</h2>
    <p class="page-sub">管理本租户下的账号与角色权限</p>

    <div class="toolbar">
      <el-input
        v-model="keyword"
        placeholder="搜索用户名 / 昵称 / 邮箱"
        :prefix-icon="Search"
        clearable
        style="width: 260px"
      />
      <el-button type="primary" :icon="Plus" @click="openCreate">新建用户</el-button>
    </div>

    <div class="card">
      <el-table :data="filteredUsers" size="small" stripe>
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="nickname" label="昵称" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" width="140">
          <template #default="{ row }"><el-tag size="small" :type="roleType(row.role)">{{ roleLabel(row.role) }}</el-tag></template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }"><el-tag size="small" :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="最近登录" width="170">
          <template #default="{ row }">{{ fmtDateTime(row.lastLoginAt) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="resetPw(row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="editing ? '编辑用户' : '新建用户'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" :disabled="editing" /></el-form-item>
        <el-form-item v-if="!editing" label="密码"><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="昵称"><el-input v-model="form.nickname" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option v-for="r in assignableRoles" :key="r.code" :label="r.name" :value="r.code" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="editing" label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="启用" value="ACTIVE" /><el-option label="停用" value="DISABLED" />
          </el-select>
        </el-form-item>
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
import { userApi, roleApi } from '../api'
import { useAuthStore } from '../store/auth'
import { fmtDateTime } from '../utils/date'

const auth = useAuthStore()
const users = ref([])
const roles = ref([])
const keyword = ref('')
const visible = ref(false)
const editing = ref(false)
const form = reactive({ id: null, username: '', password: '', nickname: '', email: '', role: 'VIEWER', status: 'ACTIVE' })

const roleMap = computed(() => Object.fromEntries(roles.value.map((r) => [r.code, r])))
// Non-super-admins cannot grant the platform super-admin role.
const assignableRoles = computed(() =>
  auth.isSuperAdmin ? roles.value : roles.value.filter((r) => r.code !== 'SUPER_ADMIN'))

const filteredUsers = computed(() => {
  const k = keyword.value.trim().toLowerCase()
  if (!k) return users.value
  return users.value.filter((u) =>
    [u.username, u.nickname, u.email].some((v) => (v || '').toLowerCase().includes(k)))
})

const FALLBACK_LABEL = { SUPER_ADMIN: '平台管理员', ADMIN: '租户管理员', ANALYST: '分析师', VIEWER: '访客' }
function roleLabel(r) { return roleMap.value[r]?.name || FALLBACK_LABEL[r] || r }
function roleType(r) { return { SUPER_ADMIN: 'danger', ADMIN: 'warning', ANALYST: 'primary', VIEWER: 'info' }[r] || 'success' }

async function load() { users.value = await userApi.list() }
async function loadRoles() {
  try { roles.value = await roleApi.list() } catch { roles.value = [] }
}
function openCreate() { editing.value = false; Object.assign(form, { id: null, username: '', password: '', nickname: '', email: '', role: 'VIEWER', status: 'ACTIVE' }); visible.value = true }
function openEdit(row) { editing.value = true; Object.assign(form, { ...row, password: '' }); visible.value = true }
async function save() {
  if (!form.username) { ElMessage.warning('请输入用户名'); return }
  if (!editing.value && !form.password) { ElMessage.warning('请输入密码'); return }
  if (editing.value) await userApi.update(form.id, form)
  else await userApi.create(form)
  ElMessage.success('保存成功'); visible.value = false; load()
}
async function resetPw(row) {
  let value
  try {
    ;({ value } = await ElMessageBox.prompt('请输入新密码', `重置 ${row.username} 的密码`, { inputValue: 'ubax@123' }))
  } catch (e) { return }
  await userApi.resetPassword(row.id, { password: value }); ElMessage.success('已重置')
}
async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.username}」？`, '提示', { type: 'warning' })
  } catch (e) { return }
  await userApi.remove(row.id); ElMessage.success('已删除'); load()
}

onMounted(() => { load(); loadRoles() })
</script>
