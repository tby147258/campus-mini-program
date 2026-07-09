const app = getApp()

/**
 * 统一API请求封装
 * @param {Object} options
 * @param {string} options.url - 请求路径（不含baseUrl）
 * @param {string} [options.method='GET'] - 请求方法
 * @param {Object} [options.data={}] - 请求参数
 * @param {boolean} [options.showLoading=true] - 是否显示加载提示
 * @param {number} [options.timeout=10000] - 超时时间（毫秒）
 */
function request(options) {
  const { url, method = 'GET', data = {}, showLoading = true, timeout = 10000 } = options

  if (showLoading) {
    wx.showLoading({ title: '加载中...', mask: true })
  }

  return new Promise((resolve, reject) => {
    const requestTask = wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      timeout,
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
        if (err.errMsg && err.errMsg.indexOf('timeout') !== -1) {
          wx.showToast({ title: '请求超时，请检查网络', icon: 'none' })
        } else {
          wx.showToast({ title: '网络异常，请重试', icon: 'none' })
        }
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