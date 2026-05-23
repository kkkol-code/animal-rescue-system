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

    <!-- 🐾 卡片式动物档案网格 -->
    <el-card shadow="never" class="table-card" v-loading="loading">
      <template #header>
        <span class="card-header-title">🐾 动物档案 · 共 {{ pagination.total }} 只小可爱</span>
      </template>

      <el-row :gutter="16" class="animal-grid">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="row in tableData" :key="row.animalId">
          <div class="animal-card" :class="'animal-card--' + row.adoptStatus">
            <!-- 卡片头部：状态 + ID -->
            <div class="ac-header">
              <el-tag :type="statusTagType(row.adoptStatus)" effect="dark" size="small" class="ac-tag">
                <span class="ac-tag-emoji">{{ statusEmoji(row.adoptStatus) }}</span>
                {{ statusLabel(row.adoptStatus) }}
              </el-tag>
              <span class="ac-id">#{{ row.animalId }}</span>
            </div>

            <!-- 卡片主体 -->
            <div class="ac-body">
              <div class="ac-species-badge">
                {{ row.species === '狗' ? '🐕' : '🐱' }}
              </div>
              <div class="ac-name">{{ row.name }}</div>
              <div class="ac-breed">{{ row.breed }}</div>
              <div class="ac-meta">
                <span class="ac-gender" :class="'ac-gender--' + row.gender">
                  {{ row.gender === '公' ? '♂' : '♀' }}
                </span>
                <span class="ac-date">📅 {{ row.entryDate }}</span>
              </div>
            </div>

            <!-- 卡片底部：操作按钮 -->
            <div class="ac-footer">
              <el-button size="small" round @click="handleViewMedical(row)">
                🩺 档案
              </el-button>
              <el-button size="small" type="success" round
                :disabled="row.adoptStatus !== 'pending'"
                @click="handleAdopt(row)">
                🏠 领养
              </el-button>
              <el-button size="small" type="danger" round @click="handleDelete(row)">
                🗑 删除
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="!loading && tableData.length === 0" description="还没有小动物档案哦~ 🐱">
        <el-button type="primary" round @click="handleAdd">🐾 添加第一只动物</el-button>
      </el-empty>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[8, 16, 24]"
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
import { Search, RefreshRight, Plus } from '@element-plus/icons-vue'
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
const statusEmoji = (status) => ({ pending: '🐱', adopted: '🏡', sick: '🤒', treating: '💊' }[status] || '🐾')

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
    ElMessage.error('新增失败，请确认后端服务已启动')
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
      ElMessage.error('删除失败，请确认后端服务已启动')
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
.animal-list { padding: 24px; }
.search-card { margin-bottom: 18px; }
.search-card :deep(.el-card__body) {
  display: flex; justify-content: space-between; align-items: flex-start;
  flex-wrap: wrap; gap: 10px; padding: 18px 22px;
}
.search-card__actions { display: flex; align-items: center; padding-top: 4px; }
.card-header-title { font-weight: 700; color: #5EA87E; font-size: 15px; }
.table-card :deep(.el-card__body) { padding: 18px 22px; }
.animal-grid { min-height: 200px; }

/* 动物卡片 */
.animal-card {
  background: #FFFEFB;
  border-radius: 20px;
  border: 2px solid #F5EDE0;
  padding: 0;
  margin-bottom: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(152,216,168,0.08);
}
.animal-card:hover {
  transform: translateY(-6px);
  box-shadow: 0 12px 32px rgba(152,216,168,0.2);
  border-color: #C8F0D0;
}

/* 状态色边框 */
.animal-card--pending { border-left: 5px solid #98D8A8; }
.animal-card--adopted { border-left: 5px solid #A0C8A0; opacity: 0.85; }
.animal-card--sick { border-left: 5px solid #F4A0A0; }
.animal-card--treating { border-left: 5px solid #FFD0A0; }

.ac-header {
  display: flex; justify-content: space-between; align-items: center;
  padding: 14px 16px 0;
}
.ac-tag-emoji { margin-right: 2px; }
.ac-id { color: #C8C0B0; font-size: 12px; font-weight: 600; }

.ac-body {
  text-align: center; padding: 12px 16px 10px;
}
.ac-species-badge {
  font-size: 40px; margin-bottom: 4px;
}
.ac-name {
  font-size: 18px; font-weight: 800; color: #5A4A3A;
  margin-bottom: 2px;
}
.ac-breed {
  font-size: 12px; color: #A09888; margin-bottom: 8px;
}
.ac-meta {
  display: flex; justify-content: center; align-items: center; gap: 14px;
  font-size: 13px; color: #A09888;
}
.ac-gender {
  display: inline-flex; align-items: center; justify-content: center;
  width: 28px; height: 28px; border-radius: 50%;
  font-size: 16px; font-weight: 700;
}
.ac-gender--公 { background: #E0F0FF; color: #6CA8E0; }
.ac-gender--母 { background: #FFE0E8; color: #E888A0; }
.ac-date { font-size: 12px; }

.ac-footer {
  display: flex; justify-content: center; gap: 6px;
  padding: 10px 16px 14px;
  border-top: 1px solid #F5F0E8;
  background: #FFFEF9;
}
.ac-footer .el-button { font-size: 12px; height: 30px; }

.pagination-wrapper { display: flex; justify-content: center; margin-top: 20px; }
</style>
