<template>
  <div>
    <section class="card">
      <h3>新增请假</h3>
      <div class="form-grid">
        <input class="input" v-model="form.empId" placeholder="员工ID" />
        <input class="input" v-model="form.leaveType" placeholder="请假类型(年假/病假)" />
        <input class="input" v-model="form.startTime" placeholder="开始时间 YYYYY-MM-DDTHH:mm" />
        <input class="input" v-model="form.endTime" placeholder="结束时间 YYYYY-MM-DDTHH:mm" />
        <input class="input" v-model="form.days" placeholder="天数" />
        <input class="input" v-model="form.reason" placeholder="原因" />
      </div>
      <div class="actions">
        <button class="btn" @click="create">提交申请</button>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑请假</h3>
      <div class="form-grid">
        <input class="input" v-model="editForm.id" placeholder="记录ID" />
        <input class="input" v-model="editForm.leaveType" placeholder="类型" />
        <input class="input" v-model="editForm.startTime" placeholder="开始时间" />
        <input class="input" v-model="editForm.endTime" placeholder="结束时间" />
        <input class="input" v-model="editForm.days" placeholder="天数" />
        <input class="input" v-model="editForm.reason" placeholder="原因" />
        <input class="input" v-model="editForm.status" placeholder="状态" />
        <input class="input" v-model="editForm.approverId" placeholder="审批人ID" />
      </div>
      <div class="actions">
        <button class="btn" @click="update">更新记录</button>
      </div>
    </section>

    <section class="card">
      <h3>审批请假</h3>
      <div class="form-grid">
        <input class="input" v-model="approve.id" placeholder="记录ID" />
        <input class="input" v-model="approve.approverId" placeholder="审批人ID" />
        <input class="input" v-model="approve.status" placeholder="状态(APPROVED/REJECTED)" />
      </div>
      <div class="actions">
        <button class="btn" @click="approveLeave">审批</button>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">请假列表</h3>
        <input class="input" v-model="search" placeholder="搜索员工/状态" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>员工ID</th>
            <th>类型</th>
            <th>开始</th>
            <th>结束</th>
            <th>状态</th>
            <th>审批人</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.empId }}</td>
            <td>{{ item.leaveType }}</td>
            <td>{{ item.startTime }}</td>
            <td>{{ item.endTime }}</td>
            <td>{{ item.status }}</td>
            <td>{{ item.approverId }}</td>
            <td>
              <button class="btn-ghost" @click="pick(item)">编辑</button>
              <button class="btn-danger btn" @click="remove(item.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ filteredList.length }} 条</div>
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

const list = ref([])
const error = ref('')
const form = ref({ empId: '', leaveType: '', startTime: '', endTime: '', days: '', reason: '' })
const editForm = ref({
  id: '',
  leaveType: '',
  startTime: '',
  endTime: '',
  days: '',
  reason: '',
  status: '',
  approverId: ''
})
const approve = ref({ id: '', approverId: '', status: '' })
const search = ref('')
const page = ref(1)
const pageSize = 6

const filteredList = computed(() => {
  const key = search.value.trim().toLowerCase()
  if (!key) return list.value
  return list.value.filter((item) =>
    `${item.empId || ''}${item.status || ''}`.toLowerCase().includes(key)
  )
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize)))

const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

const load = async () => {
  error.value = ''
  try {
    list.value = await http.get('/api/leave')
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const create = async () => {
  error.value = ''
  try {
    await http.post('/api/leave', {
      empId: form.value.empId ? Number(form.value.empId) : null,
      leaveType: form.value.leaveType,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      days: form.value.days ? Number(form.value.days) : null,
      reason: form.value.reason
    })
    form.value = { empId: '', leaveType: '', startTime: '', endTime: '', days: '', reason: '' }
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const pick = (item) => {
  editForm.value = {
    id: item.id,
    leaveType: item.leaveType || '',
    startTime: item.startTime || '',
    endTime: item.endTime || '',
    days: item.days || '',
    reason: item.reason || '',
    status: item.status || '',
    approverId: item.approverId || ''
  }
}

const update = async () => {
  error.value = ''
  try {
    await http.put(`/api/leave/${editForm.value.id}`, {
      leaveType: editForm.value.leaveType || null,
      startTime: editForm.value.startTime || null,
      endTime: editForm.value.endTime || null,
      days: editForm.value.days ? Number(editForm.value.days) : null,
      reason: editForm.value.reason || null,
      status: editForm.value.status || null,
      approverId: editForm.value.approverId ? Number(editForm.value.approverId) : null
    })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const approveLeave = async () => {
  error.value = ''
  try {
    await http.post(`/api/leave/${approve.value.id}/approve`, {
      approverId: approve.value.approverId ? Number(approve.value.approverId) : null,
      status: approve.value.status
    })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const remove = async (id) => {
  if (!confirm('确定要删除该请假记录吗？此操作不可撤销。')) return
  error.value = ''
  try {
    await http.del(`/api/leave/${id}`)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const prevPage = () => {
  if (page.value > 1) page.value -= 1
}

const nextPage = () => {
  if (page.value < totalPages.value) page.value += 1
}

onMounted(load)
</script>
