<template>
  <view class="page">
    <!-- 星级评分 -->
    <view class="score-card">
      <view class="score-label">{{ scoreText[form.score] }}</view>
      <view class="stars">
        <text
          v-for="i in 5"
          :key="i"
          class="star"
          :class="{ on: i <= form.score }"
          @click="form.score = i"
        >★</text>
      </view>
    </view>

    <!-- 快捷标签 -->
    <view class="form-item">
      <view class="label">快捷评价（可多选）</view>
      <view class="tag-group">
        <text
          v-for="t in tagPool"
          :key="t"
          class="tag"
          :class="{ active: form.tags.includes(t) }"
          @click="toggleTag(t)"
        >{{ t }}</text>
      </view>
    </view>

    <!-- 文字评价 -->
    <view class="form-item">
      <textarea
        v-model="form.content"
        maxlength="500"
        placeholder="说说这次交易的体验（选填）..."
      />
    </view>

    <button class="submit-btn" :disabled="submitting" @click="doSubmit">提交评价</button>
  </view>
</template>

<script>
import evaluateApi from '@/api/evaluate.js'

export default {
  data() {
    return {
      scoreText: { 1: '非常差', 2: '不太行', 3: '一般般', 4: '挺满意', 5: '非常棒' },
      tagPool: ['准时', '描述相符', '态度好', '回复快', '包装仔细'],
      form: {
        orderType: 2,   // 1闲置 2跑腿
        orderId: null,
        toUserId: null,
        score: 5,
        tags: [],
        content: ''
      },
      submitting: false
    }
  },
  onLoad(options) {
    if (options.orderType) this.form.orderType = Number(options.orderType)
    if (options.orderId) this.form.orderId = Number(options.orderId)
    if (options.toUserId) this.form.toUserId = Number(options.toUserId)
  },
  methods: {
    toggleTag(t) {
      const i = this.form.tags.indexOf(t)
      if (i >= 0) this.form.tags.splice(i, 1)
      else this.form.tags.push(t)
    },
    async doSubmit() {
      if (!this.form.orderId) return uni.showToast({ title: '缺少订单信息', icon: 'none' })
      this.submitting = true
      try {
        await evaluateApi.submit(this.form)
        uni.showToast({ title: '评价成功', icon: 'success' })
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
.score-card {
  background: #fff;
  border-radius: 16rpx;
  padding: 48rpx;
  text-align: center;
  margin-bottom: 24rpx;
  .score-label { color: #646566; font-size: 26rpx; }
  .stars {
    margin-top: 24rpx;
    .star {
      font-size: 72rpx;
      color: #ebedf0;
      margin: 0 12rpx;
      &.on { color: #ffb400; }
    }
  }
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
      &.active { background: #FF6A00; color: #fff; }
    }
  }
}
.submit-btn { margin-top: 24rpx; background: #FF6A00; color: #fff; border-radius: 48rpx; }
</style>
