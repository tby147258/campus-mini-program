<template>
  <div class="login-container">
    <div class="login-card">
      <h2 class="login-title">校园综合管理平台</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%" @click="openCaptcha">
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-link type="primary" @click="$router.push('/register')">注册账号</el-link>
          <el-link type="primary" style="margin-left: 16px" @click="$router.push('/forgot-password')">忘记密码</el-link>
        </el-form-item>
      </el-form>
    </div>

    <!-- 滑块验证弹窗 -->
    <CaptchaSlider
      v-if="showCaptcha"
      :visible="showCaptcha"
      @close="showCaptcha = false"
      @success="onCaptchaSuccess"
    />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { authApi, captchaApi } from '@/api/index'
import CaptchaSlider from '@/components/CaptchaSlider.vue'

const formRef = ref(null)
const loading = ref(false)
const showCaptcha = ref(false)

const form = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

// 打开滑块验证弹窗
function openCaptcha() {
  if (loading.value) return
  formRef.value.validate((valid) => {
    if (!valid) return
    showCaptcha.value = true
  })
}

// 滑块验证成功回调 — 直接接收 passToken 并调用登录接口
function onCaptchaSuccess(passToken) {
  loading.value = true
  showCaptcha.value = false

  authApi.login({
    username: form.account,
    password: form.password,
    passToken
  })
    .then(result => {
      if (!result || !result.token) {
        ElMessage.error('登录失败：未获取到有效凭证')
        return
      }
      const { token, user } = result
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify({
        id: user.id,
        nickname: user.nickname,
        role: user.role
      }))
      ElMessage.success('登录成功')
      window.location.href = '/'
    })
    .catch(err => {
      const msg = err.msg || err.message || '登录失败'
      ElMessage.error(msg)
    })
    .finally(() => {
      loading.value = false
    })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0,0,0,0.15);
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
  font-size: 24px;
}
</style>