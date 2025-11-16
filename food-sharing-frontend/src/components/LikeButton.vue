<template>
  <div class="like-button">
    <el-button
      :type="liked ? 'danger' : 'default'"
      :icon="liked ? HeartFilled : Heart"
      @click="handleLike"
      :loading="loading"
      size="small"
    >
      {{ likeCount }}
    </el-button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import Heart from '@/components/icons/Heart.vue'
import HeartFilled from '@/components/icons/HeartFilled.vue'
import { ElMessage } from 'element-plus'
import { toggleLike, getLikeStatus } from '@/api/social.js'

const props = defineProps({
  postId: {
    type: Number,
    required: true
  }
})

const liked = ref(false)
const likeCount = ref(0)
const loading = ref(false)

const loadLikeStatus = async () => {
  try {
    const response = await getLikeStatus(props.postId)
    // axios拦截器已经返回了response.data，所以直接使用response
    liked.value = response?.liked || false
    likeCount.value = response?.likeCount || 0
  } catch (error) {
    console.error('获取点赞状态失败:', error)
  }
}

const handleLike = async () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    return
  }

  loading.value = true
  try {
    const response = await toggleLike(props.postId)
    // axios拦截器已经返回了response.data，所以直接使用response
    liked.value = response?.liked || false
    likeCount.value = response?.likeCount || 0
    
    ElMessage.success(liked.value ? '点赞成功' : '取消点赞')
  } catch (error) {
    console.error('点赞操作失败:', error)
    ElMessage.error('操作失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLikeStatus()
})
</script>

<style scoped>
.like-button {
  display: inline-block;
}

.like-button .el-button {
  border-radius: 20px;
}
</style>
