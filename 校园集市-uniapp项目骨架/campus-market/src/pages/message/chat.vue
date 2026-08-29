<template>
  <view class="page">
    <!-- 消息列表 -->
    <scroll-view
      scroll-y
      class="msg-scroll"
      :scroll-into-view="anchorId"
      scroll-with-animation
    >
      <view
        v-for="(m, i) in messages"
        :key="m.id || i"
        :id="'msg-' + i"
        class="msg-row"
        :class="{ mine: m.fromUserId === myId }"
      >
        <image class="msg-avatar" :src="m.fromUserId === myId ? myAvatar : peerAvatar" mode="aspectFill" />
        <view class="msg-bubble" :class="{ image: m.type === 2 }">
          <image v-if="m.type === 2" :src="m.content" mode="widthFix" class="msg-img" @click="preview(m.content)" />
          <text v-else>{{ m.content }}</text>
        </view>
      </view>
      <view :id="bottomAnchor"></view>
      <view v-if="messages.length === 0" class="empty">打个招呼吧</view>
    </scroll-view>

    <!-- 输入栏 -->
    <view class="input-bar">
      <input
        v-model="draft"
        class="msg-input"
        confirm-type="send"
        placeholder="发送消息..."
        @confirm="send"
      />
      <button class="send-btn" size="mini" :disabled="sending" @click="send">发送</button>
    </view>
  </view>
</template>

<script>
import messageApi from '@/api/message.js'

export default {
  data() {
    return {
      peerId: null,
      peerName: '',
      peerAvatar: '',
      goodsId: null,
      messages: [],
      draft: '',
      sending: false,
      anchorId: '',
      bottomAnchor: 'msg-bottom',
      timer: null
    }
  },
  computed: {
    myId() {
      const u = this.$store.state.user.userInfo
      return u ? u.id : 0
    },
    myAvatar() {
      const u = this.$store.state.user.userInfo
      return (u && u.avatar) || '/static/default-avatar.png'
    }
  },
  onLoad(options) {
    this.peerId = Number(options.userId)
    this.peerName = options.nickname ? decodeURIComponent(options.nickname) : '同学'
    this.peerAvatar = options.avatar ? decodeURIComponent(options.avatar) : '/static/default-avatar.png'
    if (options.goodsId) this.goodsId = Number(options.goodsId)
    uni.setNavigationBarTitle({ title: this.peerName })
    this.loadHistory()
    messageApi.markRead(this.peerId).catch(() => {})
    // 轮询拉新消息（骨架方案；正式版换 WebSocket 推送）
    this.timer = setInterval(() => this.loadHistory(true), 3000)
  },
  onUnload() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    async loadHistory(silent = false) {
      try {
        const res = await messageApi.history(this.peerId, 1)
        const list = (res.list || []).reverse() // 接口倒序返回，页面正序展示
        this.messages = list
        if (!silent) this.scrollToBottom()
      } catch (e) {
        // 静默失败，避免轮询时反复弹错
      }
    },
    scrollToBottom() {
      this.anchorId = ''
      this.$nextTick(() => {
        this.anchorId = this.bottomAnchor
      })
    },
    async send() {
      const content = this.draft.trim()
      if (!content) return
      this.sending = true
      try {
        await messageApi.send(this.peerId, content, 1, this.goodsId)
        this.draft = ''
        this.loadHistory()
        this.scrollToBottom()
      } finally {
        this.sending = false
      }
    },
    preview(url) {
      uni.previewImage({ urls: [url] })
    }
  }
}
</script>

<style lang="scss">
.page { display: flex; flex-direction: column; height: 100vh; }
.msg-scroll {
  flex: 1;
  padding: 24rpx;
  box-sizing: border-box;
  .msg-row {
    display: flex;
    margin-bottom: 32rpx;
    &.mine { flex-direction: row-reverse; }
    .msg-avatar { width: 72rpx; height: 72rpx; border-radius: 50%; background: #f2f3f5; flex-shrink: 0; }
    .msg-bubble {
      max-width: 60%;
      margin: 0 20rpx;
      padding: 18rpx 24rpx;
      background: #fff;
      border-radius: 16rpx;
      font-size: 28rpx;
      line-height: 1.5;
      word-break: break-all;
      &.image { padding: 8rpx; }
      .msg-img { max-width: 360rpx; border-radius: 12rpx; }
    }
    &.mine .msg-bubble { background: #FF6A00; color: #fff; }
  }
}
.empty { text-align: center; padding: 120rpx 0; color: #969799; }
.input-bar {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #fff;
  border-top: 1rpx solid #ebedf0;
  .msg-input {
    flex: 1;
    background: #f2f3f5;
    border-radius: 32rpx;
    padding: 14rpx 28rpx;
    margin-right: 16rpx;
  }
  .send-btn { background: #FF6A00; color: #fff; border-radius: 32rpx; }
}
</style>
