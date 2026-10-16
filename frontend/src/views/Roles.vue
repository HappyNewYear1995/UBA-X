<template>
  <div class="page">
    <h2 class="page-title">角色权限</h2>
    <p class="page-sub">定义角色并为其分配功能权限，用户通过绑定角色获得对应能力</p>

    <div class="toolbar">
      <el-button type="primary" :icon="Plus" :disabled="!canManage" @click="openCreate">新建角色</el-button>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
      <span v-if="!canManage" class="hint">仅平台管理员可编辑角色</span>
    </div>

    <div class="card">
      <el-table :data="roles" size="small" stripe v-loading="loading">
        <el-table-column label="角色" min-width="200">
          <template #default="{ row }">
            <div class="role-cell">
              <span class="role-name">{{ row.name }}</span>
              <el-tag size="small" :type="roleType(row.code)" effect="plain">{{ row.code }}</el-tag>
              <el-tag v-if="row.builtIn" size="small" type="info" effect="plain">内置</el-tag>
            </div>
            <div class="role-desc">{{ row.description || '—' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="权限" width="100" align="center">
          <template #default="{ row }"><el-tag size="small" round>{{ row.permissions.length }}</el-tag></template>
        </el-table-column>
        <el-table-column label="用户数" width="90" align="center">
          <template #default="{ row }">{{ row.userCount }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openEdit(row)">{{ canManage ? '编辑' : '查看' }}</el-button>
            <el-button
              v-if="canManage"
              link type="danger" size="small"
              :disabled="row.builtIn || row.userCount > 0"
              @click="remove(row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="visible" :title="dialogTitle" width="720px" top="6vh">
      <el-form :model="form" label-width="86px">
        <el-form-item label="角色编码">
          <el-input
            v-model="form.code"
            :disabled="editing"
            placeholder="大写字母 / 数字 / 下划线，如 MARKETING"
            style="max-width: 320px"
          />
          <span class="form-tip" v-if="!editing">创建后不可修改</span>
        </el-form-item>
        <el-form-item label="角色名称">
          <el-input v-model="form.name" :disabled="readonly" placeholder="如：市场运营" style="max-width: 320px" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" :disabled="readonly" type="textarea" :rows="2" style="max-width: 460px" />
        </el-form-item>
        <el-form-item label="功能权限">
          <div class="perm-editor">
            <div v-if="form.code === 'SUPER_ADMIN'" class="super-note">
              平台超级管理员始终拥有全部权限（tenant:manage、system:manage 受保护，不可移除）。
            </div>
            <div v-for="g in permGroups" :key="g.key" class="perm-group">
              <div class="perm-group-head">
                <el-checkbox
                  :model-value="groupAll(g)"
                  :indeterminate="groupSome(g)"
                  :disabled="readonly"
                  @change="(v) => toggleGroup(g, v)"
                >
                  <span class="pg-title">{{ g.title }}</span>
                </el-checkbox>
              </div>
              <div class="perm-items">
                <el-checkbox
                  v-for="p in g.permissions"
                  :key="p.code"
                  :model-value="form.permissions.includes(p.code)"
                  :disabled="readonly || isProtected(p.code)"
                  class="perm-item"
                  @change="(v) => togglePerm(p.code, v)"
                >
                  <span class="pi-label">{{ p.label }}</span>
                  <el-tooltip v-if="p.description" :content="p.description" placement="top">
                    <el-icon class="pi-info"><InfoFilled /></el-icon>
                  </el-tooltip>
                  <span class="pi-code">{{ p.code }}</span>
                </el-checkbox>
              </div>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">{{ readonly ? '关闭' : '取消' }}</el-button>
        <el-button v-if="!readonly" type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Refresh, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roleApi } from '../api'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const canManage = computed(() => auth.isSuperAdmin)

const roles = ref([])
const permGroups = ref([])
const loading = ref(false)
const visible = ref(false)
const editing = ref(false)
// readonly when a non-super-admin opens the dialog just to view
const readonly = computed(() => !canManage.value)
const form = reactive({ code: '', name: '', description: '', permissions: [] })

const dialogTitle = computed(() => (readonly.value ? '查看角色' : editing.value ? '编辑角色' : '新建角色'))

function roleType(code) {
  return { SUPER_ADMIN: 'danger', ADMIN: 'warning', ANALYST: 'primary', VIEWER: 'info' }[code] || 'success'
}

// tenant:manage / system:manage are always retained for SUPER_ADMIN.
function isProtected(code) {
  return form.code === 'SUPER_ADMIN' && (code === 'tenant:manage' || code === 'system:manage')
}

async function load() {
  loading.value = true
  try {
    const [rs, gs] = await Promise.all([roleApi.list(), roleApi.permissions()])
    roles.value = rs.map((r) => ({ ...r, permissions: r.permissions || [] }))
    permGroups.value = gs
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = false
  Object.assign(form, { code: '', name: '', description: '', permissions: [] })
  visible.value = true
}

function openEdit(row) {
  editing.value = true
  Object.assign(form, {
    code: row.code,
    name: row.name,
    description: row.description || '',
    permissions: [...(row.permissions || [])]
  })
  visible.value = true
}

function groupAll(g) {
  return g.permissions.every((p) => form.permissions.includes(p.code))
}
function groupSome(g) {
  const some = g.permissions.some((p) => form.permissions.includes(p.code))
  return some && !groupAll(g)
}
function toggleGroup(g, checked) {
  const codes = g.permissions.map((p) => p.code)
  const set = new Set(form.permissions)
  if (checked) codes.forEach((c) => set.add(c))
  else codes.forEach((c) => { if (!isProtected(c)) set.delete(c) })
  form.permissions = [...set]
}
function togglePerm(code, checked) {
  const set = new Set(form.permissions)
  if (checked) set.add(code)
  else if (!isProtected(code)) set.delete(code)
  form.permissions = [...set]
}

async function save() {
  if (!editing.value && !form.code.trim()) { ElMessage.warning('请输入角色编码'); return }
  if (!form.name.trim()) { ElMessage.warning('请输入角色名称'); return }
  const payload = { name: form.name.trim(), description: form.description, permissions: form.permissions }
  if (editing.value) {
    await roleApi.update(form.code, payload)
  } else {
    await roleApi.create({ code: form.code.trim().toUpperCase(), ...payload })
  }
  ElMessage.success('保存成功')
  visible.value = false
  load()
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '提示', { type: 'warning' })
  } catch (e) { return }
  await roleApi.remove(row.code)
  ElMessage.success('已删除')
  load()
}

