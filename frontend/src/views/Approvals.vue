<template>
  <div>
    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">审批中心</h3>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div class="form-grid">
        <div class="input approval-operator">当前审批人：{{ currentUserLabel }}</div>
        <input class="input" v-model="decision.comment" placeholder="审批意见" />
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <table class="table">
        <thead>
          <tr>
            <th>类型</th>
            <th>申请人</th>
            <th>事项</th>
            <th>状态</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="`${item.type}-${item.businessId}`">
            <td>{{ item.source }}</td>
            <td>{{ item.applicantName }}</td>
            <td>{{ item.title }}</td>
            <td>{{ statusText(item.status) }}</td>
            <td>{{ fmt(item.createdAt) }}</td>
            <td>
              <button class="btn" @click="submit(item, 'APPROVED')">通过</button>
              <button class="btn-danger btn" @click="submit(item, 'REJECTED')">驳回</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ tasks.length }} 条</div>
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
import { getUser } from '../auth'

const tasks = ref([])
const decision = ref({ comment: '' })
const error = ref('')
const page = ref(1)
const pageSize = 8

const currentUser = computed(() => getUser() || {})
const currentUserLabel = computed(() => {
  const username = currentUser.value.username || '未登录'
  const role = currentUser.value.role || 'UNKNOWN'
  return `${username} / ${role}`
})
const totalPages = computed(() => Math.max(1, Math.ceil(tasks.value.length / pageSize)))
const pagedList = computed(() => tasks.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const load = async () => {
  error.value = ''
  try {
    const approvalData = await http.get('/api/approvals')
    tasks.value = approvalData || []
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const submit = async (item, status) => {
  error.value = ''
  try {
    await http.post('/api/approvals/decision', {
      type: item.type,
      businessId: item.businessId,
      status,
      comment: decision.value.comment || null
    })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const statusText = (status) => ({
  PENDING: '待审批',
  PENDING_L1: '一级待审',
  PENDING_L2: '二级待审',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}[status] || status || '-')

const fmt = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : '-'
const prevPage = () => { if (page.value > 1) page.value -= 1 }
const nextPage = () => { if (page.value < totalPages.value) page.value += 1 }

onMounted(load)
</script>
