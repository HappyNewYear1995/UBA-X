import { defineStore } from 'pinia'
import { authApi } from '../api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('ubax_token') || '',
    user: JSON.parse(localStorage.getItem('ubax_user') || 'null')
  }),
  getters: {
    isLogin: (s) => !!s.token,
    permissions: (s) => (s.user && s.user.permissions) || [],
    role: (s) => (s.user && s.user.role) || '',
    isSuperAdmin: (s) => s.user && s.user.role === 'SUPER_ADMIN'
  },
  actions: {
    async login(payload) {
      const data = await authApi.login(payload)
      this.token = data.token
      this.user = data
      localStorage.setItem('ubax_token', data.token)
      localStorage.setItem('ubax_user', JSON.stringify(data))
      return data
    },
    logout() {
      this.token = ''
      this.user = null
      localStorage.removeItem('ubax_token')
      localStorage.removeItem('ubax_user')
    },
    has(perm) {
      return this.permissions.includes(perm) || this.role === 'SUPER_ADMIN'
    }
  }
})
