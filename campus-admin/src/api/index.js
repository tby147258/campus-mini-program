import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  res => res.data,
  err => Promise.reject(err)
)

export const authApi = {
  login: (data) => request.post('/auth/admin-login', data)
}

export const announcementApi = {
  list: (params) => request.get('/announcements', { params }),
  getById: (id) => request.get(`/announcements/${id}`),
  create: (data) => request.post('/announcements', data),
  update: (id, data) => request.put(`/announcements/${id}`, data),
  delete: (id) => request.delete(`/announcements/${id}`)
}

export const lostFoundApi = {
  list: (params) => request.get('/lost-found', { params }),
  getById: (id) => request.get(`/lost-found/${id}`),
  audit: (id, data) => request.put(`/lost-found/${id}/audit`, data),
  delete: (id) => request.delete(`/lost-found/${id}`)
}

export const repairApi = {
  list: (params) => request.get('/repair-orders', { params }),
  getById: (id) => request.get(`/repair-orders/${id}`),
  updateStatus: (id, data) => request.put(`/repair-orders/${id}/status`, data)
}

export default request