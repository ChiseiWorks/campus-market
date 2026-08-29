import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器：自动携带 token
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('admin_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理 {code,msg,data} 契约
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非约定结构（如二进制）直接放行
    if (res === null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (res.code === 200) {
      return res.data
    }
    if (res.code === 401) {
      handleUnauthorized(res.msg)
      return Promise.reject(new Error(res.msg || '登录已过期'))
    }
    ElMessage.error(res.msg || '请求失败')
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error) => {
    const status = error.response?.status
    const msg = error.response?.data?.msg
    if (status === 401) {
      handleUnauthorized(msg)
    } else {
      ElMessage.error(msg || error.message || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

let redirecting = false
function handleUnauthorized(msg) {
  if (msg) ElMessage.error(msg)
  localStorage.removeItem('admin_token')
  localStorage.removeItem('admin_info')
  if (!redirecting && router.currentRoute.value.path !== '/login') {
    redirecting = true
    router.replace('/login').finally(() => {
      redirecting = false
    })
  }
}

export default request
