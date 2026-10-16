import { defineStore } from 'pinia'
import { appApi } from '../api'

export const useAppStore = defineStore('app', {
  state: () => ({
    apps: [],
    currentAppId: null,
    loaded: false
  }),
  getters: {
    currentApp: (s) => s.apps.find((a) => a.id === s.currentAppId) || null
  },
  actions: {
    async load(force = false) {
      if (this.loaded && !force) return this.apps
      try {
        this.apps = await appApi.list()
        // Default to "全部应用" (null) so dashboards aggregate across apps.
        this.loaded = true
      } catch (e) {
        this.apps = []
      }
      return this.apps
    },
    setCurrent(id) {
      this.currentAppId = id
    }
  }
})
