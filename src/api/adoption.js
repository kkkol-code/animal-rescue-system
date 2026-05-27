import request from '@/utils/request'

/** 提交领养申请 */
export const submitAdoption = (data) => request.post('/adoption/apply', data)

/** 获取待审核申请列表 */
export const getPendingApplications = () => request.get('/adoption/applications', { params: { status: 'pending' } })

/** 审核通过 */
export const approveApplication = (id) => request.put(`/adoption/${id}/approve`)

/** 审核拒绝 */
export const rejectApplication = (id) => request.put(`/adoption/${id}/reject`)
