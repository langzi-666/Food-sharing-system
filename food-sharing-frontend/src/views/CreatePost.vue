<template>
  <div class="create-post">
    <el-card class="post-form-card">
      <template #header>
        <div class="card-header">
          <span>发布美食内容</span>
        </div>
      </template>
      
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="80px"
        label-position="top"
      >
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="form.title"
            placeholder="请输入标题"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="6"
            placeholder="分享你的美食体验..."
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item label="分类">
          <el-select
            v-model="form.categoryId"
            placeholder="选择分类"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="category in categories"
              :key="category.id"
              :label="category.name"
              :value="category.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="标签">
          <TagSelector v-model="form.tagIds" />
        </el-form-item>
        
        <el-form-item label="图片">
          <div class="upload-section">
            <el-upload
              v-model:file-list="imageList"
              action="#"
              list-type="picture-card"
              :auto-upload="false"
              :on-change="handleImageChange"
              :on-remove="handleImageRemove"
              accept="image/*"
              multiple
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">支持 jpg、png、gif 格式，最多上传 9 张</div>
          </div>
        </el-form-item>
        
        <el-form-item label="视频">
          <div class="upload-section">
            <el-upload
              v-model:file-list="videoList"
              action="#"
              :auto-upload="false"
              :on-change="handleVideoChange"
              :on-remove="handleVideoRemove"
              accept="video/*"
              :limit="1"
            >
              <el-button type="primary">选择视频</el-button>
            </el-upload>
            <div class="upload-tip">支持 mp4、avi、mov 格式，最大 100MB</div>
          </div>
        </el-form-item>
        
        <el-form-item label="位置">
          <div class="location-section">
            <el-input
              v-model="form.address"
              placeholder="输入地址或选择位置"
              style="width: 300px"
            />
            <el-button @click="selectLocation" style="margin-left: 10px">
              选择位置
            </el-button>
          </div>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            发布
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import TagSelector from '@/components/TagSelector.vue'
import { getCategories } from '@/api/categories.js'
import { createPost } from '@/api/posts.js'
import { uploadImages, uploadVideo } from '@/api/upload.js'

const router = useRouter()
const formRef = ref()

const form = reactive({
  title: '',
  content: '',
  categoryId: null,
  tagIds: [],
  imageUrl: '',
  videoUrl: '',
  lat: null,
  lng: null,
  address: ''
})

const rules = {
  title: [
    { required: true, message: '请输入标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
}

const categories = ref([])
const imageList = ref([])
const videoList = ref([])
const submitting = ref(false)

const loadCategories = async () => {
  try {
    const response = await getCategories()
    categories.value = response.data
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleImageChange = (file, fileList) => {
  if (fileList.length > 9) {
    ElMessage.warning('最多只能上传9张图片')
    imageList.value = fileList.slice(0, 9)
  }
}

const handleImageRemove = (file, fileList) => {
  // 图片移除处理
}

const handleVideoChange = (file, fileList) => {
  if (file.size > 100 * 1024 * 1024) {
    ElMessage.error('视频文件大小不能超过 100MB')
    videoList.value = []
    return
  }
}

const handleVideoRemove = (file, fileList) => {
  // 视频移除处理
}

const selectLocation = () => {
  // 这里可以集成地图选择功能
  ElMessage.info('地图选择功能待开发')
}

const uploadFiles = async () => {
  let imageUrl = ''
  let videoUrl = ''
  
  // 上传图片
  if (imageList.value.length > 0) {
    try {
      const imageFiles = imageList.value.map(item => item.raw).filter(Boolean)
      if (imageFiles.length > 0) {
        const response = await uploadImages(imageFiles)
        // axios拦截器已经返回了response.data，所以直接使用response
        imageUrl = response?.urls?.[0] || '' // 取第一张作为主图
      }
    } catch (error) {
      throw new Error('图片上传失败: ' + error.message)
    }
  }
  
  // 上传视频
  if (videoList.value.length > 0 && videoList.value[0].raw) {
    try {
      const response = await uploadVideo(videoList.value[0].raw)
      // axios拦截器已经返回了response.data，所以直接使用response
      videoUrl = response?.url || ''
    } catch (error) {
      throw new Error('视频上传失败: ' + error.message)
    }
  }
  
  return { imageUrl, videoUrl }
}

const submitForm = async () => {
  if (!formRef.value) return
  
  const valid = await formRef.value.validate()
  if (!valid) return
  
  submitting.value = true
  
  try {
    // 先上传文件
    const { imageUrl, videoUrl } = await uploadFiles()
    
    // 创建内容
    const postData = {
      ...form,
      imageUrl,
      videoUrl
    }
    
    await createPost(postData)
    
    ElMessage.success('发布成功!')
    router.push('/')
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error(error.message || '发布失败')
  } finally {
    submitting.value = false
  }
}

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
  imageList.value = []
  videoList.value = []
  Object.assign(form, {
    title: '',
    content: '',
    categoryId: null,
    tagIds: [],
    imageUrl: '',
    videoUrl: '',
    lat: null,
    lng: null,
    address: ''
  })
}

onMounted(() => {
  loadCategories()
})
</script>

<style scoped>
.create-post {
  max-width: 800px;
  margin: 20px auto;
  padding: 0 20px;
}

.post-form-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.upload-section {
  width: 100%;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  margin-top: 5px;
}

.location-section {
  display: flex;
  align-items: center;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 100px;
  height: 100px;
}
</style>
