<template>
  <view class="page">
    <!-- 状态筛选 -->
    <view class="tab-bar">
      <text
        v-for="t in statusTabs"
        :key="t.value"
        :class="{ active: currentStatus === t.value }"
        @click="switchStatus(t.value)"
      >{{ t.name }}</text>
    </view>

    <view v-for="g in goodsList" :key="g.id" class="goods-card" @click="goDetail(g.id)">
      <image class="goods-img" :src="g.cover" mode="aspectFill" />
      <view class="goods-info">
        <view class="goods-title">{{ g.title }}</view>
        <view class="goods-price">¥{{ g.price }}</view>
        <view class="goods-bottom">
          <text class="goods-status" :class="'st-' + g.status">{{ statusText[g.status] }}</text>
          <view class="goods-actions" v-if="g.status === 1 || g.status === 3" @click.stop>
            <text v-if="g.status === 1" class="action-btn off" @click="toggleShelf(g, 3)">下架</text>
            <text v-if="g.status === 3" class="action-btn on" @click="toggleShelf(g, 1)">重新上架</text>
          </view>
        </view>
      </view>
    </view>

    <view v-if="!loading && goodsList.length === 0" class="empty">
      <view>还没有发布的闲置</view>
      <button class="empty-btn" @click="goPublish">去发布一件</button>
    </view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script>
import goodsApi from '@/api/goods.js'

export default {
  data() {
    return {
      statusTabs: [
        { value: undefined, name: '全部' },
        { value: 1, name: '在售' },
        { value: 2, name: '已售出' },
        { value: 3, name: '已下架' }
      ],
      statusText: { 0: '审核中', 1: '在售', 2: '已售出', 3: '已下架', 4: '违规下架' },
      currentStatus: undefined,
      goodsList: [],
      page: 1,
      loading: false
    }
  },
  onShow() {
    this.loadGoods(true)
  },
  onReachBottom() {
    this.page++
    this.loadGoods()
  },
  methods: {
    async loadGoods(refresh = false) {
      if (refresh) this.page = 1
      this.loading = true
      try {
        const res = await goodsApi.myList({ status: this.currentStatus, page: this.page })
        const list = res.list || []
        this.goodsList = refresh ? list : [...this.goodsList, ...list]
      } finally {
        this.loading = false
      }
    },
    switchStatus(s) {
      this.currentStatus = s
      this.loadGoods(true)
    },
    async toggleShelf(g, target) {
      const tip = target === 3 ? '确定下架该商品吗？' : '确定重新上架吗？'
      uni.showModal({
        title: '提示',
        content: tip,
        success: async (res) => {
          if (!res.confirm) return
          if (target === 3) await goodsApi.offShelf(g.id)
          else await goodsApi.onShelf(g.id)
          uni.showToast({ title: '操作成功', icon: 'success' })
          this.loadGoods(true)
        }
      })
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
    },
    goPublish() {
      uni.navigateTo({ url: '/pages/goods/publish' })
    }
  }
}
</script>

<style lang="scss">
.tab-bar {
  display: flex;
  gap: 48rpx;
  padding: 24rpx 32rpx;
  background: #fff;
  text {
    color: #969799;
    padding-bottom: 8rpx;
    &.active { color: #FF6A00; font-weight: bold; border-bottom: 4rpx solid #FF6A00; }
  }
}
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
    .goods-status {
      font-size: 22rpx;
      color: #FF6A00;
      &.st-2, &.st-3 { color: #969799; }
      &.st-4 { color: #ee0a24; }
    }
    .action-btn {
      font-size: 24rpx;
      padding: 6rpx 24rpx;
      border-radius: 28rpx;
      &.off { color: #ff976a; border: 1rpx solid #ff976a; }
      &.on { color: #FF6A00; border: 1rpx solid #FF6A00; }
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
