import request from '@/utils/request'

// ==================== 领养申请 ====================

/** 提交领养申请 */
export const submitAdoption = (data) => request.post('/adoption/apply', data)
