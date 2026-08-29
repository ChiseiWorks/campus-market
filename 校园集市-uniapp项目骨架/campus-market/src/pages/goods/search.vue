<template>
  <view class="page">
    <!-- 搜索输入区 -->
    <view class="search-header">
      <view class="search-box">
        <input
          v-model="keyword"
          focus
          confirm-type="search"
          placeholder="想找点什么？"
          @confirm="doSearch"
        />
      </view>
      <text class="search-btn" @click="doSearch">搜索</text>
    </view>

    <!-- 搜索历史（本地存储） -->
    <view v-if="!searched && history.length > 0" class="history-section">
      <view class="history-head">
        <text class="history-title">搜索历史</text>
        <text class="history-clear" @click="clearHistory">清空</text>
      </view>
      <view class="history-tags">
        <text
          v-for="(h, i) in history"
          :key="i"
          class="history-tag"
          @click="searchBy(h)"
        >{{ h }}</text>
      </view>
    </view>

    <!-- 搜索结果 -->
    <view v-if="searched" class="goods-waterfall">
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
            <text class="goods-price">¥{{ item.price }}</text>
            <text class="goods-quality">{{ qualityText[item.quality] }}</text>
          </view>
          <view class="goods-location">{{ item.locationName }}</view>
        </view>
      </view>
    </view>

    <view v-if="searched && !loading && goodsList.length === 0" class="empty">
      没有找到「{{ lastKeyword }}」相关的闲置，换个关键词试试
    </view>
    <view v-if="loading" class="loading">搜索中...</view>
    <view v-if="noMore && goodsList.length > 0" class="loading">没有更多了</view>
  </view>
</template>

<script>
import goodsApi from '@/api/goods.js'

const HISTORY_KEY = 'searchHistory'

export default {
  data() {
    return {
      keyword: '',
      lastKeyword: '',
      history: [],
      searched: false,
      goodsList: [],
      page: 1,
      loading: false,
      noMore: false,
      qualityText: { 1: '全新', 2: '九成新', 3: '八成新', 4: '有使用痕迹' }
    }
  },
  onLoad() {
    this.history = uni.getStorageSync(HISTORY_KEY) || []
  },
  onReachBottom() {
    if (this.searched && !this.noMore && !this.loading) {
      this.page++
      this.loadResults()
    }
  },
  methods: {
    searchBy(word) {
      this.keyword = word
      this.doSearch()
    },
    doSearch() {
      const kw = this.keyword.trim()
      if (!kw) return uni.showToast({ title: '请输入关键词', icon: 'none' })
      this.lastKeyword = kw
      // 记录历史：去重 + 置顶 + 最多10条
      this.history = [kw, ...this.history.filter((h) => h !== kw)].slice(0, 10)
      uni.setStorageSync(HISTORY_KEY, this.history)
      this.page = 1
      this.noMore = false
      this.searched = true
      this.loadResults(true)
    },
    async loadResults(refresh = false) {
      this.loading = true
      try {
        const res = await goodsApi.list({ keyword: this.lastKeyword, page: this.page })
        const list = res.list || []
        this.goodsList = refresh ? list : [...this.goodsList, ...list]
        if (list.length < 10) this.noMore = true
      } finally {
        this.loading = false
      }
    },
    clearHistory() {
      this.history = []
      uni.removeStorageSync(HISTORY_KEY)
    },
    goDetail(id) {
      uni.navigateTo({ url: '/pages/goods/detail?id=' + id })
    }
  }
}
</script>

<style lang="scss">
.search-header {
  display: flex;
  align-items: center;
  padding: 16rpx 24rpx;
  background: #fff;
  .search-box {
    flex: 1;
    background: #f2f3f5;
    border-radius: 32rpx;
    padding: 14rpx 28rpx;
  }
  .search-btn { margin-left: 24rpx; color: #FF6A00; font-weight: bold; }
}
.history-section {
  margin: 24rpx;
  .history-head {
    display: flex;
    justify-content: space-between;
    .history-title { font-weight: bold; }
    .history-clear { color: #969799; font-size: 24rpx; }
  }
  .history-tags {
    margin-top: 20rpx;
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
    .history-tag {
      padding: 10rpx 28rpx;
      background: #fff;
      border-radius: 28rpx;
      color: #646566;
      font-size: 24rpx;
    }
  }
}
.goods-waterfall {
  display: flex;
  flex-wrap: wrap;
  padding: 16rpx;
  justify-content: space-between;
  .goods-card {
    width: 48%;
    margin-bottom: 24rpx;
    background: #fff;
    border-radius: 16rpx;
    overflow: hidden;
    .goods-img { width: 100%; height: 340rpx; background: #f2f3f5; }
    .goods-info {
      padding: 16rpx;
      .goods-title {
        font-size: 26rpx;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
      .goods-meta {
        margin-top: 8rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
        .goods-price { color: #ff6a00; font-weight: bold; font-size: 32rpx; }
        .goods-quality { color: #969799; font-size: 22rpx; }
      }
      .goods-location { margin-top: 8rpx; color: #969799; font-size: 22rpx; }
    }
  }
}
.empty { text-align: center; padding: 120rpx 32rpx; color: #969799; }
.loading { text-align: center; padding: 24rpx; color: #969799; font-size: 24rpx; }
</style>
