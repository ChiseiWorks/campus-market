import { createSSRApp } from 'vue'
import App from './App.vue'
import store from './store'

// 全局属性：认证拦截工具，所有页面可用 this.$checkAuth()
import { checkAuth } from '@/utils/index.js'

export function createApp() {
  const app = createSSRApp(App)
  app.use(store)
  app.config.globalProperties.$checkAuth = checkAuth
  return { app }
}
