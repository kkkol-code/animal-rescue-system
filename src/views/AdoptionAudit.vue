<template>
  <div class="adoption-audit">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="form-card__header">
          <div class="form-card__header-left">
            <el-icon :size="20"><EditPen /></el-icon>
            <span>领养申请表</span>
          </div>
          <el-button v-if="role === 'admin'" type="warning" :icon="Check" round @click="handleAudit">
            审核宠物申请
          </el-button>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        label-position="right"
        style="max-width: 680px"
        status-icon
      >
        <!-- 领养人信息 -->
        <el-divider content-position="left">
          <el-icon><User /></el-icon> 领养人信息
        </el-divider>

        <el-form-item label="领养人姓名" prop="real_name">
          <el-input v-model="form.real_name" placeholder="请输入真实姓名" clearable />
        </el-form-item>

        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入11位手机号码" clearable maxlength="11" />
        </el-form-item>

        <el-form-item label="身份证号" prop="id_card">
          <el-input v-model="form.id_card" placeholder="请输入18位身份证号" clearable maxlength="18" />
        </el-form-item>

        <el-form-item label="住房情况" prop="house_condition">
          <el-select v-model="form.house_condition" placeholder="请选择住房情况" style="width: 100%">
            <el-option label="自有住房（无封窗限制）" value="自有住房" />
            <el-option label="整租（可封窗）" value="整租可封窗" />
            <el-option label="整租（不可封窗）" value="整租不可封窗" />
            <el-option label="合租" value="合租" />
            <el-option label="学生宿舍" value="宿舍" />
          </el-select>
        </el-form-item>

        <!-- 动物选择 -->
        <el-divider content-position="left">
          <el-icon><Present /></el-icon> 领养意向
        </el-divider>

        <el-form-item label="心仪动物" prop="animal_id">
          <el-select
            v-model="form.animal_id"
            placeholder="请选择心仪的动物"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="animal in availableAnimals"
              :key="animal.animalId"
              :label="`${animal.name} — ${animal.breed}（${animal.species}·${animal.gender}）`"
              :value="animal.animalId"
              :disabled="animal.adoptStatus !== 'pending'"
            >
              <span>{{ animal.name }} — {{ animal.breed }}</span>
              <el-tag
                :type="animal.adoptStatus === 'pending' ? '' : 'info'"
                size="small"
                style="margin-left: 8px"
              >
                {{ animal.adoptStatus === 'pending' ? '可领养' : '不可领养' }}
              </el-tag>
            </el-option>
          </el-select>
        </el-form-item>

        <el-form-item label="申请备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="如有补充说明请填写..."
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :icon="Check" :loading="submitting" @click="handleSubmit">
            确认办理
          </el-button>
          <el-button :icon="RefreshRight" @click="handleReset">重置表单</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- ==================== 审核弹窗（仅管理员可见）==================== -->
    <el-dialog v-model="auditDialogVisible" title="审核宠物申请" width="900px" :close-on-click-modal="false">
      <el-table :data="pendingApplications" v-loading="auditLoading" stripe border max-height="480">
        <el-table-column prop="application_id" label="申请编号" width="90" align="center" />
        <el-table-column prop="adopter_name" label="领养人" width="100" />
        <el-table-column prop="phone" label="电话" width="130" />
        <el-table-column prop="id_card" label="身份证号" width="180" />
        <el-table-column prop="house_condition" label="住房" width="110" />
        <el-table-column prop="animal_name" label="动物" width="90" />
        <el-table-column prop="breed" label="品种" width="110" />
        <el-table-column prop="apply_date" label="申请日期" width="110" align="center" sortable />
        <el-table-column label="操作" width="160" align="center" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" :icon="Check" @click="handleApprove(row)">通过</el-button>
            <el-button type="danger" size="small" :icon="Close" @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!auditLoading && pendingApplications.length === 0" description="暂无待审核的申请 🐱" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { EditPen, User, Present, Check, RefreshRight, Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdoptableAnimals } from '@/api/animals'
import { submitAdoption, getPendingApplications, approveApplication, rejectApplication } from '@/api/adoption'
import { mockAnimals } from '@/store/mockData'

