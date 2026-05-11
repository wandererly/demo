<template>
  <div>
    <section class="card">
      <h3>新增请假</h3>
      <div class="form-grid">
        <select class="input" v-model="form.empId">
          <option value="">请选择员工</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
        <select class="input" v-model="form.leaveType">
          <option value="">请选择请假类型</option>
          <option v-for="type in leaveTypes" :key="type" :value="type">{{ type }}</option>
        </select>
        <input class="input" type="datetime-local" v-model="form.startTime" />
        <input class="input" type="datetime-local" v-model="form.endTime" />
        <input class="input" type="number" v-model="form.days" placeholder="天数" min="0" step="1" />
        <input class="input" v-model="form.reason" placeholder="请假原因" />
      </div>
      <div class="actions">
        <button class="btn" @click="create">提交申请</button>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑请假</h3>
      <div v-if="editForm.id" class="badge">当前申请 #{{ editForm.id }}</div>
      <div class="form-grid">
        <select class="input" v-model="editForm.leaveType">
          <option value="">请选择请假类型</option>
          <option v-for="type in leaveTypes" :key="type" :value="type">{{ type }}</option>
        </select>
        <input class="input" type="datetime-local" v-model="editForm.startTime" />
        <input class="input" type="datetime-local" v-model="editForm.endTime" />
        <input class="input" type="number" v-model="editForm.days" placeholder="天数" min="0" step="1" />
        <input class="input" v-model="editForm.reason" placeholder="原因" />
        <select class="input" v-model="editForm.status">
          <option value="PENDING">待审批</option>
          <option value="PENDING_L1">一级待审</option>
          <option value="PENDING_L2">二级待审</option>
          <option value="APPROVED">已通过</option>
          <option value="REJECTED">已驳回</option>
        </select>
        <select class="input" v-model="editForm.approverId">
          <option value="">请选择审批人</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
      </div>
      <div class="actions">
        <button class="btn" @click="update">更新请假</button>
        <router-link class="btn-ghost" to="/approvals">统一审批</router-link>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">请假列表</h3>
        <input class="input" v-model="search" placeholder="搜索员工/状态" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr><th>ID</th><th>员工</th><th>类型</th><th>天数</th><th>状态</th><th>操作</th></tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ employeeName(item.empId) }}</td>
            <td>{{ item.leaveType }}</td>
            <td>{{ item.days }}</td>
            <td>{{ statusText(item.status) }}</td>
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
import { computed, onMounted, ref, watch } from 'vue'
import { http } from '../api/http'

const list = ref([])
const employees = ref([])
const error = ref('')
const form = ref({ empId: '', leaveType: '', startTime: '', endTime: '', days: '', reason: '' })
const editForm = ref({ id: '', leaveType: '', startTime: '', endTime: '', days: '', reason: '', status: '', approverId: '' })
const leaveTypes = ['年假', '事假', '病假', '调休', '婚假', '产假', '丧假']
const search = ref('')
const page = ref(1)
const pageSize = 6

const filteredList = computed(() => {
  const key = search.value.trim().toLowerCase()
  if (!key) return list.value
  return list.value.filter(item => `${employeeName(item.empId)}${item.status || ''}`.toLowerCase().includes(key))
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize)))
const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize
  return filteredList.value.slice(start, start + pageSize)
})

const load = async () => {
  error.value = ''
  try {
    const [leaveData, employeeData] = await Promise.all([
      http.get('/api/leave'),
      http.get('/api/employees').catch(() => employees.value)
    ])
    list.value = leaveData || []
    employees.value = employeeData || []
    page.value = 1
  }
  catch (e) { error.value = e.message }
}

const fmtDt = (v) => {
  if (!v) return ''
  const d = new Date(v)
  if (isNaN(d.getTime())) return v
  return d.toISOString().slice(0, 16)
}

const calcLeaveDays = (start, end) => {
  if (!start || !end) return ''
  const startTime = new Date(start).getTime()
  const endTime = new Date(end).getTime()
  if (Number.isNaN(startTime) || Number.isNaN(endTime) || endTime <= startTime) return ''
  const hours = (endTime - startTime) / 1000 / 60 / 60
  return Math.max(1, Math.ceil(hours / 24))
}

watch(() => [form.value.startTime, form.value.endTime], ([start, end]) => {
  const days = calcLeaveDays(start, end)
  if (days !== '') form.value.days = days
})

watch(() => [editForm.value.startTime, editForm.value.endTime], ([start, end]) => {
  const days = calcLeaveDays(start, end)
  if (days !== '') editForm.value.days = days
})

const create = async () => {
  error.value = ''
  if (!form.value.empId) { error.value = '请选择员工'; return }
  try {
    await http.post('/api/leave', {
      empId: Number(form.value.empId),
      leaveType: form.value.leaveType || null,
      startTime: form.value.startTime || null,
      endTime: form.value.endTime || null,
      days: form.value.days ? parseFloat(form.value.days) : null,
      reason: form.value.reason || null
    })
    form.value = { empId: '', leaveType: '', startTime: '', endTime: '', days: '', reason: '' }
    await load()
  } catch (e) { error.value = e.message }
}

const pick = (item) => {
  editForm.value = {
    id: item.id, leaveType: item.leaveType || '',
    startTime: fmtDt(item.startTime), endTime: fmtDt(item.endTime),
    days: item.days, reason: item.reason || '',
    status: item.status || '', approverId: item.approverId || ''
  }
}

const update = async () => {
  error.value = ''
  if (!editForm.value.id) {
    error.value = '请先从列表选择一条请假记录'
    return
  }
  try {
    await http.put(`/api/leave/${editForm.value.id}`, {
      leaveType: editForm.value.leaveType || null,
      startTime: editForm.value.startTime || null,
      endTime: editForm.value.endTime || null,
      days: editForm.value.days ? parseFloat(editForm.value.days) : null,
      reason: editForm.value.reason || null,
      status: editForm.value.status || null,
      approverId: editForm.value.approverId ? Number(editForm.value.approverId) : null
    })
    await load()
  } catch (e) { error.value = e.message }
}

const remove = async (id) => {
  if (!confirm('确定要删除该请假记录吗？此操作不可撤销。')) return
  error.value = ''
  try { await http.del(`/api/leave/${id}`); await load() }
  catch (e) { error.value = e.message }
}

const prevPage = () => { if (page.value > 1) page.value -= 1 }
const nextPage = () => { if (page.value < totalPages.value) page.value += 1 }

const employeeName = (id) => {
  if (!id) return '-'
  return employees.value.find((item) => String(item.id) === String(id))?.name || `员工 #${id}`
}

const employeeLabel = (employee) => `${employee.name}${employee.position ? `（${employee.position}）` : ''}`

const statusText = (status) => ({
  PENDING: '待审批',
  PENDING_L1: '一级待审',
  PENDING_L2: '二级待审',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}[status] || status || '-')

onMounted(load)
</script>
