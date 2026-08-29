import request from './request.js'

/** 聊天消息接口（P1：轮询方案；后续可升级 WebSocket） */
export default {
  /** 会话列表：最近联系人 + 最后一条消息 + 未读数 */
  sessions: () => request.get('/message/sessions'),

  /** 与某人的聊天记录（分页，按时间倒序） */
  history: (userId, page) => request.get('/message/history', { userId, page }),

  /** 发送消息：type 1文本 2图片；goodsId 为从商品页发起会话时带上 */
  send: (toUserId, content, type = 1, goodsId) =>
    request.post('/message/send', { toUserId, content, type, goodsId }),

  /** 将与某人的会话标记为已读 */
  markRead: (userId) => request.post('/message/read', { userId }),

  /** 全局未读数（消息 Tab 角标用） */
  unreadCount: () => request.get('/message/unread/count')
}
