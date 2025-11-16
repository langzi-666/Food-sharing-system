import api from './request'

export function loginApi(payload) {
  return api.post('/auth/login', payload)
}

export function registerApi(payload) {
  return api.post('/auth/register', payload)
}
