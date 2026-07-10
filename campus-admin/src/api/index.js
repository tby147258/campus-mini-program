import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// D1: 依赖 Vite proxy 转发 /api → http://localhost:8080，详见 vite.config.js proxy 配置
const api = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器：统一处理业务错误、401 过期、网络异常
api.interceptors.response.use(
  response => {
    const data = response.data
    // D2: 业务错误（HTTP 200 但 code ≠ 200）→ 拒绝以走 catch 统一处理
    if (data.code !== undefined && data.code !== 200) {
      // D10: 401 自动清除 token 并跳转登录页
      if (data.code === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
        return Promise.reject(new Error(data.msg || '登录已过期'))
      }
      return Promise.reject(data)
    }
    // D2: 成功时直接返回 data.data（剥离外层 Result 结构）
    return data.data !== undefined ? data.data : data
  },
  error => {
    // D11: 统一错误处理
    if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络')
    } else if (error.response) {
      const status = error.response.status
      if (status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        router.push('/login')
      } else if (status >= 500) {
        ElMessage.error('服务器异常，请稍后重试')
      }
    } else if (error.message === 'Network Error') {
      ElMessage.error('网络异常，请检查连接')
    }
    return Promise.reject(error)
  }
)

// ======== 验证码 API ========
export const captchaApi = {
  get() {
    return api.get('/captcha')
  },
  verify(params) {
    return api.post('/captcha/verify', params)
  }
}

// ======== 认证 API ========
export const authApi = {
  login(params) {
    return api.post('/auth/admin-login', params)
  },
  register(data) {
    return api.post('/auth/register', data)
  },
  forgotPassword(email) {
    return api.post('/auth/forgot-password', { email })
  },
  resetPassword(data) {
    return api.post('/auth/reset-password', data)
  },
  me() {
    return api.get('/auth/me')
  },
  logout() {
    return api.post('/auth/logout')
  }
}

// ======== 统计 API ========
export const statisticsApi = {
  get() {
    return api.get('/statistics')
  }
}

// ======== 公告 API ========
export const announcementApi = {
  list(params) {
    return api.get('/announcement', { params })
  },
  getById(id) {
    return api.get(`/announcement/${id}`)
  },
  create(data) {
    return api.post('/announcement', data)
  },
  update(id, data) {
    return api.put(`/announcement/${id}`, data)
  },
  delete(id) {
    return api.delete(`/announcement/${id}`)
  }
}

// ======== 失物招领 API ========
export const lostFoundApi = {
  list(params) {
    return api.get('/lost-found', { params })
  },
  audit(id, data) {
    return api.put(`/lost-found/${id}/audit`, data)
  },
  delete(id) {
    return api.delete(`/lost-found/${id}`)
  }
}

// ======== 报修工单 API ========
export const repairApi = {
  list(params) {
    return api.get('/repair', { params })
  },
  updateStatus(id, data) {
    return api.put(`/repair/${id}/status`, data)
  }
}

// ======== 操作日志 API ========
export const logApi = {
  page(params) {
    return api.get('/operation-logs', { params })
  }
}

// ======== 系统配置 API ========
export const configApi = {
  list() {
    return api.get('/system-config')
  },
  create(data) {
    return api.post('/system-config', data)
  },
  update(id, data) {
    return api.put(`/system-config/${id}`, data)
  },
  delete(id) {
    return api.delete(`/system-config/${id}`)
  }
}

// ======== 用户管理 API ========
export const userApi = {
  page(params) {
    return api.get('/users', { params })
  },
  del(id) {
    return api.delete(`/users/${id}`)
  },
  changePassword(id, data) {
    return api.put(`/users/${id}/password`, data)
  }
}

export default api