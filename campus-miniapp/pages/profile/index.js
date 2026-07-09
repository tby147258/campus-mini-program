const { request } = require('../../utils/request')

Page({
  data: {
    userInfo: {},
    myPosts: [],
    showPosts: false
  },

  onShow() {
    const app = getApp()
    this.setData({
      userInfo: app.globalData.userInfo || {}
    })
  },

  goMyPosts() {
    wx.showLoading({ title: '加载中...' })
    request({
      url: '/lost-found',
      showLoading: false
    }).then(data => {
      const records = data.records || data || []
      const app = getApp()
      const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null
      const myPosts = records.filter(item => item.userId === userId)
      this.setData({ myPosts, showPosts: true })
      wx.hideLoading()
    }).catch(() => {
      wx.hideLoading()
      wx.showToast({ title: '加载失败', icon: 'none' })
    })
  },

  goMyRepairs() {
    wx.switchTab({ url: '/pages/repair/index' })
  },

  goPostDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/lostfound/detail?id=' + id })
  },

  hidePosts() {
    this.setData({ showPosts: false })
  },

  goFeedback() {
    wx.showModal({
      title: '意见反馈',
      content: '如有任何问题或建议，请联系校园服务中心。\n电话：010-88888888\n邮箱：service@campus.edu',
      showCancel: false
    })
  },

  goAbout() {
    wx.showModal({
      title: '关于我们',
      content: '校园综合服务小程序 v1.0.0\n\n为师生提供便捷的校园生活服务，包括失物招领、报修中心、校园公告等功能。',
      showCancel: false
    })
  },

  logout() {
    wx.showModal({
      title: '退出登录',
      content: '确定要退出登录吗？',
      success: (res) => {
        if (res.confirm) {
          const app = getApp()
          app.globalData.token = ''
          app.globalData.userInfo = null
          this.setData({ userInfo: {} })
          wx.showToast({ title: '已退出' })
        }
      }
    })
  }
})