import request from './request.js'

/** 闲置商品相关接口 */
export default {
  /** 商品列表：categoryId/keyword/page */
  list: (params) => request.get('/goods/list', params),

  /** 商品详情（自动累计浏览数） */
  detail: (id) => request.get('/goods/' + id),

  /** 发布商品 */
  publish: (data) => request.post('/goods/publish', data),

  /** 我的发布 */
  myList: (params) => request.get('/goods/my', params),

  /** 下架 / 重新上架 */
  offShelf: (id) => request.post('/goods/' + id + '/offshelf'),
  onShelf: (id) => request.post('/goods/' + id + '/onshelf'),

  /** 收藏/取消收藏 */
  toggleFav: (id) => request.post('/goods/' + id + '/favorite'),

  /** 我的收藏列表 */
  myFavorites: (page) => request.get('/goods/favorite/my', { page }),

  /** 立即交易（生成订单并锁定商品） */
  createOrder: (goodsId, remark) => request.post('/goods/order/create', { goodsId, remark }),

  /** 我的闲置订单：role=buyer|seller，status 可选 */
  myOrders: (params) => request.get('/goods/order/my', params),

  /** 确认完成面交 */
  finishOrder: (orderId) => request.post('/goods/order/' + orderId + '/finish'),

  /** 取消订单 */
  cancelOrder: (orderId, reason) => request.post('/goods/order/' + orderId + '/cancel', { reason })
}
