<template>
  <div>
    <section class="card">
      <h3>新增部门</h3>
      <p style="color:var(--muted)">创建部门并设置负责人</p>
      <div class="form-grid">
        <input class="input" v-model="form.name" placeholder="部门名称" />
        <input class="input" v-model="form.parentId" placeholder="上级部门ID" />
        <input class="input" v-model="form.managerId" placeholder="负责人ID" />
        <input class="input" v-model="form.status" placeholder="状态(如 ACTIVE)" />
      </div>
      <div class="actions">
        <button class="btn" @click="create">新增部门</button>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑部门</h3>
      <div class="form-grid">
        <input class="input" v-model="editForm.id" placeholder="部门ID" />
        <input class="input" v-model="editForm.name" placeholder="部门名称" />
        <input class="input" v-model="editForm.parentId" placeholder="上级部门ID" />
        <input class="input" v-model="editForm.managerId" placeholder="负责人ID" />
        <input class="input" v-model="editForm.status" placeholder="状态" />
      </div>
      <div class="actions">
        <button class="btn" @click="update">更新部门</button>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">部门列表</h3>
        <input class="input" v-model="search" placeholder="搜索部门/状态" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>负责人</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.managerId }}</td>
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
  name: '',
  parentId: '',
  managerId: '',
  status: ''
})
const editForm = ref({ id: '', name: '', parentId: '', managerId: '', status: '' })
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
    list.value = await http.get('/api/departments')
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const create = async () => {
  error.value = ''
  try {
    await http.post('/api/departments', {
      name: form.value.name,
      parentId: form.value.parentId ? Number(form.value.parentId) : null,
      managerId: form.value.managerId ? Number(form.value.managerId) : null,
      status: form.value.status || null
    })
    form.value = { name: '', parentId: '', managerId: '', status: '' }
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const pick = (item) => {
  editForm.value = {
    id: item.id,
    name: item.name || '',
    parentId: item.parentId || '',
    managerId: item.managerId || '',
    status: item.status || ''
  }
}

const update = async () => {
  error.value = ''
  try {
    await http.put(`/api/departments/${editForm.value.id}`, {
      name: editForm.value.name || null,
      parentId: editForm.value.parentId ? Number(editForm.value.parentId) : null,
      managerId: editForm.value.managerId ? Number(editForm.value.managerId) : null,
      status: editForm.value.status || null
    })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const remove = async (id) => {
  error.value = ''
  try {
    await http.del(`/api/departments/${id}`)
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
