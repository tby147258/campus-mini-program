<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="title">校园服务平台 - 注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px" size="large">
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号（选填）" />
        </el-form-item>
        <el-form-item label="学号" prop="studentNo">
          <el-input v-model="form.studentNo" placeholder="请输入学号（选填）" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入密码" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width:100%">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="footer-link">
        已有账号？<router-link to="/login">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { authApi } from '../api/index.js'

export default {
  data() {
    const validatePass = (rule, value, callback) => {
      if (value !== this.form.password) callback(new Error('两次密码输入不一致'))
      else callback()
    }
    return {
      form: { email: '', nickname: '', phone: '', studentNo: '', password: '', confirmPassword: '' },
      loading: false,
      rules: {
        email: [{ required: true, type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }],
        nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
        password: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
        confirmPassword: [{ required: true, validator: validatePass, trigger: 'blur' }]
      }
    }
  },
  methods: {
    async handleRegister() {
      const valid = await this.$refs.formRef.validate().catch(() => false)
      if (!valid) return
      this.loading = true
      try {
        const res = await authApi.register(this.form)
        localStorage.setItem('token', res.data.token)
        localStorage.setItem('user', JSON.stringify(res.data.user))
        this.$message.success('注册成功')
        this.$router.push('/')
      } catch (e) {
        this.$message.error(e.response?.data?.msg || '注册失败')
      }
      this.loading = false
    }
  }
}
</script>

<style scoped>
.register-container { height: 100vh; display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.register-card { background: #fff; padding: 40px; border-radius: 12px; width: 440px; box-shadow: 0 10px 40px rgba(0,0,0,0.15); }
.title { text-align: center; margin-bottom: 30px; color: #333; font-size: 22px; }
.footer-link { text-align: center; margin-top: 16px; font-size: 14px; color: #999; }
.footer-link a { color: #409eff; text-decoration: none; }
</style>