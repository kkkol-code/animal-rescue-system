import request from '@/utils/request'

// ==================== 动物档案 ====================

/** 获取动物列表（分页 + 筛选） */
export const getAnimalList = (params) => request.get('/animals', { params })

/** 获取单只动物详情 */
export const getAnimalDetail = (id) => request.get(`/animals/${id}`)

/** 新增动物 */
export const createAnimal = (data) => request.post('/animals', data)

/** 获取可领养动物列表 */
export const getAdoptableAnimals = () => request.get('/animals', { params: { adopt_status: 'pending' } })

/** 删除动物 */
export const deleteAnimal = (id) => request.delete(`/animals/${id}`)

/** 更改动物状态 */
export const updateAnimalStatus = (id, status) => request.put(`/animals/${id}/status`, { status })
