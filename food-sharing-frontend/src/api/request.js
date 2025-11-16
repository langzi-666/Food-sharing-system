import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const api = axios.create({
  baseURL: 'http://localhost:8081/api/v1',
  timeout: 10000
})

// 请求拦截器
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理响应格式和错误
api.interceptors.response.use(
  (response) => {
    const res = response.data
    
    // 如果响应是统一格式 ApiResponse
    if (res && typeof res === 'object' && 'code' in res) {
      // 成功响应
      if (res.code === 200) {
        return res.data !== undefined ? res.data : res
      }
      // 业务错误
      const message = res.message || '请求失败'
      ElMessage.error(message)
      return Promise.reject(new Error(message))
    }
    
    // 兼容旧格式响应
    return res
  },
  (error) => {
    console.error('API错误:', error)
    
    let message = '请求失败，请稍后重试'
    
    if (error.response) {
      // 服务器返回了错误响应
      const status = error.response.status
      const data = error.response.data
      
      // 处理统一响应格式
      if (data && typeof data === 'object' && 'code' in data && 'message' in data) {
        message = data.message || message
      } else if (data && data.message) {
        message = data.message
      } else {
        // 根据HTTP状态码设置默认消息
        switch (status) {
          case 400:
            message = '请求参数错误'
            break
          case 401:
            message = '未登录或登录已过期'
            // 清除token并跳转到登录页
            localStorage.removeItem('token')
            if (window.location.pathname !== '/login') {
              window.location.href = '/login'
            }
            break
          case 403:
            message = '没有权限访问该资源'
            break
          case 404:
            message = '请求的资源不存在'
            break
          case 500:
            message = '服务器内部错误'
            break
          case 502:
            message = '网关错误'
            break
          case 503:
            message = '服务不可用'
            break
          case 504:
            message = '网关超时'
            break
          default:
            message = `请求失败 (${status})`
        }
      }
      
      // 401和403错误不显示消息框，直接跳转
      if (status === 401 || status === 403) {
        ElMessage.error(message)
        return Promise.reject(error)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应
      message = '网络错误，请检查网络连接'
    } else {
      // 请求配置出错
      message = error.message || '请求配置错误'
    }
    
    ElMessage.error(message)
    return Promise.reject(error)
  }
)

export default api
