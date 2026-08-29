import request from './request.js'

/** 评价相关接口 */
export default {
  /**
   * 提交评价（每单每人限一次，后端 uk_order_from 唯一约束兜底）
   * data: { orderType: 1闲置|2跑腿, orderId, toUserId, score: 1~5, tags: [], content }
   */
  submit: (data) => request.post('/evaluate/submit', data),

  /** 某用户收到的评价列表（卖家主页/跑男主页展示用） */
  ofUser: (userId, page) => request.get('/evaluate/user/' + userId, { page }),

  /** 我发出的评价 */
  myList: (page) => request.get('/evaluate/my', { page })
}
