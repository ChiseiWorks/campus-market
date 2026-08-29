<template>
  <view class="page">
    <view class="logo-area">
      <view class="logo-text">校园集市</view>
      <view class="logo-sub">校内闲置交易 · 跑腿互助</view>
    </view>

    <view class="form">
      <view class="form-item">
        <input v-model="form.phone" type="number" maxlength="11" placeholder="手机号" />
      </view>
      <view class="form-item">
        <input v-model="form.password" password placeholder="密码" />
      </view>
      <button class="submit-btn" :disabled="submitting" @click="doLogin">
        {{ submitting ? '登录中...' : '登 录' }}
      </button>
      <view class="form-link" @click="goRegister">没有账号？去注册</view>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/user.js'

export default {
  data() {
    return {
      form: { phone: '', password: '' },
      submitting: false
    }
  },
  methods: {
    async doLogin() {
      if (!/^1\d{10}$/.test(this.form.phone)) {
        return uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
      }
      if (!this.form.password) {
        return uni.showToast({ title: '请输入密码', icon: 'none' })
      }
      this.submitting = true
      try {
        const res = await userApi.login(this.form)
        await this.$store.dispatch('user/login', {
          token: res.token,
          userInfo: res.userInfo
        })
        uni.showToast({ title: '登录成功', icon: 'success' })
        setTimeout(() => uni.switchTab({ url: '/pages/index/index' }), 800)
      } finally {
        this.submitting = false
      }
    },
    goRegister() {
      uni.navigateTo({ url: '/pages/user/register' })
    }
  }
}
</script>

<style lang="scss">
.page { padding: 48rpx; }
.logo-area {
  text-align: center;
  margin: 120rpx 0 80rpx;
  .logo-text { font-size: 56rpx; font-weight: bold; color: #FF6A00; }
  .logo-sub { margin-top: 16rpx; color: #969799; font-size: 26rpx; }
}
.form-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}
.submit-btn {
  margin-top: 48rpx;
  background: #FF6A00;
  color: #fff;
  border-radius: 48rpx;
}
.form-link {
  margin-top: 32rpx;
  text-align: center;
  color: #FF6A00;
  font-size: 26rpx;
}
</style>
