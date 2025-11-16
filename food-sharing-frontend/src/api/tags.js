import api from './request.js'

// 获取标签列表
export const getTags = () => {
  return api.get('/tags')
}

// 创建标签
export const createTag = (data) => {
  return api.post('/tags', data)
}
