import request from './request.js'

/** 投诉类型枚举（与数据库文档 complaint.type 一致） */
export const COMPLAINT_TYPE = {
  1: '爽约',
  2: '商品与描述不符',
  3: '物品损坏',
  4: '态度恶劣',
  5: '其他'
}

/** 投诉相关接口 */
export default {
  /**
   * 提交投诉
   * data: { orderType: 1闲置|2跑腿, orderId, defendantId, type, content, evidence: [] }
   */
  submit: (data) => request.post('/complaint/submit', data),

  /** 我的投诉记录（我发起的 + 我被投诉的） */
  myList: (params) => request.get('/complaint/my', params),

  /** 投诉详情（含处理进度与结果） */
  detail: (id) => request.get('/complaint/' + id)
}
