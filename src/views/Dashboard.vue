<template>
  <div class="dashboard" v-loading="loading">
    <!-- 顶部统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-card--primary">
          <div class="stat-card__icon">
            <el-icon :size="36"><House /></el-icon>
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ stats.inStation }}</div>
            <div class="stat-card__label">当前在站动物</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-card--success">
          <div class="stat-card__icon">
            <el-icon :size="36"><CircleCheck /></el-icon>
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ stats.totalRescued }}</div>
            <div class="stat-card__label">累计救助数</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card stat-card--warning">
          <div class="stat-card__icon">
            <el-icon :size="36"><UserFilled /></el-icon>
          </div>
          <div class="stat-card__info">
            <div class="stat-card__value">{{ stats.totalAdopted }}</div>
            <div class="stat-card__label">累计领养数</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>
            <span class="chart-title">各品种动物占比</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>
            <span class="chart-title">近六个月领养 / 救助数量趋势</span>
          </template>
          <div ref="lineChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { House, CircleCheck, UserFilled } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardStats, getBreedDistribution, getMonthlyTrend } from '@/api/dashboard'

// ==================== 兜底 Mock 数据 ====================
const fallbackStats = { inStation: 47, totalRescued: 1286, totalAdopted: 903 }
const fallbackBreed = [
  { name: '中华田园犬', value: 18 }, { name: '金毛', value: 8 },
  { name: '泰迪', value: 12 }, { name: '中华田园猫', value: 22 },
  { name: '英短', value: 10 }, { name: '美短', value: 9 }, { name: '其他', value: 6 }
]
const fallbackTrend = {
  months: ['2025-12', '2026-01', '2026-02', '2026-03', '2026-04', '2026-05'],
  rescued: [32, 28, 35, 41, 37, 45],
  adopted: [18, 22, 20, 25, 30, 28]
}

const loading = ref(true)
const stats = ref({ inStation: 0, totalRescued: 0, totalAdopted: 0 })

// ==================== 图表逻辑 ====================
const pieChartRef = ref(null)
const lineChartRef = ref(null)
let pieChart = null
let lineChart = null

const buildPieOption = (data) => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} 只 ({d}%)' },
  legend: { orient: 'vertical', right: '5%', top: 'center' },
  series: [{
    type: 'pie', radius: ['45%', '70%'], center: ['40%', '50%'],
    avoidLabelOverlap: false,
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
    label: { show: false },
    emphasis: { label: { show: true, fontSize: 16, fontWeight: 'bold' } },
    data
  }]
})

const buildLineOption = (trend) => ({
  tooltip: { trigger: 'axis' },
  legend: { data: ['救助数量', '领养数量'], bottom: 0 },
  grid: { left: '3%', right: '4%', bottom: '10%', top: '10%', containLabel: true },
  xAxis: { type: 'category', boundaryGap: false, data: trend.months },
  yAxis: { type: 'value', minInterval: 5 },
  series: [
    {
      name: '救助数量', type: 'line', data: trend.rescued, smooth: true,
      symbol: 'circle', symbolSize: 8,
      lineStyle: { width: 3, color: '#409EFF' }, itemStyle: { color: '#409EFF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64,158,255,0.35)' },
          { offset: 1, color: 'rgba(64,158,255,0.05)' }
        ])
      }
    },
    {
      name: '领养数量', type: 'line', data: trend.adopted, smooth: true,
      symbol: 'circle', symbolSize: 8,
      lineStyle: { width: 3, color: '#67C23A' }, itemStyle: { color: '#67C23A' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(103,194,58,0.35)' },
          { offset: 1, color: 'rgba(103,194,58,0.05)' }
        ])
      }
    }
  ]
})

const initCharts = () => {
  pieChart = echarts.init(pieChartRef.value)
  lineChart = echarts.init(lineChartRef.value)
}

const updateCharts = (breedData, trendData) => {
  pieChart?.setOption(buildPieOption(breedData))
  lineChart?.setOption(buildLineOption(trendData))
}

const handleResize = () => {
  pieChart?.resize()
  lineChart?.resize()
}

// ==================== 数据加载 ====================
const loadData = async () => {
  loading.value = true
  let breedData = fallbackBreed
  let trendData = fallbackTrend

  try {
    const statsRes = await getDashboardStats()
    if (statsRes?.data) stats.value = statsRes.data
  } catch {
    stats.value = fallbackStats
  }

  try {
    const breedRes = await getBreedDistribution()
    if (breedRes?.data?.length) breedData = breedRes.data
  } catch { /* 使用兜底数据 */ }

  try {
    const trendRes = await getMonthlyTrend()
    if (trendRes?.data) trendData = trendRes.data
  } catch { /* 使用兜底数据 */ }

  updateCharts(breedData, trendData)
  loading.value = false
}

onMounted(async () => {
  await nextTick()
  initCharts()
  updateCharts(fallbackBreed, fallbackTrend) // 先用兜底数据渲染骨架
  window.addEventListener('resize', handleResize)
  loadData() // 异步拉取真实数据，成功则覆盖
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  pieChart?.dispose()
  lineChart?.dispose()
})
</script>

<style scoped>
.dashboard { padding: 24px; }
.stat-cards { margin-bottom: 24px; }
.stat-card { cursor: pointer; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-3px); }

.stat-card :deep(.el-card__body) {
  display: flex; align-items: center; gap: 18px; padding: 24px;
}

.stat-card__icon {
  width: 68px; height: 68px; border-radius: 18px;
  display: flex; align-items: center; justify-content: center; font-size: 32px;
}

.stat-card--primary .stat-card__icon { background: linear-gradient(135deg, #B5E8CC, #7EC8A0); }
.stat-card--success .stat-card__icon { background: linear-gradient(135deg, #FFEC8B, #FFD97A); }
.stat-card--warning .stat-card__icon { background: linear-gradient(135deg, #FFD1C1, #F4A5A5); }

.stat-card__value { font-size: 30px; font-weight: 700; color: #4A4A4A; line-height: 1.2; }
.stat-card__value small { font-size: 13px; font-weight: 400; color: #A0A0A0; margin-left: 4px; }
.stat-card__label { font-size: 13px; color: #A0A0A0; margin-top: 4px; }

.chart-row { margin-top: 0; }
.chart-title { font-weight: 700; font-size: 16px; color: #5EA87E; }
.chart-container { width: 100%; height: 380px; }
</style>
