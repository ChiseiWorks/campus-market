<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <el-icon :size="24" color="#FF6A00"><ShoppingBag /></el-icon>
        <span class="logo-text">校园集市 · 管理后台</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#FF6A00"
        class="layout-menu"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>校园集市</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown trigger="click">
            <span class="admin-info">
              <el-avatar :size="30" class="admin-avatar">
                {{ avatarChar }}
              </el-avatar>
              <span class="admin-name">{{ nickname }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item disabled>账号：{{ username }}</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()

const menuItems = [
  { path: '/dashboard', title: '数据看板', icon: 'DataAnalysis' },
  { path: '/auth', title: '认证审核', icon: 'Stamp' },
  { path: '/user', title: '用户管理', icon: 'User' },
  { path: '/goods', title: '商品管理', icon: 'Goods' },
  { path: '/errand', title: '跑腿单管理', icon: 'Van' },
  { path: '/complaint', title: '投诉处理', icon: 'Warning' },
  { path: '/location', title: '地点管理', icon: 'Location' },
  { path: '/notice', title: '公告管理', icon: 'Bell' }
]

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title || '')

const adminInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('admin_info') || '{}')
  } catch {
    return {}
  }
})
const nickname = computed(() => adminInfo.value.nickname || adminInfo.value.username || '管理员')
const username = computed(() => adminInfo.value.username || '-')
const avatarChar = computed(() => (nickname.value || 'A').charAt(0))

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
    ElMessage.success('已退出登录')
    router.replace('/login')
  } catch {
    // 取消
  }
}
</script>

<style scoped>
.layout-container {
  height: 100%;
}
.layout-aside {
  background-color: #001529;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.logo-text {
  color: #fff;
  font-size: 16px;
  font-weight: 700;
  white-space: nowrap;
}
.layout-menu {
  border-right: none;
  flex: 1;
}
.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  height: 60px;
}
.admin-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #303133;
}
.admin-avatar {
  background: #ff6a00;
  font-size: 14px;
}
.admin-name {
  font-size: 14px;
}
.layout-main {
  padding: 0;
  overflow-y: auto;
  background-color: #f5f7fa;
}
</style>
