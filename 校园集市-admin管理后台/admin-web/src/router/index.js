import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataAnalysis' }
      },
      {
        path: 'auth',
        name: 'Auth',
        component: () => import('../views/Auth.vue'),
        meta: { title: '认证审核', icon: 'Stamp' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/User.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'goods',
        name: 'Goods',
        component: () => import('../views/Goods.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: 'errand',
        name: 'Errand',
        component: () => import('../views/Errand.vue'),
        meta: { title: '跑腿单管理', icon: 'Van' }
      },
      {
        path: 'complaint',
        name: 'Complaint',
        component: () => import('../views/Complaint.vue'),
        meta: { title: '投诉处理', icon: 'Warning' }
      },
      {
        path: 'location',
        name: 'Location',
        component: () => import('../views/Location.vue'),
        meta: { title: '地点管理', icon: 'Location' }
      },
      {
        path: 'notice',
        name: 'Notice',
        component: () => import('../views/Notice.vue'),
        meta: { title: '公告管理', icon: 'Bell' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：无 token 访问任意页跳 /login
router.beforeEach((to) => {
  const token = localStorage.getItem('admin_token')
  if (to.path !== '/login' && !token) {
    return '/login'
  }
  if (to.path === '/login' && token) {
    return '/dashboard'
  }
  document.title = to.meta.title
    ? `${to.meta.title} · 校园集市管理后台`
    : '校园集市 · 管理后台'
  return true
})

export default router
