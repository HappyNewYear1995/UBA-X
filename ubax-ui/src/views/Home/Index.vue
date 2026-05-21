<template>
  <div class="page-container">
    <!-- 欢迎区域 -->
    <el-card shadow="never" class="welcome-card">
      <el-skeleton :loading="loading" animated>
        <el-row :gutter="16" justify="space-between">
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="flex items-center">
              <el-avatar :src="avatar" :size="70" class="mr-16px avatar-glow">
                <img src="@/assets/imgs/avatar.gif" alt="" />
              </el-avatar>
              <div>
                <div class="text-22px font-bold text-gradient-primary">
                  {{ t('workplace.welcome') }} {{ username }}
                </div>
                <div class="mt-8px text-14px text-gray-400">
                  {{ t('workplace.happyDay') }}
                </div>
                <div class="mt-6px text-13px text-gray-500">
                  {{ t('workplace.toady') }}，20℃ - 32℃！
                </div>
              </div>
            </div>
          </el-col>
          <el-col :xl="12" :lg="12" :md="12" :sm="24" :xs="24">
            <div class="h-70px flex items-center justify-end lt-sm:mt-10px">
              <div class="px-8px text-right stat-item">
                <div class="mb-8px text-13px text-gray-400">{{ t('workplace.project') }}</div>
                <CountTo
                  class="text-24px font-bold text-gradient-primary"
                  :start-val="0"
                  :end-val="totalSate.project"
                  :duration="2600"
                />
              </div>
              <el-divider direction="vertical" />
              <div class="px-8px text-right stat-item">
                <div class="mb-8px text-13px text-gray-400">{{ t('workplace.toDo') }}</div>
                <CountTo
                  class="text-24px font-bold text-gradient-gold"
                  :start-val="0"
                  :end-val="totalSate.todo"
                  :duration="2600"
                />
              </div>
              <el-divider direction="vertical" border-style="dashed" />
              <div class="px-8px text-right stat-item">
                <div class="mb-8px text-13px text-gray-400">{{ t('workplace.access') }}</div>
                <CountTo
                  class="text-24px font-bold text-gradient-success"
                  :start-val="0"
                  :end-val="totalSate.access"
                  :duration="2600"
                />
              </div>
            </div>
          </el-col>
        </el-row>
      </el-skeleton>
    </el-card>

    <!-- 图表区域 -->
    <el-row class="mt-16px" :gutter="16" justify="space-between">
      <el-col :xl="16" :lg="16" :md="24" :sm="24" :xs="24" class="mb-16px">
        <el-card shadow="never" class="chart-card">
          <el-skeleton :loading="loading" animated>
            <el-row :gutter="20" justify="space-between">
              <el-col :xl="10" :lg="10" :md="24" :sm="24" :xs="24">
                <el-card shadow="hover" class="chart-inner-card mb-8px">
                  <el-skeleton :loading="loading" animated>
                    <Echart :options="pieOptionsData" :height="280" />
                  </el-skeleton>
                </el-card>
              </el-col>
              <el-col :xl="14" :lg="14" :md="24" :sm="24" :xs="24">
                <el-card shadow="hover" class="chart-inner-card mb-8px">
                  <el-skeleton :loading="loading" animated>
                    <Echart :options="barOptionsData" :height="280" />
                  </el-skeleton>
                </el-card>
              </el-col>
            </el-row>
          </el-skeleton>
        </el-card>
      </el-col>
      <el-col :xl="8" :lg="8" :md="24" :sm="24" :xs="24" class="mb-16px">
        <el-card shadow="never" class="shortcut-card">
          <template #header>
            <div class="h-3 flex justify-between">
              <span class="text-15px font-600">{{ t('workplace.shortcutOperation') }}</span>
            </div>
          </template>
          <el-skeleton :loading="loading" animated>
            <el-row>
              <el-col v-for="item in shortcut" :key="`team-${item.name}`" :span="8" class="mb-8px">
                <div class="flex items-center shortcut-item">
                  <Icon :icon="item.icon" class="mr-8px" :style="{ color: item.color }" />
                  <el-link type="default" :underline="false" @click="handleShortcutClick(item.url)">
                    {{ item.name }}
                  </el-link>
                </div>
              </el-col>
            </el-row>
          </el-skeleton>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
<script lang="ts" setup>
import { set } from 'lodash-es'
import { EChartsOption } from 'echarts'
import { formatTime } from '@/utils'

import { useUserStore } from '@/store/modules/user'
// import { useWatermark } from '@/hooks/web/useWatermark'
import type { Shortcut, WorkplaceTotal } from './types'
import { barOptions, pieOptions } from './echarts-data'
import { useRouter } from 'vue-router'

