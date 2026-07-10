// 延迟获取 app 实例，避免模块加载时 getApp() 未初始化
let _app = null
function getAppInstance() {
  if (!_app) {
    _app = getApp()
  }
  return _app
}

// 全局 loading 计数器，确保 showLoading 与 hideLoading 配对
let loadingCount = 0
let isLoadingShowing = false

function showLoadingSafe() {
  if (loadingCount === 0) {
    wx.showLoading({ title: '加载中...', mask: true })
    isLoadingShowing = true
  }
  loadingCount++
}

function hideLoadingSafe() {
  loadingCount--
  if (loadingCount <= 0) {
    loadingCount = 0
    if (isLoadingShowing) {
      isLoadingShowing = false
      wx.hideLoading()
    }
  }
}

/**
 * 重置 loading 计数器（在页面 onUnload 时调用，防止计数器永久失衡）
 */
function resetLoadingCounter() {
  if (loadingCount > 0 || isLoadingShowing) {
    loadingCount = 0
    isLoadingShowing = false
    wx.hideLoading()
  }
}

/**
 * 统一API请求封装
 * @param {Object} options
 * @param {string} options.url - 请求路径（不含baseUrl）
 * @param {string} [options.method='GET'] - 请求方法
 * @param {Object} [options.data={}] - 请求参数
 * @param {boolean} [options.showLoading=true] - 是否显示加载提示
 * @param {number} [options.timeout=15000] - 超时时间（毫秒）
 */
function request(options) {
  const { url, method = 'GET', data = {}, showLoading = true, timeout = 15000 } = options

  if (showLoading) {
    showLoadingSafe()
  }

  return new Promise((resolve, reject) => {
    const app = getAppInstance()
    // D3: 无 Token 时不发送 Authorization 头，避免空 Bearer 触发 401
    const header = app.globalData.token
      ? { 'Authorization': 'Bearer ' + app.globalData.token, 'Content-Type': 'application/json' }
      : { 'Content-Type': 'application/json' }

    wx.request({
      url: app.globalData.baseUrl + url,
      method,
      data,
      timeout,
      header,
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 200) {
          resolve(res.data.data !== undefined ? res.data.data : res.data)
        } else if (res.statusCode === 401) {
          // D5: Token 过期 → 清除 token 并重新登录
          app.globalData.token = ''
          wx.showToast({ title: '登录已过期，请重新进入', icon: 'none' })
          // 使用本地设备标识重新登录
          const deviceId = wx.getStorageSync('deviceId') || 'device_' + Date.now()
          wx.request({
            url: app.globalData.baseUrl + '/auth/wx-login',
            method: 'POST',
            data: { code: deviceId },
            success: (refreshRes) => {
              if (refreshRes.data && refreshRes.data.code === 200) {
                app.globalData.token = refreshRes.data.data.token
                app.globalData.userInfo = refreshRes.data.data.user
                wx.setStorageSync('token', refreshRes.data.data.token)
              }
            }
          })
          reject(res.data)
        } else {
          // D2: 字段名使用 msg（匹配后端 Result.msg）
          wx.showToast({ title: res.data.msg || '请求失败', icon: 'none' })
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
          hideLoadingSafe()
        }
      }
    })
  })
}

module.exports = {
  request,
  get(url, data, options) {
    return request({ url, method: 'GET', data, ...options })
  },
  post(url, data, options) {
    return request({ url, method: 'POST', data, ...options })
  },
  put(url, data, options) {
    return request({ url, method: 'PUT', data, ...options })
  },
  del(url, data, options) {
    return request({ url, method: 'DELETE', data, ...options })
  },
  resetLoadingCounter
}