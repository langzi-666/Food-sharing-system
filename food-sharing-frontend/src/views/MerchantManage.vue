<template>
  <div class="merchant-manage">
    <el-card v-loading="loading" class="manage-card">
      <template #header>
        <div class="card-header">
          <span>商家管理</span>
          <el-tag :type="merchant.status === 1 ? 'success' : merchant.status === 2 ? 'warning' : 'danger'">
            {{ statusText }}
          </el-tag>
        </div>
      </template>
      
      <div v-if="!merchant.id" class="empty-state">
        <el-empty description="您还没有注册商家">
          <el-button type="primary" @click="goToRegister">立即入驻</el-button>
        </el-empty>
      </div>
      
      <div v-else>
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="120px"
          label-position="top"
        >
          <el-form-item label="商家名称" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入商家名称"
              maxlength="100"
            />
          </el-form-item>
          
          <el-form-item label="商家描述">
            <el-input
              v-model="form.description"
              type="textarea"
              :rows="4"
              placeholder="请输入商家描述"
              maxlength="2000"
            />
          </el-form-item>
          
          <el-form-item label="商家Logo">
            <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleLogoChange"
              accept="image/*"
            >
              <el-avatar v-if="form.logo" :src="form.logo" :size="100" />
              <el-button v-else type="primary">上传Logo</el-button>
            </el-upload>
          </el-form-item>
          
          <el-form-item label="封面图片">
            <el-upload
              action="#"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleCoverChange"
              accept="image/*"
            >
              <img v-if="form.coverImage" :src="form.coverImage" class="cover-preview" />
              <el-button v-else type="primary">上传封面</el-button>
            </el-upload>
          </el-form-item>
          
          <el-form-item label="联系电话">
            <el-input
              v-model="form.phone"
              placeholder="请输入联系电话"
              maxlength="20"
            />
          </el-form-item>
          
          <el-form-item label="详细地址">
            <el-input
              v-model="form.address"
              placeholder="请输入详细地址"
              maxlength="200"
            />
          </el-form-item>
          
          <el-form-item label="营业时间">
            <el-input
              v-model="form.businessHours"
              placeholder="例如：周一至周日 10:00-22:00"
              maxlength="200"
            />
          </el-form-item>
          
          <el-form-item label="人均消费">
            <el-input-number
              v-model="form.avgPrice"
              :min="0"
              :precision="2"
              placeholder="人均消费"
            />
          </el-form-item>
          
          <el-form-item label="菜系类型">
            <el-input
              v-model="form.cuisineType"
              placeholder="例如：川菜、粤菜、西餐等"
              maxlength="100"
            />
          </el-form-item>
          
          <el-form-item label="设施服务">
            <el-input
              v-model="form.facilities"
              type="textarea"
              :rows="2"
              placeholder="例如：WiFi、停车位、包间等"
              maxlength="500"
            />
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="saveForm" :loading="saving">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
        
        <el-divider />
        
        <div class="merchant-stats">
          <h3>店铺数据</h3>
          <el-row :gutter="20">
            <el-col :span="6">
              <el-statistic title="评分" :value="merchant.rating" :precision="2" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="评价数" :value="merchant.reviewCount" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="认证状态" :value="merchant.isVerified ? '已认证' : '未认证'" />
            </el-col>
            <el-col :span="6">
              <el-statistic title="状态" :value="statusText" />
            </el-col>
          </el-row>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getMyMerchant, updateMerchant } from '@/api/merchants.js'
import { uploadImage } from '@/api/upload.js'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const saving = ref(false)
const merchant = reactive({})

const form = reactive({
  name: '',
  description: '',
  logo: '',
  coverImage: '',
  phone: '',
  address: '',
  businessHours: '',
  avgPrice: null,
  cuisineType: '',
  facilities: ''
})

const rules = {
  name: [
    { required: true, message: '请输入商家名称', trigger: 'blur' }
  ]
}

const statusText = computed(() => {
  const statusMap = {
    0: '停业',
    1: '营业',
    2: '审核中'
  }
  return statusMap[merchant.status] || '未知'
})

const loadMerchant = async () => {
  loading.value = true
  try {
    const response = await getMyMerchant()
    Object.assign(merchant, response.data)
    Object.assign(form, {
      name: merchant.name || '',
      description: merchant.description || '',
      logo: merchant.logo || '',
      coverImage: merchant.coverImage || '',
      phone: merchant.phone || '',
      address: merchant.address || '',
      businessHours: merchant.businessHours || '',
      avgPrice: merchant.avgPrice || null,
      cuisineType: merchant.cuisineType || '',
      facilities: merchant.facilities || ''
    })
  } catch (error) {
    if (error.response?.status === 404) {
      // 未注册商家
    } else {
      ElMessage.error('加载商家信息失败')
    }
  } finally {
    loading.value = false
  }
}

const handleLogoChange = async (file) => {
  if (!file.raw) return
  try {
    const response = await uploadImage(file.raw)
    form.logo = response.data.url
    ElMessage.success('Logo上传成功')
  } catch (error) {
    ElMessage.error('Logo上传失败')
  }
}

const handleCoverChange = async (file) => {
  if (!file.raw) return
  try {
    const response = await uploadImage(file.raw)
    form.coverImage = response.data.url
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('封面上传失败')
  }
}

const saveForm = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate()
  if (!valid) return
  
  saving.value = true
  try {
    await updateMerchant(form)
    ElMessage.success('保存成功')
    loadMerchant()
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const goToRegister = () => {
  router.push('/merchant/register')
}

onMounted(() => {
  loadMerchant()
})
</script>

<style scoped>
.merchant-manage {
  max-width: 900px;
  margin: 20px auto;
  padding: 0 20px;
}

.manage-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.cover-preview {
  max-width: 300px;
  max-height: 200px;
  border-radius: 4px;
}

.merchant-stats {
  margin-top: 20px;
}

.merchant-stats h3 {
  margin-bottom: 20px;
}
</style>

