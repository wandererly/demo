<template>
  <aside class="sidebar">
    <div class="brand">
      <div class="brand-badge">HR</div>
      <div>
        <div style="font-weight:700">HRM Admin</div>
        <div style="font-size:12px;color:#a6b0b8">Enterprise Suite</div>
      </div>
    </div>

    <div class="nav-group">概览</div>
    <router-link to="/" class="nav-link" active-class="active">仪表盘</router-link>
    <router-link v-if="hasPerm('hr:manage')" to="/departments" class="nav-link" active-class="active">部门管理</router-link>
    <router-link v-if="hasPerm('hr:manage')" to="/employees" class="nav-link" active-class="active">员工管理</router-link>

    <div class="nav-group">考勤</div>
    <router-link v-if="hasPerm('attendance:manage')" to="/attendance" class="nav-link" active-class="active">考勤管理</router-link>
    <router-link v-if="hasPerm('leave:manage')" to="/leave" class="nav-link" active-class="active">请假管理</router-link>

    <div class="nav-group">绩效与薪酬</div>
    <router-link v-if="hasPerm('perf:manage')" to="/performance" class="nav-link" active-class="active">绩效管理</router-link>
    <router-link v-if="hasPerm('payroll:manage')" to="/payroll" class="nav-link" active-class="active">薪酬管理</router-link>

    <div class="nav-group">系统</div>
    <router-link v-if="hasPerm('rbac:manage')" to="/rbac" class="nav-link" active-class="active">权限管理</router-link>
    <router-link v-if="hasPerm('config:manage')" to="/settings" class="nav-link" active-class="active">系统设置</router-link>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { getPermissions } from '../auth'

const permissions = computed(() => getPermissions())
const hasPerm = (key) => permissions.value.includes(key)
</script>
