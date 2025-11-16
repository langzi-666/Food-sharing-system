import api from './request.js'

// 上传单张图片
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/upload/image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 上传多张图片
export const uploadImages = (files) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  return api.post('/upload/images', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 上传视频
export const uploadVideo = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return api.post('/upload/video', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
