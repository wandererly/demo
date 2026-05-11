<template>
  <aside class="sidebar">
    <div class="brand">
      <div class="auth-mark brand-mark" aria-label="HRM">
        <span class="auth-mark-core"></span>
        <span class="auth-mark-orbit auth-mark-orbit-a"></span>
        <span class="auth-mark-orbit auth-mark-orbit-b"></span>
      </div>
      <div>
        <div style="font-weight:700">HRM Admin</div>
        <div style="font-size:12px;color:#a6b0b8">Enterprise Suite</div>
      </div>
    </div>

    <template v-if="showAdmin">
      <div class="nav-group">管理后台</div>
      <router-link to="/" class="nav-link" active-class="active">工作台</router-link>
      <router-link v-if="hasPerm('hr:manage')" to="/employees" class="nav-link" active-class="active">员工管理</router-link>
      <router-link v-if="hasPerm('hr:manage')" to="/departments" class="nav-link" active-class="active">部门岗位</router-link>
      <router-link v-if="hasPerm('attendance:manage')" to="/attendance" class="nav-link" active-class="active">考勤管理</router-link>
      <router-link v-if="hasPerm('payroll:manage')" to="/payroll" class="nav-link" active-class="active">薪酬管理</router-link>
      <router-link v-if="hasPerm('approval:manage')" to="/approvals" class="nav-link" active-class="active">统一审批</router-link>
      <router-link v-if="hasAnyPerm(['report:view','hr:manage','payroll:manage'])" to="/reports" class="nav-link" active-class="active">报表</router-link>
      <router-link v-if="hasPerm('config:manage')" to="/settings" class="nav-link" active-class="active">系统设置</router-link>
    </template>

    <template v-if="showSelf">
      <div class="nav-group">员工自助</div>
      <router-link to="/self/profile" class="nav-link" active-class="active">我的档案</router-link>
      <router-link to="/self/attendance" class="nav-link" active-class="active">我的考勤</router-link>
      <router-link to="/self/leave" class="nav-link" active-class="active">我的请假</router-link>
      <router-link to="/self/performance" class="nav-link" active-class="active">我的绩效</router-link>
      <router-link to="/self/payslips" class="nav-link" active-class="active">我的工资条</router-link>
      <router-link to="/self/notifications" class="nav-link" active-class="active">我的通知</router-link>
    </template>

  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { getPermissions, getUser } from '../auth'

const permissions = computed(() => getPermissions())
const user = computed(() => getUser() || {})
const hasPerm = (key) => permissions.value.includes(key)
const hasAnyPerm = (keys) => keys.some((key) => hasPerm(key))
const adminPerms = ['hr:manage', 'attendance:manage', 'leave:manage', 'perf:manage', 'payroll:manage', 'approval:manage', 'report:view', 'config:manage', 'rbac:manage']
const showAdmin = computed(() => adminPerms.some((key) => hasPerm(key)))
const showSelf = computed(() => !showAdmin.value && (user.value.empId || user.value.role === 'EMPLOYEE' || hasPerm('self:view')))
</script>
