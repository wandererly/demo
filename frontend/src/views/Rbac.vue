<template>
  <div>
    <section class="card">
      <h3>新增用户</h3>
      <div class="form-grid">
        <input class="input" v-model="userForm.username" placeholder="登录账号" />
        <input class="input" v-model="userForm.passwordHash" placeholder="密码(可直接填明文)" />
        <input class="input" v-model="userForm.role" placeholder="角色编码（如 ADMIN）" />
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
        <input class="input" v-model="userEdit.username" placeholder="登录账号" />
        <input class="input" type="password" v-model="userEdit.passwordHash" placeholder="新密码（留空不修改）" />
        <input class="input" v-model="userEdit.role" placeholder="角色编码" />
        <input class="input" type="number" v-model="userEdit.empId" placeholder="绑定员工ID" />
        <input class="input" v-model="userEdit.status" placeholder="状态" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateUser">更新用户</button>
        <button class="btn-ghost" @click="resetPassword">重置密码</button>
      </div>
    </section>

    <section class="card">
      <h3>按员工编号重置密码</h3>
      <div class="form-grid">
        <input class="input" v-model="employeeReset.empNo" placeholder="员工编号，例如 E3003" />
        <input class="input" type="password" v-model="employeeReset.password" placeholder="新密码" />
      </div>
      <div class="actions">
        <button class="btn" @click="resetEmployeePassword">重置员工密码</button>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">用户列表</h3>
        <input class="input" v-model="userSearch" placeholder="搜索姓名或登录账号" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>用户</th>
            <th>角色</th>
            <th>员工ID</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in userPaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ usernameLabel(item) }}</td>
            <td>{{ roleLabel(item.role) }}</td>
            <td>{{ item.empId || '-' }}</td>
            <td>{{ statusLabel(item.status) }}</td>
            <td><button type="button" class="btn-ghost" @click.prevent.stop="selectUser(item)">选择</button></td>
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
            <th>角色名称</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in rolePaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ roleLabel(item.roleKey, item.roleName) }}</td>
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
            <th>权限名称</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in permissions" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ permissionLabel(item.permKey, item.permName) }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section v-if="notice" class="card">
      <div class="badge">{{ notice }}</div>
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
const notice = ref('')
const users = ref([])
const employees = ref([])
const roles = ref([])
const permissions = ref([])

const userForm = ref({ username: '', passwordHash: '', role: '', empId: '', status: '' })
const userEdit = ref({ id: '', username: '', passwordHash: '', role: '', empId: '', status: '' })
const employeeReset = ref({ empNo: '', password: '' })

const userSearch = ref('')
const roleSearch = ref('')
const userPage = ref(1)
const rolePage = ref(1)
const pageSize = 6

