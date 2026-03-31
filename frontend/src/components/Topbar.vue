<template>
  <div class="topbar">
    <div>
      <h2 style="margin:0">HRM Control Center</h2>
      <div style="color:var(--muted);font-size:14px">人事管理一体化平台</div>
    </div>
    <div style="display:flex;gap:12px;align-items:center">
      <input class="search" placeholder="搜索/员工/部门..." />
      <div class="user-chip">
        <div class="user-name">{{ username }}</div>
        <div class="user-role">{{ role }}</div>
      </div>
      <button class="btn" @click="logout">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { clearAuth, getUser } from '../auth'

const router = useRouter()
const username = computed(() => {
  const user = getUser()
  return user ? user.username : 'Guest'
})
const role = computed(() => {
  const user = getUser()
  return user ? user.role : 'UNKNOWN'
})

const logout = () => {
  clearAuth()
  router.push('/login')
}
</script>

