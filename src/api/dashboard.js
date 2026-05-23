import request from '@/utils/request'

// ==================== 数据看板 ====================

/** 获取顶部统计卡片数据 */
export const getDashboardStats = () => request.get('/dashboard/stats')

/** 获取品种分布数据 */
export const getBreedDistribution = () => request.get('/dashboard/breed-distribution')

/** 获取月度领养/救助趋势 */
export const getMonthlyTrend = () => request.get('/dashboard/monthly-trend')
