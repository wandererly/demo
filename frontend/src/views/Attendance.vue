<template>
  <div>
    <section class="card">
      <h3>新增考勤记录</h3>
      <p style="color:var(--muted)">录入当天签到/签退信息</p>
      <div class="form-grid">
        <input class="input" v-model="form.empId" placeholder="员工ID" />
        <input class="input" v-model="form.workDate" placeholder="工作日期 YYYYY-MM-DD" />
        <input class="input" v-model="form.checkIn" placeholder="签到时间 YYYYY-MM-DDTHH:mm" />
        <input class="input" v-model="form.checkOut" placeholder="签退时间 YYYYY-MM-DDTHH:mm" />
        <input class="input" v-model="form.status" placeholder="状态(如 NORMAL)" />
      </div>
      <div class="actions">
        <button class="btn" @click="create">新增记录</button>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑考勤记录</h3>
      <div class="form-grid">
        <input class="input" v-model="editForm.id" placeholder="记录ID" />
        <input class="input" v-model="editForm.workDate" placeholder="工作日期 YYYYY-MM-DD" />
        <input class="input" v-model="editForm.checkIn" placeholder="签到时间" />
        <input class="input" v-model="editForm.checkOut" placeholder="签退时间" />
        <input class="input" v-model="editForm.status" placeholder="状态" />
      </div>
      <div class="actions">
        <button class="btn" @click="update">更新记录</button>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">考勤列表</h3>
        <input class="input" v-model="search" placeholder="搜索员工/状态" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>员工ID</th>
            <th>日期</th>
            <th>签到</th>
            <th>签退</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.empId }}</td>
            <td>{{ item.workDate }}</td>
            <td>{{ item.checkIn }}</td>
            <td>{{ item.checkOut }}</td>
            <td>{{ item.status }}</td>
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
const form = ref({ empId: '', workDate: '', checkIn: '', checkOut: '', status: '' })
const editForm = ref({ id: '', workDate: '', checkIn: '', checkOut: '', status: '' })
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
    list.value = await http.get('/api/attendance')
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const create = async () => {
  error.value = ''
  try {
    await http.post('/api/attendance', {
      empId: form.value.empId ? Number(form.value.empId) : null,
      workDate: form.value.workDate,
      checkIn: form.value.checkIn || null,
      checkOut: form.value.checkOut || null,
      status: form.value.status || null
    })
    form.value = { empId: '', workDate: '', checkIn: '', checkOut: '', status: '' }
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const pick = (item) => {
  editForm.value = {
    id: item.id,
    workDate: item.workDate || '',
    checkIn: item.checkIn || '',
    checkOut: item.checkOut || '',
    status: item.status || ''
  }
}

const update = async () => {
  error.value = ''
  try {
    await http.put(`/api/attendance/${editForm.value.id}`, {
      workDate: editForm.value.workDate || null,
      checkIn: editForm.value.checkIn || null,
      checkOut: editForm.value.checkOut || null,
      status: editForm.value.status || null
    })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const remove = async (id) => {
  if (!confirm('确定要删除该考勤记录吗？此操作不可撤销。')) return
  error.value = ''
  try {
    await http.del(`/api/attendance/${id}`)
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
