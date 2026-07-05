App({
  globalData: {
    baseUrl: 'http://localhost:8080/api',
    token: '',
    userInfo: null
  },
  onLaunch() {
    // 登录
    wx.login({
      success: res => {
        wx.request({
          url: this.globalData.baseUrl + '/auth/wx-login',
          method: 'POST',
          data: { code: res.code },
          success: (res) => {
            this.globalData.token = res.data.data.token
            this.globalData.userInfo = res.data.data.user
          }
        })
      }
    })
  }
})