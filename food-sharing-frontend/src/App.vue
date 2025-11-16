<template>
  <el-container style="min-height:100vh">
    <el-header height="60px" class="header">
      <div class="header-content">
        <div class="logo" @click="goHome">
          <h2 style="margin:0; cursor: pointer">美食分享系统</h2>
        </div>
        
        <div class="nav-menu">
          <el-menu
            :default-active="activeIndex"
            mode="horizontal"
            @select="handleSelect"
            background-color="transparent"
            text-color="#333"
            active-text-color="#409eff"
          >
            <el-menu-item index="/">首页</el-menu-item>
            <el-menu-item index="/create" v-if="isLoggedIn">发布</el-menu-item>
            <el-menu-item index="/favorites" v-if="isLoggedIn">收藏</el-menu-item>
          </el-menu>
        </div>
        
        <div class="user-menu">
          <div v-if="isLoggedIn" class="user-info">
            <el-dropdown @command="handleCommand">
              <span class="user-name">
                {{ userInfo.username }}
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
          
          <div v-else class="auth-buttons">
            <el-button @click="goLogin">登录</el-button>
            <el-button type="primary" @click="goRegister">注册</el-button>
          </div>
        </div>
      </div>
    </el-header>
    
    <el-main>
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/api/users.js'

const router = useRouter()
const route = useRoute()

const userInfo = ref({})
const isLoggedIn = computed(() => !!localStorage.getItem('token'))
const activeIndex = computed(() => route.path)

const loadUserInfo = async () => {
  if (!isLoggedIn.value) return
  
  try {
    const response = await getUserInfo()
    // 适配新的响应格式（可能是data字段，也可能是直接返回对象）
    userInfo.value = response.data || response
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // 如果token无效，清除本地存储
    if (error.response?.status === 401) {
      localStorage.removeItem('token')
      userInfo.value = {}
    }
  }
}

const handleSelect = (key) => {
  router.push(key)
}

const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'logout':
      logout()
      break
  }
}

const logout = () => {
  localStorage.removeItem('token')
  userInfo.value = {}
  ElMessage.success('已退出登录')
  router.push('/')
}

const goHome = () => {
  router.push('/')
}

const goLogin = () => {
  router.push('/login')
}

const goRegister = () => {
  router.push('/register')
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style>
* {
  box-sizing: border-box;
}

html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

#app {
  min-height: 100vh;
  width: 100%;
  margin: 0;
  padding: 0;
}

.header {
  border-bottom: 1px solid #e4e7ed;
  background: white;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.logo {
  color: #409eff;
}

.nav-menu {
  flex: 1;
  display: flex;
  justify-content: center;
}

.nav-menu .el-menu {
  border-bottom: none;
}

.user-menu {
  min-width: 120px;
  display: flex;
  justify-content: flex-end;
}

.user-name {
  cursor: pointer;
  display: flex;
  align-items: center;
  color: #333;
  font-weight: 500;
}

.auth-buttons {
  display: flex;
  gap: 10px;
}

/* 响应式设计优化 */
@media (max-width: 768px) {
  .header-content {
    padding: 0 10px;
    flex-wrap: wrap;
  }
  
  .logo h2 {
    font-size: 18px;
  }
  
  .nav-menu {
    order: 3;
    width: 100%;
    margin-top: 10px;
  }
  
  .nav-menu .el-menu {
    justify-content: center;
  }
  
  .user-menu {
    min-width: auto;
  }
  
  .auth-buttons {
    flex-direction: column;
    gap: 5px;
  }
  
  .auth-buttons .el-button {
    width: 60px;
    padding: 5px 10px;
    font-size: 12px;
  }
}

@media (max-width: 480px) {
  .header {
    height: auto !important;
    min-height: 60px;
  }
  
  .header-content {
    flex-direction: column;
    padding: 10px;
  }
  
  .logo {
    width: 100%;
    text-align: center;
  }
  
  .user-menu {
    width: 100%;
    justify-content: center;
    margin-top: 10px;
  }
}
</style>
