<template>
  <div
    :class="prefixCls"
    class="relative h-[100%] lt-md:px-10px lt-sm:px-10px lt-xl:px-10px lt-xl:px-10px"
  >
    <!-- 背景装饰 -->
    <div class="login-bg"></div>
    <div class="login-particles"></div>

    <div class="relative mx-auto h-full flex">
      <!-- 左侧品牌展示区 -->
      <div 
        :class="`${prefixCls}__left flex-[1.5] relative lt-md:hidden overflow-hidden`"
        @mousemove="handleBrandMouseMove"
        @mouseleave="handleBrandMouseLeave"
        @click="handleBrandClick"
      >
        <!-- 渐变背景 -->
        <div class="left-gradient-bg"></div>
        
        <!-- 鼠标跟随光晕 -->
        <div 
          class="mouse-glow"
          :style="{
            left: `${mousePos.x}px`,
            top: `${mousePos.y}px`,
            opacity: mousePos.isActive ? 1 : 0
          }"
        ></div>
        
        <!-- 点击波纹 -->
        <div 
          v-for="ripple in clickRipples" 
          :key="ripple.id"
          class="click-ripple"
          :style="{
            left: `${ripple.x}px`,
            top: `${ripple.y}px`,
          }"
        ></div>

        <!-- 装饰几何图形 -->
        <div class="left-decorations">
          <!-- 浮动圆形 -->
          <div class="deco-circle deco-circle-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-circle deco-circle-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-circle deco-circle-3 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-circle deco-circle-4 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 旋转方块 -->
          <div class="deco-grid deco-grid-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-grid deco-grid-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-grid deco-grid-3 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 点阵网格 -->
          <div class="deco-dots deco-dots-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-dots deco-dots-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-dots deco-dots-3 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 线条 -->
          <div class="deco-line deco-line-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-line deco-line-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-line deco-line-3 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 三角形 -->
          <div class="deco-triangle deco-triangle-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-triangle deco-triangle-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 菱形 -->
          <div class="deco-diamond deco-diamond-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-diamond deco-diamond-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 圆环 -->
          <div class="deco-ring deco-ring-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-ring deco-ring-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-ring deco-ring-3 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 十字星 -->
          <div class="deco-cross deco-cross-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-cross deco-cross-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 波浪线 -->
          <div class="deco-wave deco-wave-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-wave deco-wave-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 粒子 -->
          <div class="deco-particle deco-particle-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-particle deco-particle-2 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-particle deco-particle-3 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-particle deco-particle-4 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-particle deco-particle-5 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-particle deco-particle-6 interactive-deco" @click="handleDecoClick($event)"></div>
          <!-- 光晕 -->
          <div class="deco-glow deco-glow-1 interactive-deco" @click="handleDecoClick($event)"></div>
          <div class="deco-glow deco-glow-2 interactive-deco" @click="handleDecoClick($event)"></div>
        </div>

        <!-- 内容层 -->
        <div class="relative z-10 h-full flex flex-col justify-between p-50px">
          <!-- 顶部：Logo + 标题 -->
          <div class="animate-slide-down flex items-center">
            <div
              class="mr-20px h-56px w-56px flex-shrink-0 rounded-xl flex items-center justify-center bg-white/15 backdrop-blur-sm"
            >
              <img
                alt=""
                class="h-44px w-44px brightness-110"
                src="@/assets/imgs/logo.png"
              />
            </div>
            <div>
              <span class="text-24px font-bold text-gradient-primary">
                {{ appStore.getTitle }}
              </span>
              <div class="text-13px mt-4px text-white/60">智能用户行为分析平台</div>
            </div>
          </div>

          <!-- 中间：核心描述 -->
          <div class="animate-slide-up mt-[-30px]">
            <h1
              class="text-48px font-bold leading-tight mb-20px text-white"
            >
              数据驱动决策<br />
              <span class="text-gradient-primary">AI 赋能增长</span>
            </h1>
            <p
              class="text-19px leading-relaxed w-2/3 text-white/60"
            >
              通过深度集成AI算法，UBA-X帮助企业轻松完成从数据采集、清洗、建模到可视化分析的全链路闭环，为产品迭代、用户增长和精细化运营提供精准的数据支撑。
            </p>
          </div>

          <!-- 底部：特性展示 + 版权 -->
          <div class="animate-fade-in">
            <div class="flex gap-16px mb-30px">
              <div class="feature-card flex-1 feature-card-dark">
                <div class="feature-icon feature-icon-purple">
                  <Icon icon="ep:data-analysis" :size="22" />
                </div>
                <div class="ml-12px">
                  <div class="text-15px font-semibold text-white">数据采集</div>
                  <div class="text-12px mt-4px text-white/50">全渠道数据接入</div>
                </div>
              </div>
              <div class="feature-card flex-1 feature-card-dark">
                <div class="feature-icon feature-icon-indigo">
                  <Icon icon="ep:cpu" :size="22" />
                </div>
                <div class="ml-12px">
                  <div class="text-15px font-semibold text-white">AI 分析</div>
                  <div class="text-12px mt-4px text-white/50">智能算法驱动</div>
                </div>
              </div>
              <div class="feature-card flex-1 feature-card-dark">
                <div class="feature-icon feature-icon-blue">
                  <Icon icon="ep:monitor" :size="22" />
                </div>
                <div class="ml-12px">
                  <div class="text-15px font-semibold text-white">可视化</div>
                  <div class="text-12px mt-4px text-white/50">多维度数据呈现</div>
                </div>
              </div>
              <div class="feature-card flex-1 feature-card-dark">
                <div class="feature-icon feature-icon-green">
                  <Icon icon="ep:warning" :size="22" />
                </div>
                <div class="ml-12px">
                  <div class="text-15px font-semibold text-white">异常检测</div>
                  <div class="text-12px mt-4px text-white/50">业务健康保障</div>
                </div>
              </div>
            </div>
            <div class="text-12px text-white/30">
              © {{ new Date().getFullYear() }} UBA-X · 面向未来的开源用户行为分析平台
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div
        class="relative flex-[0.8] p-30px lt-sm:p-10px overflow-x-hidden overflow-y-auto"
        :class="appStore.getIsDark ? 'bg-[#0f1425]' : 'bg-[#f0f2f5]'"
      >
        <!-- 右上角的主题、语言选择 -->
        <div
          class="flex items-center justify-between at-2xl:justify-end at-xl:justify-end animate-slide-down"
          :class="appStore.getIsDark ? 'text-white' : 'text-gray-800'"
        >
          <div class="flex items-center at-2xl:hidden at-xl:hidden">
            <div
              class="mr-10px h-48px w-48px rounded-lg bg-gradient-to-br from-purple-500 to-indigo-600 flex items-center justify-center shadow-lg"
            >
              <img
                alt=""
                class="h-36px w-36px brightness-110 drop-shadow-md"
                src="@/assets/imgs/logo.png"
              />
            </div>
            <span class="text-18px font-bold text-gradient-primary">{{
              underlineToHump(appStore.getTitle)
            }}</span>
          </div>
          <div class="flex items-center justify-end space-x-10px h-48px">
            <ThemeSwitch />
            <LocaleDropdown />
          </div>
        </div>

        <!-- 右边的登录界面 -->
        <Transition appear enter-active-class="animate__animated animate__bounceInRight">
          <div class="m-auto h-[calc(100%-60px)] w-[100%] flex items-center at-2xl:max-w-480px at-lg:max-w-480px at-md:max-w-480px at-xl:max-w-480px">
            <!-- 登录卡片容器 -->
            <div class="login-card-wrapper w-full">
              <!-- 账号登录 -->
              <LoginForm class="login-card" />
              <!-- 手机登录 -->
              <MobileForm class="login-card" />
              <!-- 三方登录 -->
              <SSOLoginVue class="login-card" />
              <!-- 忘记密码 -->
              <ForgetPasswordForm class="login-card" />
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { underlineToHump } from '@/utils'

