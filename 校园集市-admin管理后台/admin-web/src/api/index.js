import request from './request'

// ==================== 登录 ====================
export const login = (data) => request.post('/admin/login', data)

// ==================== 数据看板 ====================
export const getDashboardSummary = () => request.get('/admin/dashboard/summary')
export const getDashboardTrend = (days = 7) =>
  request.get('/admin/dashboard/trend', { params: { days } })
export const getDashboardCategory = () => request.get('/admin/dashboard/category')
export const getDashboardErrandPeak = () => request.get('/admin/dashboard/errand-peak')

// ==================== 认证审核 ====================
export const getAuthList = (params) => request.get('/admin/auth/list', { params })
export const approveAuth = (id) => request.post('/admin/auth/approve', { id })
export const rejectAuth = (id, remark) => request.post('/admin/auth/reject', { id, remark })

// ==================== 用户管理 ====================
export const getUserList = (params) => request.get('/admin/user/list', { params })
export const banUser = (id) => request.post('/admin/user/ban', { id })
export const unbanUser = (id) => request.post('/admin/user/unban', { id })
export const getUserCreditLogs = (id, params) =>
  request.get('/admin/user/credit-logs', { params: { ...params, userId: id } })

// ==================== 商品管理 ====================
export const getGoodsList = (params) => request.get('/admin/goods/list', { params })
export const takedownGoods = (id, reason) => request.post('/admin/goods/takedown', { id, reason })

// ==================== 跑腿单管理 ====================
export const getErrandList = (params) => request.get('/admin/errand/list', { params })

// ==================== 投诉处理 ====================
export const getComplaintList = (params) => request.get('/admin/complaint/list', { params })
export const getComplaintDetail = (id) => request.get(`/admin/complaint/${id}`)
export const handleComplaint = (id, data) => request.post('/admin/complaint/handle', { id, ...data })

// ==================== 地点管理 ====================
export const getLocationList = () => request.get('/admin/location/list')
export const saveLocation = (data) => request.post('/admin/location/save', data)
export const toggleLocation = (id) => request.post('/admin/location/toggle', { id })

// ==================== 公告管理 ====================
export const getNoticeList = (params) => request.get('/admin/notice/list', { params })
export const publishNotice = (data) => request.post('/admin/notice/publish', data)
export const offlineNotice = (id) => request.post('/admin/notice/offline', { id })
