import request from '@/utils/request'

export const getMedicalRecords = (animalId) => request.get('/admin/medical-records', { params: { animal_id: animalId } })
export const addMedicalRecordApi = (data) => request.post('/admin/medical-records', data)
