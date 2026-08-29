<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-header">
        <el-icon :size="36" color="#FF6A00"><ShoppingBag /></el-icon>
        <h1 class="login-title">校园集市 · 管理后台</h1>
        <p class="login-subtitle">Campus Market Admin</p>
      </div>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        size="large"
        @keyup.enter="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入管理员账号"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="请输入密码"
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-tip">默认账号：admin / admin123</div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '../api'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: 'admin',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入管理员账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const data = await login({
      username: form.username,
      password: form.password
    })
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_info', JSON.stringify(data.adminInfo || {}))
    ElMessage.success('登录成功')
    router.replace('/dashboard')
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #fff3e8 0%, #ffe0c2 50%, #ffcda1 100%);
}
.login-card {
  width: 400px;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(255, 106, 0, 0.15);
}
.login-header {
  text-align: center;
  margin-bottom: 24px;
}
.login-title {
  margin: 12px 0 4px;
  font-size: 22px;
  color: #303133;
}
.login-subtitle {
  margin: 0;
  font-size: 12px;
  color: #909399;
}
.login-btn {
  width: 100%;
}
.login-tip {
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
}
</style>
