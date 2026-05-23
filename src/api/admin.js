import request from '@/utils/request'

/** 管理员登录 */
export const login = (data) => request.post('/admin/login', data)
