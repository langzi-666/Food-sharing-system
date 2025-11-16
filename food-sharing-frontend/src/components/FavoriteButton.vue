<template>
  <div class="favorite-button">
    <el-button
      :type="favorited ? 'warning' : 'default'"
      :icon="favorited ? StarFilled : Star"
      @click="handleFavorite"
      :loading="loading"
      size="small"
    >
      {{ favoriteCount }}
    </el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Star, StarFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { toggleFavorite, getFavoriteStatus } from '@/api/social.js'

const props = defineProps({
  postId: {
    type: Number,
    required: true
  }
})

const favorited = ref(false)
const favoriteCount = ref(0)
const loading = ref(false)

const loadFavoriteStatus = async () => {
  try {
    const response = await getFavoriteStatus(props.postId)
    // axios拦截器已经返回了response.data，所以直接使用response
    favorited.value = response?.favorited || false
    favoriteCount.value = response?.favoriteCount || 0
  } catch (error) {
    console.error('获取收藏状态失败:', error)
  }
}

const handleFavorite = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const response = await toggleFavorite(props.postId)
    // axios拦截器已经返回了response.data，所以直接使用response
    favorited.value = response?.favorited || false
    favoriteCount.value = response?.favoriteCount || 0
    
    ElMessage.success(favorited.value ? '收藏成功' : '取消收藏')
  } catch (error) {
    console.error('收藏操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadFavoriteStatus()
})
</script>

<style scoped>
.favorite-button {
  display: inline-block;
}

.favorite-button .el-button {
  border-radius: 20px;
}
</style>
