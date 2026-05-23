<template>
  <div class="login-page">
    <!-- 爪子水印 -->
    <span class="paw-watermark w1">🐾</span>
    <span class="paw-watermark w2">🐾</span>
    <span class="paw-watermark w3">🐾</span>
    <span class="paw-watermark w4">🐾</span>
    <span class="paw-watermark w5">🐾</span>

    <div class="login-card">
      <div class="login-mascot">🐱</div>
      <div class="login-header">
        <h2>🐾 流浪动物救助站</h2>
        <p>每一只小动物都值得被温柔以待</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large" status-icon>
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名"
            :prefix-icon="User" clearable autocomplete="off" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
            :prefix-icon="Lock" show-password autocomplete="new-password"
            @keyup.enter="handleLogin" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width: 100%; height: 46px; font-size: 16px;"
            @click="handleLogin">
            {{ loading ? '正在开门...' : '🐾 进 入 救 助 站' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
    <p class="login-footer">🫶 爱护动物 · 从领养开始</p>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { login } from '@/api/admin'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await login({ username: form.username, password: form.password })
    const data = res.data ?? res
    sessionStorage.setItem('token', data.token)
    sessionStorage.setItem('username', data.username)
    sessionStorage.setItem('realName', data.realName || data.username)
    ElMessage.success('🐱 欢迎回来！小动物们在等你~')
    router.push('/dashboard')
  } catch {
    ElMessage.error('登录失败，请检查账号密码或确认后端服务已启动 😿')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: linear-gradient(160deg, #FFFAF5 0%, #FFF0E0 40%, #E8F8E8 100%);
  overflow: hidden;
  position: relative;
}

.login-card {
  width: 420px;
  padding: 44px 40px 36px;
  background: rgba(255,255,255,0.92);
  border-radius: 28px;
  box-shadow: 0 8px 40px rgba(126,200,160,0.18), 0 2px 8px rgba(0,0,0,0.04);
  position: relative;
  z-index: 1;
  backdrop-filter: blur(10px);
  border: 2px solid #F0E8DC;
}

.login-mascot {
  text-align: center;
  font-size: 64px;
  margin-bottom: 8px;
  animation: bob 2s ease-in-out infinite;
}

@keyframes bob {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

.login-header { text-align: center; margin-bottom: 28px; }
.login-header h2 { margin: 0 0 6px; font-size: 22px; color: #5EA87E; font-weight: 700; }
.login-header p { margin: 0; color: #B0A090; font-size: 13px; }

.login-footer {
  margin-top: 18px;
  color: #C0B8A8;
  font-size: 12px;
  z-index: 1;
}
</style>
