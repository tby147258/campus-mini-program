import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  { path: '/register', name: 'Register', component: () => import('../views/Register.vue'), meta: { noAuth: true } },
  { path: '/forgot-password', name: 'ForgotPassword', component: () => import('../views/ForgotPassword.vue'), meta: { noAuth: true } },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '仪表盘' } },
      { path: 'announcements', name: 'Announcements', component: () => import('../views/AnnouncementList.vue'), meta: { title: '公告管理' } },
      { path: 'announcements/create', name: 'CreateAnnouncement', component: () => import('../views/AnnouncementForm.vue'), meta: { title: '发布公告' } },
      { path: 'announcements/edit/:id', name: 'EditAnnouncement', component: () => import('../views/AnnouncementForm.vue'), meta: { title: '编辑公告' } },
      { path: 'lost-found', name: 'LostFound', component: () => import('../views/LostFoundManage.vue'), meta: { title: '失物招领管理' } },
      { path: 'repairs', name: 'Repairs', component: () => import('../views/RepairManage.vue'), meta: { title: '报修工单管理' } },
      { path: 'users', name: 'Users', component: () => import('../views/UserManage.vue'), meta: { title: '用户管理' } },
      { path: 'operation-logs', name: 'OperationLogs', component: () => import('../views/OperationLog.vue'), meta: { title: '操作日志' } },
      { path: 'statistics', name: 'Statistics', component: () => import('../views/Statistics.vue'), meta: { title: '数据统计' } },
      { path: 'settings', name: 'Settings', component: () => import('../views/Settings.vue'), meta: { title: '系统设置' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path === '/login' || to.meta?.noAuth) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router