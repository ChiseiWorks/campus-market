<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-radio-group v-model="query.status" @change="handleSearch">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="0">待处理</el-radio-button>
          <el-radio-button value="1">处理中</el-radio-button>
          <el-radio-button value="2">已办结</el-radio-button>
        </el-radio-group>
        <el-button :icon="Refresh" circle @click="fetchData" />
      </div>

      <el-table v-loading="loading" :data="list" border stripe size="small">
        <el-table-column prop="id" label="编号" width="70" align="center" />
        <el-table-column label="关联订单" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="row.order_type === 2 ? 'warning' : 'primary'" size="small">
              {{ row.order_type === 2 ? '跑腿单' : '闲置单' }}
            </el-tag>
            <span style="margin-left: 4px">#{{ row.order_id }}</span>
          </template>
        </el-table-column>
        <el-table-column label="投诉人" width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.plaintiff_nickname || row.plaintiff_id }}</template>
        </el-table-column>
        <el-table-column label="被投诉人" width="100" show-overflow-tooltip>
          <template #default="{ row }">{{ row.defendant_nickname || row.defendant_id }}</template>
        </el-table-column>
        <el-table-column label="投诉类型" width="130" align="center">
          <template #default="{ row }">{{ typeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="content" label="投诉内容" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="发起时间" width="140">
          <template #default="{ row }">{{ fmtDateTime(row.create_time) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="openDetail(row)">详情</el-button>
            <el-button v-if="row.status !== 2" type="danger" size="small" @click="openHandle(row)">
              处理
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="投诉详情" size="480px">
      <div v-loading="detailLoading" class="complaint-detail">
        <template v-if="detail.complaint">
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="投诉编号">#{{ detail.complaint.id }}</el-descriptions-item>
            <el-descriptions-item label="关联订单">
              {{ detail.complaint.order_type === 2 ? '跑腿单' : '闲置单' }} #{{
                detail.complaint.order_id
              }}
            </el-descriptions-item>
            <el-descriptions-item label="投诉人">
              {{ detail.complaint.plaintiff_nickname || detail.complaint.plaintiff_id }}
            </el-descriptions-item>
            <el-descriptions-item label="被投诉人">
              {{ detail.complaint.defendant_nickname || detail.complaint.defendant_id }}
            </el-descriptions-item>
            <el-descriptions-item label="投诉类型">
              {{ typeText(detail.complaint.type) }}
            </el-descriptions-item>
            <el-descriptions-item label="投诉内容">
              {{ detail.complaint.content }}
            </el-descriptions-item>
            <el-descriptions-item label="状态">
              <el-tag :type="statusTag(detail.complaint.status)" size="small">
                {{ statusText(detail.complaint.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.complaint.result" label="处理结果">
              {{ detail.complaint.result }}
            </el-descriptions-item>
            <el-descriptions-item v-if="detail.complaint.handle_time" label="办结时间">
              {{ fmtDateTime(detail.complaint.handle_time) }}
            </el-descriptions-item>
          </el-descriptions>

          <div v-if="detail.complaint.evidence?.length" class="evidence-block">
            <div class="block-title">证据材料</div>
            <el-image
              v-for="(url, i) in detail.complaint.evidence"
              :key="i"
              :src="url"
              :preview-src-list="detail.complaint.evidence"
              :initial-index="i"
              preview-teleported
              fit="cover"
              style="width: 88px; height: 88px; border-radius: 6px; margin: 0 8px 8px 0"
            >
              <template #error>
                <el-icon :size="24" color="#c0c4cc"><Picture /></el-icon>
              </template>
            </el-image>
          </div>

          <div v-if="detail.order" class="order-block">
            <div class="block-title">关联订单快照</div>
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item
                v-for="(val, key) in orderSnapshot"
                :key="key"
                :label="key"
              >
                {{ val }}
              </el-descriptions-item>
            </el-descriptions>
          </div>
        </template>
      </div>
    </el-drawer>

    <!-- 处理对话框 -->
    <el-dialog v-model="handleVisible" title="办结投诉" width="520px">
      <el-form label-width="110px">
        <el-form-item label="投诉编号">
          <span>#{{ currentRow?.id }}（{{ typeText(currentRow?.type) }}）</span>
        </el-form-item>
        <el-form-item label="处理结果" required>
          <el-input
            v-model="handleForm.result"
            type="textarea"
            :rows="4"
            placeholder="请填写仲裁结论与处理说明（必填，将同步给双方）"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="被投诉人信用">
          <el-input-number
            v-model="handleForm.defendantCreditDelta"
            :min="-50"
            :max="50"
            :step="1"
            controls-position="right"
          />
          <div class="form-tip">负数扣分、正数加分、0 不变；留空则不调整信用分</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="submitHandle">确认办结</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Picture } from '@element-plus/icons-vue'
import { getComplaintList, getComplaintDetail, handleComplaint } from '../api'
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

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref({ complaint: null, order: null })

const handleVisible = ref(false)
const currentRow = ref(null)
const handleForm = reactive({
  result: '',
  defendantCreditDelta: undefined
})

const typeText = (t) =>
  ({ 1: '爽约', 2: '商品与描述不符', 3: '物品损坏', 4: '态度恶劣', 5: '其他' }[t] ?? '未知')
const statusText = (s) => ({ 0: '待处理', 1: '处理中', 2: '已办结' }[s] ?? '未知')
const statusTag = (s) => ({ 0: 'danger', 1: 'warning', 2: 'success' }[s] ?? 'info')

// 订单快照：过滤掉过长/无意义字段，做简单中文映射
const ORDER_LABELS = {
  id: '订单号',
  title: '标题',
  goods_title: '商品标题',
  price: '价格',
  amount: '金额',
  status: '订单状态',
  create_time: '下单时间',
  address: '地址',
  remark: '备注'
}
const orderSnapshot = computed(() => {
  const order = detail.value?.order
  if (!order || typeof order !== 'object') return {}
  const out = {}
  for (const [key, label] of Object.entries(ORDER_LABELS)) {
    if (order[key] !== undefined && order[key] !== null && order[key] !== '') {
      out[label] = order[key]
    }
  }
  // 若映射不到任何字段，兜底展示前 6 个原始字段
  if (Object.keys(out).length === 0) {
    Object.entries(order)
      .slice(0, 6)
      .forEach(([k, v]) => {
        out[k] = v
      })
  }
  return out
})

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getComplaintList({
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

const openDetail = async (row) => {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = { complaint: null, order: null }
  try {
    const data = await getComplaintDetail(row.id)
    detail.value = data || { complaint: null, order: null }
  } catch {
    // 拦截器已提示
  } finally {
    detailLoading.value = false
  }
}

const openHandle = (row) => {
  currentRow.value = row
  handleForm.result = ''
  handleForm.defendantCreditDelta = undefined
  handleVisible.value = true
}

const submitHandle = async () => {
  if (!handleForm.result.trim()) {
    ElMessage.warning('请填写处理结果')
    return
  }
  submitting.value = true
  try {
    await handleComplaint(currentRow.value.id, {
      result: handleForm.result.trim(),
      defendantCreditDelta: handleForm.defendantCreditDelta
    })
    ElMessage.success('已办结')
    handleVisible.value = false
    fetchData()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

fetchData()
</script>

<style scoped>
.complaint-detail .block-title {
  font-weight: 600;
  margin: 16px 0 8px;
  color: #303133;
}
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}
</style>
