<template>
  <view class="page">
    <view class="form">
      <!-- 类型选择 -->
      <view class="form-item">
        <view class="tag-group">
          <text
            v-for="t in types"
            :key="t.id"
            class="tag"
            :class="{ active: form.type === t.id }"
            @click="form.type = t.id"
          >{{ t.name }}</text>
        </view>
      </view>

      <view class="form-item">
        <input v-model="form.title" maxlength="64" placeholder="需求标题（如：帮忙取一个快递）" />
      </view>

      <view class="form-item row" @click="pickLocation('pickup')">
        <text>取货地点</text>
        <text class="row-value">{{ pickupName || '请选择' }} ›</text>
      </view>
      <view class="form-item">
        <input v-model="form.pickupDetail" maxlength="128" placeholder="取货补充（如：A柜12号，选填）" />
      </view>
      <view class="form-item row" @click="pickLocation('delivery')">
        <text>送达地点</text>
        <text class="row-value">{{ deliveryName || '请选择' }} ›</text>
      </view>

      <view class="form-item">
        <input v-model="form.pickupCode" maxlength="32" placeholder="取件码（加密存储，仅接单后对接单人可见）" />
      </view>

      <view class="form-item">
        <view class="label">物品说明</view>
        <view class="tag-group">
          <text
            v-for="g in goodsTags"
            :key="g"
            class="tag"
            :class="{ active: form.goodsDesc.includes(g) }"
            @click="toggleGoodsTag(g)"
          >{{ g }}</text>
        </view>
      </view>

      <view class="form-item row">
        <text>悬赏金额</text>
        <input v-model="form.reward" type="digit" placeholder="¥ 0.00" class="row-input" />
      </view>
      <view class="form-item row" @click="pickTime">
        <text>期望时间</text>
        <text class="row-value">{{ expectTimeText || '请选择' }} ›</text>
      </view>

      <button class="submit-btn" :disabled="submitting" @click="doPublish">发布悬赏</button>
    </view>
  </view>
</template>

<script>
import errandApi, { ERRAND_TYPE } from '@/api/errand.js'
import commonApi from '@/api/common.js'

/** 期望时间快捷选项：label 展示，hours 为距当前的小时数 */
const TIME_OPTIONS = [
  { label: '1 小时内', hours: 1 },
  { label: '今天 18:00 前', hourOfDay: 18 },
  { label: '今天 22:00 前', hourOfDay: 22 },
  { label: '明天中午 12:00 前', hourOfDay: 12, nextDay: true }
]

function pad(n) { return n < 10 ? '0' + n : '' + n }

function formatTime(d) {
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:00`
}

export default {
  data() {
    return {
      types: Object.keys(ERRAND_TYPE).map((id) => ({ id: Number(id), name: ERRAND_TYPE[id] })),
      form: {
        type: 1, title: '', pickupLocationId: null, deliveryLocationId: null,
        pickupDetail: '', pickupCode: '', goodsDesc: [], reward: '', expectTime: ''
      },
      locations: [],
      pickupName: '',
      deliveryName: '',
      expectTimeText: '',
      goodsTags: ['小件', '大件', '易碎', '急需'],
      submitting: false
    }
  },
  onLoad() {
    this.loadLocations()
  },
  methods: {
    async loadLocations() {
      try {
        this.locations = await commonApi.locations() || []
      } catch (e) {
        // 后端未就绪时静默降级
      }
    },
    toggleGoodsTag(tag) {
      const i = this.form.goodsDesc.indexOf(tag)
      if (i >= 0) this.form.goodsDesc.splice(i, 1)
      else this.form.goodsDesc.push(tag)
    },
    pickLocation(which) {
      if (this.locations.length === 0) {
        return uni.showToast({ title: '地点加载中，请稍后再试', icon: 'none' })
      }
      uni.showActionSheet({
        itemList: this.locations.map((l) => l.name),
        success: (res) => {
          const loc = this.locations[res.tapIndex]
          if (which === 'pickup') {
            this.form.pickupLocationId = loc.id
            this.pickupName = loc.name
          } else {
            this.form.deliveryLocationId = loc.id
            this.deliveryName = loc.name
          }
        }
      })
    },
    /** 期望时间：快捷选项生成具体时间点，避免引入额外日期组件依赖 */
    pickTime() {
      uni.showActionSheet({
        itemList: TIME_OPTIONS.map((o) => o.label),
        success: (res) => {
          const opt = TIME_OPTIONS[res.tapIndex]
          const d = new Date()
          if (opt.hours) {
            d.setTime(d.getTime() + opt.hours * 3600 * 1000)
          } else {
            if (opt.nextDay) d.setDate(d.getDate() + 1)
            d.setHours(opt.hourOfDay, 0, 0, 0)
            // 若当天该时刻已过，自动顺延到明天
            if (d.getTime() <= Date.now()) d.setDate(d.getDate() + 1)
          }
          this.form.expectTime = formatTime(d)
          this.expectTimeText = opt.label
        }
      })
    },
    async doPublish() {
      if (!this.form.title.trim()) return uni.showToast({ title: '请填写需求标题', icon: 'none' })
      if (!this.form.pickupLocationId) return uni.showToast({ title: '请选择取货地点', icon: 'none' })
      if (!this.form.deliveryLocationId) return uni.showToast({ title: '请选择送达地点', icon: 'none' })
      if (this.form.pickupLocationId === this.form.deliveryLocationId) {
        return uni.showToast({ title: '取货和送达地点不能相同', icon: 'none' })
      }
      if (!this.form.reward || Number(this.form.reward) <= 0) {
        return uni.showToast({ title: '请填写悬赏金额', icon: 'none' })
      }
      this.submitting = true
      try {
        await errandApi.publish({
          ...this.form,
          goodsDesc: this.form.goodsDesc.join('、')
        })
        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => uni.switchTab({ url: '/pages/errand/hall' }), 800)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss">
.page { padding: 24rpx; }
.form-item {
  background: #fff;
  border-radius: 16rpx;
  padding: 28rpx;
  margin-bottom: 24rpx;
  &.row { display: flex; justify-content: space-between; align-items: center; }
  .row-value { color: #969799; }
  .row-input { text-align: right; flex: 1; margin-left: 32rpx; }
  .label { margin-bottom: 16rpx; }
  .tag-group {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    .tag {
      padding: 8rpx 28rpx;
      border-radius: 28rpx;
      background: #f2f3f5;
      color: #646566;
      font-size: 24rpx;
      &.active { background: #FF6A00; color: #fff; }
    }
  }
}
.submit-btn { margin-top: 24rpx; background: #FF6A00; color: #fff; border-radius: 48rpx; }
</style>
