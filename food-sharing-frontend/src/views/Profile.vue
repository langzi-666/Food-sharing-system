<template>
  <div class="profile">
    <div v-loading="loading" class="profile-container">
      <div v-if="!loading && !userInfo.id" class="error-message">
        <el-alert title="无法加载用户信息" type="error" :closable="false" />
      </div>
      
      <el-card v-else class="profile-card">
        <template #header>
          <div class="card-header">
            <span>个人中心</span>
          </div>
        </template>
        
        <div class="profile-content">
          <!-- 头像部分 -->
          <div class="avatar-section">
            <el-avatar :size="100" :src="userInfo.avatarUrl">
              {{ userInfo.username ? userInfo.username[0].toUpperCase() : 'U' }}
            </el-avatar>
            
            <div class="avatar-upload">
              <el-upload
                action="#"
                :auto-upload="false"
                :show-file-list="false"
                :on-change="handleAvatarChange"
                accept="image/*"
              >
                <el-button size="small" type="primary">更换头像</el-button>
              </el-upload>
            </div>
          </div>
          
          <!-- 用户信息 -->
          <div class="user-info-section">
            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">{{ userInfo.points || 0 }}</div>
                <div class="stat-label">积分</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">Lv.{{ userInfo.level || 1 }}</div>
                <div class="stat-label">等级</div>
              </div>
            </div>
            
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-width="80px"
              class="profile-form"
            >
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="form.username"
                  placeholder="请输入用户名"
                  :disabled="!editing"
                />
              </el-form-item>
              
              <el-form-item label="邮箱" prop="email">
                <el-input
                  v-model="form.email"
                  placeholder="请输入邮箱"
                  :disabled="!editing"
                />
              </el-form-item>
              
              <el-form-item label="角色">
                <el-input :value="userInfo.role" disabled />
              </el-form-item>
              
              <el-form-item>
                <el-button
                  v-if="!editing"
                  type="primary"
                  @click="startEdit"
                >
                  编辑资料
                </el-button>
                
                <template v-else>
                  <el-button
                    type="primary"
                    @click="saveProfile"
                    :loading="saving"
                  >
                    保存
                  </el-button>
                  <el-button @click="cancelEdit">取消</el-button>
                </template>
              </el-form-item>
            </el-form>
          </div>
        </div>
      </el-card>
      
      <!-- 修改密码 -->
      <el-card class="password-card">
        <template #header>
          <div class="card-header">
            <span>修改密码</span>
          </div>
        </template>
        
        <el-form
          ref="passwordFormRef"
          :model="passwordForm"
          :rules="passwordRules"
          label-width="100px"
          class="password-form"
        >
          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          
          <el-form-item>
            <el-button
              type="primary"
              @click="changePassword"
              :loading="changingPassword"
            >
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo, uploadAvatar, changePassword as changePasswordApi } from '@/api/users.js'

const loading = ref(false)
const editing = ref(false)
const saving = ref(false)
const changingPassword = ref(false)

const userInfo = ref({})
const form = reactive({
  username: '',
  email: '',
  avatarUrl: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const formRef = ref()
const passwordFormRef = ref()

const loadUserInfo = async () => {
  loading.value = true
  console.log('开始加载用户信息...')
  try {
    const response = await getUserInfo()
    // axios拦截器已经返回了response.data，所以直接使用response
    console.log('用户信息加载成功:', response)
    userInfo.value = response
    
    // 填充表单
    Object.assign(form, {
      username: response?.username || '',
      email: response?.email || '',
      avatarUrl: response?.avatarUrl || ''
    })
  } catch (error) {
    console.error('加载用户信息失败:', error)
    ElMessage.error('加载用户信息失败: ' + (error.response?.data?.message || error.message))
  } finally {
    loading.value = false
    console.log('用户信息加载完成, loading:', loading.value)
  }
}

const startEdit = () => {
  editing.value = true
}

const cancelEdit = () => {
  editing.value = false
  // 重置表单
  Object.assign(form, {
    username: userInfo.value.username,
    email: userInfo.value.email,
    avatarUrl: userInfo.value.avatarUrl
  })
}

const saveProfile = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate()
  if (!valid) return
  
  saving.value = true
  try {
    await updateUserInfo(form)
    
    // 更新本地用户信息
    Object.assign(userInfo.value, form)
    
    editing.value = false
    ElMessage.success('保存成功')
  } catch (error) {
    console.error('保存失败:', error)
    ElMessage.error(error.response?.data?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleAvatarChange = async (file) => {
  if (!file.raw) return
  
  // 检查文件大小
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('头像文件大小不能超过 5MB')
    return
  }
  
  try {
    const response = await uploadAvatar(file.raw)
    // axios拦截器已经返回了response.data，所以直接使用response
    const avatarUrl = response?.url
    
    // 更新头像
    userInfo.value.avatarUrl = avatarUrl
    form.avatarUrl = avatarUrl
    
    ElMessage.success('头像上传成功')
  } catch (error) {
    console.error('头像上传失败:', error)
    ElMessage.error('头像上传失败')
  }
}

const changePassword = async () => {
  if (!passwordFormRef.value) return
  
  const valid = await passwordFormRef.value.validate()
  if (!valid) return
  
  changingPassword.value = true
  try {
    await changePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    
    // 重置表单
    Object.assign(passwordForm, {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    })
    
    ElMessage.success('密码修改成功')
  } catch (error) {
    console.error('密码修改失败:', error)
    ElMessage.error(error.response?.data?.message || '密码修改失败')
  } finally {
    changingPassword.value = false
  }
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.profile-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.profile-card,
.password-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.profile-content {
  display: flex;
  gap: 40px;
  align-items: flex-start;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.user-info-section {
  flex: 1;
}

.user-stats {
  display: flex;
  gap: 30px;
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.profile-form,
.password-form {
  max-width: 400px;
}

@media (max-width: 768px) {
  .profile-content {
    flex-direction: column;
    align-items: center;
    gap: 20px;
  }
  
  .user-stats {
    justify-content: center;
  }
}
</style>
