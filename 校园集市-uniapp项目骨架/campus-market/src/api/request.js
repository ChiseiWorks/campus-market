/**
 * 统一网络请求封装
 * - 自动携带 JWT token
 * - 401 自动跳登录
 * - 统一错误提示
 * 对接后端约定：{ code: 200, msg: "success", data: {...} }
 */

export const BASE_URL = 'http://localhost:8080/api' // TODO: 部署后改为线上地址

function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? 'Bearer ' + token : ''
      },
      success(res) {
        // 未登录 / token 过期：清登录态并跳登录页
        if (res.statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.navigateTo({ url: '/pages/user/login' })
          return reject(res.data)
        }
        const body = res.data
        if (body && body.code === 200) {
          resolve(body.data)
        } else {
          uni.showToast({ title: (body && body.msg) || '请求失败', icon: 'none' })
          reject(body)
        }
      },
      fail(err) {
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default {
  get:  (url, data) => request({ url, method: 'GET', data }),
  post: (url, data) => request({ url, method: 'POST', data })
}
