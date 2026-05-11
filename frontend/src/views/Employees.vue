<template>
  <div>
    <section class="card">
      <h3>新增员工</h3>
      <p style="color:var(--muted)">录入基本信息与入职信息</p>
      <div class="form-grid">
        <input class="input" v-model="form.empNo" placeholder="员工编号(留空自动生成)" />
        <input class="input" v-model="form.name" placeholder="姓名" required />
        <select class="input" v-model="form.gender">
          <option value="">请选择性别</option>
          <option value="男">男</option>
          <option value="女">女</option>
          <option value="其他">其他</option>
        </select>
        <input class="input" v-model="form.phone" placeholder="手机号" />
        <input class="input" type="email" v-model="form.email" placeholder="邮箱" />
        <select class="input" v-model="form.deptId" required>
          <option value="">请选择部门</option>
          <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
        </select>
        <input class="input" v-model="form.position" placeholder="岗位" />
        <input class="input" type="date" v-model="form.hireDate" required />
        <input class="input" type="number" v-model="form.baseSalary" placeholder="基础薪资(不低于3500)" min="3500" step="100" required />
      </div>
      <div class="actions">
        <button class="btn" @click="create">新增员工</button>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑员工</h3>
      <div v-if="editForm.id" class="badge">当前员工 #{{ editForm.id }}</div>
      <div class="form-grid">
        <input class="input" v-model="editForm.name" placeholder="姓名" />
        <select class="input" v-model="editForm.gender">
          <option value="">请选择性别</option>
          <option value="男">男</option>
          <option value="女">女</option>
          <option value="其他">其他</option>
        </select>
        <input class="input" v-model="editForm.phone" placeholder="手机号" />
        <input class="input" type="email" v-model="editForm.email" placeholder="邮箱" />
        <select class="input" v-model="editForm.deptId">
          <option value="">请选择部门</option>
          <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
        </select>
        <input class="input" v-model="editForm.position" placeholder="岗位" />
        <input class="input" type="date" v-model="editForm.hireDate" />
        <select class="input" v-model="editForm.status">
          <option value="">状态</option>
          <option value="ACTIVE">在职</option>
          <option value="INACTIVE">离职</option>
        </select>
        <input class="input" type="number" v-model="editForm.baseSalary" placeholder="基础薪资(不低于3500)" min="3500" step="100" />
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
            <th>员工账号</th>
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
            <td>{{ item.accountUsername || '未生成' }}</td>
            <td>{{ departmentName(item.deptId) }}</td>
            <td>{{ item.position }}</td>
            <td>{{ statusLabel(item.status) }}</td>
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

    <section class="card">
      <h3>员工生命周期</h3>
      <div class="tabs">
        <button :class="{ active: lifecycleTab === 'contract' }" @click="lifecycleTab = 'contract'">合同</button>
        <button :class="{ active: lifecycleTab === 'regularization' }" @click="lifecycleTab = 'regularization'">转正</button>
        <button :class="{ active: lifecycleTab === 'transfer' }" @click="lifecycleTab = 'transfer'">调岗</button>
        <button :class="{ active: lifecycleTab === 'resignation' }" @click="lifecycleTab = 'resignation'">离职</button>
      </div>

      <div v-if="lifecycleTab === 'contract'">
        <div class="form-grid">
          <select class="input" v-model="contractForm.empId">
            <option value="">请选择员工</option>
            <option v-for="emp in list" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
          </select>
          <input class="input" v-model="contractForm.contractNo" placeholder="合同编号" />
          <select class="input" v-model="contractForm.contractType">
            <option value="">合同类型</option>
            <option value="FIXED">固定期限</option>
            <option value="UNFIXED">无固定期限</option>
            <option value="INTERNSHIP">实习协议</option>
          </select>
          <input class="input" type="date" v-model="contractForm.startDate" />
          <input class="input" type="date" v-model="contractForm.endDate" />
          <select class="input" v-model="contractForm.status">
            <option value="ACTIVE">生效</option>
            <option value="EXPIRED">到期</option>
            <option value="TERMINATED">终止</option>
          </select>
        </div>
        <div class="actions">
          <button class="btn" @click="createContract">创建合同</button>
          <button class="btn-ghost" @click="loadLifecycle">刷新</button>
        </div>
      </div>

      <div v-else>
        <div class="form-grid">
          <select class="input" v-model="eventForm.empId">
            <option value="">请选择员工</option>
            <option v-for="emp in list" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
          </select>
          <select v-if="lifecycleTab === 'transfer'" class="input" v-model="eventForm.toDeptId">
            <option value="">请选择目标部门</option>
            <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
          </select>
          <input v-if="lifecycleTab === 'transfer'" class="input" v-model="eventForm.toPosition" placeholder="目标岗位" />
          <input class="input" type="date" v-model="eventForm.effectiveDate" />
          <input class="input" v-model="eventForm.detail" :placeholder="lifecyclePlaceholder" />
        </div>
        <div class="actions">
          <button class="btn" @click="submitLifecycleEvent">{{ lifecycleActionText }}</button>
          <router-link class="btn-ghost" to="/approvals">查看统一审批</router-link>
        </div>
      </div>
      <div v-if="lifecycleInfo" class="badge">{{ lifecycleInfo }}</div>
    </section>

    <section class="grid grid-2 lifecycle-grid">
      <div class="card lifecycle-list-card">
        <h3>合同列表</h3>
        <table class="table lifecycle-table" v-if="contracts.length">
          <thead><tr><th>员工</th><th>编号</th><th>类型</th><th>开始</th><th>结束</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="item in contracts.slice(0, 8)" :key="item.id">
              <td>{{ employeeName(item.empId) }}</td>
              <td>{{ item.contractNo }}</td>
              <td>{{ contractTypeText(item.contractType) }}</td>
              <td>{{ item.startDate }}</td>
              <td>{{ item.endDate || '-' }}</td>
              <td>{{ contractStatusText(item.status) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state compact">暂无合同记录</div>
      </div>
      <div class="card lifecycle-list-card">
        <h3>生命周期记录</h3>
        <table class="table lifecycle-table" v-if="lifecycleEvents.length">
          <thead><tr><th>员工</th><th>类型</th><th>生效日期</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="item in lifecycleEvents.slice(0, 8)" :key="item.id">
              <td>{{ employeeName(item.empId) }}</td>
              <td>{{ lifecycleTypeText(item.eventType) }}</td>
              <td>{{ item.effectiveDate || '-' }}</td>
              <td>{{ lifecycleStatusText(item.status) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state compact">暂无生命周期记录</div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'

const list = ref([])
const departments = ref([])
const error = ref('')
const lifecycleInfo = ref('')
const contracts = ref([])
const lifecycleEvents = ref([])
const lifecycleTab = ref('contract')
const form = ref({
  empNo: '', name: '', gender: '', phone: '', email: '',
  deptId: '', position: '', hireDate: '', baseSalary: ''
})
const editForm = ref({
  id: '', name: '', gender: '', phone: '', email: '',
  deptId: '', position: '', hireDate: '', status: '', baseSalary: ''
})
const contractForm = ref({ empId: '', contractNo: '', contractType: '', startDate: '', endDate: '', status: 'ACTIVE' })
const eventForm = ref({ empId: '', toDeptId: '', toPosition: '', effectiveDate: '', detail: '' })
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

const lifecycleEventType = computed(() => ({
  regularization: 'REGULARIZATION',
  transfer: 'TRANSFER',
  resignation: 'RESIGNATION'
}[lifecycleTab.value]))

const lifecycleActionText = computed(() => ({
  regularization: '提交转正审批',
  transfer: '提交调岗审批',
  resignation: '提交离职审批'
}[lifecycleTab.value] || '提交审批'))

const lifecyclePlaceholder = computed(() => ({
  regularization: '转正说明',
  transfer: '调岗原因',
  resignation: '离职原因'
}[lifecycleTab.value] || '说明'))

const load = async () => {
  error.value = ''
  try {
    const [employees, deptList] = await Promise.all([
      http.get('/api/employees'),
      http.get('/api/departments').catch(() => departments.value)
    ])
    list.value = employees || []
    departments.value = deptList || []
    page.value = 1
    await loadLifecycle()
  } catch (e) {
    error.value = e.message
  }
}

const loadLifecycle = async () => {
  const [contractData, eventData] = await Promise.all([
    http.get('/api/lifecycle/contracts').catch(() => contracts.value),
    http.get('/api/lifecycle/events').catch(() => lifecycleEvents.value)
  ])
  contracts.value = contractData || []
  lifecycleEvents.value = eventData || []
}

const create = async () => {
  error.value = ''
  if (!form.value.name.trim()) {
    error.value = '姓名为必填项'
    return
  }
  try {
    await http.post('/api/employees', {
      empNo: form.value.empNo.trim() || null,
      name: form.value.name.trim(),
      gender: form.value.gender || null,
      phone: form.value.phone || null,
      email: form.value.email || null,
      deptId: form.value.deptId ? Number(form.value.deptId) : null,
      position: form.value.position || null,
      hireDate: form.value.hireDate || null,
      baseSalary: form.value.baseSalary ? parseFloat(form.value.baseSalary) : null
    })
    form.value = { empNo: '', name: '', gender: '', phone: '', email: '', deptId: '', position: '', hireDate: '', baseSalary: '' }
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const pick = (item) => {
  editForm.value = {
    id: item.id, name: item.name || '', gender: genderForForm(item.gender),
    phone: item.phone || '', email: item.email || '',
    deptId: item.deptId || '', position: item.position || '',
    hireDate: item.hireDate || '', status: item.status || '',
    baseSalary: item.baseSalary || ''
  }
}

const genderForForm = (gender) => {
  if (gender === 'M') return '男'
  if (gender === 'F') return '女'
  return gender || ''
}

const update = async () => {
  error.value = ''
  if (!editForm.value.id) {
    error.value = '请先从员工列表选择一条记录'
    return
  }
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
      baseSalary: editForm.value.baseSalary ? parseFloat(editForm.value.baseSalary) : null
    })
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const createContract = async () => {
  error.value = ''
  lifecycleInfo.value = ''
  if (!contractForm.value.empId || !contractForm.value.contractNo || !contractForm.value.contractType || !contractForm.value.startDate) {
    error.value = '请选择员工并填写合同编号、类型和开始日期'
    return
  }
  try {
    await http.post('/api/lifecycle/contracts', {
      empId: Number(contractForm.value.empId),
      contractNo: contractForm.value.contractNo,
      contractType: contractForm.value.contractType,
      startDate: contractForm.value.startDate,
      endDate: contractForm.value.endDate || null,
      status: contractForm.value.status || 'ACTIVE'
    })
    contractForm.value = { empId: '', contractNo: '', contractType: '', startDate: '', endDate: '', status: 'ACTIVE' }
    lifecycleInfo.value = '合同创建成功'
    await loadLifecycle()
  } catch (e) {
    error.value = e.message
  }
}

const submitLifecycleEvent = async () => {
  error.value = ''
  lifecycleInfo.value = ''
  if (!eventForm.value.empId || !eventForm.value.effectiveDate) {
    error.value = '请选择员工和生效日期'
    return
  }
  if (lifecycleTab.value === 'transfer' && (!eventForm.value.toDeptId || !eventForm.value.toPosition)) {
    error.value = '调岗需要选择目标部门并填写目标岗位'
    return
  }
  try {
    await http.post('/api/lifecycle/events', {
      empId: Number(eventForm.value.empId),
      eventType: lifecycleEventType.value,
      title: lifecycleActionText.value,
      toDeptId: eventForm.value.toDeptId ? Number(eventForm.value.toDeptId) : null,
      toPosition: eventForm.value.toPosition || null,
      effectiveDate: eventForm.value.effectiveDate,
      detail: eventForm.value.detail || null
    })
    eventForm.value = { empId: '', toDeptId: '', toPosition: '', effectiveDate: '', detail: '' }
    lifecycleInfo.value = '已提交统一审批'
    await loadLifecycle()
  } catch (e) {
    error.value = e.message
  }
}

const remove = async (id) => {
  if (!confirm('确定要删除该员工吗？此操作不可撤销。')) return
  error.value = ''
  try {
    await http.del(`/api/employees/${id}`)
    await load()
  } catch (e) {
    error.value = e.message
  }
}

const prevPage = () => { if (page.value > 1) page.value -= 1 }
const nextPage = () => { if (page.value < totalPages.value) page.value += 1 }

const departmentName = (id) => {
  if (!id) return '-'
  return departments.value.find((item) => String(item.id) === String(id))?.name || `部门 #${id}`
}

const employeeName = (id) => {
  if (!id) return '-'
  return list.value.find((item) => String(item.id) === String(id))?.name || `员工 #${id}`
}

const employeeLabel = (employee) => `${employee.name}${employee.position ? `（${employee.position}）` : ''}`

const statusLabel = (status) => {
  if (status === 'ACTIVE') return '在职'
  if (status === 'INACTIVE') return '离职'
  if (status === 'RESIGNED') return '已离职'
  if (status === 'PROBATION') return '试用'
  return status || '-'
}

const contractTypeText = (type) => ({ FIXED: '固定期限', UNFIXED: '无固定期限', INTERNSHIP: '实习协议' }[type] || type || '-')
const contractStatusText = (status) => ({ ACTIVE: '生效', EXPIRED: '到期', TERMINATED: '终止' }[status] || status || '-')
const lifecycleTypeText = (type) => ({ REGULARIZATION: '转正', TRANSFER: '调岗', RESIGNATION: '离职' }[type] || type || '-')
const lifecycleStatusText = (status) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }[status] || status || '-')

onMounted(load)
</script>
