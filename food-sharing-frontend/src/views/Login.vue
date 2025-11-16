<template>
  <el-card style="max-width:420px;margin:40px auto;">
    <h3>登录</h3>
    <el-form label-width="64px" @keyup.enter.native="onSubmit">
      <el-form-item label="账号">
        <el-input v-model="form.username" placeholder="用户名或邮箱" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">登录</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { loginApi } from '@/api/auth'
import { getUserInfo } from '@/api/users'
const form = reactive({ username: '', password: '' })
const loading = ref(false)
const router = useRouter()

async function onSubmit() {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const { data } = await loginApi({ usernameOrEmail: form.username, password: form.password })
    if (data?.token) {
      localStorage.setItem('token', data.token)
      // 获取用户信息并保存
      try {
        const userResponse = await getUserInfo()
        localStorage.setItem('userInfo', JSON.stringify(userResponse.data))
      } catch (error) {
        console.error('获取用户信息失败:', error)
      }
      router.push('/')
    }
  } finally {
    loading.value = false
  }
}
</script>
