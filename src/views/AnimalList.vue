<template>
  <div class="animal-list">
    <!-- 搜索筛选栏 -->
    <el-card shadow="never" class="search-card">
      <el-form :model="queryForm" inline>
        <el-form-item label="动物名称">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入名称搜索"
            clearable
            :prefix-icon="Search"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="领养状态">
          <el-select v-model="queryForm.status" placeholder="全部状态" clearable style="width: 160px">
            <el-option label="待领养" value="pending" />
            <el-option label="已领养" value="adopted" />
            <el-option label="生病中" value="sick" />
            <el-option label="治疗中" value="treating" />
          </el-select>
        </el-form-item>
        <el-form-item label="种类">
          <el-select v-model="queryForm.species" placeholder="全部种类" clearable style="width: 140px">
            <el-option label="猫" value="猫" />
            <el-option label="狗" value="狗" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">查询</el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="search-card__actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">新增动物</el-button>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card shadow="never" class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        stripe
        border
        style="width: 100%"
        :default-sort="{ prop: 'entryDate', order: 'descending' }"
      >
        <el-table-column prop="animalId" label="动物 ID" width="100" align="center" />
        <el-table-column prop="name" label="名称" width="140" />
        <el-table-column prop="species" label="种类" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.species === '狗' ? 'warning' : ''" size="small" effect="plain">
              {{ row.species }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="breed" label="品种" width="140" />
        <el-table-column prop="gender" label="性别" width="80" align="center">
          <template #default="{ row }">
            <span :style="{ color: row.gender === '公' ? '#409EFF' : '#F56C6C' }">
              {{ row.gender === '公' ? '♂' : '♀' }} {{ row.gender }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="entryDate" label="入站日期" width="130" align="center" sortable />
        <el-table-column prop="adoptStatus" label="领养状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.adoptStatus)" effect="dark">
              {{ statusLabel(row.adoptStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="info" size="small" :icon="Document" link @click="handleViewMedical(row)">
              档案
            </el-button>
            <el-button
              type="success"
              size="small"
              :icon="Present"
              link
              :disabled="row.adoptStatus !== 'pending'"
              @click="handleAdopt(row)"
            >
              领养
            </el-button>
            <el-button
              type="danger"
              size="small"
              :icon="Delete"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleQuery"
          @current-change="handleQuery"
        />
      </div>
    </el-card>

    <!-- ==================== 新增动物弹窗 ==================== -->
    <el-dialog v-model="addDialogVisible" title="新增动物" width="520px" :close-on-click-modal="false">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="90px" status-icon>
        <el-form-item label="动物名称" prop="name">
          <el-input v-model="addForm.name" placeholder="请输入动物名称" clearable />
        </el-form-item>
        <el-form-item label="种类" prop="species">
          <el-select v-model="addForm.species" placeholder="请选择种类" style="width: 100%">
            <el-option label="狗" value="狗" />
            <el-option label="猫" value="猫" />
          </el-select>
        </el-form-item>
        <el-form-item label="品种" prop="breed">
          <el-input v-model="addForm.breed" placeholder="如：中华田园犬、金毛、英短..." />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="addForm.gender">
            <el-radio value="公">公</el-radio>
            <el-radio value="母">母</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="入站日期" prop="entryDate">
          <el-date-picker v-model="addForm.entryDate" type="date" placeholder="选择日期"
            value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitAdd">确认新增</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, RefreshRight, Plus, Document, Present, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAnimalList, createAnimal, deleteAnimal } from '@/api/animals'
import { mockAnimals } from '@/store/mockData'

// ==================== 筛选表单 ====================
const queryForm = reactive({ name: '', status: '', species: '' })

// ==================== 状态映射 ====================
const statusMap = {
  pending: { label: '待领养', type: '' },
  adopted: { label: '已领养', type: 'success' },
  sick: { label: '生病中', type: 'danger' },
  treating: { label: '治疗中', type: 'warning' }
}
const statusTagType = (status) => statusMap[status]?.type || 'info'
const statusLabel = (status) => statusMap[status]?.label || status

// ==================== 表格与分页 ====================
const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })

// ==================== 前端兜底筛选 ====================
const applyFallbackFilter = () => {
  let list = [...mockAnimals]
  if (queryForm.name) list = list.filter(a => a.name.includes(queryForm.name))
  if (queryForm.status) list = list.filter(a => a.adoptStatus === queryForm.status)
  if (queryForm.species) list = list.filter(a => a.species === queryForm.species)
  pagination.total = list.length
  const start = (pagination.page - 1) * pagination.pageSize
  tableData.value = list.slice(start, start + pagination.pageSize)
}

// ==================== 新增动物弹窗 ====================
const addDialogVisible = ref(false)
const submitting = ref(false)
const addFormRef = ref(null)
const addForm = reactive({
  name: '',
  species: '',
  breed: '',
  gender: '公',
  entryDate: ''
})

const addRules = {
  name: [{ required: true, message: '请输入动物名称', trigger: 'blur' }],
  species: [{ required: true, message: '请选择种类', trigger: 'change' }],
  entryDate: [{ required: true, message: '请选择入站日期', trigger: 'change' }]
}

const handleAdd = () => {
  addForm.name = ''
  addForm.species = ''
  addForm.breed = ''
  addForm.gender = '公'
  addForm.entryDate = ''
  addDialogVisible.value = true
}

const handleSubmitAdd = async () => {
  if (!addFormRef.value) return
  try { await addFormRef.value.validate() } catch { return }
  submitting.value = true
  try {
    await createAnimal({ ...addForm })
    ElMessage.success('新增动物成功！')
    addDialogVisible.value = false
    handleQuery()
  } catch {
    // 后端未启动则写入本地 mock
    const maxId = mockAnimals.reduce((max, a) => Math.max(max, a.animalId), 0)
    mockAnimals.push({ animalId: maxId + 1, ...addForm, adoptStatus: 'pending' })
    ElMessage.success('（Mock 模式）新增成功！')
    addDialogVisible.value = false
    handleQuery()
  } finally {
    submitting.value = false
  }
}

// ==================== 方法 ====================
const handleQuery = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      page_size: pagination.pageSize,
      name: queryForm.name || undefined,
      adopt_status: queryForm.status || undefined,
      species: queryForm.species || undefined
    }
    const res = await getAnimalList(params)
    // 按后端统一格式: { data: { list: [], total: number } }
    tableData.value = res.data?.list ?? res.data ?? []
    pagination.total = res.data?.total ?? tableData.value.length
  } catch {
    applyFallbackFilter()
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  queryForm.name = ''
  queryForm.status = ''
  queryForm.species = ''
  pagination.page = 1
  handleQuery()
}

const handleViewMedical = (row) => {
  router.push({ path: '/animals/medical', query: { animal_id: row.animalId } })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该动物档案吗？删除后相关的医疗和领养记录可能会受到影响，此操作不可逆！',
    '删除确认',
    { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger' }
  ).then(async () => {
    try {
      await deleteAnimal(row.animalId)
      ElMessage.success('删除成功')
      handleQuery()
    } catch {
      // 后端未启动时从本地 mock 中移除
      const idx = mockAnimals.findIndex(a => a.animalId === row.animalId)
      if (idx !== -1) { mockAnimals.splice(idx, 1) }
      ElMessage.success('（Mock 模式）已从本地移除')
      handleQuery()
    }
  }).catch(() => {})
}

const handleAdopt = (row) => {
  ElMessageBox.confirm(
    `确认要为「${row.name}」（${row.breed}）办理领养手续吗？`,
    '领养确认',
    { confirmButtonText: '去办理', cancelButtonText: '取消', type: 'info' }
  ).then(() => {
    router.push({ path: '/adoption', query: { animal_id: row.animalId } })
  }).catch(() => {})
}

onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.animal-list {
  padding: 20px;
}

.search-card {
  margin-bottom: 16px;
}

.search-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 8px;
}

.search-card__actions {
  display: flex;
  align-items: center;
  padding-top: 4px;
}

.table-card {
  min-height: 400px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
