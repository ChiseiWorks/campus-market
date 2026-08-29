<template>
  <view class="page">
    <!-- 沉浸式橙渐变搜索头 -->
    <view class="hero">
      <view class="hero-search" @click="goSearch">
        <text class="hero-search-icon">🔍</text>
        <text class="hero-search-placeholder">搜索：想找点什么？</text>
      </view>
    </view>

    <!-- 金刚区宫格分类 -->
    <view class="category-panel">
      <view class="category-grid">
        <view
          v-for="c in categories"
          :key="c.id"
          class="category-cell"
          :class="{ active: currentCategory === c.id }"
          @click="switchCategory(c.id)"
        >
          <view class="category-icon" :style="{ background: categoryMeta[c.id].bg }">
            {{ categoryMeta[c.id].icon }}
          </view>
          <text class="category-name">{{ c.name }}</text>
        </view>
      </view>
      <!-- 当前选中分类标签条 -->
      <view v-if="currentCategory !== 0" class="current-filter">
        <text class="current-filter-label">当前分类：{{ currentCategoryName }}</text>
        <text class="current-filter-clear" @click="switchCategory(0)">✕ 清除</text>
      </view>
    </view>

    <!-- 商品双列瀑布流 -->
    <view class="goods-waterfall">
      <view
        v-for="item in goodsList"
        :key="item.id"
        class="goods-card"
        @click="goDetail(item.id)"
      >
        <image class="goods-img" :src="item.cover" mode="aspectFill" lazy-load />
        <view class="goods-info">
          <view class="goods-title">{{ item.title }}</view>
          <view class="goods-meta">
            <view class="goods-price">
              <text class="price-symbol">¥</text>
              <text class="price-int">{{ priceInt(item.price) }}</text>
              <text class="price-dec">{{ priceDec(item.price) }}</text>
            </view>
            <text class="goods-quality">{{ qualityText[item.quality] }}</text>
          </view>
          <view class="goods-location">📍 {{ item.locationName }}</view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-if="!loading && goodsList.length === 0" class="empty">
      <view class="empty-text">这里还没有闲置商品</view>
      <button class="empty-btn" @click="goPublish">去发布第一件闲置</button>
    </view>

    <view v-if="loading" class="loading">加载中...</view>
    <view v-if="noMore && goodsList.length > 0" class="loading">没有更多了</view>
  </view>
</template>

<script>
import goodsApi from '@/api/goods.js'

export default {
  data() {
    return {
      // TODO: 分类从接口 /category/list 拉取，此处先内置
      categories: [
        { id: 0, name: '推荐' },
        { id: 1, name: '教材' },
        { id: 2, name: '数码' },
        { id: 3, name: '服饰' },
        { id: 4, name: '生活用品' },
        { id: 5, name: '运动' },
        { id: 6, name: '其他' }
      ],
      // 金刚区宫格的图标与浅色底（纯 UI 元数据，按分类 id 对应）
      categoryMeta: {
        0: { icon: '✨', bg: '#FFF3E8' },
        1: { icon: '📚', bg: '#E8F1FF' },
        2: { icon: '📱', bg: '#F0E8FF' },
        3: { icon: '👕', bg: '#FFE8F0' },
        4: { icon: '🧴', bg: '#E8FAF4' },
        5: { icon: '🏀', bg: '#FFF7E0' },
        6: { icon: '📦', bg: '#F0F2F5' }
      },
      currentCategory: 0,
      goodsList: [],
      page: 1,
      loading: false,
      noMore: false,
      qualityText: { 1: '全新', 2: '九成新', 3: '八成新', 4: '有使用痕迹' }
    }
  },
  onLoad() {
    this.loadGoods(true)
  },
  onPullDownRefresh() {
    this.loadGoods(true).then(() => uni.stopPullDownRefresh())
  },
  onReachBottom() {
    if (!this.noMore && !this.loading) {
      this.page++
      this.loadGoods()
    }
  },
  computed: {
    currentCategoryName() {
      const c = this.categories.find((x) => x.id === this.currentCategory)
      return c ? c.name : ''
    }
  },
  methods: {
    /* ===== 淘宝式价格拆分：整数大字 + 小数小字（纯展示） ===== */
    priceInt(p) {
      return String(p).split('.')[0]
    },
    priceDec(p) {
      const s = String(p)
      return s.includes('.') ? '.' + s.split('.')[1] : ''
    },
    async loadGoods(refresh = false) {
      if (refresh) { this.page = 1; this.noMore = false }
      this.loading = true
      try {
        const res = await goodsApi.list({
          categoryId: this.currentCategory || undefined,
          page: this.page
        })
        const list = res.list || []
        this.goodsList = refresh ? list : [...this.goodsList, ...list]
        if (list.length < 10) this.noMore = true
      } catch (e) {
        // 错误提示已在 request 封装中统一处理
      } finally {
        this.loading = false
      }
    },
    switchCategory(id) {
      this.currentCategory = id
      this.loadGoods(true)
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
    },
    goSearch() {
      uni.navigateTo({ url: '/pages/goods/search' })
    },
    goPublish() {
      if (!this.$checkAuth()) return
      uni.navigateTo({ url: '/pages/goods/publish' })
    }
  }
}
</script>

