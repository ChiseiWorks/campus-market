<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-radio-group v-model="query.status" @change="handleSearch">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="0">待审核</el-radio-button>
          <el-radio-button value="1">已通过</el-radio-button>
          <el-radio-button value="2">已驳回</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" circle @click="fetchData" />
      </div>

      <el-table v-loading="loading" :data="list" border stripe size="small">
        <el-table-column prop="student_no" label="学号" width="110" />
        <el-table-column prop="real_name" label="真实姓名" width="100" />
        <el-table-column prop="nickname" label="昵称" width="110" show-overflow-tooltip />
        <el-table-column prop="phone" label="手机号" width="120" />
        <el-table-column prop="college" label="学院" min-width="130" show-overflow-tooltip />
        <el-table-column prop="dorm_building" label="宿舍楼" width="100" />
        <el-table-column label="申请类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 2" type="danger" effect="dark" size="small">跑男申请</el-tag>
            <el-tag v-else type="info" size="small">普通认证</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="认证材料" width="90" align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.material_url"
              :src="row.material_url"
              :preview-src-list="[row.material_url]"
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
        <el-table-column label="审核状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.audit_status)" size="small">
              {{ statusText(row.audit_status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="audit_remark" label="审核备注" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">{{ row.audit_remark || '-' }}</template>
        </el-table-column>
        <el-table-column label="申请时间" width="140">
          <template #default="{ row }">{{ fmtDateTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <template v-if="row.audit_status === 0">
              <el-button type="success" size="small" @click="handleApprove(row)">通过</el-button>
              <el-button type="danger" size="small" @click="openReject(row)">驳回</el-button>
            </template>
            <span v-else style="color: #c0c4cc">已处理</span>
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

    <!-- 驳回对话框 -->
    <el-dialog v-model="rejectVisible" title="驳回认证申请" width="480px">
      <el-form label-width="80px">
        <el-form-item label="申请人">
          <span>{{ currentRow?.real_name }}（{{ currentRow?.student_no }}）</span>
        </el-form-item>
        <el-form-item label="驳回原因" required>
          <el-input
            v-model="rejectRemark"
            type="textarea"
            :rows="3"
            placeholder="请填写驳回原因（必填，将同步给用户）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Picture } from '@element-plus/icons-vue'
import { getAuthList, approveAuth, rejectAuth } from '../api'
import { fmtDateTime } from '../utils/format'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  status: '',
  page: 1,
  size: 10
})

const rejectVisible = ref(false)
const rejectRemark = ref('')
const currentRow = ref(null)

const statusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已驳回' }[s] ?? '未知')
const statusTag = (s) => ({ 0: 'warning', 1: 'success', 2: 'danger' }[s] ?? 'info')

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getAuthList({
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

const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认通过「${row.real_name}」的${row.type === 2 ? '跑男' : '认证'}申请吗？`,
      '通过确认',
      { confirmButtonText: '通过', cancelButtonText: '取消', type: 'success' }
    )
  } catch {
    return
  }
  try {
    await approveAuth(row.id)
    ElMessage.success('已通过')
    fetchData()
  } catch {
    // 拦截器已提示
  }
}

const openReject = (row) => {
  currentRow.value = row
  rejectRemark.value = ''
  rejectVisible.value = true
}

const handleReject = async () => {
  if (!rejectRemark.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  submitting.value = true
  try {
    await rejectAuth(currentRow.value.id, rejectRemark.value.trim())
    ElMessage.success('已驳回')
    rejectVisible.value = false
    fetchData()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

fetchData()
</script>
