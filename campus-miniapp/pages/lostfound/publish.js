const { request } = require('../../utils/request')

Page({
  data: {
    isEdit: false,
    editId: null,
    types: ['失物招领', '寻物启事'],
    form: {
      type: 0,
      itemName: '',
      location: '',
      description: '',
      contactPerson: '',
      contactPhone: ''
    },
    submitting: false
  },

  onLoad(options) {
    if (options.id) {
      this.setData({ isEdit: true, editId: options.id })
      this.loadDetail(options.id)
    }
  },

  loadDetail(id) {
    request({
      url: '/lost-found/' + id,
      showLoading: true
    }).then(data => {
      this.setData({
        'form.type': data.type,
        'form.itemName': data.itemName,
        'form.location': data.location,
        'form.description': data.description || '',
        'form.contactPerson': data.contactPerson || '',
        'form.contactPhone': data.contactPhone || ''
      })
    })
  },

  onTypeChange(e) {
    const idx = e.detail.value
    this.setData({
      'form.type': idx
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
    const { form, isEdit, editId } = this.data
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

    const url = isEdit ? '/lost-found/' + editId : '/lost-found'
    const method = isEdit ? 'PUT' : 'POST'

    request({
      url,
      method,
      data: form
    }).then(() => {
      wx.showToast({ title: isEdit ? '修改成功' : '发布成功，待审核' })
      wx.navigateBack()
    }).catch(() => {
      wx.showToast({ title: '操作失败，请重试', icon: 'none' })
    }).finally(() => {
      this.setData({ submitting: false })
    })
  }
})