import { useDesign } from '@/hooks/web/useDesign'
import { useAppStore } from '@/store/modules/app'
import { ThemeSwitch } from '@/layout/components/ThemeSwitch'
import { LocaleDropdown } from '@/layout/components/LocaleDropdown'

import {
  ForgetPasswordForm,
  LoginForm,
  MobileForm,
  SSOLoginVue
} from './components'

defineOptions({ name: 'Login' })

const { t } = useI18n()
const appStore = useAppStore()
const { getPrefixCls } = useDesign()
const prefixCls = getPrefixCls('login')

// 鼠标位置跟踪
const mousePos = reactive({
  x: 0,
  y: 0,
  isActive: false
})

// 点击波纹
const clickRipples = ref<Array<{id: number, x: number, y: number}>>([])
let rippleId = 0

// 鼠标移动处理
const handleBrandMouseMove = (e: MouseEvent) => {
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  mousePos.x = e.clientX - rect.left
  mousePos.y = e.clientY - rect.top
  mousePos.isActive = true
}

// 鼠标离开处理
const handleBrandMouseLeave = () => {
  mousePos.isActive = false
}

// 点击处理
const handleBrandClick = (e: MouseEvent) => {
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  
  const id = ++rippleId
  clickRipples.value.push({ id, x, y })
  
  // 1秒后移除波纹
  setTimeout(() => {
    clickRipples.value = clickRipples.value.filter(r => r.id !== id)
  }, 1000)
}

