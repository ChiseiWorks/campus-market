<template>
  <view class="page" v-if="order">
    <!-- 状态时间线 -->
    <view class="status-card">
      <view class="status-steps">
        <view
          v-for="(step, i) in steps"
          :key="i"
          class="step"
          :class="{ done: step.time, active: i === currentStepIndex }"
        >
          <view class="step-dot"></view>
          <view class="step-name">{{ step.name }}</view>
          <view class="step-time">{{ step.time || '' }}</view>
        </view>
      </view>
    </view>

    <!-- 订单信息 -->
    <view class="info-card">
      <view class="info-head">
        <text class="info-type">{{ typeText[order.type] }}</text>
        <text class="info-reward">¥{{ order.reward }}</text>
      </view>
      <view class="info-row">取：{{ order.pickupLocationName }}{{ order.pickupDetail ? '（' + order.pickupDetail + '）' : '' }}</view>
      <view class="info-row">送：{{ order.deliveryLocationName }}</view>
      <view class="info-row" v-if="order.pickupCode">
        取件码：{{ codeVisible ? order.pickupCode : maskCode(order.pickupCode) }}
        <text class="code-eye" @click="codeVisible = !codeVisible">{{ codeVisible ? '隐藏' : '查看' }}</text>
      </view>
      <view class="info-row" v-if="order.expectTime">期望：{{ order.expectTime }} 前</view>
    </view>

    <!-- 对方信息（接单后显示） -->
    <view class="peer-card" v-if="order.peerUser">
      <text>{{ isPublisher ? '接单人' : '发单人' }}：{{ order.peerUser.nickname }}</text>
      <text class="peer-credit">信用分 {{ order.peerUser.creditScore }}</text>
    </view>

    <!-- 底部按钮区：按 状态 × 角色 动态渲染 -->
    <view class="action-bar">
      <button class="action-btn plain" v-if="showContact" @click="contact">联系对方</button>
      <button class="action-btn plain" v-if="showCancel" @click="doCancel">取消订单</button>
      <button class="action-btn primary" v-if="showGrab" @click="doGrab">马上抢单</button>
      <button class="action-btn primary" v-if="showDeliver" @click="doDeliver">开始配送</button>
      <button class="action-btn primary" v-if="showArrive" @click="doArrive">确认送达</button>
      <button class="action-btn primary" v-if="showConfirm" @click="doConfirm">确认完成</button>
      <button class="action-btn danger" v-if="showDispute" @click="doDispute">申诉</button>
      <button class="action-btn primary" v-if="showEvaluate" @click="goEvaluate">评价</button>
    </view>
  </view>
</template>

<script>
import errandApi, { ERRAND_STATUS as S, ERRAND_STATUS_TEXT, ERRAND_TYPE } from '@/api/errand.js'

