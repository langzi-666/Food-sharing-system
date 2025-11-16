import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8081/api/v1',
  timeout: 10000
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers = config.headers || {}
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (resp) => resp,
  (error) => Promise.reject(error)
)

export default api