<style lang="scss">
/* 沉浸式橙渐变搜索头 */
.hero {
  background: linear-gradient(135deg, #FF8F1F 0%, #FF5000 100%);
  padding: 24rpx 24rpx 56rpx;
  border-radius: 0 0 24rpx 24rpx;
  .hero-search {
    display: flex;
    align-items: center;
    background: #fff;
    border-radius: 44rpx;
    padding: 16rpx 28rpx;
    .hero-search-icon { font-size: 28rpx; margin-right: 12rpx; }
    .hero-search-placeholder { color: #969799; font-size: 26rpx; }
  }
}

/* 金刚区宫格分类（白卡上浮叠在渐变头上） */
.category-panel {
  margin: -32rpx 16rpx 0;
  background: #fff;
  border-radius: 16rpx;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  padding: 24rpx 8rpx 16rpx;
  .category-grid {
    display: flex;
    flex-wrap: wrap;
    .category-cell {
      width: 20%;
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 20rpx;
      .category-icon {
        width: 88rpx;
        height: 88rpx;
        border-radius: 50%;
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 44rpx;
      }
      .category-name {
        margin-top: 8rpx;
        font-size: 22rpx;
        color: #646566;
      }
      &.active .category-name { color: #FF6A00; font-weight: bold; }
    }
  }
  .current-filter {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin: 4rpx 16rpx 8rpx;
    padding: 10rpx 20rpx;
    background: #FFF3E8;
    border-radius: 24rpx;
    .current-filter-label { color: #FF6A00; font-size: 22rpx; font-weight: bold; }
    .current-filter-clear { color: #969799; font-size: 22rpx; }
  }
}

/* 淘宝式商品瀑布流卡片 */
.goods-waterfall {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx;
  justify-content: space-between;
  .goods-card {
    width: 48%;
    margin-bottom: 20rpx;
    background: #fff;
    border-radius: 16rpx;
    box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
    overflow: hidden;
    .goods-img {
      width: 100%;
      height: 340rpx;
      background: #f2f3f5;
    }
    .goods-info {
      padding: 16rpx 16rpx 20rpx;
      .goods-title {
        font-size: 26rpx;
        line-height: 1.4;
        height: 72rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        -webkit-box-orient: vertical;
      }
      .goods-meta {
        margin-top: 12rpx;
        display: flex;
        justify-content: space-between;
        align-items: baseline;
        .goods-price {
          color: #FF5000;
          .price-symbol { font-size: 22rpx; font-weight: bold; }
          .price-int { font-size: 38rpx; font-weight: bold; }
          .price-dec { font-size: 24rpx; font-weight: bold; }
        }
        .goods-quality {
          color: #FF6A00;
          background: #FFF3E8;
          font-size: 20rpx;
          padding: 2rpx 14rpx;
          border-radius: 16rpx;
        }
      }
      .goods-location {
        margin-top: 10rpx;
        color: #969799;
        font-size: 22rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }
}
.empty { text-align: center; padding: 120rpx 0; color: #969799; }
.empty-btn {
  margin: 32rpx auto;
  width: 320rpx;
  background: linear-gradient(135deg, #FF8F1F, #FF5000);
  color: #fff;
  border-radius: 48rpx;
}
.loading { text-align: center; padding: 24rpx; color: #969799; font-size: 24rpx; }
</style>
