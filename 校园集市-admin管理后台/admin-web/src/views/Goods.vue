<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input
          v-model="query.keyword"
          placeholder="商品标题 / 卖家昵称"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="在售" value="1" />
          <el-option label="已售出" value="2" />
          <el-option label="已下架" value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" circle @click="fetchData" />
      </div>

      <el-table v-loading="loading" :data="list" border stripe size="small">
        <el-table-column label="图片" width="80" align="center">
          <template #default="{ row }">
            <el-image
              v-if="coverUrl(row)"
              :src="coverUrl(row)"
              :preview-src-list="[coverUrl(row)]"
              preview-teleported
              fit="cover"
              style="width: 44px; height: 44px; border-radius: 4px"
            >
              <template #error>
                <el-icon :size="20" color="#c0c4cc"><Picture /></el-icon>
              </template>
            </el-image>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="商品标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sellerNickname" label="卖家" width="110" show-overflow-tooltip>
          <template #default="{ row }">{{ row.sellerNickname || '-' }}</template>
        </el-table-column>
        <el-table-column label="价格" width="110" align="right">
          <template #default="{ row }">
            <span style="color: #ff6a00; font-weight: 600">{{ fmtMoney(row.price) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="category" label="分类" width="100">
          <template #default="{ row }">{{ row.category || row.category_name || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="140">
          <template #default="{ row }">{{ fmtDateTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="openTakedown(row)"
            >违规下架</el-button>
            <span v-else style="color: #c0c4cc">-</span>
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

    <!-- 违规下架对话框 -->
    <el-dialog v-model="takedownVisible" title="违规下架商品" width="480px">
      <el-form label-width="80px">
        <el-form-item label="商品">
          <span>{{ currentRow?.title }}</span>
        </el-form-item>
        <el-form-item label="下架原因" required>
          <el-input
            v-model="takedownReason"
            type="textarea"
            :rows="3"
            placeholder="请填写下架原因（必填，将通知卖家）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="takedownVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleTakedown">确认下架</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Picture } from '@element-plus/icons-vue'
import { getGoodsList, takedownGoods } from '../api'
import { fmtDateTime, fmtMoney } from '../utils/format'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  status: '',
  page: 1,
  size: 10
})

const takedownVisible = ref(false)
const takedownReason = ref('')
const currentRow = ref(null)

const statusText = (s) => ({ 0: '已下架', 1: '在售', 2: '已售出' }[s] ?? '未知')
const statusTag = (s) => ({ 0: 'info', 1: 'success', 2: 'warning' }[s] ?? 'info')

const coverUrl = (row) => {
  if (row.cover_url) return row.cover_url
  if (Array.isArray(row.images) && row.images.length) return row.images[0]
  if (typeof row.images === 'string' && row.images) {
    try {
      const arr = JSON.parse(row.images)
      if (Array.isArray(arr) && arr.length) return arr[0]
    } catch {
      return row.images.split(',')[0]
    }
  }
  return ''
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getGoodsList({
      keyword: query.keyword || undefined,
      status: query.status === '' ? undefined : query.status,
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

const openTakedown = (row) => {
  currentRow.value = row
  takedownReason.value = ''
  takedownVisible.value = true
}

const handleTakedown = async () => {
  if (!takedownReason.value.trim()) {
    ElMessage.warning('请填写下架原因')
    return
  }
  submitting.value = true
  try {
    await takedownGoods(currentRow.value.id, takedownReason.value.trim())
    ElMessage.success('已下架')
    takedownVisible.value = false
    fetchData()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

fetchData()
</script>
