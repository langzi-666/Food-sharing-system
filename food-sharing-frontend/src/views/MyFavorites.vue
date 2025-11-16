<template>
  <div class="my-favorites">
    <div class="page-header">
      <h2>我的收藏</h2>
    </div>
    
    <div v-loading="loading" class="favorites-list">
      <div v-if="favorites.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无收藏内容" />
      </div>
      
      <div v-else class="favorites-grid">
        <el-card
          v-for="favorite in favorites"
          :key="favorite.id"
          class="favorite-card"
          shadow="hover"
          @click="goToDetail(favorite.post.id)"
        >
          <div class="favorite-image" v-if="favorite.post.imageUrl">
            <img :src="favorite.post.imageUrl" :alt="favorite.post.title" />
          </div>
          
          <div class="favorite-content">
            <h3 class="favorite-title">{{ favorite.post.title }}</h3>
            
            <div class="favorite-meta">
              <span class="author">{{ favorite.post.author.username }}</span>
              <span class="view-count">{{ favorite.post.viewCount }} 浏览</span>
              <span class="favorite-time">{{ formatTime(favorite.createdAt) }}</span>
            </div>
          </div>
        </el-card>
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
import Pagination from '@/components/Pagination.vue'
import { getMyFavorites } from '@/api/social.js'

const router = useRouter()

const favorites = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)

const loadFavorites = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    const response = await getMyFavorites(params)
    favorites.value = response.data.items
    total.value = response.data.totalElements
  } catch (error) {
    console.error('加载收藏失败:', error)
    ElMessage.error('加载收藏失败')
  } finally {
    loading.value = false
  }
}

const handlePagination = ({ page, limit }) => {
  currentPage.value = page
  pageSize.value = limit
  loadFavorites()
}

const goToDetail = (id) => {
  router.push(`/post/${id}`)
}

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  return date.toLocaleDateString()
}

onMounted(() => {
  loadFavorites()
})
</script>

<style scoped>
.my-favorites {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.favorites-list {
  min-height: 400px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
}

.favorite-card {
  cursor: pointer;
  transition: transform 0.2s;
}

.favorite-card:hover {
  transform: translateY(-2px);
}

.favorite-image {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 8px;
  margin-bottom: 12px;
}

.favorite-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.favorite-content {
  padding: 0 4px;
}

.favorite-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.favorite-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.favorite-meta span {
  margin-right: 10px;
}
</style>