export default {
  data() {
    return {
      orderId: null,
      order: null,
      typeText: ERRAND_TYPE,
      statusText: ERRAND_STATUS_TEXT,
      codeVisible: false
    }
  },
  computed: {
    myId() { return this.$store.state.user.userInfo ? this.$store.state.user.userInfo.id : 0 },
    isPublisher() { return this.order && this.order.publisherId === this.myId },
    isRunner() { return this.order && this.order.runnerId === this.myId },
    status() { return this.order ? this.order.status : -1 },

    /* ===== 按钮矩阵：与《状态机与接口设计》文档严格对应 ===== */
    showGrab()    { return this.status === S.PENDING && !this.isPublisher },
    showCancel()  {
      if (this.isPublisher) return [S.PENDING, S.ACCEPTED].includes(this.status)
      if (this.isRunner)    return this.status === S.ACCEPTED
      return false
    },
    showDeliver() { return this.isRunner && this.status === S.ACCEPTED },
    showArrive()  { return this.isRunner && this.status === S.DELIVERING },
    showConfirm() { return this.isPublisher && this.status === S.ARRIVED },
    showDispute() { return this.isPublisher && this.status === S.ARRIVED },
    showEvaluate(){ return (this.isPublisher || this.isRunner) && this.status === S.FINISHED },
    showContact() { return (this.isPublisher || this.isRunner) && [S.ACCEPTED, S.DELIVERING, S.ARRIVED].includes(this.status) },

    /* 状态时间线 */
    steps() {
      const o = this.order || {}
      return [
        { name: '已发布', time: o.createTime },
        { name: '已接单', time: o.acceptTime },
        { name: '配送中', time: o.deliverTime },
        { name: '已送达', time: o.arriveTime },
        { name: '已完成', time: o.finishTime }
      ]
    },
    currentStepIndex() {
      if (this.status === S.CANCELLED || this.status === S.DISPUTED) return -1
      return Math.min(this.status, 4)
    }
  },
  onLoad(options) {
    this.orderId = options.id
    this.loadDetail()
  },
  methods: {
    async loadDetail() {
      this.order = await errandApi.detail(this.orderId)
    },
    maskCode(code) {
      return code ? code[0] + '******' + code[code.length - 1] : ''
    },
    async doGrab() {
      await errandApi.accept(this.orderId)
      uni.showToast({ title: '抢单成功', icon: 'success' })
      this.loadDetail()
    },
    async doDeliver() {
      await errandApi.deliver(this.orderId)
      this.loadDetail()
    },
    async doArrive() {
      await errandApi.arrive(this.orderId)
      uni.showToast({ title: '已通知对方确认', icon: 'none' })
      this.loadDetail()
    },
    async doConfirm() {
      await errandApi.confirm(this.orderId)
      uni.showToast({ title: '订单完成', icon: 'success' })
      this.loadDetail()
    },
    doCancel() {
      // 跑男在已接单状态取消会被扣信用分，前端先明示
      const warn = this.isRunner && this.status === S.ACCEPTED
        ? '接单后取消将扣除 10 信用分，确定取消？'
        : '确定取消该订单吗？'
      uni.showModal({
        title: '提示',
        content: warn,
        success: async (res) => {
          if (!res.confirm) return
          await errandApi.cancel(this.orderId, '用户主动取消')
          this.loadDetail()
        }
      })
    },
    doDispute() {
      const defendantId = this.isPublisher ? this.order.runnerId : this.order.publisherId
      uni.navigateTo({
        url: '/pages/complaint/index?orderType=2&orderId=' + this.orderId + '&defendantId=' + defendantId
      })
    },
    contact() {
      const peer = this.order.peerUser
      if (!peer) return uni.showToast({ title: '接单后才能联系对方', icon: 'none' })
      uni.navigateTo({
        url: '/pages/message/chat?userId=' + peer.id +
          '&nickname=' + encodeURIComponent(peer.nickname || '同学') +
          '&avatar=' + encodeURIComponent(peer.avatar || '')
      })
    },
    goEvaluate() {
      const toUserId = this.isPublisher ? this.order.runnerId : this.order.publisherId
      uni.navigateTo({
        url: '/pages/evaluate/index?orderType=2&orderId=' + this.orderId + '&toUserId=' + toUserId
      })
    }
  }
}
</script>

<style lang="scss">
.status-card {
  margin: 24rpx;
  padding: 32rpx 16rpx;
  background: #fff;
  border-radius: 16rpx;
  .status-steps {
    display: flex;
    .step {
      flex: 1;
      text-align: center;
      color: #c8c9cc;
      .step-dot {
        width: 20rpx; height: 20rpx;
        border-radius: 50%;
        background: #ebedf0;
        margin: 0 auto 12rpx;
      }
      .step-name { font-size: 22rpx; }
      .step-time { font-size: 18rpx; margin-top: 8rpx; }
      &.done { color: #FF6A00; .step-dot { background: #FF6A00; } }
    }
  }
}
.info-card, .peer-card {
  margin: 0 24rpx 24rpx;
  padding: 28rpx;
  background: #fff;
  border-radius: 16rpx;
}
.info-head {
  display: flex;
  justify-content: space-between;
  margin-bottom: 16rpx;
  .info-type { font-weight: bold; }
  .info-reward { color: #ff6a00; font-weight: bold; font-size: 36rpx; }
}
.info-row { color: #646566; font-size: 26rpx; margin: 12rpx 0; }
.code-eye { color: #FF6A00; margin-left: 16rpx; }
.peer-card { display: flex; justify-content: space-between; }
.peer-credit { color: #969799; font-size: 24rpx; }
.action-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  display: flex;
  gap: 16rpx;
  padding: 16rpx 24rpx;
  background: #fff;
  box-shadow: 0 -2rpx 12rpx rgba(0, 0, 0, 0.05);
  .action-btn {
    flex: 1;
    border-radius: 44rpx;
    font-size: 28rpx;
    &.plain { background: #f2f3f5; color: #323233; }
    &.primary { background: #FF6A00; color: #fff; }
    &.danger { background: #fff; color: #ee0a24; border: 1rpx solid #ee0a24; }
  }
}
</style>
