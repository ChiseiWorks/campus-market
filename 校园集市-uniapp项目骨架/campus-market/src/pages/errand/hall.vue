<template>
  <view class="page">
    <!-- 筛选/排序条（白底吸顶） -->
    <view class="filter-bar">
      <!-- 类型筛选 -->
      <scroll-view scroll-x class="type-scroll" :show-scrollbar="false">
        <view
          v-for="t in types"
          :key="t.id"
          class="type-item"
          :class="{ active: currentType === t.id }"
          @click="switchType(t.id)"
        >
          {{ t.name }}
        </view>
      </scroll-view>

      <!-- 排序 -->
      <view class="sort-bar">
        <text :class="{ active: sort === 'latest' }" @click="switchSort('latest')">最新发布</text>
        <text :class="{ active: sort === 'reward' }" @click="switchSort('reward')">悬赏最高</text>
      </view>
    </view>

    <!-- 订单卡片列表 -->
    <view
      v-for="order in orderList"
      :key="order.id"
      class="order-card"
      @click="goDetail(order.id)"
    >
      <view class="order-head">
        <text class="order-type">{{ typeText[order.type] }}</text>
        <text class="order-reward">¥{{ order.reward }}</text>
      </view>
      <view class="order-route">
        <view class="route-item">
          <text class="route-dot pickup"></text>
          <text>取：{{ order.pickupLocationName }}</text>
        </view>
        <view class="route-item">
          <text class="route-dot deliver"></text>
          <text>送：{{ order.deliveryLocationName }}</text>
        </view>
      </view>
      <view class="order-foot">
        <text class="order-time">{{ order.expectTime }} 前 · {{ order.timeAgo }}</text>
        <button class="grab-btn" size="mini" @click.stop="grab(order)">马上抢单</button>
      </view>
    </view>

    <view v-if="!loading && orderList.length === 0" class="empty">
      暂时没有悬赏单，去发一单？
    </view>
    <view v-if="loading" class="loading">加载中...</view>
  </view>
</template>

<script>
import errandApi, { ERRAND_TYPE } from '@/api/errand.js'
import { lock } from '@/utils/index.js'

export default {
  data() {
    return {
      types: [
        { id: 0, name: '全部' },
        { id: 1, name: '取快递' },
        { id: 2, name: '代买餐' },
        { id: 3, name: '代送物品' },
        { id: 4, name: '其他' }
      ],
      typeText: ERRAND_TYPE,
      currentType: 0,
      sort: 'latest',
      orderList: [],
      page: 1,
      loading: false
    }
  },
  onLoad() {
    this.loadOrders(true)
    // 防连点包装：抢单按钮 1 秒内只允许触发一次
    this.grab = lock(this.grab)
  },
  onPullDownRefresh() {
    this.loadOrders(true).then(() => uni.stopPullDownRefresh())
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
        const res = await errandApi.hall({
          type: this.currentType || undefined,
          sort: this.sort,
          page: this.page
        })
        const list = res.list || []
        this.orderList = refresh ? list : [...this.orderList, ...list]
      } finally {
        this.loading = false
      }
    },
    switchType(id) {
      this.currentType = id
      this.loadOrders(true)
    },
    switchSort(s) {
      this.sort = s
      this.loadOrders(true)
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/errand/detail?id=' + id })
    },
    /** 抢单：先校验认证+跑男资格，再调接口；后端 CAS 保证并发安全 */
    async grab(order) {
      if (!this.$checkAuth()) return
      if (!this.$store.getters['user/isRunner']) {
        uni.showModal({
          title: '提示',
          content: '完成跑男认证后才能接单，去认证？',
          confirmText: '去认证',
          success: (res) => {
            if (res.confirm) uni.navigateTo({ url: '/pages/user/auth?type=runner' })
          }
        })
        return
      }
      try {
        await errandApi.accept(order.id)
        uni.showToast({ title: '抢单成功', icon: 'success' })
        this.goDetail(order.id)
      } catch (e) {
        // "手慢了"等提示已统一弹出，这里刷新列表即可
        this.loadOrders(true)
      }
    }
  }
}
</script>

<style lang="scss">
.type-scroll {
  white-space: nowrap;
  padding: 16rpx;
  background: #fff;
  .type-item {
    display: inline-block;
    padding: 12rpx 32rpx;
    margin-right: 16rpx;
    border-radius: 28rpx;
    background: #f2f3f5;
    color: #646566;
    &.active { background: #FF6A00; color: #fff; font-weight: bold; }
  }
}
.sort-bar {
  display: flex;
  gap: 48rpx;
  padding: 20rpx 32rpx;
  color: #969799;
  font-size: 26rpx;
  .active { color: #FF6A00; font-weight: bold; }
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
    .order-reward { color: #ff6a00; font-weight: bold; font-size: 36rpx; }
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
    align-items: center;
    .order-time { color: #969799; font-size: 22rpx; }
    .grab-btn {
      background: linear-gradient(135deg, #FF8F1F, #FF5000);
      color: #fff;
      border-radius: 32rpx;
      padding: 0 32rpx;
      font-weight: bold;
      box-shadow: 0 4rpx 12rpx rgba(255, 106, 0, 0.3);
    }
  }
}
.empty { text-align: center; padding: 120rpx 0; color: #969799; }
.loading { text-align: center; padding: 24rpx; color: #969799; font-size: 24rpx; }
</style>
