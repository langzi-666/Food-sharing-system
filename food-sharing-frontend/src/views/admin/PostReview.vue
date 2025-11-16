<template>
  <div class="post-review">
    <el-card>
      <template #header>
        <span>内容审核</span>
      </template>

      <el-table :data="posts" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" width="200" show-overflow-tooltip />
        <el-table-column label="封面" width="100">
          <template #default="{ row }">
            <img v-if="row.imageUrl" :src="row.imageUrl" style="width: 60px; height: 60px; object-fit: cover;" />
          </template>
        </el-table-column>
        <el-table-column prop="author.username" label="作者" width="120" />
        <el-table-column prop="viewCount" label="浏览量" width="100" />
        <el-table-column prop="createdAt" label="发布时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="goToDetail(row.id)">
              查看详情
            </el-button>
            <el-button size="small" type="success" @click="handleApprove(row)">
              通过
            </el-button>
            <el-button size="small" type="danger" @click="handleReject(row)">
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadPosts"
          @current-change="loadPosts"
        />
      </div>
    </el-card>

    <!-- 拒绝原因对话框 -->
    <el-dialog v-model="rejectDialogVisible" title="拒绝原因" width="500px">
      <el-input
        v-model="rejectReason"
        type="textarea"
        :rows="4"
        placeholder="请输入拒绝原因（可选）"
      />
      <template #footer>
        <el-button @click="rejectDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmReject">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPendingPosts, approvePost, rejectPost } from '@/api/admin'
import { ElMessage } from 'element-plus'

const router = useRouter()

const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const rejectDialogVisible = ref(false)
const selectedPost = ref(null)
const rejectReason = ref('')

const loadPosts = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    const response = await getPendingPosts(params)
    posts.value = response.data.items
    total.value = response.data.totalElements
  } catch (error) {
    console.error('加载待审核内容失败:', error)
    ElMessage.error('加载待审核内容失败')
  } finally {
    loading.value = false
  }
}

const goToDetail = (id) => {
  window.open(`/post/${id}`, '_blank')
}

const handleApprove = async (post) => {
  try {
    await approvePost(post.id)
    ElMessage.success('审核通过')
    loadPosts()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

const handleReject = (post) => {
  selectedPost.value = post
  rejectReason.value = ''
  rejectDialogVisible.value = true
}

const confirmReject = async () => {
  try {
    await rejectPost(selectedPost.value.id, rejectReason.value)
    ElMessage.success('已拒绝')
    rejectDialogVisible.value = false
    loadPosts()
  } catch (error) {
    console.error('操作失败:', error)
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  loadPosts()
})
</script>

<style scoped>
.post-review {
  padding: 0;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