// 装饰元素点击效果
const handleDecoClick = (e: MouseEvent) => {
  const target = e.currentTarget as HTMLElement
  target.classList.add('deco-clicked')
  setTimeout(() => {
    target.classList.remove('deco-clicked')
  }, 600)
}
</script>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-login;

.#{$prefix-cls} {
  overflow: auto;

  // 背景层
  .login-bg {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: linear-gradient(
      135deg,
      var(--login-bg-gradient-1) 0%,
      var(--login-bg-gradient-2) 50%,
      var(--login-bg-gradient-3) 100%
    );
    z-index: -2;
  }

  // 粒子装饰层
  .login-particles {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    z-index: -1;
    overflow: hidden;

    &::before,
    &::after {
      position: absolute;
      border-radius: 50%;
      content: '';
      filter: blur(80px);
    }

    &::before {
      top: -10%;
      right: 20%;
      width: 500px;
      height: 500px;
      background: linear-gradient(135deg, #667eea, #764ba2);
      animation: float 8s ease-in-out infinite;
    }

    &::after {
      bottom: -10%;
      left: 10%;
      width: 400px;
      height: 400px;
      background: linear-gradient(135deg, #11998e, #38ef7d);
      animation: float 10s ease-in-out infinite reverse;
    }
  }

  &__left {
    background: var(--login-bg-color);

    // 渐变背景
    .left-gradient-bg {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      background: linear-gradient(135deg, #0a0e27 0%, #1a1f4d 50%, #0d1235 100%);
      z-index: 0;
    }

    // 装饰几何图形
    .left-decorations {
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      z-index: 1;
      pointer-events: none;
      overflow: hidden;

      .deco-circle {
        position: absolute;
        border-radius: 50%;
        opacity: 0.08;
      }

      .deco-circle-1 {
        top: 15%;
        right: 10%;
        width: 200px;
        height: 200px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        animation: float 8s ease-in-out infinite;
      }

      .deco-circle-2 {
        bottom: 25%;
        left: 5%;
        width: 120px;
        height: 120px;
        background: linear-gradient(135deg, #11998e, #38ef7d);
        animation: float 10s ease-in-out infinite reverse;
      }

      .deco-circle-3 {
        top: 50%;
        right: 30%;
        width: 80px;
        height: 80px;
        background: linear-gradient(135deg, #f2994a, #f2c94c);
        animation: float 12s ease-in-out infinite;
      }

      .deco-circle-4 {
        top: 70%;
        left: 15%;
        width: 60px;
        height: 60px;
        background: linear-gradient(135deg, #667eea, #38ef7d);
        animation: float 9s ease-in-out infinite reverse;
      }

      .deco-grid {
        position: absolute;
        opacity: 0.04;
      }

      .deco-grid-1 {
        top: 20%;
        left: 15%;
        width: 160px;
        height: 160px;
        border: 2px solid #667eea;
        transform: rotate(45deg);
        animation: rotate-slow 20s linear infinite;
      }

      .deco-grid-2 {
        bottom: 15%;
        right: 20%;
        width: 100px;
        height: 100px;
        border: 2px solid #11998e;
        transform: rotate(30deg);
        animation: rotate-slow 25s linear infinite reverse;
      }

      .deco-grid-3 {
        top: 70%;
        left: 40%;
        width: 60px;
        height: 60px;
        border: 2px solid #f2994a;
        transform: rotate(60deg);
        animation: rotate-slow 30s linear infinite;
      }

      // 点阵网格
      .deco-dots {
        position: absolute;
        background-image: radial-gradient(circle, rgba(102, 126, 234, 0.3) 1px, transparent 1px);
        background-size: 12px 12px;
        opacity: 0.15;
      }

      .deco-dots-1 {
        top: 10%;
        left: 20%;
        width: 80px;
        height: 80px;
        animation: pulse 4s ease-in-out infinite;
      }

      .deco-dots-2 {
        bottom: 30%;
        right: 15%;
        width: 60px;
        height: 60px;
        animation: pulse 5s ease-in-out infinite reverse;
      }

      .deco-dots-3 {
        top: 55%;
        left: 25%;
        width: 50px;
        height: 50px;
        animation: pulse 6s ease-in-out infinite;
      }

      // 线条
      .deco-line {
        position: absolute;
        height: 2px;
        border-radius: 1px;
        opacity: 0.1;
      }

      .deco-line-1 {
        top: 35%;
        left: 10%;
        width: 120px;
        background: linear-gradient(90deg, transparent, #667eea, transparent);
        animation: slide-right 6s ease-in-out infinite;
      }

      .deco-line-2 {
        bottom: 40%;
        right: 10%;
        width: 100px;
        background: linear-gradient(90deg, transparent, #11998e, transparent);
        animation: slide-left 7s ease-in-out infinite;
      }

      .deco-line-3 {
        top: 80%;
        left: 20%;
        width: 150px;
        background: linear-gradient(90deg, transparent, #f2994a, transparent);
        animation: slide-right 8s ease-in-out infinite;
      }

      // 三角形
      .deco-triangle {
        position: absolute;
        width: 0;
        height: 0;
        border-left: 30px solid transparent;
        border-right: 30px solid transparent;
        border-bottom: 52px solid rgba(242, 153, 74, 0.1);
      }

      .deco-triangle-1 {
        top: 25%;
        right: 25%;
        animation: float 9s ease-in-out infinite;
      }

      .deco-triangle-2 {
        bottom: 20%;
        left: 20%;
        border-bottom-color: rgba(102, 126, 234, 0.1);
        animation: float 11s ease-in-out infinite reverse;
      }

      // 菱形
      .deco-diamond {
        position: absolute;
        width: 50px;
        height: 50px;
        border: 2px solid;
        transform: rotate(45deg);
        opacity: 0.08;
      }

      .deco-diamond-1 {
        top: 60%;
        left: 10%;
        border-color: #f2994a;
        animation: float 8s ease-in-out infinite;
      }

      .deco-diamond-2 {
        top: 15%;
        right: 15%;
        width: 35px;
        height: 35px;
        border-color: #38ef7d;
        animation: float 10s ease-in-out infinite reverse;
      }

      // 圆环
      .deco-ring {
        position: absolute;
        border-radius: 50%;
        border: 3px solid;
        opacity: 0.06;
      }

      .deco-ring-1 {
        top: 45%;
        right: 5%;
        width: 100px;
        height: 100px;
        border-color: #667eea;
        animation: pulse 6s ease-in-out infinite;
      }

      .deco-ring-2 {
        bottom: 10%;
        left: 30%;
        width: 70px;
        height: 70px;
        border-color: #11998e;
        animation: pulse 7s ease-in-out infinite reverse;
      }

      .deco-ring-3 {
        top: 20%;
        left: 5%;
        width: 50px;
        height: 50px;
        border-color: #f2994a;
        animation: pulse 5s ease-in-out infinite;
      }

      // 十字星
      .deco-cross {
        position: absolute;
        width: 40px;
        height: 40px;
        opacity: 0.1;
      }

      .deco-cross::before,
      .deco-cross::after {
        position: absolute;
        content: '';
        background: linear-gradient(90deg, transparent, #f2c94c, transparent);
      }

      .deco-cross::before {
        top: 50%;
        left: 0;
        width: 100%;
        height: 2px;
        transform: translateY(-50%);
      }

      .deco-cross::after {
        top: 0;
        left: 50%;
        width: 2px;
        height: 100%;
        transform: translateX(-50%);
      }

      .deco-cross-1 {
        top: 30%;
        left: 30%;
        animation: rotate-slow 15s linear infinite;
      }

      .deco-cross-2 {
        bottom: 35%;
        right: 30%;
        animation: rotate-slow 18s linear infinite reverse;
      }

      // 波浪线
      .deco-wave {
        position: absolute;
        width: 200px;
        height: 40px;
        opacity: 0.08;
        background: repeating-linear-gradient(
          90deg,
          transparent,
          transparent 20px,
          rgba(102, 126, 234, 0.3) 20px,
          rgba(102, 126, 234, 0.3) 21px
        );
      }

      .deco-wave-1 {
        bottom: 50%;
        left: 5%;
        animation: wave-move 4s ease-in-out infinite;
      }

      .deco-wave-2 {
        top: 40%;
        right: 5%;
        width: 150px;
        height: 30px;
        background: repeating-linear-gradient(
          90deg,
          transparent,
          transparent 15px,
          rgba(17, 153, 142, 0.3) 15px,
          rgba(17, 153, 142, 0.3) 16px
        );
        animation: wave-move 5s ease-in-out infinite reverse;
      }

      // 粒子
      .deco-particle {
        position: absolute;
        width: 4px;
        height: 4px;
        border-radius: 50%;
        background: #667eea;
        opacity: 0.4;
      }

      .deco-particle-1 {
        top: 15%;
        left: 35%;
        animation: particle-float 3s ease-in-out infinite;
      }

      .deco-particle-2 {
        top: 45%;
        right: 25%;
        background: #11998e;
        animation: particle-float 4s ease-in-out infinite 0.5s;
      }

      .deco-particle-3 {
        bottom: 35%;
        left: 20%;
        background: #f2994a;
        animation: particle-float 3.5s ease-in-out infinite 1s;
      }

      .deco-particle-4 {
        top: 65%;
        right: 15%;
        background: #38ef7d;
        animation: particle-float 4.5s ease-in-out infinite 1.5s;
      }

      .deco-particle-5 {
        top: 25%;
        left: 50%;
        background: #f2c94c;
        animation: particle-float 3s ease-in-out infinite 2s;
      }

      .deco-particle-6 {
        bottom: 20%;
        right: 35%;
        background: #764ba2;
        animation: particle-float 5s ease-in-out infinite 0.8s;
      }

      // 光晕
      .deco-glow {
        position: absolute;
        border-radius: 50%;
        filter: blur(60px);
        opacity: 0.06;
      }

      .deco-glow-1 {
        top: 10%;
        right: 15%;
        width: 300px;
        height: 300px;
        background: linear-gradient(135deg, #667eea, #764ba2);
        animation: glow-pulse 8s ease-in-out infinite;
      }

      .deco-glow-2 {
        bottom: 15%;
        left: 10%;
        width: 250px;
        height: 250px;
        background: linear-gradient(135deg, #11998e, #38ef7d);
        animation: glow-pulse 10s ease-in-out infinite reverse;
      }
    }
  }
}

// 特性卡片
.feature-card {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-radius: var(--radius-lg);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-4px);
  }
}

.feature-card-dark {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(10px);

  &:hover {
    background: rgba(255, 255, 255, 0.08);
    border-color: rgba(255, 255, 255, 0.12);
  }
}

.feature-card-light {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(10px);

  &:hover {
    background: rgba(255, 255, 255, 0.85);
    border-color: rgba(0, 0, 0, 0.1);
  }
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-icon-purple {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.2), rgba(118, 75, 162, 0.2));
  color: #8b5cf6;
}

.feature-icon-indigo {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  color: #6366f1;
}

.feature-icon-blue {
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.2), rgba(6, 182, 212, 0.2));
  color: #3b82f6;
}

.feature-icon-green {
  background: linear-gradient(135deg, rgba(16, 185, 129, 0.2), rgba(5, 150, 105, 0.2));
  color: #10b981;
}

// 慢旋转动画
@keyframes rotate-slow {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

// 线条滑动动画
@keyframes slide-right {
  0%, 100% {
    transform: translateX(0);
    opacity: 0.1;
  }
  50% {
    transform: translateX(30px);
    opacity: 0.2;
  }
}

@keyframes slide-left {
  0%, 100% {
    transform: translateX(0);
    opacity: 0.1;
  }
  50% {
    transform: translateX(-30px);
    opacity: 0.2;
  }
}

// 波浪移动动画
@keyframes wave-move {
  0%, 100% {
    transform: translateX(0) scaleY(1);
  }
  50% {
    transform: translateX(10px) scaleY(1.2);
  }
}

// 粒子浮动动画
@keyframes particle-float {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.4;
  }
  50% {
    transform: translateY(-20px) scale(1.5);
    opacity: 0.8;
  }
}

// 光晕脉冲动画
@keyframes glow-pulse {
  0%, 100% {
    opacity: 0.06;
    transform: scale(1);
  }
  50% {
    opacity: 0.12;
    transform: scale(1.1);
  }
}

// 登录卡片容器
.login-card-wrapper {
  .login-card {
    backdrop-filter: blur(20px) !important;
    -webkit-backdrop-filter: blur(20px) !important;
    border-radius: var(--radius-xl) !important;
    padding: 28px !important;
    transition: all 0.3s ease !important;
  }
}

// 粒子透明度
.dark .login-particles::before,
.dark .login-particles::after {
  opacity: 0.15;
}

// ===== 装饰元素交互效果 =====

// 鼠标跟随光晕
.mouse-glow {
  position: absolute;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: radial-gradient(
    circle,
    rgba(102, 126, 234, 0.15) 0%,
    rgba(118, 75, 162, 0.08) 40%,
    transparent 70%
  );
  pointer-events: none;
  transform: translate(-50%, -50%);
  transition: opacity 0.3s ease;
  z-index: 1;
  filter: blur(20px);
}

// 点击波纹
@keyframes ripple-expand {
  0% {
    width: 0;
    height: 0;
    opacity: 0.6;
    border-width: 3px;
  }
  100% {
    width: 600px;
    height: 600px;
    opacity: 0;
    border-width: 1px;
  }
}

.click-ripple {
  position: absolute;
  border: 3px solid rgba(102, 126, 234, 0.6);
  border-radius: 50%;
  pointer-events: none;
  transform: translate(-50%, -50%);
  animation: ripple-expand 1s ease-out forwards;
  z-index: 2;
}

// 背景鼠标悬停效果
.#{$prefix-cls}__left {
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: radial-gradient(
      600px circle at var(--mouse-x, 50%) var(--mouse-y, 50%),
      rgba(102, 126, 234, 0.1),
      transparent 40%
    );
    opacity: 0;
    transition: opacity 0.3s ease;
    z-index: 1;
    pointer-events: none;
  }

  &:hover::before {
    opacity: 1;
  }

  &:active {
    .left-gradient-bg {
      filter: brightness(0.95);
      transition: filter 0.1s ease;
    }
  }
}

// 背景点击闪光
@keyframes brand-flash {
  0% {
    opacity: 0;
  }
  20% {
    opacity: 0.3;
  }
  100% {
    opacity: 0;
  }
}

.brand-click-flash {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: radial-gradient(
    circle at center,
    rgba(139, 92, 246, 0.4),
    transparent 60%
  );
  pointer-events: none;
  animation: brand-flash 0.6s ease-out;
  z-index: 1;
}

// 鼠标悬停效果
.interactive-deco {
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  will-change: transform, opacity;

  &:hover {
    filter: brightness(1.5) drop-shadow(0 0 12px rgba(102, 126, 234, 0.6));
    transform: scale(1.2) !important;
    opacity: 1 !important;
  }

  &:active {
    transform: scale(0.9) !important;
    transition-duration: 0.1s;
  }
}

// 点击波纹效果
@keyframes deco-ripple {
  0% {
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0.6);
  }
  50% {
    box-shadow: 0 0 0 20px rgba(102, 126, 234, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(102, 126, 234, 0);
  }
}

// 点击闪光效果
@keyframes deco-flash {
  0% {
    filter: brightness(1) drop-shadow(0 0 0 transparent);
  }
  30% {
    filter: brightness(2.5) drop-shadow(0 0 20px rgba(139, 92, 246, 0.8));
  }
  100% {
    filter: brightness(1) drop-shadow(0 0 0 transparent);
  }
}

.deco-clicked {
  animation: deco-flash 0.6s ease-out;
}

// 特性卡片增强交互
.feature-card {
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
    transition: left 0.5s ease;
  }

  &:hover::before {
    left: 100%;
  }

  &:hover {
    transform: translateY(-4px) scale(1.02);
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.2);
  }

  &:active {
    transform: translateY(-2px) scale(0.98);
  }
}

// Logo 区域交互
.animate-slide-down {
  cursor: pointer;
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);

    .mr-20px {
      background: rgba(255, 255, 255, 0.25) !important;
      box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
    }
  }

  &:active {
    transform: translateY(0);
  }
}

