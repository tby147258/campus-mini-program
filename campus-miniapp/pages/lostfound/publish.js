Page({
  data: {
    types: ['失物招领', '寻物启事'],
    form: {
      type: 0,
      typeName: '',
      itemName: '',
      location: '',
      description: '',
      contactPerson: '',
      contactPhone: ''
    },
    submitting: false
  },

  onTypeChange(e) {
    const idx = e.detail.value
    this.setData({
      'form.type': idx,
      'form.typeName': this.data.types[idx]
    })
  },

  onFieldChange(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({
      ['form.' + field]: value
    })
  },

  submitPublish() {
    const { form } = this.data
    if (!form.itemName) {
      wx.showToast({ title: '请输入物品名称', icon: 'none' })
      return
    }
    if (!form.location) {
      wx.showToast({ title: '请输入地点', icon: 'none' })
      return
    }
    if (!form.contactPhone) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' })
      return
    }

    this.setData({ submitting: true })
    const app = getApp()
    wx.request({
      url: app.globalData.baseUrl + '/lost-found',
      method: 'POST',
      data: {
        type: form.type,
        itemName: form.itemName,
        location: form.location,
        description: form.description,
        contactPerson: form.contactPerson,
        contactPhone: form.contactPhone
      },
      header: {
        'Authorization': 'Bearer ' + app.globalData.token,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        wx.showToast({ title: '发布成功，待审核' })
        wx.navigateBack()
      },
      fail: () => {
        wx.showToast({ title: '发布失败，请重试', icon: 'none' })
      },
      complete: () => {
        this.setData({ submitting: false })
      }
    })
  }
})