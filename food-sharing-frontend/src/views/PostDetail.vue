<template>
  <div class="post-detail">
    <div v-loading="loading" class="detail-container">
      <div v-if="!post && !loading" class="not-found">
        <el-result
          icon="warning"
          title="内容不存在"
          sub-title="您访问的内容可能已被删除或不存在"
        >
          <template #extra>
            <el-button type="primary" @click="goHome">返回首页</el-button>
          </template>
        </el-result>
      </div>
      
      <div v-else-if="post" class="post-content">
        <!-- 标题和元信息 -->
        <div class="post-header">
          <h1 class="post-title">{{ post.title }}</h1>
          
          <div class="post-meta">
            <div class="author-info">
              <el-avatar :size="40" :src="post.author?.avatarUrl">
                {{ post.author?.username?.[0] }}
              </el-avatar>
              <div class="author-details">
                <div class="author-name">{{ post.author?.username }}</div>
                <div class="post-time">{{ formatTime(post.createdAt) }}</div>
              </div>
            </div>
            
            <div class="post-stats">
              <span class="view-count">{{ post.viewCount }} 浏览</span>
            </div>
          </div>
        </div>
        
        <!-- 分类和标签 -->
        <div class="post-tags-section" v-if="post.category || (post.tags && post.tags.length > 0)">
          <el-tag v-if="post.category" type="primary" size="large">
            {{ post.category.name }}
          </el-tag>
          
          <el-tag
            v-for="tag in post.tags"
            :key="tag.id"
            type="info"
            size="large"
            style="margin-left: 8px"
          >
            {{ tag.name }}
          </el-tag>
        </div>
        
        <!-- 图片展示 -->
        <div class="post-image" v-if="post.imageUrl">
          <el-image
            :src="post.imageUrl"
            :alt="post.title"
            fit="cover"
            :preview-src-list="[post.imageUrl]"
            preview-teleported
          />
        </div>
        
        <!-- 视频展示 -->
        <div class="post-video" v-if="post.videoUrl">
          <video :src="post.videoUrl" controls width="100%">
            您的浏览器不支持视频播放
          </video>
        </div>
        
        <!-- 内容正文 -->
        <div class="post-body">
          <div class="content-text">{{ post.content }}</div>
        </div>
        
        <!-- 位置信息 -->
        <div class="post-location" v-if="post.address">
          <div class="location-header">
            <el-icon><Location /></el-icon>
            <span>位置信息</span>
          </div>
          <div class="location-address">{{ post.address }}</div>
          <div class="location-coords" v-if="post.lat && post.lng">
            经纬度: {{ post.lat }}, {{ post.lng }}
          </div>
        </div>
        
        <!-- 社交互动按钮 -->
        <div class="post-interactions">
          <div class="interaction-buttons">
            <LikeButton :post-id="post.id" />
            <FavoriteButton :post-id="post.id" />
            <div class="comment-count">
              <el-button size="small" :icon="ChatDotRound">
                {{ post.commentCount || 0 }}
              </el-button>
            </div>
          </div>
        </div>
        
        <!-- 评论区 -->
        <CommentSection :post-id="post.id" />
        
        <!-- 操作按钮 -->
        <div class="post-actions">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="goHome">首页</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Location, ChatDotRound } from '@element-plus/icons-vue'
import { getPostDetail } from '@/api/posts.js'
import LikeButton from '@/components/LikeButton.vue'
import FavoriteButton from '@/components/FavoriteButton.vue'
import CommentSection from '@/components/CommentSection.vue'

const route = useRoute()
const router = useRouter()

const post = ref(null)
const loading = ref(false)

const loadPostDetail = async () => {
  const postId = route.params.id
  if (!postId) {
    ElMessage.error('无效的内容ID')
    return
  }
  
  loading.value = true
  try {
    const response = await getPostDetail(postId)
    post.value = response.data
  } catch (error) {
    console.error('加载内容详情失败:', error)
    if (error.response?.status === 404) {
      ElMessage.error('内容不存在')
    } else {
      ElMessage.error('加载失败')
    }
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const goBack = () => {
  router.go(-1)
}

const goHome = () => {
  router.push('/')
}

onMounted(() => {
  loadPostDetail()
})
</script>

<style scoped>
.post-detail {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

.detail-container {
  min-height: 400px;
}

.post-content {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.post-header {
  padding: 30px 30px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.post-title {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 20px 0;
  line-height: 1.4;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-details {
  display: flex;
  flex-direction: column;
}

.author-name {
  font-weight: bold;
  color: #333;
  font-size: 16px;
}

.post-time {
  color: #666;
  font-size: 14px;
  margin-top: 2px;
}

.post-stats {
  color: #666;
  font-size: 14px;
}

.post-tags-section {
  padding: 20px 30px;
  border-bottom: 1px solid #f0f0f0;
}

.post-image {
  padding: 20px 30px;
  text-align: center;
}

.post-image :deep(.el-image) {
  max-width: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-video {
  padding: 20px 30px;
  text-align: center;
}

.post-video video {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-body {
  padding: 30px;
}

.content-text {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
  word-break: break-word;
}

.post-location {
  padding: 20px 30px;
  background: #f8f9fa;
  border-top: 1px solid #f0f0f0;
}

.location-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
}

.location-address {
  font-size: 16px;
  color: #333;
  margin-bottom: 5px;
}

.location-coords {
  font-size: 14px;
  color: #666;
}

.post-actions {
  padding: 20px 30px;
  text-align: center;
  border-top: 1px solid #f0f0f0;
}

.post-actions .el-button {
  margin: 0 10px;
}

.not-found {
  text-align: center;
  padding: 60px 0;
}

.post-interactions {
  padding: 20px 30px;
  border-top: 1px solid #f0f0f0;
  border-bottom: 1px solid #f0f0f0;
}

.interaction-buttons {
  display: flex;
  gap: 15px;
  align-items: center;
}

.comment-count .el-button {
  border-radius: 20px;
}
</style>
