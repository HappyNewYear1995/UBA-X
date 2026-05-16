<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>

    <div class="login-wrapper">
      <div class="left-panel">
        <div class="left-content">
          <div class="brand">
            <img src="/logo.png" alt="logo" class="logo-icon" />
            <h1 class="brand-name">UBA-X</h1>
          </div>
          <h2 class="brand-slogan">面向未来的开源用户行为分析平台</h2>
          <div class="brand-values">
            <div class="value-item">
              <div class="value-icon">
                <el-icon><Delete /></el-icon>
              </div>
              <div class="value-text">
                <h3>极简</h3>
                <p>拒绝繁琐配置，追求 One-Command Start</p>
              </div>
            </div>
            <div class="value-item">
              <div class="value-icon">
                <el-icon><Lightning /></el-icon>
              </div>
              <div class="value-text">
                <h3>极速</h3>
                <p>毫秒级延迟，数据流入即洞察流出</p>
              </div>
            </div>
            <div class="value-item">
              <div class="value-icon">
                <el-icon><MagicStick /></el-icon>
              </div>
              <div class="value-text">
                <h3>智能</h3>
                <p>AI Native，不仅仅是统计，更是预测和归因</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="right-panel">
        <div class="login-card">
          <div class="login-header">
            <h2 class="login-title">欢迎登录</h2>
            <p class="login-subtitle">请输入您的账号信息以继续</p>
          </div>

          <div class="login-tabs">
            <div
              v-for="tab in loginTabs"
              :key="tab.key"
              class="login-tab"
              :class="{ active: activeTab === tab.key }"
              @click="activeTab = tab.key"
            >
              {{ tab.label }}
            </div>
          </div>

          <div class="form-container">
            <el-form
              v-show="activeTab === 'account'"
              ref="formRef"
              :model="loginForm"
              :rules="rules"
              class="login-form"
              @submit.prevent="handleLogin"
            >
              <el-form-item prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  class="custom-input"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <el-icon><User /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <el-form-item prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  size="large"
                  show-password
                  class="custom-input"
                  @keyup.enter="handleLogin"
                >
                  <template #prefix>
                    <el-icon><Lock /></el-icon>
                  </template>
                </el-input>
              </el-form-item>

              <div class="form-options">
                <el-checkbox v-model="loginForm.remember" class="custom-checkbox">记住我</el-checkbox>
                <el-button class="link-btn primary" size="small">忘记密码？</el-button>
              </div>

              <el-form-item>
                <el-button type="primary" size="large" class="btn-login" :loading="loading" @click="handleLogin">
                  {{ loading ? '登录中...' : '登 录' }}
                </el-button>
              </el-form-item>
            </el-form>

            <el-form v-show="activeTab === 'email'" class="login-form">
              <el-form-item>
                <el-input
                  v-model="emailForm.email"
                  placeholder="请输入邮箱地址"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Message /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="emailForm.code"
                  placeholder="请输入验证码"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                  <template #append>
                    <el-button class="btn-code" :disabled="codeCountdown > 0">
                      {{ codeCountdown > 0 ? `${codeCountdown}s` : '获取验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" class="btn-login" @click="handleLogin">
                  登 录
                </el-button>
              </el-form-item>
            </el-form>

            <el-form v-show="activeTab === 'sms'" class="login-form">
              <el-form-item>
                <el-input
                  v-model="smsForm.phone"
                  placeholder="请输入手机号"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Phone /></el-icon>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-input
                  v-model="smsForm.code"
                  placeholder="请输入短信验证码"
                  size="large"
                  class="custom-input"
                >
                  <template #prefix>
                    <el-icon><Key /></el-icon>
                  </template>
                  <template #append>
                    <el-button class="btn-code" :disabled="smsCountdown > 0">
                      {{ smsCountdown > 0 ? `${smsCountdown}s` : '获取验证码' }}
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" size="large" class="btn-login" @click="handleLogin">
                  登 录
                </el-button>
              </el-form-item>
            </el-form>
          </div>

          <div class="login-footer">
            <span>还没有账号？</span>
            <el-button class="link-btn primary">立即注册</el-button>
          </div>

          <div class="social-login">
            <div class="divider">
              <span>其他登录方式</span>
            </div>
            <div class="social-icons">
              <div class="social-icon" title="邮箱登录" @click="activeTab = 'email'">
                <el-icon><Message /></el-icon>
              </div>
              <div class="social-icon" title="短信登录" @click="activeTab = 'sms'">
                <el-icon><ChatLineRound /></el-icon>
              </div>
              <el-popover
                placement="top"
                :width="160"
                trigger="click"
                popper-class="social-popover"
              >
                <template #reference>
                  <div class="social-icon" title="社交登录">
                    <el-icon><Connection /></el-icon>
                  </div>
                </template>
                <div class="social-options">
                  <div class="social-option" @click="activeTab = 'account'">
                    <div class="option-icon dingtalk">
                      <el-icon><ChatDotRound /></el-icon>
                    </div>
                    <span>钉钉</span>
                  </div>
                  <div class="social-option" @click="activeTab = 'account'">
                    <div class="option-icon feishu">
                      <el-icon><Promotion /></el-icon>
                    </div>
                    <span>飞书</span>
                  </div>
                  <div class="social-option" @click="activeTab = 'account'">
                    <div class="option-icon wechat">
                      <el-icon><ChatRound /></el-icon>
                    </div>
                    <span>微信</span>
                  </div>
                </div>
              </el-popover>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import {
  User,
  Lock,
  Message,
  Key,
  Phone,
  ChatLineRound,
  Connection,
  ChatDotRound,
  Promotion,
  ChatRound,
  Delete,
  Lightning,
  MagicStick,
} from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const activeTab = ref('account')
const codeCountdown = ref(0)
const smsCountdown = ref(0)

const loginTabs = [
  { key: 'account', label: '账号密码' },
  { key: 'email', label: '邮箱登录' },
  { key: 'sms', label: '短信登录' },
]

const loginForm = reactive({
  username: '',
  password: '',
  remember: false,
})

const emailForm = reactive({
  email: '',
  code: '',
})

const smsForm = reactive({
  phone: '',
  code: '',
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const handleLogin = async () => {
  if (activeTab.value === 'account' && formRef.value) {
    await formRef.value.validate(async (valid) => {
      if (valid) {
        loading.value = true
        setTimeout(() => {
          loading.value = false
          router.push('/dashboard')
        }, 1500)
      }
    })
  } else {
    router.push('/dashboard')
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, #0f172a 0%, #1e293b 50%, #0f172a 100%);
}

.login-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;

  .bg-shape {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    opacity: 0.4;
    animation: float 8s ease-in-out infinite;

    &.shape-1 {
      width: 400px;
      height: 400px;
      background: linear-gradient(135deg, #6366f1, #818cf8);
      top: -100px;
      left: -100px;
      animation-delay: 0s;
    }

    &.shape-2 {
      width: 300px;
      height: 300px;
      background: linear-gradient(135deg, #8b5cf6, #a78bfa);
      bottom: -50px;
      right: -50px;
      animation-delay: 2s;
    }

    &.shape-3 {
      width: 200px;
      height: 200px;
      background: linear-gradient(135deg, #10b981, #34d399);
      top: 50%;
      left: 60%;
      animation-delay: 4s;
    }
  }
}

.login-wrapper {
  position: relative;
  z-index: 1;
  display: flex;
  width: 100%;
  max-width: 1200px;
  min-height: 640px;
  margin: 20px;
  border-radius: var(--radius-xl);
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.4);
  animation: scaleIn 0.6s ease-out;
}

.left-panel {
  flex: 1;
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 50%, #a78bfa 100%);
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
    animation: rotate 20s linear infinite;
  }

  .left-content {
    position: relative;
    z-index: 1;
    color: #fff;

    .brand {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;

      .logo-icon {
        width: 72px;
        height: 72px;
        border-radius: var(--radius-md);
        background: rgba(255, 255, 255, 0.2);
        backdrop-filter: blur(10px);
        padding: 10px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
        object-fit: contain;
      }

      .brand-name {
        font-size: 32px;
        font-weight: 700;
        letter-spacing: 2px;
      }
    }

    .brand-slogan {
      font-size: 24px;
      font-weight: 600;
      margin-bottom: 12px;
      line-height: 1.4;
    }

    .brand-desc {
      font-size: 15px;
      opacity: 0.85;
      line-height: 1.7;
      margin-bottom: 40px;
    }

    .brand-values {
      display: flex;
      flex-direction: column;
      gap: 16px;

      .value-item {
        display: flex;
        align-items: flex-start;
        gap: 14px;
        padding: 14px;
        background: rgba(255, 255, 255, 0.1);
        backdrop-filter: blur(10px);
        border-radius: var(--radius-md);
        border: 1px solid rgba(255, 255, 255, 0.12);
        transition: all var(--transition-base);

        &:hover {
          background: rgba(255, 255, 255, 0.15);
          transform: translateX(4px);
        }

        .value-icon {
          width: 40px;
          height: 40px;
          border-radius: 10px;
          background: rgba(255, 255, 255, 0.2);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 18px;
          flex-shrink: 0;
        }

        .value-text {
          h3 {
            font-size: 15px;
            font-weight: 600;
            margin-bottom: 4px;
          }

          p {
            font-size: 12px;
            opacity: 0.75;
            line-height: 1.5;
          }
        }
      }
    }
  }
}

.right-panel {
  width: 480px;
  background: var(--card-bg);
  padding: 48px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  .login-card {
    width: 100%;
  }

  .login-header {
    margin-bottom: 28px;

    .login-title {
      font-size: 26px;
      font-weight: 700;
      color: var(--text-primary);
      margin-bottom: 8px;
    }

    .login-subtitle {
      font-size: 14px;
      color: var(--text-secondary);
    }
  }

  .login-tabs {
    display: flex;
    gap: 8px;
    margin-bottom: 24px;
    padding: 4px;
    background: var(--bg-color);
    border-radius: var(--radius-md);

    .login-tab {
      flex: 1;
      padding: 10px 16px;
      text-align: center;
      font-size: 14px;
      font-weight: 500;
      color: var(--text-secondary);
      border-radius: var(--radius-sm);
      cursor: pointer;
      transition: all var(--transition-base);

      &:hover {
        color: var(--primary-color);
      }

      &.active {
        background: #fff;
        color: var(--primary-color);
        font-weight: 600;
        box-shadow: var(--shadow-sm);
      }
    }
  }

  .form-container {
    position: relative;
    min-height: 280px;
  }

  .login-form {
    .custom-input {
      :deep(.el-input__wrapper) {
        background: var(--bg-color);
        border: 1px solid var(--border-color);
        border-radius: var(--radius-md);
        padding: 8px 14px;
        box-shadow: none;
        transition: all var(--transition-base);

        &:hover {
          border-color: var(--primary-light);
        }

        &.is-focus {
          border-color: var(--primary-color);
          box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
        }

        .el-input__inner {
          color: var(--text-primary);
          font-size: 15px;
          font-weight: 500;

          &::placeholder {
            color: var(--text-secondary);
          }
        }

        .el-input__prefix-inner {
          color: var(--text-secondary);
        }
      }

      :deep(.el-input-group__append) {
        background: transparent;
        border: none;
        padding: 0;
        margin: 0;

        .el-button {
          margin: 0;
          height: 100%;
          border-radius: 0 var(--radius-md) var(--radius-md) 0;
        }
      }
    }

    .btn-code {
      background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
      border: none;
      color: #fff;
      font-size: 13px;
      font-weight: 500;
      padding: 8px 16px;
      border-radius: var(--radius-sm);
      transition: all var(--transition-base);

      &:hover:not(:disabled) {
        opacity: 0.9;
      }

      &:disabled {
        opacity: 0.6;
        cursor: not-allowed;
      }
    }

    .form-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      :deep(.el-checkbox__label) {
        color: var(--text-secondary);
        font-size: 13px;
      }
    }

    .btn-login {
      width: 100%;
      height: 48px;
      background: linear-gradient(135deg, var(--primary-color), var(--primary-light));
      border: none;
      border-radius: var(--radius-md);
      font-size: 16px;
      font-weight: 600;
      letter-spacing: 2px;
      box-shadow: 0 8px 24px rgba(99, 102, 241, 0.3);
      transition: all var(--transition-base);

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 12px 32px rgba(99, 102, 241, 0.4);
      }

      &:active {
        transform: translateY(0);
      }
    }
  }

  .login-footer {
    text-align: center;
    margin-top: 20px;
    font-size: 14px;
    color: var(--text-secondary);
  }

  .social-login {
    margin-top: 28px;

    .divider {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 20px;

      &::before,
      &::after {
        content: '';
        flex: 1;
        height: 1px;
        background: var(--border-color);
      }

      span {
        font-size: 12px;
        color: var(--text-secondary);
        white-space: nowrap;
      }
    }

    .social-icons {
      display: flex;
      justify-content: center;
      gap: 16px;

      .social-icon {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        background: var(--bg-color);
        border: 1px solid var(--border-color);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 18px;
        color: var(--text-secondary);
        cursor: pointer;
        transition: all var(--transition-base);

        &:hover {
          background: rgba(99, 102, 241, 0.08);
          border-color: var(--primary-color);
          color: var(--primary-color);
          transform: translateY(-2px);
        }
      }
    }
  }
}

.link-btn {
  &.primary {
    color: var(--primary-color);
    background: transparent;
    border: none;
    padding: 0;
    font-weight: 500;

    &:hover {
      color: var(--primary-light);
    }
  }
}

:deep(.social-popover) {
  padding: 6px;
  border-radius: var(--radius-md);
}

.social-options {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .social-option {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 10px;
    border-radius: var(--radius-sm);
    cursor: pointer;
    transition: all var(--transition-fast);
    font-size: 13px;
    color: var(--text-regular);

    &:hover {
      background: var(--bg-color);
    }

    .option-icon {
      width: 28px;
      height: 28px;
      border-radius: 6px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 14px;
      color: #fff;

      &.dingtalk { background: linear-gradient(135deg, #007fff, #00a8ff); }
      &.feishu { background: linear-gradient(135deg, #3370ff, #5b8ff9); }
      &.wechat { background: linear-gradient(135deg, #07c160, #06ad56); }
    }
  }
}

@media (max-width: 900px) {
  .left-panel {
    display: none;
  }

  .right-panel {
    width: 100%;
  }
}
</style>
