const request = require('../../utils/request')

Page({
  data: {
    userInfo: {},
    editForm: { nickname: '', phone: '', studentNo: '' },
    submitting: false
  },

  onShow() {
    const app = getApp()
    this.setData({
      userInfo: app.globalData.userInfo || {}
    })
  },

  // 手动登录（当 app.js 自动登录失败时，用户可点击登录按钮重试）
  login() {
    const app = getApp()
    // 兜底：如果 deviceId 不存在则重新生成
    let deviceId = wx.getStorageSync('deviceId')
    if (!deviceId) {
      deviceId = 'device_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
      wx.setStorageSync('deviceId', deviceId)
    }
    wx.showLoading({ title: '登录中...' })
    wx.request({
      url: app.globalData.baseUrl + '/auth/wx-login',
      method: 'POST',
      data: { code: deviceId },
      success: (res) => {
        wx.hideLoading()
        if (res.data && res.data.code === 200) {
          app.globalData.token = res.data.data.token
          app.globalData.userInfo = res.data.data.user
          wx.setStorageSync('token', res.data.data.token)
          this.setData({ userInfo: res.data.data.user })
          wx.showToast({ title: '登录成功' })
        } else {
          wx.showToast({ title: res.data.msg || '登录失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '网络异常，请重试', icon: 'none' })
      }
    })
  },

  // 表单输入
  onNicknameInput(e) {
    this.setData({ 'editForm.nickname': e.detail.value })
  },
  onPhoneInput(e) {
    this.setData({ 'editForm.phone': e.detail.value })
  },
  onStudentNoInput(e) {
    this.setData({ 'editForm.studentNo': e.detail.value })
  },

  // 提交完善资料
  async submitProfile() {
    const { nickname, phone } = this.data.editForm
    if (!nickname || !nickname.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' })
      return
    }
    if (phone && !/^1[3-9]\d{9}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      return
    }
    this.setData({ submitting: true })
    try {
      const res = await request.put('/auth/profile', this.data.editForm)
      const app = getApp()
      app.globalData.userInfo = res
      this.setData({ userInfo: res })
      wx.showToast({ title: '保存成功' })
    } catch (e) {
      wx.showToast({ title: e.msg || e.message || '保存失败', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
    }
  },

  // 我的发布
  goMyPosts() {
    const app = getApp()
    const userId = app.globalData.userInfo?.id
    if (!userId) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/lostfound/index?userId=' + userId })
  },

  // 我的报修
  goMyRepairs() {
    const app = getApp()
    if (!app.globalData.userInfo?.id) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }
    wx.navigateTo({ url: '/pages/repair/index' })
  },

  // 意见反馈
  goFeedback() {
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  // 关于我们
  goAbout() {
    wx.showToast({ title: '校园综合服务平台 v1.0', icon: 'none' })
  },

  // 退出登录
  logout() {
    const app = getApp()
    // 通知服务端
    request.post('/auth/logout').catch(() => {})
    // 清除本地
    app.globalData.token = ''
    app.globalData.userInfo = null
    wx.removeStorageSync('token')
    this.setData({ userInfo: {} })
    wx.showToast({ title: '已退出登录' })
  }
})