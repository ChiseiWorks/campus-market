<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input
          v-model="query.keyword"
          placeholder="标题 / 发单人 / 接单人"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="待接单" value="0" />
          <el-option label="已接单" value="1" />
          <el-option label="配送中" value="2" />
          <el-option label="已完成" value="3" />
          <el-option label="已取消" value="4" />
        </el-select>
        <el-select v-model="query.type" placeholder="类型" clearable style="width: 140px" @change="handleSearch">
          <el-option label="取快递" value="1" />
          <el-option label="代买" value="2" />
          <el-option label="代办" value="3" />
          <el-option label="其他" value="4" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" circle @click="fetchData" />
        <el-tag v-if="abnormalCount > 0" type="danger" effect="dark" style="margin-left: auto">
          本页异常订单 {{ abnormalCount }} 条
        </el-tag>
      </div>

      <el-table
        v-loading="loading"
        :data="list"
        border
        stripe
        size="small"
        :row-class-name="rowClassName"
      >
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.type)">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="悬赏" width="100" align="right">
          <template #default="{ row }">
            <span style="color: #ff6a00; font-weight: 600">{{ fmtMoney(row.reward) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发单人" width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.publisherNickname || '-' }}</template>
        </el-table-column>
        <el-table-column label="接单人" width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.runnerNickname || '待接单' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="期望时间" width="140">
          <template #default="{ row }">{{ fmtDateTime(row.expect_time) }}</template>
        </el-table-column>
        <el-table-column label="异常" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.abnormal === true || row.abnormal === 1" type="danger" effect="dark" size="small">
              异常
            </el-tag>
            <span v-else style="color: #c0c4cc">正常</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-bar">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          @current-change="fetchData"
          @size-change="handleSearch"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getErrandList } from '../api'
import { fmtDateTime, fmtMoney } from '../utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  status: '',
  type: '',
  keyword: '',
  page: 1,
  size: 10
})

const typeText = (t) => ({ 1: '取快递', 2: '代买', 3: '代办', 4: '其他' }[t] ?? '未知')
const typeTag = (t) => ({ 1: 'primary', 2: 'success', 3: 'warning', 4: 'info' }[t] ?? 'info')
const statusText = (s) => ({ 0: '待接单', 1: '已接单', 2: '配送中', 3: '已完成', 4: '已取消' }[s] ?? '未知')
const statusTag = (s) => ({ 0: 'warning', 1: 'primary', 2: 'danger', 3: 'success', 4: 'info' }[s] ?? 'info')

const abnormalCount = computed(
  () => list.value.filter((r) => r.abnormal === true || r.abnormal === 1).length
)

const rowClassName = ({ row }) =>
  row.abnormal === true || row.abnormal === 1 ? 'errand-abnormal-row' : ''

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getErrandList({
      status: query.status === '' ? undefined : query.status,
      type: query.type === '' ? undefined : query.type,
      keyword: query.keyword || undefined,
      page: query.page,
      size: query.size
    })
    list.value = data?.list || []
    total.value = data?.total || 0
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  query.page = 1
  fetchData()
}

fetchData()
</script>
