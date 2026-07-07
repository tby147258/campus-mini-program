<template>
  <div class="forgot-container">
    <div class="forgot-card">
      <h2 class="title">重置密码</h2>
      <el-steps :active="step" align-center finish-status="success" style="margin: 20px 0 30px">
        <el-step title="填写邮箱" />
        <el-step title="验证码" />
        <el-step title="重置密码" />
      </el-steps>

      <!-- 第一步：填写邮箱 -->
      <div v-if="step === 1">
        <el-form :model="form" :rules="rules1" ref="form1Ref" label-width="80px">
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入注册时使用的邮箱" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="sendCode" :loading="sending" style="width:100%">发送验证码</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第二步：输入验证码 -->
      <div v-if="step === 2">
        <p style="color:#666;margin-bottom:16px">验证码已发送至 <strong>{{ form.email }}</strong>，请查收</p>
        <el-form :model="form" :rules="rules2" ref="form2Ref" label-width="80px">
          <el-form-item label="验证码" prop="code">
            <el-input v-model="form.code" placeholder="请输入6位验证码" maxlength="6" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="verifyCode" :loading="verifying" style="width:100%">下一步</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 第三步：重置密码 -->
      <div v-if="step === 3">
        <el-form :model="form" :rules="rules3" ref="form3Ref" label-width="100px">
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="resetPassword" :loading="resetting" style="width:100%">确认重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="footer-link" v-if="step < 3">
        想起密码了？<router-link to="/login">返回登录</router-link>
      </div>
      <div class="footer-link" v-if="step === 3 && resetDone">
        密码已重置，<router-link to="/login">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { authApi } from '../api/index.js'

export default {
  data() {
    const validatePass = (rule, value, callback) => {
      if (value !== this.form.newPassword) callback(new Error('两次密码不一致'))
      else callback()
    }
    return {
      step: 1,
      form: { email: '', code: '', newPassword: '', confirmPassword: '' },
      sending: false,
      verifying: false,
      resetting: false,
      resetDone: false,
      rules1: { email: [{ required: true, type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }] },
      rules2: { code: [{ required: true, min: 6, max: 6, message: '请输入6位验证码', trigger: 'blur' }] },
      rules3: {
        newPassword: [{ required: true, min: 6, message: '密码至少6位', trigger: 'blur' }],
        confirmPassword: [{ required: true, validator: validatePass, trigger: 'blur' }]
      }
    }
  },
  methods: {
    async sendCode() {
      const valid = await this.$refs.form1Ref.validate().catch(() => false)
      if (!valid) return
      this.sending = true
      try {
        await authApi.forgotPassword(this.form.email)
        this.$message.success('验证码已发送（请查看后端控制台输出）')
        this.step = 2
      } catch (e) {
        this.$message.error(e.response?.data?.msg || '发送失败')
      }
      this.sending = false
    },
    async verifyCode() {
      const valid = await this.$refs.form2Ref.validate().catch(() => false)
      if (!valid) return
      this.verifying = true
      setTimeout(() => { this.verifying = false; this.step = 3 }, 500)
    },
    async resetPassword() {
      const valid = await this.$refs.form3Ref.validate().catch(() => false)
      if (!valid) return
      this.resetting = true
      try {
        await authApi.resetPassword({ email: this.form.email, code: this.form.code, newPassword: this.form.newPassword })
        this.$message.success('密码重置成功')
        this.resetDone = true
      } catch (e) {
        this.$message.error(e.response?.data?.msg || '重置失败')
      }
      this.resetting = false
    }
  }
}
</script>

<style scoped>
.forgot-container { height: 100vh; display: flex; justify-content: center; align-items: center; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
.forgot-card { background: #fff; padding: 40px; border-radius: 12px; width: 480px; box-shadow: 0 10px 40px rgba(0,0,0,0.15); }
.title { text-align: center; margin-bottom: 8px; color: #333; font-size: 22px; }
.footer-link { text-align: center; margin-top: 20px; font-size: 14px; color: #999; }
.footer-link a { color: #409eff; text-decoration: none; }
</style>