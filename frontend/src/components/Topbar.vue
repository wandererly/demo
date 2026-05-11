<template>
  <div class="topbar">
    <div>
      <h2 style="margin:0">HRM Control Center</h2>
      <div style="color:var(--muted);font-size:14px">人事管理一体化平台</div>
    </div>
    <div style="display:flex;gap:12px;align-items:center">
      <input class="search" placeholder="搜索员工/部门..." v-model="searchQuery" @keyup.enter="doSearch" />
      <div class="user-chip">
        <div class="user-name">{{ username }}</div>
        <div class="user-role">{{ role }}</div>
      </div>
      <button class="btn" @click="logout">退出登录</button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { clearAuth, getUser, getPermissions } from '../auth'

const router = useRouter()
const searchQuery = ref('')
const username = computed(() => {
  const user = getUser()
  return user ? user.username : 'Guest'
})
const role = computed(() => {
  const user = getUser()
  return user ? user.role : 'UNKNOWN'
})

const doSearch = () => {
  const q = searchQuery.value.trim()
  if (!q) return
  const perms = getPermissions()
  if (perms.includes('hr:manage')) {
    router.push('/employees')
  } else if (perms.includes('hr:manage')) {
    router.push('/departments')
  }
}

const logout = () => {
  clearAuth()
  router.push('/login')
}
</script>

