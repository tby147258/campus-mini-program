const { request } = require('../../utils/request')

Page({
  data: {
    banners: [
      '/images/banner_default.png'
    ],
    weather: { temp: '--', text: '加载中', city: '--' },
    announcements: []
  },

  onLoad() {
    this.loadWeather()
    this.loadAnnouncements()
  },

  onShow() {
    this.loadAnnouncements()
  },

  onPullDownRefresh() {
    Promise.all([
      this.loadWeather(),
      this.loadAnnouncements()
    ]).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadWeather() {
    const app = getApp()
    return request({
      url: '/weather/now?location=101010100',
      showLoading: false
    }).then(data => {
      if (data && data.now) {
        this.setData({ weather: data.now })
      }
    }).catch(() => {})
  },

  loadAnnouncements() {
    return request({
      url: '/announcement',
      showLoading: false
    }).then(data => {
      const records = data.records || data || []
      this.setData({ announcements: records })
    }).catch(() => {})
  },

  goLostFound() { wx.switchTab({ url: '/pages/lostfound/index' }) },
  goRepair() { wx.switchTab({ url: '/pages/repair/index' }) },
  goWeather() { wx.showToast({ title: '当前天气已展示在首页', icon: 'none' }) },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/index/detail?id=' + id })
  }
})