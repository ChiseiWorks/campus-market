<template>
  <div class="page-container" v-loading="loading">
    <!-- 统计卡片 -->
    <el-row :gutter="16">
      <el-col :xs="12" :sm="12" :md="6" :lg="4" v-for="card in statCards" :key="card.label">
        <div class="stat-card">
          <div class="stat-icon" :style="{ background: card.color }">
            <el-icon><component :is="card.icon" /></el-icon>
          </div>
          <div>
            <div class="stat-label">{{ card.label }}</div>
            <div class="stat-value">{{ card.value }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 待办提醒 -->
    <el-row :gutter="16" style="margin-top: 16px" v-if="summary.pendingAuthCount || summary.pendingComplaintCount">
      <el-col :xs="24" :sm="12">
        <div class="stat-card alert-card">
          <div class="stat-icon" style="background: #f56c6c">
            <el-icon><Stamp /></el-icon>
          </div>
          <div>
            <div class="stat-label">待审核认证申请</div>
            <div class="stat-value" style="color: #f56c6c">{{ summary.pendingAuthCount || 0 }}</div>
          </div>
          <el-button type="danger" plain size="small" style="margin-left: auto" @click="$router.push('/auth')">
            去处理
          </el-button>
        </div>
      </el-col>
      <el-col :xs="24" :sm="12">
        <div class="stat-card alert-card">
          <div class="stat-icon" style="background: #e6a23c">
            <el-icon><Warning /></el-icon>
          </div>
          <div>
            <div class="stat-label">待处理投诉</div>
            <div class="stat-value" style="color: #e6a23c">{{ summary.pendingComplaintCount || 0 }}</div>
          </div>
          <el-button type="warning" plain size="small" style="margin-left: auto" @click="$router.push('/complaint')">
            去处理
          </el-button>
        </div>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :xs="24" :lg="12">
        <div class="page-card chart-card">
          <div class="chart-title">近 7 日趋势（新增用户 / 商品 / 订单）</div>
          <div ref="trendRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="6">
        <div class="page-card chart-card">
          <div class="chart-title">热门商品分类</div>
          <div ref="categoryRef" class="chart-box"></div>
        </div>
      </el-col>
      <el-col :xs="24" :lg="6">
        <div class="page-card chart-card">
          <div class="chart-title">跑腿高峰时段</div>
          <div ref="peakRef" class="chart-box"></div>
        </div>
      </el-col>
    </el-row>

    <!-- 累计数据 -->
    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <div class="page-card">
          <div class="chart-title">平台累计数据</div>
          <el-descriptions :column="5" border>
            <el-descriptions-item label="累计用户">{{ summary.totalUsers ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="累计商品">{{ summary.totalGoods ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="累计商品订单">{{ summary.totalGoodsOrders ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="累计跑腿订单">{{ summary.totalErrandOrders ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="今日交易额">{{ fmtMoney(summary.todayTradeAmount) }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, nextTick } from 'vue'
import * as echarts from 'echarts'
import {
  getDashboardSummary,
  getDashboardTrend,
  getDashboardCategory,
  getDashboardErrandPeak
} from '../api'
import { fmtMoney } from '../utils/format'

const loading = ref(false)
const summary = ref({})

const trendRef = ref()
const categoryRef = ref()
const peakRef = ref()
let trendChart, categoryChart, peakChart

const statCards = computed(() => [
  {
    label: '今日新增用户',
    value: summary.value.todayNewUsers ?? '-',
    icon: 'User',
    color: '#409eff'
  },
  {
    label: '今日发布商品',
    value: summary.value.todayNewGoods ?? '-',
    icon: 'Goods',
    color: '#67c23a'
  },
  {
    label: '今日商品订单',
    value: summary.value.todayNewGoodsOrders ?? '-',
    icon: 'ShoppingCart',
    color: '#ff6a00'
  },
  {
    label: '今日跑腿订单',
    value: summary.value.todayNewErrandOrders ?? '-',
    icon: 'Van',
    color: '#9b59b6'
  },
  {
    label: '今日交易额',
    value: fmtMoney(summary.value.todayTradeAmount),
    icon: 'Money',
    color: '#e6a23c'
  }
])

const initCharts = () => {
  trendChart = echarts.init(trendRef.value)
  categoryChart = echarts.init(categoryRef.value)
  peakChart = echarts.init(peakRef.value)
}

const renderTrend = (data) => {
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '新增商品', '新增订单'], bottom: 0 },
    grid: { left: 40, right: 20, top: 30, bottom: 50 },
    xAxis: { type: 'category', data: data.dates || [] },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      { name: '新增用户', type: 'line', smooth: true, data: data.newUsers || [], itemStyle: { color: '#409eff' } },
      { name: '新增商品', type: 'line', smooth: true, data: data.newGoods || [], itemStyle: { color: '#67c23a' } },
      { name: '新增订单', type: 'line', smooth: true, data: data.newOrders || [], itemStyle: { color: '#ff6a00' } }
    ]
  })
}

const renderCategory = (list) => {
  categoryChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['35%', '65%'],
        center: ['50%', '45%'],
        data: list || [],
        label: { formatter: '{b}' }
      }
    ]
  })
}

const renderPeak = (list) => {
  const hours = (list || []).map((i) => `${i.hour}时`)
  const counts = (list || []).map((i) => i.count)
  peakChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: hours },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'bar',
        data: counts,
        itemStyle: { color: '#ff6a00', borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 24
      }
    ]
  })
}

const resizeCharts = () => {
  trendChart?.resize()
  categoryChart?.resize()
  peakChart?.resize()
}

const fetchData = async () => {
  loading.value = true
  try {
    const [sum, trend, category, peak] = await Promise.all([
      getDashboardSummary(),
      getDashboardTrend(7),
      getDashboardCategory(),
      getDashboardErrandPeak()
    ])
    summary.value = sum || {}
    await nextTick()
    renderTrend(trend || {})
    renderCategory(category)
    renderPeak(peak)
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  initCharts()
  await fetchData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart?.dispose()
  categoryChart?.dispose()
  peakChart?.dispose()
})
</script>

<style scoped>
.chart-card {
  min-height: 320px;
}
.chart-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
}
.chart-box {
  height: 280px;
}
</style>
