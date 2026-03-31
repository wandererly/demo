import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Departments from '../views/Departments.vue'
import Employees from '../views/Employees.vue'
import Attendance from '../views/Attendance.vue'
import Leave from '../views/Leave.vue'
import Performance from '../views/Performance.vue'
import Payroll from '../views/Payroll.vue'
import Rbac from '../views/Rbac.vue'
import Settings from '../views/Settings.vue'
import Login from '../views/Login.vue'
import { getStoredToken, getPermissions } from '../auth'

const routes = [
  { path: '/login', component: Login, meta: { layout: 'auth' } },
  { path: '/', component: Dashboard },
  { path: '/departments', component: Departments, meta: { perms: ['hr:manage'] } },
  { path: '/employees', component: Employees, meta: { perms: ['hr:manage'] } },
  { path: '/attendance', component: Attendance, meta: { perms: ['attendance:manage'] } },
  { path: '/leave', component: Leave, meta: { perms: ['leave:manage'] } },
  { path: '/performance', component: Performance, meta: { perms: ['perf:manage'] } },
  { path: '/payroll', component: Payroll, meta: { perms: ['payroll:manage'] } },
  { path: '/rbac', component: Rbac, meta: { perms: ['rbac:manage'] } },
  { path: '/settings', component: Settings, meta: { perms: ['config:manage'] } }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  if (to.path === '/login') return true
  const token = getStoredToken()
  if (!token) return '/login'
  if (to.meta && to.meta.perms) {
    const perms = getPermissions()
    const ok = to.meta.perms.some((p) => perms.includes(p))
    if (!ok) return '/'
  }
  return true
})

export default router
