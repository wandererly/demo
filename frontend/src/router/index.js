import { createRouter, createWebHistory } from 'vue-router'
import Dashboard from '../views/Dashboard.vue'
import Departments from '../views/Departments.vue'
import Employees from '../views/Employees.vue'
import Attendance from '../views/Attendance.vue'
import Leave from '../views/Leave.vue'
import Payroll from '../views/Payroll.vue'
import Approvals from '../views/Approvals.vue'
import Reports from '../views/Reports.vue'
import AuditLogs from '../views/AuditLogs.vue'
import Rbac from '../views/Rbac.vue'
import Login from '../views/Login.vue'
import SelfService from '../views/SelfService.vue'
import { getStoredToken, getPermissions, getUser } from '../auth'

// 路由元数据只控制前端菜单和页面入口，后端仍会再次校验权限。
const routes = [
  { path: '/login', component: Login, meta: { layout: 'auth' } },
  { path: '/', component: Dashboard },
  { path: '/departments', component: Departments, meta: { perms: ['hr:manage'] } },
  { path: '/employees', component: Employees, meta: { perms: ['hr:manage'] } },
  { path: '/attendance', component: Attendance, meta: { perms: ['attendance:manage'] } },
  { path: '/leave', component: Leave, meta: { perms: ['leave:manage'] } },
  { path: '/payroll', component: Payroll, meta: { perms: ['payroll:manage'] } },
  { path: '/approvals', component: Approvals, meta: { perms: ['approval:manage'] } },
  { path: '/reports', component: Reports, meta: { perms: ['report:view', 'hr:manage', 'payroll:manage'] } },
  { path: '/rbac', component: Rbac, meta: { perms: ['rbac:manage'] } },
  { path: '/audit-logs', component: AuditLogs, meta: { perms: ['audit:view', 'config:manage'] } },
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
	// 未登录用户回到登录页；员工账号默认进入自助首页，管理员进入管理首页。
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