// 标题文字交互
.text-gradient-primary {
  transition: all 0.3s ease;

  &:hover {
    background-size: 200% auto;
    animation: gradient-shift 2s ease infinite;
  }
}

@keyframes gradient-shift {
  0%, 100% {
    background-position: 0% center;
  }
  50% {
    background-position: 100% center;
  }
}

// 核心描述区域交互
.animate-slide-up {
  h1 {
    transition: all 0.3s ease;

    &:hover {
      text-shadow: 0 0 30px rgba(102, 126, 234, 0.3);
    }
  }

  p {
    transition: all 0.3s ease;

    &:hover {
      color: rgba(255, 255, 255, 0.8) !important;
    }
  }
}

// 版权信息交互
.text-white\/30 {
  transition: all 0.3s ease;
  cursor: default;

  &:hover {
    color: rgba(255, 255, 255, 0.5) !important;
  }
}

.light .login-particles::before,
.light .login-particles::after {
  opacity: 0.08;
}

// 修复 Divider 文字与横线重叠
.login-form {
  .el-divider {
    margin: 16px 0 !important;

    .el-divider__text {
      padding: 0 12px !important;
      white-space: nowrap;
    }
  }

  &::before,
  &::after {
    display: table;
    content: '';
  }

  &::after {
    clear: both;
  }
}
</style>

