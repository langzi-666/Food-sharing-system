import api from './request'

// 个性化推荐
export const getPersonalizedRecommendations = (limit = 10) => {
  return api.get('/recommendations/personalized', { params: { limit } })
}

// 基于位置的推荐
export const getLocationBasedRecommendations = (lat, lng, limit = 10) => {
  return api.get('/recommendations/location-based', { params: { lat, lng, limit } })
}

// 热门内容
export const getPopularPosts = (limit = 10) => {
  return api.get('/recommendations/popular', { params: { limit } })
}

// 热门内容（综合评分）
export const getHotPosts = (limit = 10) => {
  return api.get('/recommendations/hot', { params: { limit } })
}

// 最新内容
export const getLatestPosts = (limit = 10) => {
  return api.get('/recommendations/latest', { params: { limit } })
}