// ==================== 可领养动物 ====================
const route = useRoute()
const role = sessionStorage.getItem('role') || 'owner'
const availableAnimals = ref([])

const fetchAnimals = async () => {
  try {
    const res = await getAdoptableAnimals()
    availableAnimals.value = res.data?.list ?? res.data ?? []
    if (availableAnimals.value.length === 0) {
      availableAnimals.value = [...mockAnimals]
    }
  } catch {
    availableAnimals.value = [...mockAnimals]
  }

  // 如果从动物档案页跳转过来，自动选中对应动物
  const animalId = route.query.animal_id
  if (animalId) {
    const id = Number(animalId)
    const found = availableAnimals.value.find(a => a.animalId === id)
    if (found && found.adoptStatus === 'pending') {
      form.animal_id = id
    }
  }
}

// ==================== 表单数据 ====================
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  real_name: '',
  phone: '',
  id_card: '',
  house_condition: '',
  animal_id: '',
  remark: ''
})

// ==================== 校验规则 ====================
const rules = {
  real_name: [
    { required: true, message: '请输入领养人姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号码', trigger: 'blur' }
  ],
  id_card: [
    { pattern: /(^\d{15}$)|(^\d{18}$)|(^\d{17}(\d|X|x)$)/, message: '请输入正确的身份证号', trigger: 'blur' }
  ],
  house_condition: [
    { required: true, message: '请选择住房情况', trigger: 'change' }
  ],
  animal_id: [
    { required: true, message: '请选择心仪的动物', trigger: 'change' }
  ]
}

// ==================== 提交逻辑 ====================
const handleSubmit = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
  } catch {
    ElMessage.warning('请完善表单必填项后再提交')
    return
  }

  submitting.value = true

  const payload = {
    real_name: form.real_name,
    phone: form.phone,
    id_card: form.id_card,
    house_condition: form.house_condition,
    animal_id: form.animal_id,
    remark: form.remark,
    role: sessionStorage.getItem('role') || 'owner'
  }

  try {
    await submitAdoption(payload)
    const isAdmin = sessionStorage.getItem('role') === 'admin'
    ElMessage.success(isAdmin ? '线下登记完成，动物已直接标记为已领养' : '领养申请已成功提交！工作人员将在 3 个工作日内与您联系。')
    handleReset()
  } catch {
    ElMessage.error('提交失败，请确认后端服务已启动')
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.remark = ''
}

// ==================== 审核弹窗 ====================
const auditDialogVisible = ref(false)
const auditLoading = ref(false)
const pendingApplications = ref([])

const handleAudit = async () => {
  auditDialogVisible.value = true
  auditLoading.value = true
  try {
    const res = await getPendingApplications()
    pendingApplications.value = res.data ?? []
  } catch {
    pendingApplications.value = []
  } finally {
    auditLoading.value = false
  }
}

const handleApprove = (row) => {
  ElMessageBox.confirm(
    `确认通过「${row.adopter_name}」对「${row.animal_name}」的领养申请？通过后该动物将标记为已领养。`,
    '审核通过确认',
    { confirmButtonText: '确认通过', cancelButtonText: '取消', type: 'success' }
  ).then(async () => {
    try {
      await approveApplication(row.application_id)
      ElMessage.success('已通过该申请，动物已标记为已领养')
      handleAudit()
    } catch {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const handleReject = (row) => {
  ElMessageBox.confirm(
    `确认拒绝「${row.adopter_name}」对「${row.animal_name}」的领养申请？`,
    '审核拒绝确认',
    { confirmButtonText: '确认拒绝', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    try {
      await rejectApplication(row.application_id)
      ElMessage.success('已拒绝该申请')
      handleAudit()
    } catch {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  fetchAnimals()
})
</script>

<style scoped>
.adoption-audit { padding: 24px; }
.form-card { max-width: 800px; margin: 0 auto; }
.form-card__header { display: flex; align-items: center; justify-content: space-between; }
.form-card__header-left { display: flex; align-items: center; gap: 10px; font-size: 17px; font-weight: 700; color: #5EA87E; }
:deep(.el-divider) { margin: 28px 0 22px; }
:deep(.el-divider__text) { color: #5EA87E; font-weight: 600; }
</style>