<style lang="scss">
// 深色模式登录表单适配（右侧为浅色背景）
.dark .login-form {
  .el-divider__text {
    background-color: transparent;
    color: rgba(0, 0, 0, 0.6);
  }

  .el-card {
    background-color: rgba(255, 255, 255, 0.6) !important;
    border-color: rgba(0, 0, 0, 0.08) !important;
  }

  .el-input__wrapper {
    background-color: rgba(255, 255, 255, 0.8) !important;
    border: 1px solid rgba(0, 0, 0, 0.1) !important;
    box-shadow: none !important;

    &:hover,
    &.is-focus {
      background-color: #fff !important;
      border-color: var(--el-color-primary) !important;
      box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
    }

    .el-input__inner {
      color: #333 !important;

      &::placeholder {
        color: rgba(0, 0, 0, 0.4) !important;
      }
    }
  }

  .el-button {
    color: rgba(0, 0, 0, 0.85) !important;

    &:hover {
      color: rgba(0, 0, 0, 0.85) !important;
    }

    &--default {
      background-color: rgba(255, 255, 255, 0.8) !important;
      border: 1px solid rgba(0, 0, 0, 0.1) !important;
      color: rgba(0, 0, 0, 0.85) !important;

      &:hover {
        background-color: rgba(255, 255, 255, 0.8) !important;
        border-color: rgba(0, 0, 0, 0.1) !important;
        color: rgba(0, 0, 0, 0.85) !important;
      }
    }

    &--primary {
      color: #fff !important;
      background: var(--primary-gradient) !important;
      border: none !important;

      &:hover {
        background: var(--primary-gradient) !important;
        box-shadow: none !important;
        color: #fff !important;
      }
    }
  }

  .el-link {
    color: rgba(0, 0, 0, 0.6) !important;

    &:hover {
      color: var(--el-color-primary) !important;
    }
  }

  .el-checkbox__label {
    color: rgba(0, 0, 0, 0.6) !important;
  }

  .el-checkbox__inner {
    background-color: #fff !important;
    border-color: rgba(0, 0, 0, 0.2) !important;
  }

  h2 {
    color: #333 !important;
  }

  // 其他登录方式分割线文字
  .other-login-divider {
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 16px 0;

    &::before,
    &::after {
      flex: 1;
      content: '';
      border-top: 1px solid rgba(0, 0, 0, 0.12);
    }

    span {
      padding: 0 12px;
      color: rgba(0, 0, 0, 0.5);
      font-size: 13px;
      white-space: nowrap;
    }
  }
}

