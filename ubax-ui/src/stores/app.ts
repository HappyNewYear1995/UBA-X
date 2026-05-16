import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface MenuItem {
  path: string
  title: string
  icon: string
}

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const theme = ref<'light' | 'dark'>('light')

  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  const setTheme = (newTheme: 'light' | 'dark') => {
    theme.value = newTheme
  }

  return {
    sidebarCollapsed,
    theme,
    toggleSidebar,
    setTheme,
  }
})
