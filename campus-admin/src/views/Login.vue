<template>
  <div style="height: 100vh; display: flex; align-items: center; justify-content: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);">
    <el-card style="width: 400px; padding: 20px;">
      <h2 style="text-align: center; margin-bottom: 30px;">校园综合服务平台</h2>
      <h3 style="text-align: center; margin-bottom: 20px; color: #666;">管理员登录</h3>
      <el-form ref="formRef" :model="form" :rules="rules">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="用户名" prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%;" @click="openCaptcha" :loading="loading">登 录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <CaptchaSlider :visible="showCaptcha" @close="showCaptcha=false" @success="onCaptchaSuccess" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '../api'
import { ElMessage } from 'element-plus'
import CaptchaSlider from '../components/CaptchaSlider.vue'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const showCaptcha = ref(false)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const openCaptcha = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  showCaptcha.value = true
}

const onCaptchaSuccess = async (passToken) => {
  showCaptcha.value = false
  loading.value = true
  try {
    const res = await authApi.login({ username: form.username, password: form.password, passToken })
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    ElMessage.success('登录成功')
    router.push('/')
  } catch (err) {
    ElMessage.error(err.response?.data?.msg || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>