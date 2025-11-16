<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.users?.total || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <div class="stat-detail">
            <span>启用: {{ statistics.users?.enabled || 0 }}</span>
            <span>禁用: {{ statistics.users?.disabled || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.posts?.total || 0 }}</div>
            <div class="stat-label">总内容数</div>
          </div>
          <div class="stat-detail">
            <span>待审核: {{ statistics.posts?.pending || 0 }}</span>
            <span>已通过: {{ statistics.posts?.approved || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.interactions?.likes || 0 }}</div>
            <div class="stat-label">总点赞数</div>
          </div>
          <div class="stat-detail">
            <span>评论: {{ statistics.interactions?.comments || 0 }}</span>
            <span>收藏: {{ statistics.interactions?.favorites || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.merchants || 0 }}</div>
            <div class="stat-label">商家数量</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="chart-card" style="margin-top: 20px;">
      <template #header>
        <span>数据概览</span>
      </template>
      <div class="chart-placeholder">
        <p>数据统计图表（可集成 ECharts）</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStatistics } from '@/api/admin'
import { ElMessage } from 'element-plus'

const statistics = ref({
  users: {},
  posts: {},
  interactions: {},
  merchants: 0
})

const loadStatistics = async () => {
  try {
    const response = await getStatistics()
    statistics.value = response.data
  } catch (error) {
    console.error('加载统计数据失败:', error)
    ElMessage.error('加载统计数据失败')
  }
}

onMounted(() => {
  loadStatistics()
})
</script>

<style scoped>
.dashboard {
  padding: 0;
}

.stat-card {
  text-align: center;
}

.stat-content {
  padding: 20px 0;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-detail {
  display: flex;
  justify-content: space-around;
  padding-top: 10px;
  border-top: 1px solid #f0f0f0;
  font-size: 12px;
  color: #606266;
}

.chart-card {
  min-height: 400px;
}

.chart-placeholder {
  text-align: center;
  padding: 100px 0;
  color: #909399;
}
</style>

