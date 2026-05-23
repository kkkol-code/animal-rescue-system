<template>
  <div class="medical-record" v-loading="loading">
    <div class="mr-header">
      <el-button :icon="ArrowLeft" @click="goBack">返回档案大厅</el-button>
      <span class="mr-title">医疗档案 — {{ animal.name }}</span>
    </div>

    <!-- 动物基本信息 -->
    <el-card shadow="never" class="mr-info">
      <template #header>动物基本信息</template>
      <el-descriptions :column="4" border>
        <el-descriptions-item label="动物 ID">{{ animal.animalId }}</el-descriptions-item>
        <el-descriptions-item label="名称">{{ animal.name }}</el-descriptions-item>
        <el-descriptions-item label="种类">{{ animal.species }}</el-descriptions-item>
        <el-descriptions-item label="品种">{{ animal.breed }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ animal.gender }}</el-descriptions-item>
        <el-descriptions-item label="入站日期">{{ animal.entryDate }}</el-descriptions-item>
        <el-descriptions-item label="领养状态">
          <el-tag :type="statusTagType(animal.adoptStatus)" effect="dark">
            {{ statusLabel(animal.adoptStatus) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="救助编号">RES-{{ String(animal.animalId).padStart(6, '0') }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 医疗记录表格 -->
    <el-card shadow="never" class="mr-records">
      <template #header>
        <div class="mr-records__header">
          <span>医疗记录（共 {{ records.length }} 条）</span>
          <el-button type="primary" :icon="Plus" @click="openAddDialog">新增记录</el-button>
        </div>
      </template>
      <el-table :data="records" stripe border style="width: 100%">
        <el-table-column prop="recordId" label="记录 ID" width="90" align="center" />
        <el-table-column prop="checkDate" label="检查日期" width="120" align="center" sortable />
        <el-table-column prop="type" label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="typeTagType(row.type)" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="vetName" label="兽医" width="90" />
        <el-table-column prop="vaccination" label="免疫情况" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="vaccineTagType(row.vaccination)" size="small" effect="plain">
              {{ row.vaccination || '未记录' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="diagnosis" label="诊断结果" min-width="180" show-overflow-tooltip />
        <el-table-column prop="treatment" label="治疗方案" min-width="180" show-overflow-tooltip />
        <el-table-column prop="weight" label="体重(kg)" width="90" align="center" />
        <el-table-column prop="temperature" label="体温(°C)" width="90" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link :icon="View" @click="openDetailDialog(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- ==================== 新增记录弹窗 ==================== -->
    <el-dialog v-model="addDialogVisible" title="新增医疗记录" width="600px" :close-on-click-modal="false">
      <el-form ref="addFormRef" :model="addForm" :rules="addRules" label-width="100px" status-icon>
        <el-form-item label="检查日期" prop="checkDate">
          <el-date-picker v-model="addForm.checkDate" type="date" placeholder="选择日期"
            value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="记录类型" prop="type">
          <el-select v-model="addForm.type" placeholder="请选择" style="width: 100%">
            <el-option label="体检" value="体检" />
            <el-option label="疫苗" value="疫苗" />
            <el-option label="手术" value="手术" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="免疫情况" prop="vaccination">
          <el-radio-group v-model="addForm.vaccination">
            <el-radio value="已免疫">已免疫</el-radio>
            <el-radio value="未免疫">未免疫</el-radio>
            <el-radio value="部分免疫">部分免疫</el-radio>
            <el-radio value="未记录">未知</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="兽医" prop="vetName">
          <el-input v-model="addForm.vetName" placeholder="主治兽医姓名" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="addForm.weight" :min="0" :max="200" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="体温(°C)" prop="temperature">
          <el-input-number v-model="addForm.temperature" :min="34" :max="43" :precision="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="诊断结果" prop="diagnosis">
          <el-input v-model="addForm.diagnosis" type="textarea" :rows="3"
            placeholder="请输入病情描述、诊断结论..." maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="治疗方案" prop="treatment">
          <el-input v-model="addForm.treatment" type="textarea" :rows="3"
            placeholder="请输入治疗方案、用药说明..." maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleAddRecord">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 详情弹窗 ==================== -->
    <el-dialog v-model="detailDialogVisible" title="医疗记录详情" width="560px">
      <el-descriptions v-if="detailRow" :column="2" border>
        <el-descriptions-item label="记录 ID">{{ detailRow.recordId }}</el-descriptions-item>
        <el-descriptions-item label="检查日期">{{ detailRow.checkDate }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="typeTagType(detailRow.type)" size="small">{{ detailRow.type }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="免疫情况">
          <el-tag :type="vaccineTagType(detailRow.vaccination)" size="small">{{ detailRow.vaccination || '未记录' }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="兽医">{{ detailRow.vetName }}</el-descriptions-item>
        <el-descriptions-item label="体重">{{ detailRow.weight }} kg</el-descriptions-item>
        <el-descriptions-item label="体温">{{ detailRow.temperature }} °C</el-descriptions-item>
        <el-descriptions-item label="诊断结果" :span="2">{{ detailRow.diagnosis }}</el-descriptions-item>
        <el-descriptions-item label="治疗方案" :span="2">{{ detailRow.treatment }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Plus, View } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAnimalDetail } from '@/api/animals'
import { mockMedicalRecords, findAnimal, addMedicalRecord } from '@/store/mockData'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)

// ==================== 状态映射 ====================
const statusMap = {
  pending: { label: '待领养', type: '' },
  adopted: { label: '已领养', type: 'success' },
  sick: { label: '生病中', type: 'danger' },
  treating: { label: '治疗中', type: 'warning' }
}
const statusTagType = (s) => statusMap[s]?.type || 'info'
const statusLabel = (s) => statusMap[s]?.label || s

const typeTagType = (t) => ({ '体检': '', '疫苗': 'success', '手术': 'danger' }[t] || 'info')
const vaccineTagType = (v) => ({ '已免疫': 'success', '未免疫': 'danger', '部分免疫': 'warning' }[v] || 'info')

let currentAnimalId = null

const animal = ref({ animalId: '', name: '', species: '', breed: '', gender: '', adoptStatus: '', entryDate: '' })
const records = ref([])

// ==================== 新增记录 ====================
const addDialogVisible = ref(false)
const addFormRef = ref(null)
const addForm = reactive({
  checkDate: '',
  type: '',
  vaccination: '未记录',
  vetName: '',
  weight: null,
  temperature: null,
  diagnosis: '',
  treatment: ''
})

const addRules = {
  checkDate: [{ required: true, message: '请选择检查日期', trigger: 'change' }],
  type: [{ required: true, message: '请选择记录类型', trigger: 'change' }],
  vetName: [{ required: true, message: '请输入兽医姓名', trigger: 'blur' }],
  diagnosis: [{ required: true, message: '请输入诊断结果/病情描述', trigger: 'blur' }],
  treatment: [{ required: true, message: '请输入治疗方案', trigger: 'blur' }]
}

const resetAddForm = () => {
  addForm.checkDate = ''
  addForm.type = ''
  addForm.vaccination = '未记录'
  addForm.vetName = ''
  addForm.weight = null
  addForm.temperature = null
  addForm.diagnosis = ''
  addForm.treatment = ''
}

const openAddDialog = () => {
  resetAddForm()
  addDialogVisible.value = true
}

const handleAddRecord = async () => {
  if (!addFormRef.value) return
  try { await addFormRef.value.validate() } catch { return }

  saving.value = true
  setTimeout(() => {
    addMedicalRecord(currentAnimalId, {
      checkDate: addForm.checkDate,
      type: addForm.type,
      vaccination: addForm.vaccination,
      vetName: addForm.vetName,
      weight: addForm.weight ?? 0,
      temperature: addForm.temperature ?? 0,
      diagnosis: addForm.diagnosis,
      treatment: addForm.treatment
    })
    records.value = [...(mockMedicalRecords[currentAnimalId] || [])]
    ElMessage.success('医疗记录已保存（刷新不丢失）')
    addDialogVisible.value = false
    saving.value = false
  }, 300)
}

// ==================== 详情弹窗 ====================
const detailDialogVisible = ref(false)
const detailRow = ref(null)
const openDetailDialog = (row) => {
  detailRow.value = row
  detailDialogVisible.value = true
}

// ==================== 加载 ====================
onMounted(async () => {
  const animalId = Number(route.query.animal_id)
  if (!animalId) {
    console.warn('未获取到动物ID，请确认跳转路径携带 animal_id 参数')
    return
  }
  currentAnimalId = animalId
  loading.value = true

  // 1. 获取动物基本信息（优先后端 API，失败则走本地 store）
  try {
    const res = await getAnimalDetail(animalId)
    animal.value = res.data ?? res
  } catch {
    const found = findAnimal(animalId)
    if (found) animal.value = found
  }

  // 2. 获取医疗记录（优先后端 API，失败则走本地 store）
  try {
    // TODO: 对接后端 GET /api/animals/{id}/medical-records 后替换
    // const res = await getMedicalRecords(animalId)
    // records.value = res.data?.list ?? res.data ?? []
    throw new Error('暂未对接后端')
  } catch {
    records.value = [...(mockMedicalRecords[animalId] || [])]
  }

  loading.value = false
})

const goBack = () => router.push('/animals')
</script>

<style scoped>
.medical-record { padding: 20px; }
.mr-header { display: flex; align-items: center; gap: 16px; margin-bottom: 16px; }
.mr-title { font-size: 18px; font-weight: 600; color: #303133; }
.mr-info { margin-bottom: 16px; }
.mr-records__header { display: flex; justify-content: space-between; align-items: center; }
</style>
