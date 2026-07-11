App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: '',
    userInfo: null
  },
  onLaunch() {
    // 获取或生成本地设备标识，确保同一设备始终使用同一用户
    let deviceId = wx.getStorageSync('deviceId')
    // 关键修复：必须明确判断空值，防止 storage 返回空字符串时仍被当成有效值
    if (deviceId === '' || deviceId == null) {
      deviceId = 'wx_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)
      wx.setStorageSync('deviceId', deviceId)
    }

    // 检查本地有无缓存的 token
    const cachedToken = wx.getStorageSync('token')
    if (cachedToken) {
      // 尝试用缓存的 token 获取用户信息
      this.globalData.token = cachedToken
      wx.request({
        url: this.globalData.baseUrl + '/auth/me',
        method: 'GET',
        header: { 'Authorization': 'Bearer ' + cachedToken, 'Content-Type': 'application/json' },
        success: (res) => {
          if (res.data && res.data.code === 200) {
            this.globalData.userInfo = res.data.data
          } else {
            // token 过期，重新登录
            this.doLogin(deviceId)
          }
        },
        fail: () => {
          // 网络异常，尝试重新登录
          this.doLogin(deviceId)
        }
      })
    } else {
      // 首次启动，执行登录
      this.doLogin(deviceId)
    }
  },

  doLogin(deviceId) {
    wx.request({
      url: this.globalData.baseUrl + '/auth/wx-login',
      method: 'POST',
      header: { 'Content-Type': 'application/json' },
      data: JSON.stringify({ code: deviceId }),
      success: (res) => {
        if (res.data && res.data.code === 200) {
          this.globalData.token = res.data.data.token
          this.globalData.userInfo = res.data.data.user
          wx.setStorageSync('token', res.data.data.token)
        }
      },
      fail: () => {
        console.warn('登录失败，网络异常')
      }
    })
  }
})