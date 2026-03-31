<template>
  <div>
    <div v-if="isAuthLayout">
      <router-view />
    </div>
    <div v-else class="app-shell">
      <Sidebar />
      <main class="main">
        <Topbar />
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from './components/Sidebar.vue'
import Topbar from './components/Topbar.vue'

const route = useRoute()
const isAuthLayout = computed(() => route.meta.layout === 'auth')
</script>
