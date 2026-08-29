<template>
  <view class="page">
    <!-- 我发布的 / 我接的 -->
    <view class="tab-bar">
      <text :class="{ active: role === 'publish' }" @click="switchRole('publish')">我发布的</text>
      <text :class="{ active: role === 'accept' }" @click="switchRole('accept')">我接的</text>
    </view>

    <!-- 状态筛选 -->
    <scroll-view scroll-x class="status-scroll" :show-scrollbar="false">
      <text
        v-for="t in statusTabs"
        :key="t.value"
        class="status-item"
        :class="{ active: currentStatus === t.value }"
        @click="switchStatus(t.value)"
      >{{ t.name }}</text>
    </scroll-view>

    <view v-for="o in orderList" :key="o.id" class="order-card" @click="goDetail(o.id)">
      <view class="order-head">
        <text class="order-type">{{ typeText[o.type] }}</text>
        <text class="order-reward">¥{{ o.reward }}</text>
      </view>
      <view class="order-route">
        <view class="route-item">
          <text class="route-dot pickup"></text>
          <text>取：{{ o.pickupLocationName }}</text>
        </view>
        <view class="route-item">
          <text class="route-dot deliver"></text>
          <text>送：{{ o.deliveryLocationName }}</text>
        </view>
      </view>
      <view class="order-foot">
        <text class="order-status" :class="'st-' + o.status">{{ statusText[o.status] }}</text>
        <text class="order-time">{{ o.createTime }}</text>
      </view>
    </view>

    <view v-if="!loading && orderList.length === 0" class="empty">
      {{ role === 'publish' ? '还没有发布过跑腿单' : '还没有接过单，去大厅看看？' }}
    </view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script>
import errandApi, { ERRAND_TYPE, ERRAND_STATUS_TEXT } from '@/api/errand.js'

export default {
  data() {
    return {
      role: 'publish',
      typeText: ERRAND_TYPE,
      statusText: ERRAND_STATUS_TEXT,
      statusTabs: [
        { value: undefined, name: '全部' },
        { value: 0, name: '待接单' },
        { value: 1, name: '已接单' },
        { value: 2, name: '配送中' },
        { value: 3, name: '待确认' },
        { value: 4, name: '已完成' },
        { value: 5, name: '已取消' },
        { value: 6, name: '申诉中' }
      ],
      currentStatus: undefined,
      orderList: [],
      page: 1,
      loading: false
    }
  },
  onLoad(options) {
    if (options.role === 'accept') this.role = 'accept'
    this.loadOrders(true)
  },
  onShow() {
    if (this.orderList.length > 0) this.loadOrders(true)
  },
  onReachBottom() {
    this.page++
    this.loadOrders()
  },
  methods: {
    async loadOrders(refresh = false) {
      if (refresh) this.page = 1
      this.loading = true
      try {
        const api = this.role === 'publish' ? errandApi.myPublish : errandApi.myAccept
        const res = await api({ status: this.currentStatus, page: this.page })
        const list = res.list || []
        this.orderList = refresh ? list : [...this.orderList, ...list]
      } finally {
        this.loading = false
      }
    },
    switchRole(r) {
      this.role = r
      this.loadOrders(true)
    },
    switchStatus(s) {
      this.currentStatus = s
      this.loadOrders(true)
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/errand/detail?id=' + id })
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
.status-scroll {
  white-space: nowrap;
  padding: 16rpx;
  background: #fff;
  border-top: 1rpx solid #f2f3f5;
  .status-item {
    display: inline-block;
    padding: 8rpx 24rpx;
    margin-right: 12rpx;
    border-radius: 24rpx;
    background: #f2f3f5;
    color: #646566;
    font-size: 24rpx;
    &.active { background: #FF6A00; color: #fff; }
  }
}
.order-card {
  margin: 16rpx 24rpx;
  padding: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  .order-head {
    display: flex;
    justify-content: space-between;
    .order-type { font-weight: bold; }
    .order-reward { color: #ff6a00; font-weight: bold; }
  }
  .order-route {
    margin: 16rpx 0;
    color: #646566;
    font-size: 26rpx;
    .route-item { display: flex; align-items: center; margin: 8rpx 0; }
    .route-dot {
      width: 16rpx; height: 16rpx; border-radius: 50%;
      margin-right: 16rpx;
      &.pickup { background: #ff976a; }
      &.deliver { background: #FF6A00; }
    }
  }
  .order-foot {
    display: flex;
    justify-content: space-between;
    font-size: 22rpx;
    .order-status { color: #FF6A00; &.st-5 { color: #969799; } &.st-6 { color: #ee0a24; } }
    .order-time { color: #c8c9cc; }
  }
}
.empty { text-align: center; padding: 120rpx 0; color: #969799; }
.loading { text-align: center; padding: 24rpx; color: #969799; font-size: 24rpx; }
</style>
