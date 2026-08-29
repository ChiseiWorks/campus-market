/**
 * 全局工具函数
 */

/**
 * 认证拦截：发布/下单/抢单/聊天 前统一调用
 * 返回 true = 已认证可继续；false = 已被拦截并弹窗引导
 */
export function checkAuth() {
  const store = this.$store
  if (!store.getters['user/isLogin']) {
    uni.showModal({
      title: '提示',
      content: '请先登录',
      confirmText: '去登录',
      success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/user/login' })
      }
    })
    return false
  }
  if (!store.getters['user/isAuthed']) {
    uni.showModal({
      title: '未认证',
      content: '完成校园实名认证后才能进行交易，去认证？',
      confirmText: '去认证',
      success: (res) => {
        if (res.confirm) uni.navigateTo({ url: '/pages/user/auth' })
      }
    })
    return false
  }
  return true
}

/** 相对时间：2分钟前 / 1小时前 / 昨天 */
export function timeAgo(timeStr) {
  const time = new Date(timeStr.replace(/-/g, '/')).getTime()
  const diff = Date.now() - time
  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour
  if (diff < minute) return '刚刚'
  if (diff < hour) return Math.floor(diff / minute) + '分钟前'
  if (diff < day) return Math.floor(diff / hour) + '小时前'
  if (diff < 2 * day) return '昨天'
  return timeStr.slice(0, 10)
}

/** 防重复提交锁 */
export function lock(fn) {
  let locked = false
  return async function (...args) {
    if (locked) return
    locked = true
    try {
      return await fn.apply(this, args)
    } finally {
      setTimeout(() => { locked = false }, 1000)
    }
  }
}
