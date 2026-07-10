const { request } = require('../../utils/request')

// D18: 同步防重复提交变量（不受 setData 异步影响）
let _submitting = false

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

  // D11: onLoad 登录拦截
  onLoad(options) {
    const app = getApp()
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(() => wx.navigateBack(), 1000)
      return
    }
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
    this.setData({ 'form.type': idx })
  },

  onFieldChange(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({ ['form.' + field]: value })
  },

  submitPublish() {
    // D18: 同步防重复提交
    if (_submitting) return
    _submitting = true

    const { form, isEdit, editId } = this.data

    // D9: 登录态检查（onLoad 已拦截，此处二次防御）
    const app = getApp()
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      _submitting = false
      return
    }

    // D19: 物品名称长度校验
    if (!form.itemName) {
      wx.showToast({ title: '请输入物品名称', icon: 'none' })
      _submitting = false
      return
    }
    if (form.itemName.length > 128) {
      wx.showToast({ title: '物品名称不超过128个字符', icon: 'none' })
      _submitting = false
      return
    }
    // D14: 补充 description 校验
    if (!form.description) {
      wx.showToast({ title: '请输入物品描述', icon: 'none' })
      _submitting = false
      return
    }
    if (!form.location) {
      wx.showToast({ title: '请输入地点', icon: 'none' })
      _submitting = false
      return
    }
    // D14: 补充 contactPerson 校验
    if (!form.contactPerson) {
      wx.showToast({ title: '请输入联系人', icon: 'none' })
      _submitting = false
      return
    }
    // D15: 手机号格式正则校验
    if (!form.contactPhone) {
      wx.showToast({ title: '请输入联系电话', icon: 'none' })
      _submitting = false
      return
    }
    if (!/^1[3-9]\d{9}$/.test(form.contactPhone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' })
      _submitting = false
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
      _submitting = false
    })
  }
})