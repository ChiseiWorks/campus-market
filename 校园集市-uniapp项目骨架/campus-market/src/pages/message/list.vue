<template>
  <view class="page">
    <!-- 顶部切换 -->
    <view class="tab-bar">
      <text :class="{ active: tab === 'chat' }" @click="tab = 'chat'">聊天消息</text>
      <text :class="{ active: tab === 'notice' }" @click="tab = 'notice'">系统通知</text>
    </view>

    <!-- 聊天会话列表 -->
    <view v-if="tab === 'chat'">
      <view v-for="s in sessions" :key="s.userId" class="session-item" @click="goChat(s)">
        <image class="session-avatar" :src="s.avatar || '/static/default-avatar.png'" mode="aspectFill" />
        <view class="session-info">
          <view class="session-top">
            <text class="session-name">{{ s.nickname }}</text>
            <text class="session-time">{{ s.timeAgo || s.lastTime }}</text>
          </view>
          <view class="session-last">{{ s.lastMessage }}</view>
        </view>
        <view class="unread-badge" v-if="s.unread">{{ s.unread > 99 ? '99+' : s.unread }}</view>
      </view>
      <view v-if="sessions.length === 0" class="empty">还没有会话，去逛逛吧</view>
    </view>

    <!-- 系统通知 -->
    <view v-else>
      <view v-for="n in notices" :key="n.id" class="notice-item">
        <view class="notice-title">{{ n.title }}</view>
        <view class="notice-content">{{ n.content }}</view>
        <view class="notice-time">{{ n.createTime }}</view>
      </view>
      <view v-if="notices.length === 0" class="empty">暂无系统通知</view>
    </view>
  </view>
</template>

<script>
import messageApi from '@/api/message.js'
import commonApi from '@/api/common.js'

export default {
  data() {
    return {
      tab: 'chat',
      sessions: [],
      notices: []
    }
  },
  onShow() {
    this.loadSessions()
    this.loadNotices()
  },
  methods: {
    async loadSessions() {
      try {
        const res = await messageApi.sessions()
        this.sessions = res.list || res || []
      } catch (e) {
        // 未登录或后端未就绪时静默
      }
    },
    async loadNotices() {
      try {
        const res = await commonApi.notices()
        this.notices = res.list || res || []
      } catch (e) {}
    },
    goChat(s) {
      uni.navigateTo({
        url: '/pages/message/chat?userId=' + s.userId +
          '&nickname=' + encodeURIComponent(s.nickname || '同学') +
          '&avatar=' + encodeURIComponent(s.avatar || '')
      })
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
.session-item {
  display: flex;
  align-items: center;
  padding: 24rpx 32rpx;
  background: #fff;
  border-bottom: 1rpx solid #ebedf0;
  .session-avatar { width: 88rpx; height: 88rpx; border-radius: 50%; background: #f2f3f5; }
  .session-info { flex: 1; margin-left: 20rpx; overflow: hidden; }
  .session-top { display: flex; justify-content: space-between; }
  .session-name { font-weight: bold; }
  .session-time { color: #c8c9cc; font-size: 20rpx; }
  .session-last {
    color: #969799;
    font-size: 24rpx;
    margin-top: 8rpx;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
  .unread-badge {
    min-width: 36rpx;
    height: 36rpx;
    border-radius: 18rpx;
    background: #ee0a24;
    color: #fff;
    font-size: 20rpx;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 8rpx;
  }
}
.notice-item {
  margin: 16rpx 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 16rpx;
  .notice-title { font-weight: bold; }
  .notice-content { color: #646566; font-size: 26rpx; margin: 12rpx 0; }
  .notice-time { color: #c8c9cc; font-size: 20rpx; }
}
.empty { text-align: center; padding: 120rpx 0; color: #969799; }
</style>
