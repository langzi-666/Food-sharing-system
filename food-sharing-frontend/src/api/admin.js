import api from './request.js'

// 用户管理
export function getUsers(params) {
  return api.get('/admin/users', { params })
}

export function getUser(id) {
  return api.get(`/admin/users/${id}`)
}

export function enableUser(id, enabled) {
  return api.put(`/admin/users/${id}/enable`, { enabled })
}

export function updateUserRole(id, role) {
  return api.put(`/admin/users/${id}/role`, { role })
}

export function deleteUser(id) {
  return api.delete(`/admin/users/${id}`)
}

// 内容审核
export function getPendingPosts(params) {
  return api.get('/admin/posts/pending', { params })
}

export function approvePost(id) {
  return api.put(`/admin/posts/${id}/approve`)
}

export function rejectPost(id, reason) {
  return api.put(`/admin/posts/${id}/reject`, { reason })
}

// 数据统计
export function getStatistics() {
  return api.get('/admin/statistics/overview')
}

// 系统配置
export function getConfigs() {
  return api.get('/admin/configs')
}

export function getConfig(key) {
  return api.get(`/admin/configs/${key}`)
}

export function createConfig(data) {
  return api.post('/admin/configs', data)
}

export function updateConfig(key, data) {
  return api.put(`/admin/configs/${key}`, data)
}

export function deleteConfig(key) {
  return api.delete(`/admin/configs/${key}`)
}

// 日志查看
export function getLogs(params) {
  return api.get('/admin/logs', { params })
}

