const { request } = require('../../utils/request')

Page({
  data: {
    type: 0,
    list: [],
    searchText: '',
    page: 1,
    pageSize: 10,
    total: 0,
    hasMore: true,
    _loading: false
  },

  onLoad() {
    this.setData({ _loading: true })
    this.loadList(true)
  },

  onShow() {
    // D15: 使用 setData 更新 _loading
    if (!this.data._loading) {
      this.setData({ _loading: true })
      this.loadList(true)
    }
  },

  onUnload() {
    // D6: 页面卸载时重置 loading 计数器
    const { resetLoadingCounter } = require('../../utils/request')
    resetLoadingCounter()
  },

  onPullDownRefresh() {
    this.loadList(true).finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  // D14: 触底加载更多
  onReachBottom() {
    if (!this.data.hasMore || this.data._loading) return
    this.loadList(false)
  },

  loadList(reset) {
    const { type, searchText, page, pageSize } = this.data
    const currentPage = reset ? 1 : page + 1

    this.setData({ _loading: true })

    // D13: 传 type/status 到服务端分页，取消客户端过滤
    // D17: 搜索关键字也传服务端
    const params = {
      page: currentPage,
      size: pageSize,
      type: type
    }
    if (searchText.trim()) {
      params.keyword = searchText.trim()
    }

    return request({
      url: '/lost-found',
      data: params,
      showLoading: false
    }).then(data => {
      const records = data.records || []
      // D16: 错误提示 — 无数据时显示空状态
      if (records.length === 0 && reset) {
        this.setData({ list: [], total: 0, hasMore: false })
      } else {
        this.setData({
          list: reset ? records : [...this.data.list, ...records],
          page: currentPage,
          total: data.total || 0,
          hasMore: records.length >= pageSize
        })
      }
    }).catch(() => {
      // D16: 加载失败时提示用户
      wx.showToast({ title: '加载失败，下拉可重试', icon: 'none' })
    }).finally(() => {
      this.setData({ _loading: false })
    })
  },

  switchType(e) {
    const type = parseInt(e.currentTarget.dataset.type)
    this.setData({ type }, () => {
      this.loadList(true)
    })
  },

  onSearchInput(e) {
    this.setData({ searchText: e.detail.value })
  },

  onSearch() {
    // D17: 搜索走服务端，重新加载第一页
    this.loadList(true)
  },

  goDetail(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({ url: '/pages/lostfound/detail?id=' + id })
  },

  goPublish() {
    wx.navigateTo({
      url: '/pages/lostfound/publish'
    })
  }
})