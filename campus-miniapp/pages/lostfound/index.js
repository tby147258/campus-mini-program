const { request } = require('../../utils/request')

Page({
  data: {
    type: 0,
    list: [],
    searchText: '',
    allList: []
  },

  onLoad() {
    this.loadList()
  },

  onShow() {
    this.loadList()
  },

  onPullDownRefresh() {
    this.loadList().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  loadList() {
    return request({
      url: '/lost-found',
      showLoading: false
    }).then(data => {
      const records = data.records || data || []
      this.setData({ allList: records })
      this.applyFilters()
    }).catch(() => {})
  },

  switchType(e) {
    const type = parseInt(e.currentTarget.dataset.type)
    this.setData({ type }, () => {
      this.applyFilters()
    })
  },

  onSearchInput(e) {
    this.setData({ searchText: e.detail.value })
  },

  onSearch() {
    this.applyFilters()
  },

  applyFilters() {
    const { allList, type, searchText } = this.data
    let filtered = allList.filter(item => item.type === type)
    if (searchText.trim()) {
      filtered = filtered.filter(item =>
        (item.itemName && item.itemName.indexOf(searchText) !== -1) ||
        (item.location && item.location.indexOf(searchText) !== -1)
      )
    }
    this.setData({ list: filtered })
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