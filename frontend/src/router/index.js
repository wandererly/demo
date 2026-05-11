import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Departments from '../views/Departments.vue'
import Employees from '../views/Employees.vue'
import Attendance from '../views/Attendance.vue'
import Leave from '../views/Leave.vue'
import Performance from '../views/Performance.vue'
import Payroll from '../views/Payroll.vue'
import Approvals from '../views/Approvals.vue'
import Reports from '../views/Reports.vue'
import AuditLogs from '../views/AuditLogs.vue'
import Rbac from '../views/Rbac.vue'
import Org from '../views/Org.vue'
import Settings from '../views/Settings.vue'
import Login from '../views/Login.vue'
import SelfService from '../views/SelfService.vue'
import { getStoredToken, getPermissions, getUser } from '../auth'

const routes = [
  { path: '/login', component: Login, meta: { layout: 'auth' } },
  { path: '/', component: Dashboard },
  { path: '/departments', component: Departments, meta: { perms: ['hr:manage'] } },
  { path: '/employees', component: Employees, meta: { perms: ['hr:manage'] } },
  { path: '/attendance', component: Attendance, meta: { perms: ['attendance:manage'] } },
  { path: '/leave', component: Leave, meta: { perms: ['leave:manage'] } },
  { path: '/performance', component: Performance, meta: { perms: ['perf:manage'] } },
  { path: '/payroll', component: Payroll, meta: { perms: ['payroll:manage'] } },
  { path: '/approvals', component: Approvals, meta: { perms: ['approval:manage'] } },
  { path: '/reports', component: Reports, meta: { perms: ['report:view', 'hr:manage', 'payroll:manage'] } },
  { path: '/org', component: Org, meta: { perms: ['hr:manage'] } },
  { path: '/rbac', component: Rbac, meta: { perms: ['rbac:manage'] } },
  { path: '/audit-logs', component: AuditLogs, meta: { perms: ['audit:view', 'config:manage'] } },
  { path: '/settings', component: Settings, meta: { perms: ['config:manage'] } },
  { path: '/self/:section?', component: SelfService, meta: { self: true } }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to) => {
  if (to.path === '/login') return true
  const token = getStoredToken()
  if (!token) return '/login'
  const user = getUser()
  const perms = getPermissions()
  const hasAdminEntry = perms.some((p) => ['hr:manage', 'attendance:manage', 'leave:manage', 'perf:manage', 'payroll:manage', 'approval:manage', 'report:view', 'config:manage', 'rbac:manage'].includes(p))
  const hasSelfEntry = !!(user && (user.empId || user.role === 'EMPLOYEE' || perms.includes('self:view')))
  if (to.path === '/' && !hasAdminEntry && hasSelfEntry) {
    return '/self/profile'
  }
  if (to.meta && to.meta.perms) {
    const ok = to.meta.perms.some((p) => perms.includes(p))
    if (!ok) return '/'
  }
  if (to.meta && to.meta.self && !hasSelfEntry) return '/'
  return true
})

export default router
