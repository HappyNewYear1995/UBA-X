<script lang="ts" setup>
import { computed, onMounted, ref, unref, watch } from 'vue'
import { useAppStore } from '@/store/modules/app'
import { useDesign } from '@/hooks/web/useDesign'

defineOptions({ name: 'Logo' })

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('logo')

const appStore = useAppStore()

const show = ref(true)

const title = computed(() => appStore.getTitle)

const layout = computed(() => appStore.getLayout)

const collapse = computed(() => appStore.getCollapse)

onMounted(() => {
  if (unref(collapse)) show.value = false
})

watch(
  () => collapse.value,
  (collapse: boolean) => {
    if (unref(layout) === 'topLeft' || unref(layout) === 'cutMenu') {
      show.value = true
      return
    }
    if (!collapse) {
      setTimeout(() => {
        show.value = !collapse
      }, 400)
    } else {
      show.value = !collapse
    }
  }
)

watch(
  () => layout.value,
  (layout) => {
    if (layout === 'top' || layout === 'cutMenu') {
      show.value = true
    } else {
      if (unref(collapse)) {
        show.value = false
      } else {
        show.value = true
      }
    }
  }
)
</script>

<template>
  <div>
    <router-link
      :class="[
        prefixCls,
        layout !== 'classic' ? `${prefixCls}__Top` : '',
        'flex !h-[var(--logo-height)] items-center cursor-pointer pl-8px relative decoration-none overflow-hidden transition-all duration-300'
      ]"
      to="/"
      style="border-bottom: 1px solid var(--logo-border-color);"
    >
      <div class="flex items-center justify-center h-[calc(var(--logo-height)-10px)] w-[calc(var(--logo-height)-10px)] rounded-lg bg-gradient-to-br from-purple-500 to-indigo-600 shadow-lg">
        <img
          class="h-[calc(var(--logo-height)-18px)] w-[calc(var(--logo-height)-18px)]"
          src="@/assets/imgs/logo.png"
        />
      </div>
      <div
        v-if="show"
        :class="[
          'ml-10px text-18px font-700 tracking-wide',
          {
            'text-[var(--logo-title-text-color)]': layout === 'classic',
            'text-[var(--top-header-text-color)]':
              layout === 'topLeft' || layout === 'top' || layout === 'cutMenu'
          }
        ]"
        style="background: var(--logo-bg-gradient); -webkit-background-clip: text; -webkit-text-fill-color: transparent; background-clip: text;"
      >
        {{ title }}
      </div>
    </router-link>
  </div>
</template>
