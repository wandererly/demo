<template>
  <div>
    <section class="card">
      <h3>新增员工</h3>
      <p style="color:var(--muted)">录入基本信息与入职信息</p>
      <div class="form-grid">
        <input class="input" v-model="form.empNo" placeholder="员工编号" />
        <input class="input" v-model="form.name" placeholder="姓名" />
        <input class="input" v-model="form.gender" placeholder="性别" />
        <input class="input" v-model="form.phone" placeholder="手机号" />
        <input class="input" v-model="form.email" placeholder="邮箱" />
        <input class="input" v-model="form.deptId" placeholder="部门ID" />
        <input class="input" v-model="form.position" placeholder="岗位" />
        <input class="input" v-model="form.hireDate" placeholder="入职日期 YYYY-MM-DD" />
        <input class="input" v-model="form.baseSalary" placeholder="基础薪资" />
      </div>
      <div class="actions">
        <button class="btn" @click="create">新增员工</button>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑员工</h3>
      <div class="form-grid">
        <input class="input" v-model="editForm.id" placeholder="员工ID" />
        <input class="input" v-model="editForm.name" placeholder="姓名" />
        <input class="input" v-model="editForm.gender" placeholder="性别" />
        <input class="input" v-model="editForm.phone" placeholder="手机号" />
        <input class="input" v-model="editForm.email" placeholder="邮箱" />
        <input class="input" v-model="editForm.deptId" placeholder="部门ID" />
        <input class="input" v-model="editForm.position" placeholder="岗位" />
        <input class="input" v-model="editForm.hireDate" placeholder="入职日期 YYYY-MM-DD" />
        <input class="input" v-model="editForm.status" placeholder="状态" />
        <input class="input" v-model="editForm.baseSalary" placeholder="基础薪资" />
      </div>
      <div class="actions">
        <button class="btn" @click="update">更新员工</button>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">员工列表</h3>
        <input class="input" v-model="search" placeholder="搜索姓名/状态" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>姓名</th>
            <th>部门</th>
            <th>岗位</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.deptId }}</td>
            <td>{{ item.position }}</td>
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
const form = ref({
  empNo: '',
  name: '',
  gender: '',
  phone: '',
  email: '',
  deptId: '',
  position: '',
  hireDate: '',
  baseSalary: ''
})
const editForm = ref({
  id: '',
  name: '',
  gender: '',
  phone: '',
  email: '',
  deptId: '',
  position: '',
  hireDate: '',
  status: '',
  baseSalary: ''
})
const search = ref('')
const page = ref(1)
const pageSize = 6

const filteredList = computed(() => {
  const key = search.value.trim().toLowerCase()
  if (!key) return list.value
  return list.value.filter((item) =>
    `${item.name || ''}${item.status || ''}`.toLowerCase().includes(key)
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
    list.value = await http.get('/api/employees')
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const create = async () => {
  error.value = ''
  try {
    await http.post('/api/employees', {
      empNo: form.value.empNo,
      name: form.value.name,
      gender: form.value.gender,
      phone: form.value.phone,
      email: form.value.email,
      deptId: form.value.deptId ? Number(form.value.deptId) : null,
      position: form.value.position,
      hireDate: form.value.hireDate,
      baseSalary: form.value.baseSalary ? Number(form.value.baseSalary) : null
    })
    form.value = { empNo: '', name: '', gender: '', phone: '', email: '', deptId: '', position: '', hireDate: '', baseSalary: '' }
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const pick = (item) => {
  editForm.value = {
    id: item.id,
    name: item.name || '',
    gender: item.gender || '',
    phone: item.phone || '',
    email: item.email || '',
    deptId: item.deptId || '',
    position: item.position || '',
    hireDate: item.hireDate || '',
    status: item.status || '',
    baseSalary: item.baseSalary || ''
  }
}

const update = async () => {
  error.value = ''
  try {
    await http.put(`/api/employees/${editForm.value.id}`, {
      name: editForm.value.name || null,
      gender: editForm.value.gender || null,
      phone: editForm.value.phone || null,
      email: editForm.value.email || null,
      deptId: editForm.value.deptId ? Number(editForm.value.deptId) : null,
      position: editForm.value.position || null,
      hireDate: editForm.value.hireDate || null,
      status: editForm.value.status || null,
      baseSalary: editForm.value.baseSalary ? Number(editForm.value.baseSalary) : null
    })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const remove = async (id) => {
  error.value = ''
  try {
    await http.del(`/api/employees/${id}`)
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
