import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({ baseURL: '/api', timeout: 20000 })

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('ubax_token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

http.interceptors.response.use(
  (resp) => {
    const body = resp.data
    const silent = resp.config?.silent
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) return body.data
      if (body.code === 401) {
        localStorage.removeItem('ubax_token')
        if (location.hash !== '#/login' && location.pathname !== '/login') {
          location.href = '/login'
        }
        return Promise.reject(new Error(body.message || '未认证'))
      }
      if (!silent) ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return body
  },
  (err) => {
    const status = err.response?.status
    if (status === 401) {
      localStorage.removeItem('ubax_token')
      location.href = '/login'
    }
    // background pollers pass { silent: true } so an outage doesn't spam toasts
    if (!err.config?.silent) {
      ElMessage.error(err.response?.data?.message || err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default http
