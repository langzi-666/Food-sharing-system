import api from './request.js'

// 获取分类列表
export const getCategories = () => {
  return api.get('/categories')
}

// 创建分类
export const createCategory = (data) => {
  return api.post('/categories', data)
}
