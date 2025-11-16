import api from './request'

// 商家入驻
export const registerMerchant = (data) => {
  return api.post('/merchants/register', data)
}

// 上传营业执照
export const uploadLicense = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/merchants/license', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 获取我的商家信息
export const getMyMerchant = () => {
  return api.get('/merchants/me')
}

// 更新商家信息
export const updateMerchant = (data) => {
  return api.put('/merchants/me', data)
}

// 获取商家详情
export const getMerchantDetail = (id) => {
  return api.get(`/merchants/${id}`)
}

// 获取商家列表
export const getMerchants = (params) => {
  return api.get('/merchants', { params })
}

// 获取热门商家
export const getTopRatedMerchants = (params) => {
  return api.get('/merchants/top-rated', { params })
}

