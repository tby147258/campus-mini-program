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

  loadList() {
    const app = getApp()
    wx.request({
      url: app.globalData.baseUrl + '/lost-found',
      success: (res) => {
        const records = res.data.data || res.data.records || []
        this.setData({ allList: records, list: records })
        this.filterByType()
      }
    })
  },

  switchType(e) {
    const type = parseInt(e.currentTarget.dataset.type)
    this.setData({ type }, () => {
      this.filterByType()
      this.filterBySearch()
    })
  },

  onSearchInput(e) {
    this.setData({ searchText: e.detail.value })
  },

  onSearch() {
    this.filterBySearch()
  },

  filterByType() {
    const { allList, type } = this.data
    const filtered = allList.filter(item => item.type === type)
    this.setData({ list: filtered })
  },

  filterBySearch() {
    const { allList, searchText, type } = this.data
    let filtered = allList.filter(item => item.type === type)
    if (searchText.trim()) {
      filtered = filtered.filter(item =>
        item.itemName.indexOf(searchText) !== -1 ||
        item.location.indexOf(searchText) !== -1
      )
    }
    this.setData({ list: filtered })
  },

  goPublish() {
    wx.navigateTo({
      url: '/pages/lostfound/publish?type=' + this.data.type
    })
  }
})