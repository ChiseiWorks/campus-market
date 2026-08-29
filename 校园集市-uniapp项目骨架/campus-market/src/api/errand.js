import request from './request.js'

/** 跑腿单状态枚举（与后端 ErrandStatusEnum 严格一致） */
export const ERRAND_STATUS = {
  PENDING:    0, // 待接单
  ACCEPTED:   1, // 已接单
  DELIVERING: 2, // 配送中
  ARRIVED:    3, // 送达待确认
  FINISHED:   4, // 已完成
  CANCELLED:  5, // 已取消
  DISPUTED:   6  // 申诉中
}

export const ERRAND_STATUS_TEXT = {
  0: '待接单', 1: '已接单', 2: '配送中', 3: '送达待确认',
  4: '已完成', 5: '已取消', 6: '申诉中'
}

export const ERRAND_TYPE = {
  1: '取快递', 2: '代买餐', 3: '代送物品', 4: '其他'
}

/** 跑腿相关接口 */
export default {
  /** 发布悬赏 */
  publish: (data) => request.post('/errand/publish', data),

  /** 接单大厅：type/sort(latest|reward)/page */
  hall: (params) => request.get('/errand/hall', params),

  /** 订单详情（服务端按角色+状态脱敏返回） */
  detail: (id) => request.get('/errand/' + id),

  /** 抢单（后端原子 CAS，失败提示"手慢了"） */
  accept: (id) => request.post('/errand/' + id + '/accept'),

  /** 开始配送 */
  deliver: (id) => request.post('/errand/' + id + '/deliver'),

  /** 确认送达 */
  arrive: (id) => request.post('/errand/' + id + '/arrive'),

  /** 发单人确认完成 */
  confirm: (id) => request.post('/errand/' + id + '/confirm'),

  /** 取消订单（body 带原因；跑男在已接单状态取消会被扣信用分） */
  cancel: (id, reason) => request.post('/errand/' + id + '/cancel', { reason }),

  /** 发起申诉 */
  dispute: (id, reason) => request.post('/errand/' + id + '/dispute', { reason }),

  /** 我发布的单 */
  myPublish: (params) => request.get('/errand/my/publish', params),

  /** 我接的单 */
  myAccept: (params) => request.get('/errand/my/accept', params)
}