// 浅色模式登录表单适配（右侧为深色背景）
.light .login-form {
  .el-divider__text {
    background-color: transparent;
    color: rgba(255, 255, 255, 0.6);
  }

  .el-card {
    background-color: transparent;
    border-color: rgba(255, 255, 255, 0.06);
  }

  .el-input__wrapper {
    background-color: rgba(255, 255, 255, 0.05) !important;
    border: 1px solid rgba(255, 255, 255, 0.1) !important;
    box-shadow: none !important;

    &:hover,
    &.is-focus {
      background-color: rgba(255, 255, 255, 0.08) !important;
      border-color: var(--el-color-primary) !important;
      box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
    }

    .el-input__inner {
      color: #fff !important;

      &::placeholder {
        color: rgba(255, 255, 255, 0.4) !important;
      }
    }
  }

  .el-button {
    color: rgba(0, 0, 0, 0.85) !important;

    &:hover {
      color: rgba(0, 0, 0, 0.85) !important;
    }

    &--default {
      background-color: rgba(255, 255, 255, 0.8) !important;
      border: 1px solid rgba(0, 0, 0, 0.1) !important;
      color: rgba(0, 0, 0, 0.85) !important;

      &:hover {
        background-color: rgba(255, 255, 255, 0.8) !important;
        border-color: rgba(0, 0, 0, 0.1) !important;
        color: rgba(0, 0, 0, 0.85) !important;
      }
    }

    &--primary {
      color: #fff !important;

      &:hover {
        background: var(--primary-gradient) !important;
        box-shadow: none !important;
      }
    }
  }

  .el-link {
    color: rgba(255, 255, 255, 0.6) !important;

    &:hover {
      color: var(--el-color-primary) !important;
    }
  }

  h2 {
    color: #fff !important;
  }
}

// 登录卡片全局样式 - 深色模式（右侧为浅色背景）
.dark .login-card {
  background: rgba(255, 255, 255, 0.7) !important;
  border: 1px solid rgba(0, 0, 0, 0.08) !important;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.1) !important;

  &:hover {
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15) !important;
    border-color: rgba(102, 126, 234, 0.2) !important;
  }
}

// 登录卡片全局样式 - 浅色模式（右侧为深色背景）
.light .login-card {
  background: rgba(17, 22, 40, 0.6) !important;
  border: 1px solid rgba(255, 255, 255, 0.08) !important;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.3) !important;

  &:hover {
    box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4) !important;
    border-color: rgba(255, 255, 255, 0.12) !important;
  }
}
</style>
