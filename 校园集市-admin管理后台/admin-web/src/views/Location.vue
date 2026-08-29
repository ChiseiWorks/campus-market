<template>
  <div class="page-container">
    <div class="page-card">
      <div class="filter-bar">
        <el-radio-group v-model="filterType" @change="applyFilter">
          <el-radio-button value="">全部类型</el-radio-button>
          <el-radio-button :value="1">宿舍楼</el-radio-button>
          <el-radio-button :value="2">教学楼</el-radio-button>
          <el-radio-button :value="3">快递点</el-radio-button>
          <el-radio-button :value="4">食堂</el-radio-button>
          <el-radio-button :value="5">其他</el-radio-button>
        </el-radio-group>
        <div>
          <el-button :icon="Refresh" circle @click="fetchData" style="margin-right: 8px" />
          <el-button type="primary" :icon="Plus" @click="openEdit(null)">新增地点</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="filteredList" border stripe size="small">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="name" label="地点名称" min-width="160" show-overflow-tooltip />
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTag(row.type)" size="small">{{ typeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序值" width="90" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用中' : '已停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" plain @click="openEdit(row)">编辑</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              size="small"
              @click="handleToggle(row)"
            >
              {{ row.status === 1 ? '停用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑地点' : '新增地点'" width="460px">
      <el-form label-width="80px">
        <el-form-item label="名称" required>
          <el-input v-model="editForm.name" placeholder="如：梅苑 3 号楼" maxlength="50" />
        </el-form-item>
        <el-form-item label="类型" required>
          <el-select v-model="editForm.type" style="width: 100%">
            <el-option :value="1" label="宿舍楼" />
            <el-option :value="2" label="教学楼" />
            <el-option :value="3" label="快递点" />
            <el-option :value="4" label="食堂" />
            <el-option :value="5" label="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序值">
          <el-input-number v-model="editForm.sort" :min="0" :max="9999" controls-position="right" />
          <div class="form-tip">数值越小越靠前</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh, Plus } from '@element-plus/icons-vue'
import { getLocationList, saveLocation, toggleLocation } from '../api'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const filterType = ref('')

const editVisible = ref(false)
const editForm = reactive({
  id: null,
  name: '',
  type: 1,
  sort: 0
})

const typeText = (t) =>
  ({ 1: '宿舍楼', 2: '教学楼', 3: '快递点', 4: '食堂', 5: '其他' }[t] ?? '未知')
const typeTag = (t) =>
  ({ 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger', 5: 'info' }[t] ?? 'info')

const filteredList = computed(() => {
  if (filterType.value === '') return list.value
  return list.value.filter((item) => item.type === filterType.value)
})

const applyFilter = () => {
  // computed 自动响应，这里仅做语义占位
}

const fetchData = async () => {
  loading.value = true
  try {
    const data = await getLocationList()
    list.value = Array.isArray(data) ? data : data?.list || []
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

const openEdit = (row) => {
  if (row) {
    editForm.id = row.id
    editForm.name = row.name
    editForm.type = row.type
    editForm.sort = row.sort ?? 0
  } else {
    editForm.id = null
    editForm.name = ''
    editForm.type = 1
    editForm.sort = 0
  }
  editVisible.value = true
}

const submitEdit = async () => {
  if (!editForm.name.trim()) {
    ElMessage.warning('请填写地点名称')
    return
  }
  submitting.value = true
  try {
    const payload = {
      name: editForm.name.trim(),
      type: editForm.type,
      sort: editForm.sort
    }
    if (editForm.id) payload.id = editForm.id
    await saveLocation(payload)
    ElMessage.success(editForm.id ? '已保存修改' : '已新增地点')
    editVisible.value = false
    fetchData()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

const handleToggle = async (row) => {
  const action = row.status === 1 ? '停用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确认${action}「${row.name}」吗？${row.status === 1 ? '停用后用户端将不再展示该地点。' : ''}`,
      `${action}确认`,
      { confirmButtonText: action, cancelButtonText: '取消', type: 'warning' }
    )
  } catch {
    return
  }
  try {
    await toggleLocation(row.id)
    ElMessage.success(`已${action}`)
    fetchData()
  } catch {
    // 拦截器已提示
  }
}

fetchData()
</script>

<style scoped>
.form-tip {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 4px;
}
</style>
