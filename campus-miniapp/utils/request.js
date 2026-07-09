const app = getApp()

function request(options) {
  const { url, method = 'GET', data = {}, showLoading = true } = options

  if (showLoading) {
    wx.showLoading({ title: '加载中...', mask: true })
  }

  return new Promise((resolve, reject) => {
    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      header: {
        'Authorization': 'Bearer ' + (app.globalData.token || ''),
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data !== undefined ? res.data.data : res.data)
        } else if (res.statusCode === 401) {
          wx.showToast({ title: '登录已过期，请重新进入', icon: 'none' })
          reject(res.data)
        } else {
          wx.showToast({ title: res.data.message || '请求失败', icon: 'none' })
          reject(res.data)
        }
      },
      fail: (err) => {
        wx.showToast({ title: '网络异常，请重试', icon: 'none' })
        reject(err)
      },
      complete: () => {
        if (showLoading) {
          wx.hideLoading()
        }
      }
    })
  })
}

module.exports = { request }