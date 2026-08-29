<template>
  <view class="page">
    <view v-for="g in favList" :key="g.id" class="goods-card" @click="goDetail(g.goodsId || g.id)">
      <image class="goods-img" :src="g.cover" mode="aspectFill" />
      <view class="goods-info">
        <view class="goods-title">{{ g.title }}</view>
        <view class="goods-price">¥{{ g.price }}</view>
        <view class="goods-bottom">
          <text class="goods-status" :class="{ off: g.status !== 1 }">
            {{ g.status === 1 ? '在售' : '已下架/售出' }}
          </text>
          <text class="unfav-btn" @click.stop="unfav(g)">取消收藏</text>
        </view>
      </view>
    </view>

    <view v-if="!loading && favList.length === 0" class="empty">
      <view>还没有收藏任何商品</view>
      <button class="empty-btn" @click="goHome">去逛逛</button>
    </view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script>
import goodsApi from '@/api/goods.js'

export default {
  data() {
    return {
      favList: [],
      page: 1,
      loading: false
    }
  },
  onShow() {
    this.loadFavs(true)
  },
  onReachBottom() {
    this.page++
    this.loadFavs()
  },
  methods: {
    async loadFavs(refresh = false) {
      if (refresh) this.page = 1
      this.loading = true
      try {
        const res = await goodsApi.myFavorites(this.page)
        const list = res.list || []
        this.favList = refresh ? list : [...this.favList, ...list]
      } finally {
        this.loading = false
      }
    },
    async unfav(g) {
      await goodsApi.toggleFav(g.goodsId || g.id)
      uni.showToast({ title: '已取消收藏', icon: 'none' })
      this.loadFavs(true)
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
    },
    goHome() {
      uni.switchTab({ url: '/pages/index/index' })
    }
  }
}
</script>

<style lang="scss">
.goods-card {
  display: flex;
  margin: 16rpx 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  .goods-img { width: 160rpx; height: 160rpx; border-radius: 12rpx; background: #f2f3f5; }
  .goods-info { flex: 1; margin-left: 20rpx; }
  .goods-title { font-size: 28rpx; }
  .goods-price { color: #ff6a00; font-weight: bold; margin-top: 12rpx; }
  .goods-bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 16rpx;
    .goods-status { font-size: 22rpx; color: #FF6A00; &.off { color: #969799; } }
    .unfav-btn {
      font-size: 24rpx;
      color: #969799;
      border: 1rpx solid #dcdee0;
      padding: 6rpx 24rpx;
      border-radius: 28rpx;
    }
  }
}
.empty { text-align: center; padding: 120rpx 0; color: #969799; }
.empty-btn {
  margin: 32rpx auto;
  width: 320rpx;
  background: #FF6A00;
  color: #fff;
  border-radius: 40rpx;
}
.loading { text-align: center; padding: 24rpx; color: #969799; font-size: 24rpx; }
</style>
