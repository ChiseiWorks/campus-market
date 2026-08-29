import request, { BASE_URL } from './request.js'

/** 通用基础数据接口：分类 / 地点 / 公告 / 文件上传 */
export default {
  /** 分类列表：type 1闲置分类 2跑腿类型，不传返回全部启用分类 */
  categories: (type) => request.get('/category/list', type ? { type } : {}),

  /** 校内地点库（预置，用户只能选不能填） */
  locations: () => request.get('/location/list'),

  /** 系统公告列表 */
  notices: () => request.get('/notice/list'),

  /**
   * 上传单张图片到后端 /file/upload，返回可访问的 URL
   * 后端无响应时 reject，调用方自行决定降级策略
   */
  upload(filePath) {
    return new Promise((resolve, reject) => {
      const token = uni.getStorageSync('token')
      uni.uploadFile({
        url: BASE_URL + '/file/upload',
        filePath,
        name: 'file',
        header: { 'Authorization': token ? 'Bearer ' + token : '' },
        success(res) {
          try {
            const body = JSON.parse(res.data)
            if (body && body.code === 200 && body.data) {
              resolve(typeof body.data === 'string' ? body.data : body.data.url)
            } else {
              reject(body)
            }
          } catch (e) {
            reject(e)
          }
        },
        fail: reject
      })
    })
  },

  /** 批量上传，返回 URL 数组（顺序与输入一致） */
  async uploadAll(filePaths) {
    const urls = []
    for (const p of filePaths) {
      urls.push(await this.upload(p))
    }
    return urls
  }
}
