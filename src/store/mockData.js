import { reactive } from 'vue'

// ==================== 默认初始数据（字段已统一为驼峰，与后端 JSON 一致）====================
const defaultAnimals = [
  { animalId: 1,  name: '大黄', species: '狗', breed: '中华田园犬', gender: '公', adoptStatus: 'pending',  entryDate: '2026-01-15', vaccination: '已免疫' },
  { animalId: 2,  name: '小花', species: '猫', breed: '中华田园猫', gender: '母', adoptStatus: 'adopted',  entryDate: '2025-11-20', vaccination: '未记录' },
  { animalId: 3,  name: '旺财', species: '狗', breed: '金毛',       gender: '公', adoptStatus: 'pending',  entryDate: '2026-03-08', vaccination: '未记录' },
  { animalId: 4,  name: '咪咪', species: '猫', breed: '英短',       gender: '母', adoptStatus: 'sick',     entryDate: '2026-02-14', vaccination: '未免疫' },
  { animalId: 5,  name: '小白', species: '狗', breed: '泰迪',       gender: '母', adoptStatus: 'pending',  entryDate: '2026-04-01', vaccination: '未记录' },
  { animalId: 6,  name: '大橘', species: '猫', breed: '中华田园猫', gender: '公', adoptStatus: 'treating', entryDate: '2026-03-22', vaccination: '未记录' },
  { animalId: 7,  name: '黑子', species: '狗', breed: '中华田园犬', gender: '公', adoptStatus: 'adopted',  entryDate: '2025-09-10', vaccination: '未记录' },
  { animalId: 8,  name: '团子', species: '猫', breed: '美短',       gender: '母', adoptStatus: 'pending',  entryDate: '2026-05-02', vaccination: '未记录' },
  { animalId: 9,  name: '阿福', species: '狗', breed: '金毛',       gender: '公', adoptStatus: 'pending',  entryDate: '2026-05-10', vaccination: '未记录' },
  { animalId: 10, name: '雪球', species: '猫', breed: '英短',       gender: '母', adoptStatus: 'adopted',  entryDate: '2025-12-05', vaccination: '未记录' },
  { animalId: 11, name: '虎子', species: '狗', breed: '中华田园犬', gender: '公', adoptStatus: 'pending',  entryDate: '2026-04-18', vaccination: '未记录' },
  { animalId: 12, name: '奶茶', species: '猫', breed: '美短',       gender: '母', adoptStatus: 'sick',     entryDate: '2026-05-08', vaccination: '未免疫' }
]

const defaultMedicalRecords = {
  1: [
    { recordId: 1, checkDate: '2026-01-15', type: '体检', vetName: '李医生', diagnosis: '体表有轻微寄生虫，整体健康状况良好',      treatment: '体外驱虫处理，观察3天',                     weight: 12.5, temperature: 38.2 },
    { recordId: 2, checkDate: '2026-02-20', type: '疫苗', vetName: '王医生', diagnosis: '健康状况良好，适合接种',                    treatment: '接种狂犬疫苗+四联疫苗第一针',                weight: 13.2, temperature: 38.5 },
    { recordId: 3, checkDate: '2026-04-10', type: '体检', vetName: '李医生', diagnosis: '右后腿轻微跛行，疑似运动扭伤',              treatment: '限制活动一周，口服消炎药',                   weight: 14.0, temperature: 38.3 }
  ],
  4: [
    { recordId: 4, checkDate: '2026-02-14', type: '体检', vetName: '李医生', diagnosis: '呼吸道感染，伴有咳嗽、打喷嚏症状',          treatment: '抗生素治疗7天，隔离观察',                     weight: 4.2,  temperature: 39.1 },
    { recordId: 5, checkDate: '2026-02-22', type: '手术', vetName: '张医生', diagnosis: '绝育手术，术后恢复中',                    treatment: '术后消炎3天，佩戴伊丽莎白圈',                weight: 3.9,  temperature: 38.7 }
  ],
  12: [
    { recordId: 6, checkDate: '2026-05-08', type: '体检', vetName: '王医生', diagnosis: '皮肤真菌感染（猫癣），背部多处脱毛',        treatment: '外用抗真菌药膏每日涂抹，每周药浴一次',        weight: 3.5,  temperature: 38.4 }
  ]
}

// ==================== localStorage 读写 ====================
const load = (key, fallback) => {
  try { const raw = localStorage.getItem(key); return raw ? JSON.parse(raw) : fallback } catch { return fallback }
}
const save = (key, data) => {
  try { localStorage.setItem(key, JSON.stringify(data)) } catch { /* quota exceeded */ }
}

// ==================== 持久化响应式数据 ====================
export const mockAnimals = reactive(load('ar_animals_v2', defaultAnimals))
export const mockMedicalRecords = reactive(load('ar_medical_records_v2', defaultMedicalRecords))
const persist = () => {
  save('ar_animals_v2', mockAnimals)
  save('ar_medical_records_v2', mockMedicalRecords)
}

// ==================== 暴露方法 ====================

/** 新增医疗记录 */
let nextRecordId = (() => {
  let max = 0
  Object.values(mockMedicalRecords).forEach(arr => arr.forEach(r => { if (r.recordId > max) max = r.recordId }))
  return max + 1
})()

export const addMedicalRecord = (animalId, record) => {
  if (!mockMedicalRecords[animalId]) mockMedicalRecords[animalId] = []
  mockMedicalRecords[animalId].push({ recordId: nextRecordId++, ...record })
  persist()
}

/** 按 animalId 查找动物 */
export const findAnimal = (animalId) => mockAnimals.find(a => a.animalId === animalId)