const userFiltered = computed(() => {
  const key = userSearch.value.trim().toLowerCase()
  if (!key) return users.value
  return users.value.filter((item) => `${usernameLabel(item)}${item.username || ''}`.toLowerCase().includes(key))
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

// 页面显示中文名称，接口仍使用数据库中的英文编码。
const roleLabel = (value, fallback = '') => ({
  ADMIN: '系统管理员',
  HR: '人事专员',
  EMPLOYEE: '普通员工'
}[value] || fallback || value || '-')

const statusLabel = (value) => ({
  ACTIVE: '启用',
  DISABLED: '停用',
  INACTIVE: '停用'
}[value] || value || '-')

const permissionLabel = (value, fallback = '') => ({
  'rbac:manage': '权限管理',
  'config:manage': '系统配置',
  'hr:manage': '员工与部门管理',
  'attendance:manage': '考勤管理',
  'leave:manage': '请假管理',
  'perf:manage': '绩效管理',
  'payroll:manage': '薪酬管理',
  'approval:manage': '统一审批',
  'audit:view': '审计日志',
  'report:view': '报表查看',
  'self:view': '员工自助'
}[value] || fallback || value || '-')

const employeeName = (empId) => {
  if (!empId) return ''
  return employees.value.find((item) => String(item.id) === String(empId))?.name || ''
}

const usernameLabel = (item) => {
  const account = item?.username || '-'
  const name = employeeName(item?.empId)
  if (name) return `${name}（${account}）`
  if (account.toLowerCase() === 'admin') return `系统管理员（${account}）`
  return account
}

const loadEmployees = async () => {
  try {
    employees.value = await http.get('/api/employees') || []
  } catch (_) {
    // 员工姓名仅用于显示，加载失败不影响权限管理的账号操作。
    employees.value = []
  }
}

const loadUsers = async () => {
  error.value = ''
  try {
    users.value = await http.get('/api/rbac/users')
    userPage.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const selectUser = (item) => {
  userEdit.value = {
    id: item.id || '',
    username: item.username || '',
    passwordHash: '',
    role: item.role || '',
    empId: item.empId || '',
    status: item.status || ''
  }
  notice.value = `已选择用户 ${usernameLabel(item)}，请输入新密码后重置`
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

const createUser = async () => {
  error.value = ''
  notice.value = ''
  try {
    await http.post('/api/rbac/users', {
      username: userForm.value.username,
      passwordHash: userForm.value.passwordHash,
      role: userForm.value.role || null,
      empId: userForm.value.empId ? Number(userForm.value.empId) : null,
      status: userForm.value.status || null
    })
    userForm.value = { username: '', passwordHash: '', role: '', empId: '', status: '' }
    notice.value = '用户创建成功'
    await loadUsers()
  } catch (e) {
    error.value = e.message
  }
}

const updateUser = async () => {
  error.value = ''
  notice.value = ''
  if (!userEdit.value.id) {
    error.value = '请先在用户列表中选择用户'
    return
  }
  try {
    await http.put(`/api/rbac/users/${userEdit.value.id}`, {
      username: userEdit.value.username || null,
      passwordHash: userEdit.value.passwordHash || null,
      role: userEdit.value.role || null,
      empId: userEdit.value.empId ? Number(userEdit.value.empId) : null,
      status: userEdit.value.status || null
    })
    notice.value = '用户信息更新成功'
    await loadUsers()
  } catch (e) {
    error.value = e.message
  }
}

const resetPassword = async () => {
  error.value = ''
  notice.value = ''
  if (!userEdit.value.id) {
    error.value = '请先在用户列表中选择用户'
    return
  }
  if (!userEdit.value.passwordHash) {
    error.value = '请输入新密码'
    return
  }
  try {
    await http.post(`/api/rbac/users/${userEdit.value.id}/reset-password`, {
      password: userEdit.value.passwordHash
    })
    userEdit.value.passwordHash = ''
    notice.value = `用户 ${userEdit.value.username} 的密码已重置`
  } catch (e) {
    error.value = e.message
  }
}

const resetEmployeePassword = async () => {
  error.value = ''
  notice.value = ''
  if (!employeeReset.value.empNo || !employeeReset.value.password) {
    error.value = '请输入员工编号和新密码'
    return
  }
  try {
    await http.post('/api/rbac/users/reset-employee-password', employeeReset.value)
    notice.value = `员工 ${employeeReset.value.empNo.toUpperCase()} 的密码已重置`
    employeeReset.value.password = ''
    await loadUsers()
  } catch (e) {
    error.value = e.message
  }
}

const prevUser = () => { if (userPage.value > 1) userPage.value -= 1 }
const nextUser = () => { if (userPage.value < userPages.value) userPage.value += 1 }
const prevRole = () => { if (rolePage.value > 1) rolePage.value -= 1 }
const nextRole = () => { if (rolePage.value < rolePages.value) rolePage.value += 1 }

onMounted(() => {
  loadEmployees()
  loadUsers()
  loadRoles()
  loadPerms()
})
</script>