onMounted(load)
</script>

<style scoped>
.page { padding: 20px 24px; }
.page-title { font-size: 20px; font-weight: 700; margin: 0 0 4px; }
.page-sub { color: #8a94a6; font-size: 13px; margin: 0 0 16px; }
.toolbar { display: flex; align-items: center; gap: 12px; margin-bottom: 14px; }
.hint { color: #a0a7b4; font-size: 12.5px; }
.card { background: #fff; border-radius: 10px; padding: 8px 12px; box-shadow: 0 1px 3px rgba(0,0,0,.05); }
.role-cell { display: flex; align-items: center; gap: 8px; }
.role-name { font-weight: 600; }
.role-desc { color: #94a0b3; font-size: 12px; margin-top: 3px; }
.form-tip { color: #a0a7b4; font-size: 12px; margin-left: 10px; }
.perm-editor { width: 100%; border: 1px solid #ebeef5; border-radius: 8px; padding: 4px 12px 10px; max-height: 46vh; overflow-y: auto; }
.super-note { background: #fdf6ec; color: #b88230; font-size: 12.5px; padding: 8px 10px; border-radius: 6px; margin: 8px 0; }
.perm-group { padding: 8px 0; border-bottom: 1px dashed #eef0f4; }
.perm-group:last-child { border-bottom: none; }
.perm-group-head { margin-bottom: 2px; }
.pg-title { font-weight: 600; font-size: 13.5px; }
.perm-items { display: flex; flex-wrap: wrap; gap: 4px 22px; padding-left: 24px; }
.perm-item { height: 30px; margin-right: 0; }
.pi-label { font-size: 13px; }
.pi-info { color: #b6bfcc; font-size: 13px; margin-left: 4px; vertical-align: -2px; cursor: help; }
.pi-code { color: #b6bfcc; font-size: 11px; margin-left: 6px; font-family: monospace; }
</style>
