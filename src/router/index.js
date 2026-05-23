import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据统计看板' }
      },
      {
        path: 'animals',
        name: 'AnimalList',
        component: () => import('@/views/AnimalList.vue'),
        meta: { title: '动物档案大厅' }
      },
      {
        path: 'animals/medical',
        name: 'MedicalRecord',
        component: () => import('@/views/MedicalRecord.vue'),
        meta: { title: '医疗档案' }
      },
      {
        path: 'adoption',
        name: 'AdoptionAudit',
        component: () => import('@/views/AdoptionAudit.vue'),
        meta: { title: '领养办理与审核' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// ==================== 全局路由守卫 ====================
router.beforeEach((to, from, next) => {
  const token = sessionStorage.getItem('token')
  // 登录页不需要校验
  if (to.meta.noAuth) {
    return next()
  }
  // 无 token 则跳转登录页
  if (!token) {
    return next('/login')
  }
  next()
})

export default router
