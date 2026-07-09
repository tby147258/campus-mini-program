const { request } = require('../../utils/request')

Page({
  data: {
    repairTypes: ['水暖维修', '电力维修', '门窗维修', '网络故障', '设备报修', '其他'],
    form: {
      repairType: '',
      location: '',
      description: '',
      images: [],
      contact: ''
    },
    myOrders: [],
    submitting: false,
    _loading: false
  },

  onLoad() {
    this.loadMyOrders()
  },

  onShow() {
    if (!this.data._loading) {
      this.loadMyOrders()
    }
  },

  onPullDownRefresh() {
    this.loadMyOrders().finally(() => {
      wx.stopPullDownRefresh()
    })
  },

  onTypeChange(e) {
    const repairType = this.data.repairTypes[e.detail.value]
    this.setData({
      'form.repairType': repairType
    })
  },

  onFieldChange(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({
      ['form.' + field]: value
    })
  },

  chooseImage() {
    wx.chooseImage({
      count: 3,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempPaths = res.tempFilePaths
        const currentImages = this.data.form.images
        const newImages = currentImages.concat(tempPaths)
        if (newImages.length > 3) {
          newImages.splice(3)
        }
        this.setData({
          'form.images': newImages
        })
      }
    })
  },

  removeImage(e) {
    const index = e.currentTarget.dataset.index
    const images = this.data.form.images
    images.splice(index, 1)
    this.setData({
      'form.images': images
    })
  },

  submitRepair() {
    const { form } = this.data
    if (!form.repairType) {
      wx.showToast({ title: '请选择报修类型', icon: 'none' })
      return
    }
    if (!form.location) {
      wx.showToast({ title: '请输入报修地点', icon: 'none' })
      return
    }
    if (!form.description) {
      wx.showToast({ title: '请输入问题描述', icon: 'none' })
      return
    }
    if (!form.contact) {
      wx.showToast({ title: '请输入联系方式', icon: 'none' })
      return
    }

    this.setData({ submitting: true })

    request({
      url: '/repair',
      method: 'POST',
      data: form
    }).then(() => {
      wx.showToast({ title: '提交成功' })
      this.setData({
        form: { repairType: '', location: '', description: '', images: [], contact: '' }
      })
      this.loadMyOrders()
    }).catch(() => {
      wx.showToast({ title: '提交失败，请重试', icon: 'none' })
    }).finally(() => {
      this.setData({ submitting: false })
    })
  },

  loadMyOrders() {
    this.data._loading = true
    return request({
      url: '/repair/my-orders',
      showLoading: false
    }).then(data => {
      this.setData({ myOrders: data.records || data || [] })
    }).catch(() => {}).finally(() => {
      this.data._loading = false
    })
  }
})