<template>
  <view class="page">
    <!-- 买到 / 卖出 切换 -->
    <view class="tab-bar">
      <text :class="{ active: role === 'buyer' }" @click="switchRole('buyer')">我买到的</text>
      <text :class="{ active: role === 'seller' }" @click="switchRole('seller')">我卖出的</text>
    </view>

    <view v-for="o in orders" :key="o.id" class="order-card" @click="goDetail(o)">
      <image class="order-img" :src="o.goodsCover" mode="aspectFill" />
      <view class="order-info">
        <view class="order-title">{{ o.goodsTitle }}</view>
        <view class="order-price">¥{{ o.dealPrice }}</view>
        <view class="order-bottom">
          <text class="order-status" :class="'st-' + o.status">{{ statusText[o.status] }}</text>
          <text class="order-time">{{ o.createTime }}</text>
        </view>
        <!-- 操作按钮：按状态 × 角色渲染 -->
        <view class="order-actions" v-if="hasActions(o)" @click.stop>
          <text v-if="o.status === 1 && role === 'buyer'" class="action-btn primary" @click="doFinish(o)">确认完成面交</text>
          <text v-if="o.status === 0 && role === 'seller'" class="action-btn primary" @click="doFinish(o)">确认交易</text>
          <text v-if="[0, 1].includes(o.status)" class="action-btn plain" @click="doCancel(o)">取消订单</text>
          <text v-if="o.status === 2" class="action-btn primary" @click="goEvaluate(o)">评价</text>
          <text v-if="o.status === 2" class="action-btn danger" @click="goComplaint(o)">投诉</text>
        </view>
      </view>
    </view>

    <view v-if="!loading && orders.length === 0" class="empty">暂无订单</view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script>
import goodsApi from '@/api/goods.js'

export default {
  data() {
    return {
      role: 'buyer',
      orders: [],
      page: 1,
      loading: false,
      statusText: { 0: '待卖家确认', 1: '交易中', 2: '已完成', 3: '已取消', 4: '申诉中' }
    }
  },
  onShow() {
    this.loadOrders(true)
  },
  onReachBottom() {
    this.page++
    this.loadOrders()
  },
  methods: {
    hasActions(o) {
      return [0, 1, 2].includes(o.status)
    },
    switchRole(r) {
      this.role = r
      this.loadOrders(true)
    },
    async loadOrders(refresh = false) {
      if (refresh) this.page = 1
      this.loading = true
      try {
        const res = await goodsApi.myOrders({ role: this.role, page: this.page })
        const list = res.list || []
        this.orders = refresh ? list : [...this.orders, ...list]
      } finally {
        this.loading = false
      }
    },
    async doFinish(o) {
      uni.showModal({
        title: '提示',
        content: '确认面交已完成吗？确认后订单进入已完成状态。',
        success: async (res) => {
          if (!res.confirm) return
          await goodsApi.finishOrder(o.id)
          uni.showToast({ title: '交易完成', icon: 'success' })
          this.loadOrders(true)
        }
      })
    },
    doCancel(o) {
      uni.showModal({
        title: '提示',
        content: '确定取消该订单吗？',
        success: async (res) => {
          if (!res.confirm) return
          await goodsApi.cancelOrder(o.id, '用户主动取消')
          this.loadOrders(true)
        }
      })
    },
    goEvaluate(o) {
      const toUserId = this.role === 'buyer' ? o.sellerId : o.buyerId
      uni.navigateTo({
        url: `/pages/evaluate/index?orderType=1&orderId=${o.id}&toUserId=${toUserId}`
      })
    },
    goComplaint(o) {
      const defendantId = this.role === 'buyer' ? o.sellerId : o.buyerId
      uni.navigateTo({
        url: `/pages/complaint/index?orderType=1&orderId=${o.id}&defendantId=${defendantId}`
      })
    },
    goDetail(o) {
      uni.navigateTo({ url: '/pages/goods/detail?id=' + o.goodsId })
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
.order-card {
  display: flex;
  margin: 16rpx 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  .order-img { width: 160rpx; height: 160rpx; border-radius: 12rpx; background: #f2f3f5; }
  .order-info { flex: 1; margin-left: 20rpx; }
  .order-title { font-size: 28rpx; }
  .order-price { color: #ff6a00; font-weight: bold; margin-top: 12rpx; }
  .order-bottom {
    display: flex;
    justify-content: space-between;
    margin-top: 16rpx;
    font-size: 22rpx;
    .order-status { color: #FF6A00; &.st-3 { color: #969799; } &.st-4 { color: #ee0a24; } }
    .order-time { color: #c8c9cc; }
  }
  .order-actions {
    display: flex;
    justify-content: flex-end;
    gap: 16rpx;
    margin-top: 20rpx;
    .action-btn {
      font-size: 24rpx;
      padding: 8rpx 28rpx;
      border-radius: 32rpx;
      &.primary { background: #FF6A00; color: #fff; }
      &.plain { background: #f2f3f5; color: #646566; }
      &.danger { color: #ee0a24; border: 1rpx solid #ee0a24; }
    }
  }
}
.empty { text-align: center; padding: 120rpx 0; color: #969799; }
.loading { text-align: center; padding: 24rpx; color: #969799; font-size: 24rpx; }
</style>
