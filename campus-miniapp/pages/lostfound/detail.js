const { request } = require('../../utils/request')

Page({
  data: {
    detail: null,
    isOwner: false
  },

  onLoad(options) {
    const id = options.id
    if (id) {
      this.loadDetail(id)
    }
  },

  loadDetail(id) {
    request({
      url: '/lost-found/' + id
    }).then(data => {
      const app = getApp()
      const userId = app.globalData.userInfo ? app.globalData.userInfo.id : null
      this.setData({
        detail: data,
        isOwner: userId && data.userId === userId
      })
    }).catch(() => {
      wx.showToast({ title: '加载失败', icon: 'none' })
    })
  },

  goEdit() {
    const id = this.data.detail.id
    wx.navigateTo({
      url: '/pages/lostfound/publish?id=' + id
    })
  },

  confirmDelete() {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条信息吗？',
      success: (res) => {
        if (res.confirm) {
          this.deleteItem()
        }
      }
    })
  },

  deleteItem() {
    const id = this.data.detail.id
    request({
      url: '/lost-found/' + id,
      method: 'DELETE'
    }).then(() => {
      wx.showToast({ title: '删除成功' })
      wx.navigateBack()
    }).catch(() => {
      wx.showToast({ title: '删除失败', icon: 'none' })
    })
  }
})