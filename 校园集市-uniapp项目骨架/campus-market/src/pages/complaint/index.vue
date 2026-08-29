<template>
  <view class="page">
    <view class="tip-box">投诉提交后将进入平台仲裁队列，请如实描述并提供证据。</view>

    <view class="form">
      <!-- 投诉类型 -->
      <view class="form-item">
        <view class="label">投诉类型</view>
        <view class="tag-group">
          <text
            v-for="(name, id) in typeText"
            :key="id"
            class="tag"
            :class="{ active: form.type === Number(id) }"
            @click="form.type = Number(id)"
          >{{ name }}</text>
        </view>
      </view>

      <!-- 投诉描述 -->
      <view class="form-item">
        <textarea
          v-model="form.content"
          maxlength="500"
          placeholder="请描述事情经过，如：对方接单后失联两小时..."
        />
      </view>

      <!-- 证据图片 -->
      <view class="form-item">
        <view class="label">证据图片（聊天记录/照片，最多 3 张）</view>
        <view class="img-grid">
          <image
            v-for="(img, i) in form.evidence"
            :key="i"
            :src="img"
            mode="aspectFill"
            class="img-item"
            @click="preview(i)"
          />
          <view v-if="form.evidence.length < 3" class="img-add" @click="chooseImage">+</view>
        </view>
      </view>

      <button class="submit-btn" :disabled="submitting" @click="doSubmit">提交投诉</button>
    </view>
  </view>
</template>

<script>
import complaintApi, { COMPLAINT_TYPE } from '@/api/complaint.js'
import commonApi from '@/api/common.js'

export default {
  data() {
    return {
      typeText: COMPLAINT_TYPE,
      form: {
        orderType: 2,   // 1闲置 2跑腿，由入口参数决定
        orderId: null,
        defendantId: null,
        type: 1,
        content: '',
        evidence: []
      },
      submitting: false
    }
  },
  onLoad(options) {
    if (options.orderType) this.form.orderType = Number(options.orderType)
    if (options.orderId) this.form.orderId = Number(options.orderId)
    if (options.defendantId) this.form.defendantId = Number(options.defendantId)
    // 无入口参数时展示我的投诉列表的场景由 complaint/my 承担，此页专注提交
    if (!this.form.orderId) {
      uni.showToast({ title: '请从订单详情页发起投诉', icon: 'none' })
    }
  },
  methods: {
    chooseImage() {
      uni.chooseImage({
        count: 3 - this.form.evidence.length,
        success: async (res) => {
          // 尝试上传，失败则保留本地路径降级（骨架阶段后端可能未就绪）
          for (const path of res.tempFilePaths) {
            try {
              const url = await commonApi.upload(path)
              this.form.evidence.push(url)
            } catch (e) {
              this.form.evidence.push(path)
            }
          }
        }
      })
    },
    preview(i) {
      uni.previewImage({ urls: this.form.evidence, current: i })
    },
    async doSubmit() {
      if (!this.form.orderId) return uni.showToast({ title: '缺少订单信息', icon: 'none' })
      if (!this.form.content.trim()) return uni.showToast({ title: '请填写投诉描述', icon: 'none' })
      this.submitting = true
      try {
        await complaintApi.submit(this.form)
        uni.showToast({ title: '已提交，等待平台处理', icon: 'success' })
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
  background: #fff7e8;
  color: #ff976a;
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
  .label { margin-bottom: 16rpx; }
  .tag-group {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    .tag {
      padding: 8rpx 28rpx;
      border-radius: 28rpx;
      background: #f2f3f5;
      color: #646566;
      font-size: 24rpx;
      &.active { background: #ee0a24; color: #fff; }
    }
  }
}
.img-grid {
  display: flex;
  flex-wrap: wrap;
  .img-item, .img-add {
    width: 160rpx; height: 160rpx;
    border-radius: 12rpx;
    margin: 0 16rpx 16rpx 0;
    background: #f2f3f5;
  }
  .img-add {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 64rpx;
    color: #c8c9cc;
    border: 1rpx dashed #dcdee0;
  }
}
.submit-btn { margin-top: 24rpx; background: #ee0a24; color: #fff; border-radius: 48rpx; }
</style>
