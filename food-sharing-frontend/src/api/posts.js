import api from './request.js'

// 创建内容
export const createPost = (data) => {
  return api.post('/posts', data)
}

// 获取内容列表
export const getPosts = (params) => {
  return api.get('/posts', { params })
}

// 获取内容详情
export const getPostDetail = (id) => {
  return api.get(`/posts/${id}`)
}
