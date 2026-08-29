<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-radio-group v-model="query.status" @change="handleSearch">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="1">已发布</el-radio-button>
          <el-radio-button value="0">已下线</el-radio-button>
        </el-radio-group>
        <div>
          <el-button :icon="Refresh" circle @click="fetchData" style="margin-right: 8px" />
          <el-button type="primary" :icon="Plus" @click="openPublish">发布公告</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="list" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="公告标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容摘要" min-width="240" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '已下线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发布时间" width="150">
          <template #default="{ row }">{{ fmtDateTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="openPreview(row)">预览</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" @click="handleOffline(row)">
              下线
            </el-button>
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

    <!-- 发布公告对话框 -->
    <el-dialog v-model="publishVisible" title="发布公告" width="560px">
      <el-form label-width="80px">
        <el-form-item label="标题" required>
          <el-input v-model="publishForm.title" placeholder="公告标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input
            v-model="publishForm.content"
            type="textarea"
            :rows="8"
            placeholder="公告正文，发布后立即对全站用户可见"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitPublish">立即发布</el-button>
      </template>
    </el-dialog>

    <!-- 预览对话框 -->
    <el-dialog v-model="previewVisible" :title="previewRow?.title" width="560px">
      <div class="notice-content">{{ previewRow?.content }}</div>
      <template #footer>
        <span class="preview-time">发布于 {{ fmtDateTime(previewRow?.create_time) }}</span>
        <el-button @click="previewVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { getNoticeList, publishNotice, offlineNotice } from '../api'
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

const publishVisible = ref(false)
const publishForm = reactive({
  title: '',
  content: ''
})

const previewVisible = ref(false)
const previewRow = ref(null)

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getNoticeList({
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

const openPublish = () => {
  publishForm.title = ''
  publishForm.content = ''
  publishVisible.value = true
}

const submitPublish = async () => {
  if (!publishForm.title.trim()) {
    ElMessage.warning('请填写公告标题')
    return
  }
  if (!publishForm.content.trim()) {
    ElMessage.warning('请填写公告内容')
    return
  }
  submitting.value = true
  try {
    await publishNotice({
      title: publishForm.title.trim(),
      content: publishForm.content.trim()
    })
    ElMessage.success('公告已发布')
    publishVisible.value = false
    handleSearch()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

const openPreview = (row) => {
  previewRow.value = row
  previewVisible.value = true
}

const handleOffline = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确认下线公告「${row.title}」吗？下线后用户端将不再展示。`,
      '下线确认',
      { confirmButtonText: '下线', cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await offlineNotice(row.id)
    ElMessage.success('已下线')
    fetchData()
  } catch {
    // 拦截器已提示
  }
}

fetchData()
</script>

<style scoped>
.notice-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
}
.preview-time {
  float: left;
  font-size: 12px;
  color: #909399;
  line-height: 32px;
}
</style>
