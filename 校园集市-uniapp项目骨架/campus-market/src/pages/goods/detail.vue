<template>
  <view class="page" v-if="goods">
    <!-- 图片轮播 -->
    <swiper class="banner" indicator-dots indicator-active-color="#FF6A00">
      <swiper-item v-for="(img, i) in goods.images" :key="i">
        <image :src="img" mode="aspectFill" class="banner-img" @click="preview(i)" />
      </swiper-item>
    </swiper>

    <!-- 价格标题 -->
    <view class="head-card">
      <view class="price-line">
        <text class="price">¥{{ goods.price }}</text>
        <text class="original-price" v-if="goods.originalPrice">¥{{ goods.originalPrice }}</text>
        <text class="quality-tag">{{ qualityText[goods.quality] }}</text>
      </view>
      <view class="title">{{ goods.title }}</view>
      <view class="meta-line">
        <text>📍 面交：{{ goods.locationName }}</text>
        <text>{{ goods.viewCount }}浏览 · {{ goods.wantCount }}人想要</text>
      </view>
    </view>

    <!-- 卖家卡片 -->
    <view class="seller-card" @click="goSeller">
      <image class="seller-avatar" :src="goods.seller.avatar" mode="aspectFill" />
      <view class="seller-info">
        <view class="seller-name">{{ goods.seller.nickname }}</view>
        <view class="seller-sub">已认证 · 信用分 {{ goods.seller.creditScore }} · 在售{{ goods.seller.sellingCount }}件</view>
      </view>
      <text class="arrow">›</text>
    </view>

    <!-- 商品描述 -->
    <view class="desc-card">
      <view class="desc-title">商品描述</view>
      <view class="desc-content">{{ goods.description }}</view>
    </view>

    <!-- 底部操作栏 -->
    <view class="action-bar">
      <view class="icon-btn" @click="toggleFav">
        <view>{{ favored ? '★' : '☆' }}</view>
        <view class="icon-text">{{ favored ? '已收藏' : '收藏' }}</view>
      </view>
      <view class="icon-btn" @click="chat">
        <view>💬</view>
        <view class="icon-text">我想要</view>
      </view>
      <button class="buy-btn" :disabled="submitting" @click="buy">立即交易</button>
    </view>
  </view>
</template>

<script>
import goodsApi from '@/api/goods.js'

export default {
  data() {
    return {
      goodsId: null,
      goods: null,
      favored: false,
      submitting: false,
      qualityText: { 1: '全新', 2: '九成新', 3: '八成新', 4: '有使用痕迹' }
    }
  },
  onLoad(options) {
    this.goodsId = options.id
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      this.goods = await goodsApi.detail(this.goodsId)
      this.favored = this.goods.favored
    },
    preview(i) {
      uni.previewImage({ urls: this.goods.images, current: i })
    },
    async toggleFav() {
      if (!this.$checkAuth()) return
      this.favored = await goodsApi.toggleFav(this.goodsId)
    },
    chat() {
      if (!this.$checkAuth()) return
      const seller = this.goods.seller
      if (seller.id === this.$store.state.user.userInfo.id) {
        return uni.showToast({ title: '这是你自己发布的商品', icon: 'none' })
      }
      uni.navigateTo({
        url: '/pages/message/chat?userId=' + seller.id +
          '&goodsId=' + this.goodsId +
          '&nickname=' + encodeURIComponent(seller.nickname || '同学') +
          '&avatar=' + encodeURIComponent(seller.avatar || '')
      })
    },
    /** 立即交易：生成订单并锁定商品；后端校验防重复下单 */
    async buy() {
      if (!this.$checkAuth()) return
      if (this.goods.userId === this.$store.state.user.userInfo.id) {
        return uni.showToast({ title: '不能购买自己的商品', icon: 'none' })
      }
      this.submitting = true
      try {
        const order = await goodsApi.createOrder(this.goodsId, '')
        uni.showToast({ title: '已锁定，请联系卖家面交', icon: 'success' })
        setTimeout(() => uni.navigateTo({ url: '/pages/order/list' }), 800)
      } finally {
        this.submitting = false
      }
    },
    goSeller() {
      // TODO: 卖家主页（展示在售商品 + 收到的评价）
      uni.showToast({ title: '卖家主页开发中', icon: 'none' })
    }
  }
}
</script>

<style lang="scss">
.banner { height: 750rpx; }
.banner-img { width: 100%; height: 100%; background: #f2f3f5; }
.head-card, .seller-card, .desc-card {
  margin: 16rpx 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 16rpx;
}
.price-line { display: flex; align-items: baseline; gap: 16rpx; }
.price { color: #ff6a00; font-size: 48rpx; font-weight: bold; }
.original-price { color: #c8c9cc; text-decoration: line-through; font-size: 24rpx; }
.quality-tag {
  margin-left: auto;
  font-size: 20rpx;
  color: #FF6A00;
  background: #FFF3E8;
  padding: 4rpx 16rpx;
  border-radius: 20rpx;
}
.title { font-size: 32rpx; margin-top: 16rpx; }
.meta-line {
  display: flex;
  justify-content: space-between;
  color: #969799;
  font-size: 22rpx;
  margin-top: 16rpx;
}
.seller-card { display: flex; align-items: center; }
.seller-avatar { width: 88rpx; height: 88rpx; border-radius: 50%; background: #f2f3f5; }
.seller-info { flex: 1; margin-left: 20rpx; }
.seller-name { font-weight: bold; }
.seller-sub { color: #969799; font-size: 22rpx; margin-top: 8rpx; }
.arrow { color: #c8c9cc; }
.desc-title { font-weight: bold; margin-bottom: 16rpx; }
.desc-content { color: #646566; font-size: 26rpx; line-height: 1.6; }
.action-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  display: flex;
  align-items: center;
  padding: 12rpx 24rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);
  .icon-btn {
    width: 120rpx;
    text-align: center;
    color: #646566;
    .icon-text { font-size: 20rpx; }
  }
  .buy-btn {
    flex: 1;
    background: #FF6A00;
    color: #fff;
    border-radius: 44rpx;
  }
}
</style>
