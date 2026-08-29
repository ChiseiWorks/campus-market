<template>
  <view class="page">
    <!-- 当前信用分 -->
    <view class="score-card">
      <view class="score-num">{{ creditScore }}</view>
      <view class="score-label">当前信用分（满分 100）</view>
      <view class="score-tip" v-if="creditScore < 60">信用分低于 60 将被限制发布与接单</view>
    </view>

    <!-- 变动明细 -->
    <view class="log-list">
      <view class="log-title">变动明细</view>
      <view v-for="log in logs" :key="log.id" class="log-item">
        <view class="log-info">
          <view class="log-reason">{{ log.reason }}</view>
          <view class="log-time">{{ log.createTime }}</view>
        </view>
        <view class="log-right">
          <text class="log-value" :class="log.changeValue > 0 ? 'plus' : 'minus'">
            {{ log.changeValue > 0 ? '+' : '' }}{{ log.changeValue }}
          </text>
          <text class="log-balance">余 {{ log.balance }}</text>
        </view>
      </view>
      <view v-if="!loading && logs.length === 0" class="empty">暂无信用变动记录</view>
      <view v-if="loading" class="loading">加载中...</view>
    </view>
  </view>
</template>

<script>
import userApi from '@/api/user.js'
import { mapGetters } from 'vuex'

export default {
  data() {
    return {
      logs: [],
      page: 1,
      loading: false
    }
  },
  computed: {
    ...mapGetters('user', ['creditScore'])
  },
  onLoad() {
    this.loadLogs(true)
  },
  onReachBottom() {
    this.page++
    this.loadLogs()
  },
  methods: {
    async loadLogs(refresh = false) {
      if (refresh) this.page = 1
      this.loading = true
      try {
        const res = await userApi.creditLogs(this.page)
        const list = res.list || []
        this.logs = refresh ? list : [...this.logs, ...list]
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style lang="scss">
.score-card {
  margin: 24rpx;
  padding: 48rpx;
  background: linear-gradient(135deg, #FF6A00, #05a853);
  border-radius: 16rpx;
  text-align: center;
  color: #fff;
  .score-num { font-size: 80rpx; font-weight: bold; }
  .score-label { margin-top: 8rpx; font-size: 24rpx; opacity: 0.9; }
  .score-tip {
    margin-top: 16rpx;
    font-size: 22rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 24rpx;
    padding: 8rpx 24rpx;
    display: inline-block;
  }
}
.log-list {
  margin: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  .log-title { font-weight: bold; margin-bottom: 16rpx; }
  .log-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24rpx 0;
    border-bottom: 1rpx solid #f2f3f5;
    &:last-child { border-bottom: none; }
    .log-reason { font-size: 26rpx; }
    .log-time { color: #c8c9cc; font-size: 20rpx; margin-top: 8rpx; }
    .log-right { text-align: right; }
    .log-value {
      font-weight: bold;
      font-size: 32rpx;
      &.plus { color: #FF6A00; }
      &.minus { color: #ee0a24; }
    }
    .log-balance { display: block; color: #969799; font-size: 20rpx; margin-top: 4rpx; }
  }
}
.empty { text-align: center; padding: 80rpx 0; color: #969799; }
.loading { text-align: center; padding: 24rpx; color: #969799; font-size: 24rpx; }
</style>
