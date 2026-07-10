const { request } = require('../../utils/request')

Page({
  data: {
    banners: [
      '/images/banner_default.png'
    ],
    weather: { temp: '--', text: '加载中', city: '--' },
    announcements: [],
    _loading: false
  },

  onLoad() {
    // D8: 开启 _loading 标记，防止 onShow 重复请求
    this.setData({ _loading: true })
    this.loadWeather()
    this.loadAnnouncements()
  },

  onShow() {
    // D11: _loading 标记在 onLoad 中设为 true，onShow 不再重复请求
    if (!this.data._loading) {
      this.loadAnnouncements()
    }
  },

  onUnload() {
    this.setData({ _loading: false })
  },

  onPullDownRefresh() {
    // D12: 使用 allSettled 避免某个请求失败阻塞下拉动画
    Promise.allSettled([
      this.loadWeather(),
      this.loadAnnouncements()
    ]).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadWeather() {
    const app = getApp()
    // D9: 天气城市编码 101010100 = 北京，可后续扩展为 wx.getLocation 动态获取
    return request({
      url: '/weather/now?location=101010100',
      showLoading: false
    }).then(data => {
      if (data && data.now) {
        this.setData({ weather: data.now })
      }
    }).catch(() => {
      // 天气加载失败不影响首页其他内容
      console.warn('天气加载失败')
    })
  },

  loadAnnouncements() {
    return request({
      url: '/announcement',
      data: { page: 1, size: 10 },  // D10: 显式传分页参数
      showLoading: false
    }).then(data => {
      const records = data.records || data || []
      this.setData({ announcements: records })
    }).catch(() => {
      console.warn('公告加载失败')
    })
  },

  goLostFound() { wx.switchTab({ url: '/pages/lostfound/index' }) },
  goRepair() { wx.switchTab({ url: '/pages/repair/index' }) },
  goWeather() { wx.showToast({ title: '当前天气已展示在首页', icon: 'none' }) },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/index/detail?id=' + id })
  }
})