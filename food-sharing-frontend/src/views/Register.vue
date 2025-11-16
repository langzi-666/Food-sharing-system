<template>
  <el-card style="max-width:420px;margin:40px auto;">
    <h3>注册</h3>
    <el-form label-width="64px" @keyup.enter.native="onSubmit">
      <el-form-item label="账号">
        <el-input v-model="form.username" placeholder="用户名" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" placeholder="邮箱" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" placeholder="密码" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit">注册</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerApi } from '@/api/auth'
const form = reactive({ username: '', email: '', password: '' })
const loading = ref(false)
const router = useRouter()

async function onSubmit() {
  if (!form.username || !form.email || !form.password) return
  loading.value = true
  try {
    await registerApi(form)
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>
