import api from './request.js'

// 点赞相关
export const toggleLike = (postId) => {
  return api.post(`/likes/posts/${postId}`)
}

export const getLikeStatus = (postId) => {
  return api.get(`/likes/posts/${postId}`)
}

// 评论相关
export const createComment = (postId, data) => {
  return api.post(`/comments/posts/${postId}`, data)
}

export const getComments = (postId, params) => {
  return api.get(`/comments/posts/${postId}`, { params })
}

export const deleteComment = (commentId) => {
  return api.delete(`/comments/${commentId}`)
}

// 收藏相关
export const toggleFavorite = (postId) => {
  return api.post(`/favorites/posts/${postId}`)
}

export const getFavoriteStatus = (postId) => {
  return api.get(`/favorites/posts/${postId}`)
}

export const getMyFavorites = (params) => {
  return api.get('/favorites/my', { params })
}

// 关注相关
export const toggleFollow = (userId) => {
  return api.post(`/follows/users/${userId}`)
}

export const getFollowStatus = (userId) => {
  return api.get(`/follows/users/${userId}`)
}

export const getFollowers = (params) => {
  return api.get('/follows/followers', { params })
}

export const getFollowing = (params) => {
  return api.get('/follows/following', { params })
}
