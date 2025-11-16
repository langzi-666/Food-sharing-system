import api from './request.js'

// 获取用户信息
export const getUserInfo = () => {
  return api.get('/users/me')
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return api.put('/users/me', data)
}

// 上传头像
export const uploadAvatar = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/users/me/avatar', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 修改密码
export const changePassword = (data) => {
  return api.post('/users/me/password', data)
}
