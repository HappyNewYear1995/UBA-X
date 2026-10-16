<template>
  <div class="page">
    <h2 class="page-title">个人中心</h2>
    <p class="page-sub">维护个人资料、修改登录密码，并查看本人登录记录</p>

    <div class="row2">
      <div class="card">
        <div class="card-title">我的资料</div>
        <div class="profile">
          <el-avatar :size="64" class="avatar">{{ (me.username || '?').charAt(0).toUpperCase() }}</el-avatar>
          <div class="pinfo">
            <div class="pname">{{ me.nickname || me.username }}</div>
            <div class="muted">@{{ me.username }}</div>
            <el-tag size="small" style="margin-top:6px" :type="me.role === 'SUPER_ADMIN' ? 'danger' : 'primary'">{{ roleLabel(me.role) }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="1" border size="small" style="margin-top:16px">
          <el-descriptions-item label="所属租户">{{ me.tenantName || '平台（超级管理员）' }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="me.status === 'ACTIVE' || me.status === 'ENABLED' ? 'success' : 'info'" size="small">{{ me.status || '—' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ fmtDateTime(me.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="上次登录">{{ fmtDateTime(me.lastLoginAt) }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <div class="card">
        <div class="card-title">编辑资料</div>
        <el-form :model="profileForm" label-width="80px" style="max-width:420px">
          <el-form-item label="用户名">
            <el-input :model-value="me.username" disabled />
          </el-form-item>
          <el-form-item label="昵称">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="50" />
          </el-form-item>
          <el-form-item label="邮箱">
            <el-input v-model="profileForm.email" placeholder="请输入邮箱" maxlength="120" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">保存</el-button>
          </el-form-item>
        </el-form>

        <el-divider />

        <div class="card-title">修改密码</div>
        <el-form :model="pwdForm" label-width="80px" style="max-width:420px">
          <el-form-item label="原密码">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少 6 位" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="pwdForm.confirm" type="password" show-password placeholder="再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingPwd" @click="savePwd">修改密码</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <div class="card">
      <div class="card-title">我的登录记录</div>
      <el-table :data="logins" size="small" stripe>
        <el-table-column label="时间" width="180">
          <template #default="{ row }">{{ fmtDateTime(row.createdAt) }}</template>
        </el-table-column>
        <el-table-column label="类型" width="90">
          <template #default="{ row }">
            <el-tag :type="row.type === 'LOGIN' ? 'primary' : 'info'" size="small" effect="plain">{{ row.type === 'LOGIN' ? '登录' : '登出' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="150" />
        <el-table-column prop="location" label="归属地" width="120" />
        <el-table-column label="结果" width="80">
          <template #default="{ row }">
            <el-tag :type="row.success ? 'success' : 'danger'" size="small">{{ row.success ? '成功' : '失败' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="客户端" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">{{ row.userAgent || '—' }}</template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { accountApi } from '../../api'
import { fmtDateTime } from '../../utils/date'

const me = ref({})
const logins = ref([])
const savingProfile = ref(false)
const savingPwd = ref(false)
const profileForm = reactive({ nickname: '', email: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirm: '' })

function roleLabel(r) {
  return { SUPER_ADMIN: '超级管理员', ADMIN: '租户管理员', ANALYST: '分析师' }[r] || r || '—'
}

async function loadMe() {
  me.value = await accountApi.me()
  profileForm.nickname = me.value.nickname || ''
  profileForm.email = me.value.email || ''
}
async function loadLogins() {
  try {
    const res = await accountApi.logins({ limit: 20 })
    logins.value = Array.isArray(res) ? res : (res?.rows || [])
  } catch (e) { logins.value = [] }
}
async function saveProfile() {
  savingProfile.value = true
  try {
    await accountApi.updateProfile({ nickname: profileForm.nickname, email: profileForm.email })
    ElMessage.success('资料已更新')
    await loadMe()
  } finally {
    savingProfile.value = false
  }
}
async function savePwd() {
  if (!pwdForm.oldPassword || !pwdForm.newPassword) { ElMessage.warning('请填写完整'); return }
  if (pwdForm.newPassword.length < 6) { ElMessage.warning('新密码至少 6 位'); return }
  if (pwdForm.newPassword !== pwdForm.confirm) { ElMessage.warning('两次输入的新密码不一致'); return }
  savingPwd.value = true
  try {
    await accountApi.changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码已修改')
    pwdForm.oldPassword = ''; pwdForm.newPassword = ''; pwdForm.confirm = ''
  } finally {
    savingPwd.value = false
  }
}

onMounted(async () => { await loadMe(); await loadLogins() })
</script>

<style scoped>
.profile { display: flex; align-items: center; gap: 16px; }
.avatar { background: #2b5cff; color: #fff; font-size: 26px; font-weight: 600; }
.pname { font-size: 18px; font-weight: 600; }
</style>
