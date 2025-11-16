<template>
  <div class="home">
    <!-- 搜索栏 -->
    <div class="search-section">
      <SearchBar @search="handleSearch" />
    </div>
    
    <!-- 内容列表 -->
    <div class="content-section">
      <div class="section-header">
        <h2>最新美食分享</h2>
        <el-button type="primary" @click="goToCreate">发布内容</el-button>
      </div>
      
      <div v-loading="loading" class="post-list">
        <div v-if="posts.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无内容" />
        </div>
        
        <div v-else class="post-grid">
          <el-card
            v-for="post in posts"
            :key="post.id"
            class="post-card"
            shadow="hover"
            @click="goToDetail(post.id)"
          >
            <div class="post-image" v-if="post.imageUrl">
              <img :src="post.imageUrl" :alt="post.title" />
            </div>
            
            <div class="post-content">
              <h3 class="post-title">{{ post.title }}</h3>
              
              <div class="post-meta">
                <span class="author">{{ post.author?.username }}</span>
                <span class="view-count">{{ post.viewCount }} 浏览</span>
                <span class="created-time">{{ formatTime(post.createdAt) }}</span>
              </div>
              
              <div class="post-tags" v-if="post.tags && post.tags.length > 0">
                <el-tag
                  v-for="tag in post.tags"
                  :key="tag.id"
                  size="small"
                  type="info"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              
              <div class="post-category" v-if="post.category">
                <el-tag type="primary" size="small">{{ post.category.name }}</el-tag>
              </div>
              
              <div class="post-interactions" @click.stop>
                <div class="interaction-stats">
                  <span class="stat-item">
                    <el-icon><Heart /></el-icon>
                    {{ post.likeCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <el-icon><ChatDotRound /></el-icon>
                    {{ post.commentCount || 0 }}
                  </span>
                  <span class="stat-item">
                    <el-icon><Star /></el-icon>
                    {{ post.favoriteCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </el-card>
        </div>
      </div>
      
      <!-- 分页 -->
      <Pagination
        v-if="total > 0"
        :total="total"
        :page="currentPage"
        :limit="pageSize"
        @pagination="handlePagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star } from '@element-plus/icons-vue'
import Heart from '@/components/icons/Heart.vue'
import SearchBar from '@/components/SearchBar.vue'
import Pagination from '@/components/Pagination.vue'
import { getPosts } from '@/api/posts.js'

const router = useRouter()

const posts = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const searchParams = ref({})

const loadPosts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1, // 后端从0开始
      size: pageSize.value,
      ...searchParams.value
    }
    
    const response = await getPosts(params)
    posts.value = response.data.items
    total.value = response.data.totalElements
  } catch (error) {
    console.error('加载内容失败:', error)
    ElMessage.error('加载内容失败')
  } finally {
    loading.value = false
  }
}

const handleSearch = (params) => {
  searchParams.value = params
  currentPage.value = 1
  loadPosts()
}

const handlePagination = ({ page, limit }) => {
  currentPage.value = page
  pageSize.value = limit
  loadPosts()
}

const goToCreate = () => {
  const token = localStorage.getItem('token')
  if (!token) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push('/create')
}

const goToDetail = (id) => {
  router.push(`/post/${id}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 2592000000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString()
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.home {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.search-section {
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  margin: 0;
  color: #333;
}

.post-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.post-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.post-card:hover {
  transform: translateY(-2px);
}

.post-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 12px;
}

.post-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.post-content {
  padding: 0 4px;
}

.post-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.post-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  font-size: 12px;
  color: #666;
}

.post-tags {
  margin-bottom: 8px;
}

.post-tags .el-tag {
  margin-right: 5px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.post-interactions {
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
}

.interaction-stats {
  display: flex;
  gap: 15px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #666;
  font-size: 14px;
}

.stat-item .el-icon {
  font-size: 16px;
}
</style>
