<template>
  <div class="login-wrap">
    <div class="brand">
      <div class="brand-inner">
        <div class="brand-logo">UBA-X</div>
        <h1>数智化用户行为洞察与分析系统</h1>
        <p>User Behavior Analytics · 采集 → 实时处理 → 存储 → 分析洞察 全链路平台</p>
        <ul class="feats">
          <li>🌐 多端数据采集（Web / H5 / App / 小程序）</li>
          <li>⚡ 内置实时流处理引擎</li>
          <li>🏢 多租户隔离与 RBAC 权限体系</li>
          <li>📊 漏斗 / 留存 / 画像 / 热力图 / 会话回放</li>
          <li>🛰️ ubax-pilot 分布式探针远程管理</li>
        </ul>
      </div>
    </div>

    <div class="form-side">
      <el-card class="login-card" shadow="always">
        <h2>登录</h2>
        <p class="muted">请输入账号信息登录 UBA-X 控制台</p>
        <el-form :model="form" @submit.prevent="onSubmit" label-position="top">
          <el-form-item label="租户编码（平台管理员可留空）">
            <el-input v-model="form.tenantCode" placeholder="如 demo" clearable />
          </el-form-item>
          <el-form-item label="用户名">
            <el-input v-model="form.username" placeholder="用户名" clearable>
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password @keyup.enter="onSubmit">
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-button type="primary" class="submit" :loading="loading" @click="onSubmit">登 录</el-button>
        </el-form>

        <el-divider>演示账号</el-divider>
        <div class="demo-accounts">
          <el-tag class="acc" @click="fill('demo','demo','demo123')">租户管理员 demo / demo123</el-tag>
          <el-tag class="acc" type="warning" @click="fill('','admin','admin123')">平台管理员 admin / admin123</el-tag>
          <el-tag class="acc" type="success" @click="fill('demo','analyst','analyst123')">分析师 analyst / analyst123</el-tag>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../store/auth'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(false)
const form = reactive({ tenantCode: 'demo', username: 'demo', password: 'demo123' })

function fill(tc, u, p) {
  form.tenantCode = tc; form.username = u; form.password = p
}

async function onSubmit() {
  if (!form.username || !form.password) { ElMessage.warning('请输入用户名和密码'); return }
  loading.value = true
  try {
    await auth.login({ ...form })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrap { display: flex; height: 100vh; }
.brand { flex: 1.2; background: linear-gradient(135deg, #1e2a5a, #2b5cff 55%, #6a3cff); color: #fff; display: flex; align-items: center; padding: 0 8%; }
.brand-inner h1 { font-size: 27px; margin: 18px 0 8px; }
.brand-inner p { opacity: .85; font-size: 14px; margin: 0 0 26px; }
.brand-logo { font-size: 30px; font-weight: 800; letter-spacing: 2px; background: rgba(255,255,255,.15); display: inline-block; padding: 6px 16px; border-radius: 12px; }
.feats { list-style: none; padding: 0; line-height: 2.2; font-size: 14.5px; opacity: .95; }
.form-side { flex: 1; display: flex; align-items: center; justify-content: center; background: #f4f6fa; }
.login-card { width: 380px; border-radius: 14px; }
.login-card h2 { margin: 0 0 4px; }
.submit { width: 100%; margin-top: 6px; }
.demo-accounts { display: flex; flex-direction: column; gap: 8px; }
.acc { cursor: pointer; }
@media (max-width: 860px) { .brand { display: none; } }
</style>
