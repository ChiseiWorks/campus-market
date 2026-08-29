<template>
  <view class="page">
    <!-- 图片上传：最多9张 -->
    <view class="img-grid">
      <image
        v-for="(img, i) in form.images"
        :key="i"
        :src="img"
        mode="aspectFill"
        class="img-item"
        @click="preview(i)"
      />
      <view v-if="form.images.length < 9" class="img-add" @click="chooseImage">+</view>
    </view>

    <view class="form">
      <view class="form-item">
        <input v-model="form.title" maxlength="64" placeholder="标题：一句话说清楚卖什么" />
      </view>

      <view class="form-item row" @click="pickCategory">
        <text>分类</text>
        <text class="row-value">{{ categoryName || '请选择' }} ›</text>
      </view>

      <view class="form-item row">
        <text>价格</text>
        <input v-model="form.price" type="digit" placeholder="¥ 0.00" class="row-input" />
      </view>
      <view class="form-item row">
        <text>原价（选填）</text>
        <input v-model="form.originalPrice" type="digit" placeholder="¥ 0.00" class="row-input" />
      </view>

      <view class="form-item">
        <view class="label">新旧程度</view>
        <view class="tag-group">
          <text
            v-for="q in qualities"
            :key="q.id"
            class="tag"
            :class="{ active: form.quality === q.id }"
            @click="form.quality = q.id"
          >{{ q.name }}</text>
        </view>
      </view>

      <view class="form-item row" @click="pickLocation">
        <text>面交地点</text>
        <text class="row-value">{{ locationName || '请选择' }} ›</text>
      </view>

      <view class="form-item">
        <textarea v-model="form.description" maxlength="500" placeholder="描述一下商品的成色、购买渠道、出手原因..." />
      </view>

      <button class="submit-btn" :disabled="submitting" @click="doPublish">
        {{ submitting ? '发布中...' : '确认发布' }}
      </button>
    </view>
  </view>
</template>

<script>
import goodsApi from '@/api/goods.js'
import commonApi from '@/api/common.js'

export default {
  data() {
    return {
      form: {
        images: [], title: '', categoryId: null, price: '', originalPrice: '',
        quality: 3, locationId: null, description: ''
      },
      categories: [],   // 从 /category/list 拉取
      locations: [],    // 从 /location/list 拉取
      categoryName: '',
      locationName: '',
      qualities: [
        { id: 1, name: '全新' }, { id: 2, name: '九成新' },
        { id: 3, name: '八成新' }, { id: 4, name: '有使用痕迹' }
      ],
      submitting: false
    }
  },
  onLoad() {
    this.loadBaseData()
  },
  methods: {
    /** 拉取分类与地点库；失败时静默降级（骨架阶段后端可能未启动） */
    async loadBaseData() {
      try {
        this.categories = await commonApi.categories(1) || []
      } catch (e) {}
      try {
        this.locations = await commonApi.locations() || []
      } catch (e) {}
    },
    pickCategory() {
      if (this.categories.length === 0) {
        return uni.showToast({ title: '分类加载中，请稍后再试', icon: 'none' })
      }
      uni.showActionSheet({
        itemList: this.categories.map((c) => c.name),
        success: (res) => {
          this.form.categoryId = this.categories[res.tapIndex].id
          this.categoryName = this.categories[res.tapIndex].name
        }
      })
    },
    pickLocation() {
      if (this.locations.length === 0) {
        return uni.showToast({ title: '地点加载中，请稍后再试', icon: 'none' })
      }
      uni.showActionSheet({
        itemList: this.locations.map((l) => l.name),
        success: (res) => {
          this.form.locationId = this.locations[res.tapIndex].id
          this.locationName = this.locations[res.tapIndex].name
        }
      })
    },
    chooseImage() {
      uni.chooseImage({
        count: 9 - this.form.images.length,
        success: (res) => {
          this.form.images.push(...res.tempFilePaths)
        }
      })
    },
    preview(i) {
      uni.previewImage({ urls: this.form.images, current: i })
    },
    /** 逐张上传图片；后端未就绪时降级为本地路径，保证骨架可跑通 */
    async uploadImages() {
      const urls = []
      for (const path of this.form.images) {
        // 已经是远程 URL 的跳过（如二次编辑场景）
        if (/^https?:\/\//.test(path)) { urls.push(path); continue }
        try {
          urls.push(await commonApi.upload(path))
        } catch (e) {
          urls.push(path)
        }
      }
      return urls
    },
    async doPublish() {
      if (!this.form.title.trim()) return uni.showToast({ title: '请填写标题', icon: 'none' })
      if (!this.form.categoryId) return uni.showToast({ title: '请选择分类', icon: 'none' })
      if (!this.form.price || Number(this.form.price) <= 0) {
        return uni.showToast({ title: '请填写正确的价格', icon: 'none' })
      }
      if (this.form.images.length === 0) {
        return uni.showToast({ title: '至少上传一张图片', icon: 'none' })
      }
      this.submitting = true
      try {
        const images = await this.uploadImages()
        await goodsApi.publish({ ...this.form, images })
        uni.showToast({ title: '发布成功', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 800)
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss">
.page { padding: 24rpx; }
.img-grid {
  display: flex;
  flex-wrap: wrap;
  .img-item, .img-add {
    width: 160rpx; height: 160rpx;
    border-radius: 12rpx;
    margin: 0 16rpx 16rpx 0;
    background: #fff;
  }
  .img-add {
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 64rpx;
    color: #c8c9cc;
    border: 1rpx dashed #dcdee0;
  }
}
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
