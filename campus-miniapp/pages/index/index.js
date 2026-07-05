Page({
  data: {
    banners: [],
    weather: { temp: '--', text: '加载中', city: '--' },
    announcements: []
  },
  onLoad() {
    this.loadWeather()
    this.loadAnnouncements()
  },
  loadWeather() {
    const app = getApp()
    wx.request({
      url: app.globalData.baseUrl + '/weather/now?location=101010100',
      success: (res) => {
        if (res.data.data) {
          this.setData({ weather: res.data.data.now || {} })
        }
      }
    })
  },
  loadAnnouncements() {
    const app = getApp()
    wx.request({
      url: app.globalData.baseUrl + '/announcements',
      success: (res) => {
        this.setData({ announcements: res.data.data.records || [] })
      }
    })
  },
  goLostFound() { wx.switchTab({ url: '/pages/lostfound/index' }) },
  goRepair() { wx.switchTab({ url: '/pages/repair/index' }) },
  goWeather() { wx.showToast({ title: '当前天气已展示在首页', icon: 'none' }) },
  goDetail(e) {
    wx.navigateTo({ url: `/pages/index/detail?id=${e.currentTarget.dataset.id}` })
  }
})