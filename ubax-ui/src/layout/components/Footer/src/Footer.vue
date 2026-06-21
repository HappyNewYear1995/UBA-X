<script lang="ts" setup>
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'

// eslint-disable-next-line vue/no-reserved-component-names
defineOptions({ name: 'Footer' })

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('footer')

const appStore = useAppStore()

const title = computed(() => appStore.getTitle)

// 添加当前年份计算属性
const currentYear = computed(() => new Date().getFullYear())
</script>

<template>
  <div
    :class="prefixCls"
    class="h-[var(--app-footer-height)] bg-[var(--app-content-bg-color)] text-center leading-[var(--app-footer-height)] text-[var(--el-text-color-placeholder)] dark:bg-[var(--el-bg-color)] overflow-hidden transition-all duration-300"
  >
    <span class="text-13px font-500">Copyright ©{{ currentYear }} 杭州欢年科技有限公司</span>
  </div>
</template>

<style lang="scss" scoped>
$prefix-cls: #{$namespace}-footer;

.#{$prefix-cls} {
  border-top: 1px solid var(--border-color-light);
}
</style>
