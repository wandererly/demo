<template>
  <div>
    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">审计日志</h3>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <input class="input" v-model="search" placeholder="搜索模块/动作/操作人" style="max-width:320px;margin-top:12px" />
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <table class="table">
        <thead>
          <tr>
            <th>时间</th>
            <th>模块</th>
            <th>动作</th>
            <th>对象</th>
            <th>操作人</th>
            <th>详情</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="item.id">
            <td>{{ fmt(item.createdAt) }}</td>
            <td>{{ item.module }}</td>
            <td>{{ item.action }}</td>
            <td>{{ item.targetType }} #{{ item.targetId }}</td>
            <td>{{ item.actor }}</td>
            <td>{{ item.detail || '-' }}</td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ filtered.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevPage" :disabled="page === 1">上一页</button>
          <div>第 {{ page }} / {{ totalPages }} 页</div>
          <button class="btn-ghost" @click="nextPage" :disabled="page === totalPages">下一页</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'

const logs = ref([])
const search = ref('')
const error = ref('')
const page = ref(1)
const pageSize = 10

const filtered = computed(() => {
  const key = search.value.trim().toLowerCase()
  if (!key) return logs.value
  return logs.value.filter((item) => `${item.module || ''}${item.action || ''}${item.actor || ''}${item.detail || ''}`.toLowerCase().includes(key))
})
const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
const pagedList = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const load = async () => {
  error.value = ''
  try {
    logs.value = await http.get('/api/audit-logs')
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const fmt = (value) => value ? String(value).replace('T', ' ').slice(0, 19) : '-'
const prevPage = () => { if (page.value > 1) page.value -= 1 }
const nextPage = () => { if (page.value < totalPages.value) page.value += 1 }

onMounted(load)
</script>
