<template>
  <view class="page">
    <view class="choice-card" @click="goGoodsPublish">
      <view class="choice-icon">🛍</view>
      <view class="choice-title">发闲置</view>
      <view class="choice-desc">教材、数码、服饰... 挂出来卖给同学</view>
    </view>

    <view class="choice-card" @click="goErrandPublish">
      <view class="choice-icon">🏃</view>
      <view class="choice-title">发跑腿</view>
      <view class="choice-desc">取快递、代买饭，悬赏找人帮你跑</view>
    </view>
  </view>
</template>

<script>
export default {
  methods: {
    goGoodsPublish() {
      if (!this.$checkAuth()) return
      uni.navigateTo({ url: '/pages/goods/publish' })
    },
    goErrandPublish() {
      if (!this.$checkAuth()) return
      uni.navigateTo({ url: '/pages/errand/publish' })
    }
  }
}
</script>

<style lang="scss">
.page { padding: 32rpx; }
.choice-card {
  background: #fff;
  border-radius: 24rpx;
  padding: 64rpx 32rpx;
  text-align: center;
  margin-bottom: 32rpx;
  .choice-icon { font-size: 80rpx; }
  .choice-title { font-size: 36rpx; font-weight: bold; margin-top: 24rpx; }
  .choice-desc { color: #969799; font-size: 24rpx; margin-top: 12rpx; }
}
</style>