defineOptions({ name: 'Index' })

const { t } = useI18n()
const router = useRouter()
const userStore = useUserStore()
// const { setWatermark } = useWatermark()
const loading = ref(true)
const avatar = userStore.getUser.avatar
const username = userStore.getUser.nickname
const pieOptionsData = reactive<EChartsOption>(pieOptions) as EChartsOption
// 获取统计数
let totalSate = reactive<WorkplaceTotal>({
  project: 0,
  access: 0,
  todo: 0
})

const getCount = async () => {
  const data = {
    project: 40,
    access: 2340,
    todo: 10
  }
  totalSate = Object.assign(totalSate, data)
}

// 获取快捷入口
let shortcut = reactive<Shortcut[]>([])

const getShortcut = async () => {
  const data = [
    {
      name: '首页',
      icon: 'ion:home-outline',
      url: '/',
      color: '#1fdaca'
    }
  ]
  shortcut = Object.assign(shortcut, data)
}

// 用户来源
const getUserAccessSource = async () => {
  const data = [
    { value: 335, name: 'analysis.directAccess' },
    { value: 310, name: 'analysis.mailMarketing' },
    { value: 234, name: 'analysis.allianceAdvertising' },
    { value: 135, name: 'analysis.videoAdvertising' },
    { value: 1548, name: 'analysis.searchEngines' }
  ]
  set(
    pieOptionsData,
    'legend.data',
    data.map((v) => t(v.name))
  )
  pieOptionsData!.series![0].data = data.map((v) => {
    return {
      name: t(v.name),
      value: v.value
    }
  })
}
const barOptionsData = reactive<EChartsOption>(barOptions) as EChartsOption

// 周活跃量
const getWeeklyUserActivity = async () => {
  const data = [
    { value: 13253, name: 'analysis.monday' },
    { value: 34235, name: 'analysis.tuesday' },
    { value: 26321, name: 'analysis.wednesday' },
    { value: 12340, name: 'analysis.thursday' },
    { value: 24643, name: 'analysis.friday' },
    { value: 1322, name: 'analysis.saturday' },
    { value: 1324, name: 'analysis.sunday' }
  ]
  set(
    barOptionsData,
    'xAxis.data',
    data.map((v) => t(v.name))
  )
  set(barOptionsData, 'series', [
    {
      name: t('analysis.activeQuantity'),
      data: data.map((v) => v.value),
      type: 'bar'
    }
  ])
}

const getAllApi = async () => {
  await Promise.all([getCount(), getShortcut(), getUserAccessSource(), getWeeklyUserActivity()])
  loading.value = false
}

const handleShortcutClick = (url: string) => {
  router.push(url)
}

getAllApi()
</script>

<style lang="scss" scoped>
// 欢迎卡片
.welcome-card {
  background: linear-gradient(135deg, var(--app-content-card-bg) 0%, rgba(102, 126, 234, 0.05) 100%) !important;
  border: 1px solid var(--app-content-card-border) !important;
  border-radius: var(--radius-lg) !important;
  overflow: hidden;

  &::before {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 3px;
    background: var(--primary-gradient);
    content: '';
  }
}

// 头像发光效果
.avatar-glow {
  box-shadow: 0 0 20px rgba(102, 126, 234, 0.4) !important;
  border: 2px solid rgba(102, 126, 234, 0.3) !important;
}

// 统计项
.stat-item {
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-2px);
  }
}

// 图表卡片
.chart-card {
  background: transparent !important;
  border: none !important;
  box-shadow: none !important;
  padding: 0 !important;
}

.chart-inner-card {
  background: var(--app-content-card-bg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  border-radius: var(--radius-lg) !important;
  transition: all 0.3s ease !important;

  &:hover {
    box-shadow: var(--shadow-lg) !important;
    transform: translateY(-2px);
  }
}

// 快捷操作卡片
.shortcut-card {
  background: var(--app-content-card-bg) !important;
  border: 1px solid var(--app-content-card-border) !important;
  border-radius: var(--radius-lg) !important;

  :deep(.el-card__header) {
    border-bottom: 1px solid var(--app-content-card-border);
    padding: 16px 20px;
  }
}

// 快捷操作项
.shortcut-item {
  padding: 10px 12px;
  border-radius: var(--radius-md);
  transition: all 0.3s ease;

  &:hover {
    background: var(--left-menu-hover-bg-color);
    transform: translateX(4px);
  }
}

// 文字渐变 - 成功色
.text-gradient-success {
  background: var(--success-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
</style>
