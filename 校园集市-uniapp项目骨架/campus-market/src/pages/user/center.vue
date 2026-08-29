<template>
  <view class="page">
    <!-- 橙渐变用户信息头卡 -->
    <view class="user-card" @click="onUserCardClick">
      <image class="avatar" :src="userInfo ? userInfo.avatar : '/static/default-avatar.png'" mode="aspectFill" />
      <view class="user-info">
        <view class="nickname">{{ userInfo ? userInfo.nickname : '点击登录' }}</view>
        <view class="auth-line" v-if="userInfo">
          <text class="auth-tag" :class="'auth-' + authStatus">{{ authStatusText }}</text>
          <text class="credit">信用分 {{ creditScore }}</text>
        </view>
        <view class="login-tip" v-else>登录后发布闲置、跑腿接单</view>
      </view>
    </view>

    <!-- 三入口宫格 -->
    <view class="entry-grid">
      <view class="entry" @click="go('/pages/goods/my')">我的发布</view>
      <view class="entry" @click="go('/pages/order/list')">我的订单</view>
      <view class="entry" @click="go('/pages/errand/my')">我的跑腿</view>
    </view>

    <!-- 功能列表 -->
    <view class="menu-list">
      <view class="menu-item" @click="go('/pages/user/auth?type=runner')">
        <text>跑男认证（接单必备）</text><text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/user/credit')">
        <text>信用分明细</text><text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/user/favorite')">
        <text>我的收藏</text><text class="arrow">›</text>
      </view>
      <view class="menu-item" @click="go('/pages/complaint/index')">
        <text>投诉中心</text><text class="arrow">›</text>
      </view>
      <view class="menu-item" v-if="isLogin" @click="onLogout">
        <text class="logout">退出登录</text><text class="arrow">›</text>
      </view>
    </view>
  </view>
</template>

<script>
import { mapGetters, mapState } from 'vuex'

export default {
  computed: {
    ...mapState('user', ['userInfo']),
    ...mapGetters('user', ['isLogin', 'authStatus', 'creditScore']),
    authStatusText() {
      return { 0: '未认证', 1: '认证审核中', 2: '已认证 ✓', 3: '认证被驳回' }[this.authStatus]
    }
  },
  methods: {
    onUserCardClick() {
      if (!this.isLogin) uni.navigateTo({ url: '/pages/user/login' })
    },
    go(url) {
      if (!this.isLogin) return uni.navigateTo({ url: '/pages/user/login' })
      uni.navigateTo({ url })
    },
    onLogout() {
      uni.showModal({
        title: '提示',
        content: '确定退出登录吗？',
        success: (res) => {
          if (res.confirm) this.$store.dispatch('user/logout')
        }
      })
    }
  }
}
</script>

<style lang="scss">
.user-card {
  display: flex;
  align-items: center;
  margin: 24rpx;
  padding: 48rpx 32rpx;
  background: linear-gradient(135deg, #FF8F1F 0%, #FF5000 100%);
  border-radius: 16rpx;
  box-shadow: 0 8rpx 24rpx rgba(255, 106, 0, 0.25);
  .avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    border: 4rpx solid rgba(255, 255, 255, 0.6);
  }
  .user-info { margin-left: 24rpx; }
  .nickname { font-size: 36rpx; font-weight: bold; color: #fff; }
  .login-tip { margin-top: 12rpx; font-size: 24rpx; color: rgba(255, 255, 255, 0.85); }
  .auth-line { margin-top: 12rpx; display: flex; align-items: center; }
  .auth-tag {
    font-size: 20rpx;
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
    background: rgba(255, 255, 255, 0.25);
    color: #fff;
    &.auth-2 { background: #fff; color: #FF6A00; font-weight: bold; }
    &.auth-1 { background: rgba(255, 255, 255, 0.35); color: #fff; }
  }
  .credit { margin-left: 16rpx; color: rgba(255, 255, 255, 0.9); font-size: 24rpx; }
}
.entry-grid {
  display: flex;
  margin: 0 24rpx;
  background: #fff;
  border-radius: 16rpx;
  padding: 32rpx 0;
  .entry { flex: 1; text-align: center; color: #323233; font-size: 26rpx; }
}
.menu-list {
  margin: 24rpx;
  background: #fff;
  border-radius: 16rpx;
  .menu-item {
    display: flex;
    justify-content: space-between;
    padding: 32rpx;
    border-bottom: 1rpx solid #ebedf0;
    &:last-child { border-bottom: none; }
    .arrow { color: #c8c9cc; }
    .logout { color: #ee0a24; }
  }
}
</style>
