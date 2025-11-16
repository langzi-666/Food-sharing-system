<template>
  <div class="system-logs">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统日志</span>
          <div class="filters">
            <el-select v-model="filterModule" placeholder="模块" clearable style="width: 120px" @change="loadLogs">
              <el-option label="用户" value="USER" />
              <el-option label="内容" value="POST" />
              <el-option label="商家" value="MERCHANT" />
              <el-option label="系统" value="SYSTEM" />
            </el-select>
            <el-select v-model="filterLevel" placeholder="级别" clearable style="width: 120px; margin-left: 10px" @change="loadLogs">
              <el-option label="信息" value="INFO" />
              <el-option label="警告" value="WARN" />
              <el-option label="错误" value="ERROR" />
            </el-select>
          </div>
        </div>
      </template>

      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="level" label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="getLevelType(row.level)">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="action" label="操作" width="150" />
        <el-table-column prop="message" label="消息" show-overflow-tooltip />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="createdAt" label="时间" width="180" />
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getLogs } from '@/api/admin'
import { ElMessage } from 'element-plus'

const logs = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(50)
const total = ref(0)
const filterModule = ref('')
const filterLevel = ref('')

const getLevelType = (level) => {
  const types = {
    INFO: '',
    WARN: 'warning',
    ERROR: 'danger'
  }
  return types[level] || ''
}

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value - 1,
      size: pageSize.value
    }
    if (filterModule.value) {
      params.module = filterModule.value
    }
    if (filterLevel.value) {
      params.level = filterLevel.value
    }
    const response = await getLogs(params)
    logs.value = response.data.items
    total.value = response.data.totalElements
  } catch (error) {
    console.error('加载日志失败:', error)
    ElMessage.error('加载日志失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.system-logs {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  display: flex;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>

