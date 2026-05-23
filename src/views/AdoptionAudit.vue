<template>
  <div class="adoption-audit">
    <el-card shadow="never" class="form-card">
      <template #header>
        <div class="form-card__header">
          <el-icon :size="20"><EditPen /></el-icon>
          <span>领养申请表</span>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { EditPen, User, Present, Check, RefreshRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getAdoptableAnimals } from '@/api/animals'
import { submitAdoption } from '@/api/adoption'
import { mockAnimals, updateAnimalStatus, addApplication } from '@/store/mockData'

// ==================== 可领养动物 ====================
const route = useRoute()
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
    remark: form.remark
  }

  try {
    await submitAdoption(payload)
    ElMessage.success('领养申请已成功提交！工作人员将在 3 个工作日内与您联系。')
    handleReset()
  } catch {
    // 后端未启动时降级为 Mock 提交，持久化到 localStorage
    updateAnimalStatus(form.animal_id, 'adopted')
    addApplication({
      adopter_name: form.real_name,
      adopter_phone: form.phone,
      animal_id: form.animal_id,
      remark: form.remark
    })
    ElMessage.success('领养成功！该动物已标记为已领养。')
    handleReset()
  } finally {
    submitting.value = false
  }
}

const handleReset = () => {
  if (!formRef.value) return
  formRef.value.resetFields()
  form.remark = ''
}

onMounted(() => {
  fetchAnimals()
})
</script>

<style scoped>
.adoption-audit {
  padding: 20px;
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
}

.form-card__header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

:deep(.el-divider) {
  margin: 24px 0 20px;
}
</style>
