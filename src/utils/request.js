import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// ==================== 请求拦截器 ====================
request.interceptors.request.use(
  (config) => {
    // TODO: 附加 token
    // const token = localStorage.getItem('token')
    // if (token) config.headers.Authorization = `Bearer ${token}`
    return config
  },
  (error) => Promise.reject(error)
)

// ==================== 错误防抖（8 秒内最多弹一次）====================
let lastWarningTime = 0
const WARNING_COOLDOWN = 8000

const showOnce = (msg) => {
  const now = Date.now()
  if (now - lastWarningTime > WARNING_COOLDOWN) {
    lastWarningTime = now
    ElMessage.warning(msg)
  }
  // 冷却期内的错误静默吞掉，不弹任何提示
}

// ==================== 响应拦截器 ====================
request.interceptors.response.use(
  (response) => {
    const { data } = response
    // 后端业务层错误码（如 code=500），透传给组件自行处理
    if (data.code !== 200) {
      return Promise.reject(new Error(data.msg || '请求失败'))
    }
    return data
  },
  (error) => {
    const status = error.response?.status

    // ---- 5xx：后端调试崩溃 → 防抖提示中文，静默拒绝，让组件 catch 走 Mock ----
    if (status && status >= 500) {
      showOnce('后端调试中，已自动切换本地数据展示，页面不影响使用')
      return Promise.reject(error)
    }

    // ---- 4xx：请求参数问题 → 直接提示（频率低，无需防抖）----
    if (status && status >= 400) {
      ElMessage.error(error.response?.data?.msg || '请求参数有误，请检查输入')
      return Promise.reject(error)
    }

    // ---- 网络断开 / 超时 / CORS 等 → 防抖提示 ----
    showOnce('网络波动或后端未启动，已切换本地数据展示')
    return Promise.reject(error)
  }
)

export default request
