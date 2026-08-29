<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-input
          v-model="query.keyword"
          placeholder="昵称 / 手机号 / 学号"
          clearable
          style="width: 240px"
          :prefix-icon="Search"
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
        <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px" @change="handleSearch">
          <el-option label="正常" value="1" />
          <el-option label="已封禁" value="0" />
        </el-select>
        <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
        <el-button :icon="Refresh" circle @click="fetchData" />
      </div>

      <el-table v-loading="loading" :data="list" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="nickname" label="昵称" min-width="110" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="student_no" label="学号" width="110">
          <template #default="{ row }">{{ row.student_no || '-' }}</template>
        </el-table-column>
        <el-table-column label="认证状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="authTag(row.auth_status)" size="small">{{ authText(row.auth_status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="信用分" width="90" align="center">
          <template #default="{ row }">
            <span :style="{ color: creditColor(row.credit_score), fontWeight: 600 }">
              {{ row.credit_score ?? '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="跑男" width="80" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.is_runner === 1 || row.is_runner === true" type="danger" size="small">跑男</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="账号状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '正常' : '已封禁' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="140">
          <template #default="{ row }">{{ fmtDateTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button size="small" @click="openCreditDrawer(row)">信用流水</el-button>
            <el-button
              v-if="row.status === 1"
              type="danger"
              size="small"
              @click="handleBan(row)"
            >封禁</el-button>
            <el-button
              v-else
              type="success"
              size="small"
              @click="handleUnban(row)"
            >解封</el-button>
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

    <!-- 信用流水抽屉 -->
    <el-drawer v-model="drawerVisible" :title="`信用流水 · ${creditUser?.nickname || ''}`" size="560px">
      <div style="padding: 0 20px 20px">
        <el-table v-loading="creditLoading" :data="creditLogs" border stripe size="small">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="变动分值" width="100" align="center">
            <template #default="{ row }">
              <span :style="{ color: (row.delta ?? row.change_score) >= 0 ? '#67c23a' : '#f56c6c', fontWeight: 600 }">
                {{ (row.delta ?? row.change_score) >= 0 ? '+' : '' }}{{ row.delta ?? row.change_score }}
              </span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="变动原因" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">{{ row.reason || row.remark || '-' }}</template>
          </el-table-column>
          <el-table-column label="时间" width="140">
            <template #default="{ row }">{{ fmtDateTime(row.create_time) }}</template>
          </el-table-column>
        </el-table>
        <div class="pager-bar">
          <el-pagination
            v-model:current-page="creditQuery.page"
            v-model:page-size="creditQuery.size"
            :total="creditTotal"
            layout="total, prev, pager, next"
            small
            @current-change="fetchCreditLogs"
          />
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getUserList, banUser, unbanUser, getUserCreditLogs } from '../api'
import { fmtDateTime } from '../utils/format'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  status: '',
  page: 1,
  size: 10
})

const authText = (s) => ({ 0: '未认证', 1: '已认证', 2: '审核中', 3: '已驳回' }[s] ?? '未知')
const authTag = (s) => ({ 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }[s] ?? 'info')
const creditColor = (score) => {
  if (score == null) return '#909399'
  if (score >= 80) return '#67c23a'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getUserList({
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

const handleBan = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认封禁用户「${row.nickname}」吗？封禁后该用户将无法正常使用平台。`,
      '封禁确认',
      { confirmButtonText: '封禁', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await banUser(row.id)
    ElMessage.success('已封禁')
    fetchData()
  } catch {
    // 拦截器已提示
  }
}

const handleUnban = async (row) => {
  try {
    await ElMessageBox.confirm(`确认解封用户「${row.nickname}」吗？`, '解封确认', {
      confirmButtonText: '解封',
      cancelButtonText: '取消',
      type: 'success'
    })
  } catch {
    return
  }
  try {
    await unbanUser(row.id)
    ElMessage.success('已解封')
    fetchData()
  } catch {
    // 拦截器已提示
  }
}

// ============ 信用流水 ============
const drawerVisible = ref(false)
const creditLoading = ref(false)
const creditLogs = ref([])
const creditTotal = ref(0)
const creditUser = ref(null)
const creditQuery = reactive({ page: 1, size: 10 })

const openCreditDrawer = (row) => {
  creditUser.value = row
  creditQuery.page = 1
  drawerVisible.value = true
  fetchCreditLogs()
}

const fetchCreditLogs = async () => {
  creditLoading.value = true
  try {
    const data = await getUserCreditLogs(creditUser.value.id, {
      page: creditQuery.page,
      size: creditQuery.size
    })
    creditLogs.value = data?.list || []
    creditTotal.value = data?.total || 0
  } catch {
    // 拦截器已提示
  } finally {
    creditLoading.value = false
  }
}

fetchData()
</script>
