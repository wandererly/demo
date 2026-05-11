<template>
  <div>
    <section class="card">
      <h3>新增用户</h3>
      <div class="form-grid">
        <input class="input" v-model="userForm.username" placeholder="用户名" />
        <input class="input" v-model="userForm.passwordHash" placeholder="密码(可直接填明文)" />
        <input class="input" v-model="userForm.role" placeholder="角色(ADMIN/HR/EMPLOYEE)" />
        <input class="input" type="number" v-model="userForm.empId" placeholder="绑定员工ID" />
        <input class="input" v-model="userForm.status" placeholder="状态" />
      </div>
      <div class="actions">
        <button class="btn" @click="createUser">创建用户</button>
        <button class="btn-ghost" @click="loadUsers">刷新</button>
      </div>
    </section>

    <section class="card">
      <h3>编辑用户</h3>
      <div class="form-grid">
        <input class="input" type="number" v-model="userEdit.id" placeholder="用户ID" />
        <input class="input" v-model="userEdit.username" placeholder="用户名" />
        <input class="input" v-model="userEdit.passwordHash" placeholder="密码" />
        <input class="input" v-model="userEdit.role" placeholder="角色" />
        <input class="input" type="number" v-model="userEdit.empId" placeholder="绑定员工ID" />
        <input class="input" v-model="userEdit.status" placeholder="状态" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateUser">更新用户</button>
      </div>
    </section>

    <section class="card">
      <h3>新增角色</h3>
      <div class="form-grid">
        <input class="input" v-model="roleForm.roleName" placeholder="角色名称" />
        <input class="input" v-model="roleForm.roleKey" placeholder="角色标识" />
      </div>
      <div class="actions">
        <button class="btn" @click="createRole">创建角色</button>
        <button class="btn-ghost" @click="loadRoles">刷新角色</button>
      </div>
    </section>

    <section class="card">
      <h3>编辑角色</h3>
      <div class="form-grid">
        <input class="input" type="number" v-model="roleEdit.id" placeholder="角色ID" />
        <input class="input" v-model="roleEdit.roleName" placeholder="角色名称" />
        <input class="input" v-model="roleEdit.roleKey" placeholder="角色标识" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateRole">更新角色</button>
      </div>
    </section>

    <section class="card">
      <h3>权限管理</h3>
      <div class="form-grid">
        <input class="input" v-model="permForm.permKey" placeholder="权限标识 (e.g. hr:manage)" />
        <input class="input" v-model="permForm.permName" placeholder="权限名称" />
      </div>
      <div class="actions">
        <button class="btn" @click="createPerm">创建权限</button>
        <button class="btn-ghost" @click="loadPerms">加载权限</button>
      </div>

      <div class="form-grid" style="margin-top:12px">
        <input class="input" v-model="assign.roleKey" placeholder="角色标识" />
        <input class="input" v-model="assign.permKey" placeholder="权限标识" />
      </div>
      <div class="actions">
        <button class="btn" @click="assignPerm">授权</button>
        <button class="btn-ghost" @click="revokePerm">撤销</button>
        <button class="btn-ghost" @click="loadRolePerms">查看角色权限</button>
      </div>
      <div v-if="rolePerms.length" class="badge">{{ assign.roleKey }} 权限: {{ rolePerms.join(', ') }}</div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">用户列表</h3>
        <input class="input" v-model="userSearch" placeholder="搜索用户名" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户名</th>
            <th>角色</th>
            <th>员工ID</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in userPaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.username }}</td>
            <td>{{ item.role }}</td>
            <td>{{ item.empId || '-' }}</td>
            <td>{{ item.status }}</td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ userFiltered.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevUser" :disabled="userPage === 1">上一页</button>
          <div>第 {{ userPage }} / {{ userPages }} 页</div>
          <button class="btn-ghost" @click="nextUser" :disabled="userPage === userPages">下一页</button>
        </div>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">角色列表</h3>
        <input class="input" v-model="roleSearch" placeholder="搜索角色" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>标识</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in rolePaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.roleName }}</td>
            <td>{{ item.roleKey }}</td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ roleFiltered.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevRole" :disabled="rolePage === 1">上一页</button>
          <div>第 {{ rolePage }} / {{ rolePages }} 页</div>
          <button class="btn-ghost" @click="nextRole" :disabled="rolePage === rolePages">下一页</button>
        </div>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">权限列表</h3>
        <button class="btn-ghost" @click="loadPerms">刷新</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>标识</th>
            <th>名称</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in permissions" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.permKey }}</td>
            <td>{{ item.permName }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-if="error" class="card">
      <div class="alert">{{ error }}</div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'

const error = ref('')
const users = ref([])
const roles = ref([])
const permissions = ref([])
const rolePerms = ref([])

const userForm = ref({ username: '', passwordHash: '', role: '', empId: '', status: '' })
const userEdit = ref({ id: '', username: '', passwordHash: '', role: '', empId: '', status: '' })
const roleForm = ref({ roleName: '', roleKey: '' })
const roleEdit = ref({ id: '', roleName: '', roleKey: '' })
const permForm = ref({ permKey: '', permName: '' })
const assign = ref({ roleKey: '', permKey: '' })

const userSearch = ref('')
const roleSearch = ref('')
const userPage = ref(1)
const rolePage = ref(1)
const pageSize = 6

const userFiltered = computed(() => {
  const key = userSearch.value.trim().toLowerCase()
  if (!key) return users.value
  return users.value.filter((item) => `${item.username || ''}`.toLowerCase().includes(key))
})
const userPages = computed(() => Math.max(1, Math.ceil(userFiltered.value.length / pageSize)))
const userPaged = computed(() => userFiltered.value.slice((userPage.value - 1) * pageSize, userPage.value * pageSize))

const roleFiltered = computed(() => {
  const key = roleSearch.value.trim().toLowerCase()
  if (!key) return roles.value
  return roles.value.filter((item) => `${item.roleName || ''}`.toLowerCase().includes(key))
})
const rolePages = computed(() => Math.max(1, Math.ceil(roleFiltered.value.length / pageSize)))
const rolePaged = computed(() => roleFiltered.value.slice((rolePage.value - 1) * pageSize, rolePage.value * pageSize))

const loadUsers = async () => {
  error.value = ''
  try {
    users.value = await http.get('/api/rbac/users')
    userPage.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const loadRoles = async () => {
  error.value = ''
  try {
    roles.value = await http.get('/api/rbac/roles')
    rolePage.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const loadPerms = async () => {
  error.value = ''
  try {
    permissions.value = await http.get('/api/permissions')
  } catch (e) {
    error.value = e.message
  }
}

const loadRolePerms = async () => {
  error.value = ''
  if (!assign.value.roleKey) return
  try {
    rolePerms.value = await http.get(`/api/permissions/role/${assign.value.roleKey}`)
  } catch (e) {
    error.value = e.message
  }
}

const createUser = async () => {
  error.value = ''
  try {
    await http.post('/api/rbac/users', {
      username: userForm.value.username,
      passwordHash: userForm.value.passwordHash,
      role: userForm.value.role || null,
      empId: userForm.value.empId ? Number(userForm.value.empId) : null,
      status: userForm.value.status || null
    })
    userForm.value = { username: '', passwordHash: '', role: '', empId: '', status: '' }
    await loadUsers()
  } catch (e) {
    error.value = e.message
  }
}

const updateUser = async () => {
  error.value = ''
  try {
    await http.put(`/api/rbac/users/${userEdit.value.id}`, {
      username: userEdit.value.username || null,
      passwordHash: userEdit.value.passwordHash || null,
      role: userEdit.value.role || null,
      empId: userEdit.value.empId ? Number(userEdit.value.empId) : null,
      status: userEdit.value.status || null
    })
    await loadUsers()
  } catch (e) {
    error.value = e.message
  }
}

const createRole = async () => {
  error.value = ''
  try {
    await http.post('/api/rbac/roles', {
      roleName: roleForm.value.roleName,
      roleKey: roleForm.value.roleKey
    })
    roleForm.value = { roleName: '', roleKey: '' }
    await loadRoles()
  } catch (e) {
    error.value = e.message
  }
}

const updateRole = async () => {
  error.value = ''
  try {
    await http.put(`/api/rbac/roles/${roleEdit.value.id}`, {
      roleName: roleEdit.value.roleName || null,
      roleKey: roleEdit.value.roleKey || null
    })
    await loadRoles()
  } catch (e) {
    error.value = e.message
  }
}

const createPerm = async () => {
  error.value = ''
  try {
    await http.post('/api/permissions', {
      permKey: permForm.value.permKey,
      permName: permForm.value.permName
    })
    permForm.value = { permKey: '', permName: '' }
    await loadPerms()
  } catch (e) {
    error.value = e.message
  }
}

const assignPerm = async () => {
  error.value = ''
  try {
    await http.post('/api/permissions/assign', {
      roleKey: assign.value.roleKey,
      permKey: assign.value.permKey
    })
    await loadRolePerms()
  } catch (e) {
    error.value = e.message
  }
}

const revokePerm = async () => {
  error.value = ''
  try {
    await http.post('/api/permissions/revoke', {
      roleKey: assign.value.roleKey,
      permKey: assign.value.permKey
    })
    await loadRolePerms()
  } catch (e) {
    error.value = e.message
  }
}

const prevUser = () => { if (userPage.value > 1) userPage.value -= 1 }
const nextUser = () => { if (userPage.value < userPages.value) userPage.value += 1 }
const prevRole = () => { if (rolePage.value > 1) rolePage.value -= 1 }
const nextRole = () => { if (rolePage.value < rolePages.value) rolePage.value += 1 }

onMounted(() => {
  loadUsers()
  loadRoles()
  loadPerms()
})
</script>
