<template>
  <div class="comment-section">
    <div class="comment-header">
      <h3>评论 ({{ totalComments }})</h3>
    </div>
    
    <!-- 发表评论 -->
    <div class="comment-form" v-if="isLoggedIn">
      <el-input
        v-model="newComment"
        type="textarea"
        :rows="3"
        placeholder="写下你的评论..."
        maxlength="1000"
        show-word-limit
      />
      <div class="comment-actions">
        <el-button
          type="primary"
          @click="submitComment"
          :loading="submitting"
          :disabled="!newComment.trim()"
        >
          发表评论
        </el-button>
      </div>
    </div>
    
    <div v-else class="login-tip">
      <el-alert
        title="请登录后发表评论"
        type="info"
        :closable="false"
        show-icon
      />
    </div>
    
    <!-- 评论列表 -->
    <div v-loading="loading" class="comment-list">
      <div v-if="comments.length === 0 && !loading" class="empty-comments">
        <el-empty description="暂无评论" />
      </div>
      
      <div v-else>
        <div
          v-for="comment in comments"
          :key="comment.id"
          class="comment-item"
        >
          <div class="comment-avatar">
            <el-avatar :size="40" :src="comment.user.avatarUrl">
              {{ comment.user.username[0] }}
            </el-avatar>
          </div>
          
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-author">{{ comment.user.username }}</span>
              <span class="comment-time">{{ formatTime(comment.createdAt) }}</span>
            </div>
            
            <div class="comment-text">{{ comment.content }}</div>
            
            <div class="comment-actions">
              <el-button
                text
                size="small"
                @click="replyToComment(comment)"
              >
                回复
              </el-button>
              
              <el-button
                v-if="canDeleteComment(comment)"
                text
                type="danger"
                size="small"
                @click="deleteComment(comment.id)"
              >
                删除
              </el-button>
            </div>
            
            <!-- 回复列表 -->
            <div v-if="comment.replies && comment.replies.length > 0" class="replies">
              <div
                v-for="reply in comment.replies"
                :key="reply.id"
                class="reply-item"
              >
                <div class="reply-avatar">
                  <el-avatar :size="32" :src="reply.user.avatarUrl">
                    {{ reply.user.username[0] }}
                  </el-avatar>
                </div>
                
                <div class="reply-content">
                  <div class="reply-header">
                    <span class="reply-author">{{ reply.user.username }}</span>
                    <span class="reply-time">{{ formatTime(reply.createdAt) }}</span>
                  </div>
                  <div class="reply-text">{{ reply.content }}</div>
                </div>
              </div>
            </div>
            
            <!-- 回复表单 -->
            <div v-if="replyingTo === comment.id" class="reply-form">
              <el-input
                v-model="replyContent"
                type="textarea"
                :rows="2"
                :placeholder="`回复 @${comment.user.username}`"
                maxlength="1000"
              />
              <div class="reply-actions">
                <el-button size="small" @click="cancelReply">取消</el-button>
                <el-button
                  type="primary"
                  size="small"
                  @click="submitReply"
                  :loading="replySubmitting"
                  :disabled="!replyContent.trim()"
                >
                  回复
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 分页 -->
      <Pagination
        v-if="totalComments > 0"
        :total="totalComments"
        :page="currentPage"
        :limit="pageSize"
        @pagination="handlePagination"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import Pagination from './Pagination.vue'
import { getComments, createComment, deleteComment as deleteCommentApi } from '@/api/social.js'
import { getUserInfo } from '@/api/users.js'

const props = defineProps({
  postId: {
    type: Number,
    required: true
  }
})

const comments = ref([])
const loading = ref(false)
const totalComments = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

const newComment = ref('')
const submitting = ref(false)

const replyingTo = ref(null)
const replyContent = ref('')
const replySubmitting = ref(false)

const currentUser = ref({})
const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const loadComments = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    
    const response = await getComments(props.postId, params)
    // axios拦截器已经返回了response.data，所以直接使用response
    comments.value = response?.items || []
    totalComments.value = response?.totalElements || 0
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败')
  } finally {
    loading.value = false
  }
}

const loadCurrentUser = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const response = await getUserInfo()
    currentUser.value = response.data
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  
  submitting.value = true
  try {
    await createComment(props.postId, {
      content: newComment.value.trim()
    })
    
    newComment.value = ''
    ElMessage.success('评论发表成功')
    
    // 重新加载评论
    currentPage.value = 1
    await loadComments()
  } catch (error) {
    console.error('发表评论失败:', error)
    ElMessage.error('发表评论失败')
  } finally {
    submitting.value = false
  }
}

const replyToComment = (comment) => {
  replyingTo.value = comment.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const submitReply = async () => {
  if (!replyContent.value.trim()) return
  
  replySubmitting.value = true
  try {
    await createComment(props.postId, {
      content: replyContent.value.trim(),
      parentId: replyingTo.value
    })
    
    cancelReply()
    ElMessage.success('回复成功')
    
    // 重新加载评论
    await loadComments()
  } catch (error) {
    console.error('回复失败:', error)
    ElMessage.error('回复失败')
  } finally {
    replySubmitting.value = false
  }
}

const canDeleteComment = (comment) => {
  return currentUser.value.id === comment.user.id
}

const deleteComment = async (commentId) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      type: 'warning'
    })
    
    await deleteCommentApi(commentId)
    ElMessage.success('删除成功')
    
    // 重新加载评论
    await loadComments()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除评论失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

const handlePagination = ({ page, limit }) => {
  currentPage.value = page
  pageSize.value = limit
  loadComments()
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
  loadComments()
  loadCurrentUser()
})
</script>

<style scoped>
.comment-section {
  margin-top: 30px;
}

.comment-header h3 {
  margin: 0 0 20px 0;
  color: #333;
}

.comment-form {
  margin-bottom: 30px;
}

.comment-actions {
  margin-top: 10px;
  text-align: right;
}

.login-tip {
  margin-bottom: 30px;
}

.comment-list {
  min-height: 200px;
}

.empty-comments {
  text-align: center;
  padding: 40px 0;
}

.comment-item {
  display: flex;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.comment-avatar {
  margin-right: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-author {
  font-weight: bold;
  color: #333;
  margin-right: 10px;
}

.comment-time {
  color: #999;
  font-size: 12px;
}

.comment-text {
  color: #333;
  line-height: 1.6;
  margin-bottom: 10px;
  white-space: pre-wrap;
}

.comment-actions {
  display: flex;
  gap: 10px;
}

.replies {
  margin-top: 15px;
  padding-left: 20px;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  display: flex;
  margin-bottom: 15px;
}

.reply-avatar {
  margin-right: 10px;
}

.reply-content {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.reply-author {
  font-weight: bold;
  color: #333;
  margin-right: 8px;
  font-size: 14px;
}

.reply-time {
  color: #999;
  font-size: 12px;
}

.reply-text {
  color: #333;
  line-height: 1.5;
  font-size: 14px;
}

.reply-form {
  margin-top: 15px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.reply-actions {
  margin-top: 10px;
  text-align: right;
}
</style>
