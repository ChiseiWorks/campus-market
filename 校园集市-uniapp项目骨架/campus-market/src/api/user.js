import request from './request.js'

/** 用户与认证相关接口 */
export default {
  /** 手机号密码登录 */
  login: (data) => request.post('/user/login', data),

  /** 注册（data: phone/password/smsCode） */
  register: (data) => request.post('/user/register', data),

  /** 发送短信验证码 */
  sendSms: (phone) => request.post('/user/sms', { phone }),

  /** 获取当前用户信息 */
  getInfo: () => request.get('/user/info'),

  /** 提交校园认证（学号+姓名+材料图） */
  submitAuth: (data) => request.post('/user/auth', data),

  /** 信用分流水 */
  creditLogs: (page) => request.get('/user/credit/logs', { page })
}
