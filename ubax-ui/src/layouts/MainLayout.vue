<template>
  <div class="main-layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '240px'" class="sidebar">
      <div class="logo">
        <img src="/logo.png" alt="logo" class="logo-icon" />
        <span v-show="!appStore.sidebarCollapsed" class="logo-text">UBA-X</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="appStore.sidebarCollapsed"
        background-color="var(--sidebar-bg)"
        text-color="var(--sidebar-text)"
        active-text-color="var(--sidebar-active)"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据仪表盘</template>
        </el-menu-item>
        <el-menu-item index="/collection">
          <el-icon><Download /></el-icon>
          <template #title>数据采集</template>
        </el-menu-item>
        <el-menu-item index="/modeling">
          <el-icon><Connection /></el-icon>
          <template #title>数据建模</template>
        </el-menu-item>
        <el-menu-item index="/analysis">
          <el-icon><TrendCharts /></el-icon>
          <template #title>行为分析</template>
        </el-menu-item>
        <el-menu-item index="/anomaly">
          <el-icon><Warning /></el-icon>
          <template #title>异常检测</template>
        </el-menu-item>
        <el-menu-item index="/ai-insights">
          <el-icon><Cpu /></el-icon>
          <template #title>AI 智能洞察</template>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container class="main-container">
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown>
            <span class="user-info">
              <el-icon><User /></el-icon>
              <span>管理员</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="router.push('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import {
  DataAnalysis,
  Download,
  Connection,
  TrendCharts,
  Warning,
  Cpu,
  Setting,
  Fold,
  Expand,
  User,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => (route.meta.title as string) || '')

const handleLogout = () => {
  router.push('/login')
}
</script>

<style scoped lang="scss">
.main-layout {
  display: flex;
  height: 100vh;
  width: 100%;
  background: var(--bg-color);
}

.sidebar {
  background: var(--sidebar-bg);
  transition: all var(--transition-slow);
  overflow: hidden;
  position: relative;
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
  
  &::before {
    content: '';
    position: absolute;
    top: 0;
    right: 0;
    width: 1px;
    height: 100%;
    background: linear-gradient(180deg, 
      rgba(99, 102, 241, 0.3) 0%, 
      rgba(99, 102, 241, 0) 50%, 
      rgba(99, 102, 241, 0.3) 100%);
  }

  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 64px;
    padding: 0 20px;
    position: relative;
    
    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 20px;
      right: 20px;
      height: 1px;
      background: linear-gradient(90deg, 
        transparent 0%, 
        rgba(255, 255, 255, 0.1) 50%, 
        transparent 100%);
    }

    .logo-icon {
      width: 36px;
      height: 36px;
      filter: drop-shadow(0 0 8px rgba(99, 102, 241, 0.4));
      transition: transform var(--transition-bounce);
      
      &:hover {
        transform: scale(1.1) rotate(5deg);
      }
    }

    .logo-text {
      margin-left: 14px;
      font-size: 22px;
      font-weight: 700;
      background: linear-gradient(135deg, #fff 0%, #818cf8 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      background-clip: text;
      letter-spacing: 1px;
      white-space: nowrap;
    }
  }

  :deep(.el-menu) {
    border-right: none;
    padding: 12px 8px;
    background: transparent;
  }

  :deep(.el-menu-item) {
    height: 48px;
    line-height: 48px;
    margin-bottom: 4px;
    border-radius: 12px;
    transition: all var(--transition-base);
    position: relative;
    overflow: hidden;
    font-weight: 600;
    font-size: 15px;
    
    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, rgba(99, 102, 241, 0.1) 0%, rgba(129, 140, 248, 0.05) 100%);
      opacity: 0;
      transition: opacity var(--transition-base);
    }
    
    &:hover {
      background: rgba(255, 255, 255, 0.05);
      
      &::before {
        opacity: 1;
      }
      
      .el-icon {
        transform: scale(1.1);
      }
    }
    
    &.is-active {
      background: linear-gradient(135deg, rgba(99, 102, 241, 0.2) 0%, rgba(129, 140, 248, 0.1) 100%);
      box-shadow: 0 4px 12px rgba(99, 102, 241, 0.2), inset 0 0 0 1px rgba(99, 102, 241, 0.3);
      
      &::before {
        opacity: 1;
      }
      
      .el-icon {
        color: #818cf8;
        filter: drop-shadow(0 0 6px rgba(99, 102, 241, 0.5));
      }
      
      span {
        color: #fff;
        font-weight: 700;
      }
    }
    
    .el-icon {
      transition: all var(--transition-base);
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: linear-gradient(135deg, var(--bg-gradient-start) 0%, var(--bg-gradient-end) 100%);
}

.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: var(--header-bg);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid var(--border-light);
  box-shadow: var(--shadow-sm);
  position: relative;
  z-index: 10;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, 
      transparent 0%, 
      var(--border-color) 20%, 
      var(--border-color) 80%, 
      transparent 100%);
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 20px;

    .collapse-btn {
      width: 36px;
      height: 36px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 10px;
      font-size: 18px;
      cursor: pointer;
      color: var(--text-regular);
      transition: all var(--transition-base);
      background: transparent;
      
      &:hover {
        background: var(--bg-color);
        color: var(--primary-color);
        transform: scale(1.05);
      }
      
      &:active {
        transform: scale(0.95);
      }
    }
    
    :deep(.el-breadcrumb) {
      .el-breadcrumb__item {
        .el-breadcrumb__inner {
          font-weight: 400;
          color: var(--text-secondary);
          transition: color var(--transition-fast);
          
          &:hover {
            color: var(--primary-color);
          }
        }
        
        &:last-child {
          .el-breadcrumb__inner {
            color: var(--text-primary);
            font-weight: 500;
          }
        }
      }
    }
  }

  .header-right {
    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 8px 16px;
      border-radius: 12px;
      cursor: pointer;
      color: var(--text-regular);
      transition: all var(--transition-base);
      background: transparent;
      
      &:hover {
        background: var(--bg-color);
        color: var(--primary-color);
      }
      
      .el-icon {
        font-size: 18px;
      }
      
      span {
        font-weight: 500;
      }
    }
  }
}

.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.fade-enter-active {
  animation: fadeInUp 0.35s ease-out;
}

.fade-leave-active {
  animation: fadeIn 0.2s ease-out reverse;
}
</style>
