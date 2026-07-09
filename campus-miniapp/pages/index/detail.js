const { request } = require('../../utils/request')

Page({
  data: {
    announcement: null
  },

  onLoad(options) {
    const id = options.id
    if (id) {
      this.loadDetail(id)
    }
  },

  loadDetail(id) {
    request({
      url: '/announcement/' + id
    }).then(data => {
      this.setData({ announcement: data })
    }).catch(() => {
      wx.showToast({ title: '加载失败', icon: 'none' })
    })
  }
})