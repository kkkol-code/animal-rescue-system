import request from '@/utils/request'

// ==================== 领养申请 ====================

/** 提交领养申请 */
export const submitAdoption = (data) => request.post('/adoption/apply', data)

/** 获取领养申请列表 */
export const getAdoptionList = (params) => request.get('/adoption-applications', { params })

/** 获取单条申请详情 */
export const getAdoptionDetail = (id) => request.get(`/adoption-applications/${id}`)

/** 审核领养申请 */
export const auditAdoption = (id, data) => request.put(`/adoption-applications/${id}/audit`, data)
