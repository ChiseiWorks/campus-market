<template>
  <view class="page">
    <view class="form">
      <view class="form-item">
        <input v-model="form.phone" type="number" maxlength="11" placeholder="手机号" />
      </view>
      <view class="form-item sms-item">
        <input v-model="form.smsCode" type="number" maxlength="6" placeholder="短信验证码" />
        <button class="sms-btn" size="mini" :disabled="countdown > 0" @click="sendCode">
          {{ countdown > 0 ? countdown + 's' : '获取验证码' }}
        </button>
      </view>
      <view class="form-item">
        <input v-model="form.password" password placeholder="设置密码（6-20位）" />
      </view>
      <view class="form-item">
        <input v-model="form.nickname" maxlength="16" placeholder="昵称（如：张同学）" />
      </view>
      <button class="submit-btn" :disabled="submitting" @click="doRegister">注 册</button>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/user.js'

export default {
  data() {
    return {
      form: { phone: '', smsCode: '', password: '', nickname: '' },
      countdown: 0,
      submitting: false
    }
  },
  methods: {
    async sendCode() {
      if (!/^1\d{10}$/.test(this.form.phone)) {
        return uni.showToast({ title: '请先输入正确的手机号', icon: 'none' })
      }
      await userApi.sendSms(this.form.phone)
      uni.showToast({ title: '验证码已发送', icon: 'none' })
      this.countdown = 60
      const timer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) clearInterval(timer)
      }, 1000)
    },
    async doRegister() {
      if (!/^1\d{10}$/.test(this.form.phone)) {
        return uni.showToast({ title: '手机号格式不正确', icon: 'none' })
      }
      if (!this.form.smsCode) return uni.showToast({ title: '请输入验证码', icon: 'none' })
      if (this.form.password.length < 6 || this.form.password.length > 20) {
        return uni.showToast({ title: '密码需为6-20位', icon: 'none' })
      }
      this.submitting = true
      try {
        await userApi.register(this.form)
        uni.showToast({ title: '注册成功，请登录', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss">
.page { padding: 48rpx; }
.form-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  &.sms-item { display: flex; align-items: center; }
  .sms-btn { background: #FF6A00; color: #fff; border-radius: 32rpx; }
}
.submit-btn { margin-top: 48rpx; background: #FF6A00; color: #fff; border-radius: 48rpx; }
</style>
