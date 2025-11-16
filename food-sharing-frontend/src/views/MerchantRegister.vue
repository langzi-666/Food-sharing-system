<template>
  <div class="merchant-register">
    <el-card class="register-card">
      <template #header>
        <div class="card-header">
          <span>商家入驻</span>
        </div>
      </template>
      
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
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="商家描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商家描述"
            maxlength="2000"
            show-word-limit
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
        
        <el-form-item label="联系电话" prop="phone">
          <el-input
            v-model="form.phone"
            placeholder="请输入联系电话"
            maxlength="20"
          />
        </el-form-item>
        
        <el-form-item label="详细地址" prop="address">
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
        
        <el-form-item label="营业执照" prop="licenseUrl">
          <el-upload
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleLicenseChange"
            accept="image/*"
          >
            <el-button type="warning">上传营业执照</el-button>
          </el-upload>
          <div v-if="form.licenseUrl" class="license-preview">
            <img :src="form.licenseUrl" alt="营业执照" />
          </div>
          <div class="form-tip">请上传清晰的营业执照照片</div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            提交申请
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { registerMerchant, uploadLicense } from '@/api/merchants.js'
import { uploadImage } from '@/api/upload.js'

const router = useRouter()
const formRef = ref()
const submitting = ref(false)

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
  facilities: '',
  licenseUrl: ''
})

const rules = {
  name: [
    { required: true, message: '请输入商家名称', trigger: 'blur' },
    { min: 2, max: 100, message: '商家名称长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' }
  ],
  address: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ],
  licenseUrl: [
    { required: true, message: '请上传营业执照', trigger: 'change' }
  ]
}

const handleLogoChange = async (file) => {
  if (!file.raw) return
  try {
    const response = await uploadImage(file.raw)
    // axios拦截器已经返回了response.data，所以直接使用response
    form.logo = response?.url || ''
    ElMessage.success('Logo上传成功')
  } catch (error) {
    ElMessage.error('Logo上传失败')
  }
}

const handleCoverChange = async (file) => {
  if (!file.raw) return
  try {
    const response = await uploadImage(file.raw)
    // axios拦截器已经返回了response.data，所以直接使用response
    form.coverImage = response?.url || ''
    ElMessage.success('封面上传成功')
  } catch (error) {
    ElMessage.error('封面上传失败')
  }
}

const handleLicenseChange = async (file) => {
  if (!file.raw) return
  try {
    const response = await uploadLicense(file.raw)
    // axios拦截器已经返回了response.data，所以直接使用response
    form.licenseUrl = response?.url || ''
    ElMessage.success('营业执照上传成功')
  } catch (error) {
    ElMessage.error('营业执照上传失败')
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate()
  if (!valid) return
  
  submitting.value = true
  try {
    await registerMerchant(form)
    ElMessage.success('商家入驻申请已提交，请等待审核')
    router.push('/profile')
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.error(error.response?.data?.message || '提交失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  Object.assign(form, {
    name: '',
    description: '',
    logo: '',
    coverImage: '',
    phone: '',
    address: '',
    businessHours: '',
    avgPrice: null,
    cuisineType: '',
    facilities: '',
    licenseUrl: ''
  })
}
</script>

<style scoped>
.merchant-register {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.register-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.cover-preview {
  max-width: 300px;
  max-height: 200px;
  border-radius: 4px;
}

.license-preview {
  margin-top: 10px;
}

.license-preview img {
  max-width: 400px;
  max-height: 300px;
  border-radius: 4px;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>

