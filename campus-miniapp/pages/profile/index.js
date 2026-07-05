Page({
  data: {
    userInfo: {}
  },

  onShow() {
    const app = getApp()
    this.setData({
      userInfo: app.globalData.userInfo || {}
    })
  },

  goMyPosts() {
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  goMyRepairs() {
    wx.switchTab({ url: '/pages/repair/index' })
  },

  goFeedback() {
    wx.showToast({ title: '功能开发中', icon: 'none' })
  },

  goAbout() {
    wx.showModal({
      title: '关于我们',
      content: '校园综合服务小程序 v1.0.0\n为师生提供便捷的校园生活服务。',
      showCancel: false
    })
  }
})