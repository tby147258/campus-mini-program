const { request } = require('../../utils/request')

// D18: 同步防重复提交变量（不受 setData 异步影响）
let _submitting = false

Page({
  data: {
    // D8: 报修类型对齐后端约定值
    repairTypes: ['电器维修', '水暖维修', '门窗维修', '网络维修', '设备报修', '其他'],
    form: {
      repairType: '',
      // D5: 单字段 location → 三字段 campus/building/room
      campus: '',
      building: '',
      room: '',
      description: '',
      images: [],
      // D6: contact → contactPerson + contactPhone
      contactPerson: '',
      contactPhone: ''
    },
    myOrders: [],
    submitting: false,
    _loading: false
  },

  onLoad() {
    // 未登录用户仍可浏览表单，但 my-orders 接口需登录态
    if (this._checkLogin()) {
      this.loadMyOrders()
    }
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
    this.setData({ 'form.repairType': repairType })
  },

  onFieldChange(e) {
    const field = e.currentTarget.dataset.field
    const value = e.detail.value
    this.setData({ ['form.' + field]: value })
  },

  // D20: 图片选择 + 上传
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
        this.setData({ 'form.images': newImages })
      }
    })
  },

  removeImage(e) {
    const index = e.currentTarget.dataset.index
    const images = this.data.form.images
    images.splice(index, 1)
    this.setData({ 'form.images': images })
  },

  // D20: 上传单张图片 → 返回 URL
  uploadImage(filePath) {
    return new Promise((resolve, reject) => {
      const app = getApp()
      wx.uploadFile({
        url: app.globalData.baseUrl + '/file/upload',
        filePath: filePath,
        name: 'file',
        success: (res) => {
          try {
            const data = JSON.parse(res.data)
            if (data.code === 200) {
              resolve(data.data) // 返回 URL 字符串，如 "/uploads/xxx.jpg"
            } else {
              reject(new Error(data.msg || '上传失败'))
            }
          } catch (e) {
            reject(e)
          }
        },
        fail: reject
      })
    })
  },

  // D20: 上传所有待上传图片（本地路径 → 远程 URL）
  async uploadAllImages() {
    const { images } = this.data.form
    if (!images || images.length === 0) return []

    const uploadedUrls = []
    for (const img of images) {
      // 已经是 URL 的跳过（编辑模式回填）
      if (img.startsWith('/uploads/') || img.startsWith('http')) {
        uploadedUrls.push(img)
      } else {
        try {
          const url = await this.uploadImage(img)
          uploadedUrls.push(url)
        } catch (e) {
          console.warn('图片上传失败:', e)
          // 单张失败不影响整体
        }
      }
    }
    return uploadedUrls
  },

  // D9: 登录检查 — 未登录时引导跳转 profile 页
  _checkLogin() {
    const app = getApp()
    if (!app.globalData.token) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      setTimeout(() => wx.switchTab({ url: '/pages/profile/index' }), 1000)
      return false
    }
    return true
  },

  async submitRepair() {
    // D18: 同步防重复提交
    if (_submitting) return
    _submitting = true

    // D9: 登录态检查
    if (!this._checkLogin()) {
      _submitting = false
      return
    }

    const { form } = this.data

    // D8: 报修类型校验
    if (!form.repairType) {
      wx.showToast({ title: '请选择报修类型', icon: 'none' })
      _submitting = false
      return
    }
    // D17: 地点校验（三字段至少填一个）
    if (!form.campus && !form.building && !form.room) {
      wx.showToast({ title: '请输入报修地点', icon: 'none' })
      _submitting = false
      return
    }
    // 描述校验
    if (!form.description) {
      wx.showToast({ title: '请输入问题描述', icon: 'none' })
      _submitting = false
      return
    }
    // D16: contactPerson/contactPhone 至少填一个
    if (!form.contactPerson && !form.contactPhone) {
      wx.showToast({ title: '请输入联系方式', icon: 'none' })
      _submitting = false
      return
    }

    this.setData({ submitting: true })

    try {
      // D20: 先上传图片，获得远程 URL
      const imageUrls = await this.uploadAllImages()

      // D5/D6: 发送与后端 RepairOrder 字段匹配的请求体，自动携带用户ID
      const body = {
        repairType: form.repairType,
        campus: form.campus,
        building: form.building,
        room: form.room,
        description: form.description,
        images: imageUrls.length > 0 ? imageUrls : null,
        contactPerson: form.contactPerson,
        contactPhone: form.contactPhone,
        userId: getApp().globalData.userInfo?.id
      }

      await request({
        url: '/repair',
        method: 'POST',
        data: body
      })

      wx.showToast({ title: '提交成功' })
      this.setData({
        form: { repairType: '', campus: '', building: '', room: '', description: '', images: [], contactPerson: '', contactPhone: '' }
      })
      this.loadMyOrders()
    } catch (e) {
      wx.showToast({ title: '提交失败，请重试', icon: 'none' })
    } finally {
      this.setData({ submitting: false })
      _submitting = false
    }
  },

  // D12: 修复 loadMyOrders — setData 规范 + 错误提示
  loadMyOrders() {
    this.setData({ _loading: true })
    return request({
      url: '/repair/my-orders',
      showLoading: false
    }).then(data => {
      this.setData({ myOrders: data.records || data || [] })
    }).catch(() => {
      // 静默处理，不影响列表显示
      console.warn('我的报修加载失败')
    }).finally(() => {
      this.setData({ _loading: false })
    })
  }
})