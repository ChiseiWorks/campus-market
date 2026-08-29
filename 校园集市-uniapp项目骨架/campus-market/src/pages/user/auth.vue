<template>
  <view class="page">
    <view class="tip-box">
      {{ isRunnerApply ? '跑男认证通过后才可以接单，请如实填写。' : '认证信息仅用于平台审核，保障校内交易安全。' }}
    </view>

    <view class="form">
      <view class="form-item">
        <input v-model="form.studentNo" maxlength="20" placeholder="学号" />
      </view>
      <view class="form-item">
        <input v-model="form.realName" maxlength="16" placeholder="真实姓名" />
      </view>
      <view class="form-item">
        <input v-model="form.college" maxlength="32" placeholder="学院/系（选填）" />
      </view>
      <view class="form-item">
        <input v-model="form.dormBuilding" maxlength="32" placeholder="宿舍楼（如：3号宿舍楼）" />
      </view>

      <!-- 认证材料上传 -->
      <view class="upload-area" @click="chooseImage">
        <image v-if="form.materialUrl" :src="form.materialUrl" mode="aspectFill" class="material-img" />
        <view v-else class="upload-placeholder">
          <view class="upload-plus">+</view>
          <view class="upload-text">上传校园卡 / 教务系统截图</view>
        </view>
      </view>

      <button class="submit-btn" :disabled="submitting" @click="doSubmit">提交认证</button>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/user.js'

export default {
  data() {
    return {
      isRunnerApply: false,
      form: { studentNo: '', realName: '', college: '', dormBuilding: '', materialUrl: '' },
      submitting: false
    }
  },
  onLoad(options) {
    this.isRunnerApply = options.type === 'runner'
    if (this.isRunnerApply) uni.setNavigationBarTitle({ title: '跑男认证' })
  },
  methods: {
    chooseImage() {
      uni.chooseImage({
        count: 1,
        success: async (res) => {
          const path = res.tempFilePaths[0]
          try {
            // 上传到后端 /file/upload，拿到可访问 URL
            const commonApi = (await import('@/api/common.js')).default
            this.form.materialUrl = await commonApi.upload(path)
          } catch (e) {
            // 后端未就绪时降级为本地预览路径
            this.form.materialUrl = path
          }
        }
      })
    },
    async doSubmit() {
      if (!this.form.studentNo || !this.form.realName) {
        return uni.showToast({ title: '学号和姓名必填', icon: 'none' })
      }
      if (!this.form.materialUrl) {
        return uni.showToast({ title: '请上传认证材料', icon: 'none' })
      }
      this.submitting = true
      try {
        await userApi.submitAuth({ ...this.form, type: this.isRunnerApply ? 2 : 1 })
        uni.showToast({ title: '已提交，等待审核', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss">
.page { padding: 24rpx; }
.tip-box {
  background: #FFF3E8;
  color: #FF6A00;
  font-size: 24rpx;
  padding: 20rpx 28rpx;
  border-radius: 16rpx;
  margin-bottom: 24rpx;
}
.form-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
}
.upload-area {
  background: #fff;
  border-radius: 16rpx;
  height: 360rpx;
  display: flex;
  align-items: center;
  justify-content: center;
  .material-img { width: 100%; height: 100%; border-radius: 16rpx; }
  .upload-placeholder { text-align: center; color: #969799; }
  .upload-plus { font-size: 80rpx; }
  .upload-text { font-size: 24rpx; margin-top: 12rpx; }
}
.submit-btn { margin-top: 48rpx; background: #FF6A00; color: #fff; border-radius: 48rpx; }
</style>
