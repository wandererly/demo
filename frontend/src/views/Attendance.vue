<template>
  <div>
    <section class="card">
      <div class="actions" style="justify-content: space-between; margin-top:0">
        <h3 style="margin:0">考勤管理</h3>
        <button class="btn-ghost" @click="loadAll">刷新</button>
      </div>
      <div class="tabs">
        <button v-for="tab in tabs" :key="tab.key" :class="{ active: activeTab === tab.key }" @click="activeTab = tab.key">{{ tab.label }}</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
      <div v-if="info" class="badge">{{ info }}</div>
    </section>

    <template v-if="activeTab === 'daily'">
      <section class="card">
        <h3>新增考勤记录</h3>
        <p style="color:var(--muted)">录入当天签到/签退信息</p>
        <div class="form-grid">
          <select class="input" v-model="form.empId">
            <option value="">请选择员工</option>
            <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
          </select>
          <input class="input" type="date" v-model="form.workDate" />
          <input class="input" type="datetime-local" v-model="form.checkIn" />
          <input class="input" type="datetime-local" v-model="form.checkOut" />
          <select class="input" v-model="form.status">
            <option value="NORMAL">正常</option>
            <option value="LATE">迟到</option>
            <option value="EARLY_LEAVE">早退</option>
            <option value="ABSENT">缺勤</option>
            <option value="LEAVE">请假</option>
          </select>
        </div>
        <div class="actions">
          <button class="btn" @click="create">新增记录</button>
        </div>
      </section>

      <section class="card">
        <h3>编辑考勤记录</h3>
        <div v-if="editForm.id" class="badge">当前记录 #{{ editForm.id }}</div>
        <div class="form-grid">
          <input class="input" type="date" v-model="editForm.workDate" />
          <input class="input" type="datetime-local" v-model="editForm.checkIn" />
          <input class="input" type="datetime-local" v-model="editForm.checkOut" />
          <select class="input" v-model="editForm.status">
            <option value="NORMAL">正常</option>
            <option value="LATE">迟到</option>
            <option value="EARLY_LEAVE">早退</option>
            <option value="ABSENT">缺勤</option>
            <option value="LEAVE">请假</option>
          </select>
        </div>
        <div class="actions"><button class="btn" @click="update">更新记录</button></div>
      </section>

      <section class="card">
        <div class="actions" style="justify-content: space-between;">
          <h3 style="margin:0">考勤列表</h3>
          <input class="input" v-model="search" placeholder="搜索员工/状态" style="max-width:240px" />
        </div>
        <table class="table">
          <thead><tr><th>序号</th><th>员工</th><th>日期</th><th>签到</th><th>签退</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="(item, index) in pagedList" :key="item.id">
              <td>{{ index + 1 }}</td>
              <td>{{ employeeName(item.empId) }}</td>
              <td>{{ item.workDate }}</td>
              <td>{{ fmtDisplay(item.checkIn) }}</td>
              <td>{{ fmtDisplay(item.checkOut) }}</td>
              <td>{{ statusText(item.status) }}</td>
              <td>
                <button class="btn-ghost" @click="pick(item)">编辑</button>
                <button class="btn-danger btn" @click="remove(item.id)">删除</button>
              </td>
            </tr>
          </tbody>
        </table>
        <Pager :total="pagedList.length" :page="page" :total-pages="totalPages" :label="pageLabel" @prev="prevPage" @next="nextPage" />
      </section>
    </template>

    <section v-if="activeTab === 'shifts'" class="card">
      <h3>班次规则</h3>
      <div class="form-grid">
        <input class="input" v-model="shiftForm.shiftName" placeholder="班次名称" />
        <input class="input" type="time" v-model="shiftForm.startTime" />
        <input class="input" type="time" v-model="shiftForm.endTime" />
        <input class="input" type="number" v-model="shiftForm.workHours" placeholder="工时" min="1" step="0.5" />
        <select class="input" v-model="shiftForm.status"><option value="ACTIVE">启用</option><option value="INACTIVE">停用</option></select>
      </div>
      <div class="actions"><button class="btn" @click="createShift">新增班次</button></div>
      <SimpleTable :rows="shifts" :columns="shiftColumns" />
    </section>

    <section v-if="activeTab === 'holidays'" class="card">
      <h3>节假日</h3>
      <div class="form-grid">
        <input class="input" type="date" v-model="holidayForm.holidayDate" />
        <input class="input" v-model="holidayForm.holidayName" placeholder="节假日名称" />
        <select class="input" v-model="holidayForm.holidayType"><option value="HOLIDAY">法定假日</option><option value="WORKDAY">调休工作日</option></select>
      </div>
      <div class="actions"><button class="btn" @click="saveHoliday">保存节假日</button></div>
      <SimpleTable :rows="holidays" :columns="holidayColumns" />
    </section>

    <section v-if="activeTab === 'corrections'" class="card">
      <h3>补卡申请</h3>
      <div class="form-grid">
        <select class="input" v-model="correctionForm.empId"><option value="">请选择员工</option><option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option></select>
        <input class="input" type="date" v-model="correctionForm.workDate" />
        <input class="input" type="datetime-local" v-model="correctionForm.checkIn" />
        <input class="input" type="datetime-local" v-model="correctionForm.checkOut" />
        <input class="input" v-model="correctionForm.reason" placeholder="补卡原因" />
      </div>
      <div class="actions"><button class="btn" @click="submitCorrection">提交补卡审批</button><router-link class="btn-ghost" to="/approvals">统一审批</router-link></div>
      <table class="table" v-if="corrections.length">
        <thead><tr><th>员工</th><th>日期</th><th>签到</th><th>签退</th><th>状态</th></tr></thead>
        <tbody><tr v-for="item in corrections" :key="item.id"><td>{{ employeeName(item.empId) }}</td><td>{{ item.workDate }}</td><td>{{ fmtDisplay(item.checkIn) }}</td><td>{{ fmtDisplay(item.checkOut) }}</td><td>{{ approvalStatus(item.status) }}</td></tr></tbody>
      </table>
    </section>

    <section v-if="activeTab === 'overtime'" class="card">
      <h3>加班申请</h3>
      <div class="form-grid">
        <select class="input" v-model="overtimeForm.empId"><option value="">请选择员工</option><option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option></select>
        <input class="input" type="date" v-model="overtimeForm.workDate" />
        <input class="input" type="number" v-model="overtimeForm.hours" placeholder="加班小时" min="0" step="1" />
        <input class="input" v-model="overtimeForm.reason" placeholder="加班原因" />
      </div>
      <div class="actions"><button class="btn" @click="submitOvertime">提交加班审批</button><router-link class="btn-ghost" to="/approvals">统一审批</router-link></div>
      <table class="table" v-if="overtimes.length">
        <thead><tr><th>员工</th><th>日期</th><th>小时</th><th>状态</th></tr></thead>
        <tbody><tr v-for="item in overtimes" :key="item.id"><td>{{ employeeName(item.empId) }}</td><td>{{ item.workDate }}</td><td>{{ item.hours }}</td><td>{{ approvalStatus(item.status) }}</td></tr></tbody>
      </table>
    </section>

    <section v-if="activeTab === 'summary'" class="card">
      <h3>月度考勤汇总</h3>
      <div class="form-grid">
        <input class="input" type="month" v-model="summaryForm.cycleMonth" />
        <select class="input" v-model="summaryForm.empId"><option value="">全员</option><option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option></select>
      </div>
      <div class="actions"><button class="btn" @click="generateSummary">生成汇总</button></div>
      <table class="table" v-if="summaries.length">
        <thead><tr><th>员工</th><th>月份</th><th>出勤</th><th>迟到</th><th>早退</th><th>缺勤</th><th>请假</th><th>加班</th></tr></thead>
        <tbody><tr v-for="item in summaries" :key="item.id"><td>{{ employeeName(item.empId) }}</td><td>{{ item.cycleMonth }}</td><td>{{ item.presentDays }}</td><td>{{ item.lateCount }}</td><td>{{ item.earlyLeaveCount }}</td><td>{{ item.absentDays }}</td><td>{{ item.leaveDays }}</td><td>{{ item.overtimeHours }}</td></tr></tbody>
      </table>
    </section>
  </div>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, ref, watch } from 'vue'
import { http } from '../api/http'

const tabs = [
  { key: 'daily', label: '日考勤' },
  { key: 'shifts', label: '班次' },
  { key: 'holidays', label: '节假日' },
  { key: 'corrections', label: '补卡' },
  { key: 'overtime', label: '加班' },
  { key: 'summary', label: '月度汇总' }
]
const activeTab = ref('daily')
const list = ref([])
const employees = ref([])
const shifts = ref([])
const holidays = ref([])
const corrections = ref([])
const overtimes = ref([])
const summaries = ref([])
const error = ref('')
const info = ref('')
const form = ref({ empId: '', workDate: '', checkIn: '', checkOut: '', status: 'NORMAL' })
const editForm = ref({ id: '', workDate: '', checkIn: '', checkOut: '', status: '' })
const shiftForm = ref({ shiftName: '', startTime: '', endTime: '', workHours: '', status: 'ACTIVE' })
const holidayForm = ref({ holidayDate: '', holidayName: '', holidayType: 'HOLIDAY' })
const correctionForm = ref({ empId: '', workDate: '', checkIn: '', checkOut: '', reason: '' })
const overtimeForm = ref({ empId: '', workDate: '', hours: '', reason: '' })
const summaryForm = ref({ cycleMonth: new Date().toISOString().slice(0, 7), empId: '' })
const search = ref('')
const page = ref(1)
const pageSize = 1

const filteredList = computed(() => {
  const key = search.value.trim().toLowerCase()
  const source = !key ? list.value : list.value.filter(item => `${employeeName(item.empId)}${item.status || ''}`.toLowerCase().includes(key))
  return [...source].sort((a, b) => {
    const dateCompare = String(b.workDate || '').localeCompare(String(a.workDate || ''))
    if (dateCompare !== 0) return dateCompare
    const checkInCompare = checkInSortValue(a).localeCompare(checkInSortValue(b))
    if (checkInCompare !== 0) return checkInCompare
    const employeeCompare = employeeName(a.empId).localeCompare(employeeName(b.empId), 'zh-CN')
    if (employeeCompare !== 0) return employeeCompare
    return Number(a.id || 0) - Number(b.id || 0)
  })
})
const groupedDates = computed(() => {
  const dates = []
  const seen = new Set()
  filteredList.value.forEach((item) => {
    if (!item.workDate || seen.has(item.workDate)) return
    seen.add(item.workDate)
    dates.push(item.workDate)
  })
  return dates
})
const totalPages = computed(() => Math.max(1, groupedDates.value.length))
const currentDate = computed(() => groupedDates.value[Math.min(page.value - 1, groupedDates.value.length - 1)] || '')
const pagedList = computed(() => currentDate.value ? filteredList.value.filter((item) => item.workDate === currentDate.value) : [])
const pageLabel = computed(() => currentDate.value ? `${currentDate.value} · 第 ${page.value} / ${totalPages.value} 天` : `第 ${page.value} / ${totalPages.value} 天`)
const shiftColumns = [{ key: 'shiftName', label: '班次' }, { key: 'startTime', label: '上班' }, { key: 'endTime', label: '下班' }, { key: 'workHours', label: '工时' }, { key: 'status', label: '状态' }]
const holidayColumns = [{ key: 'holidayDate', label: '日期' }, { key: 'holidayName', label: '名称' }, { key: 'holidayType', label: '类型' }]

const loadAll = async () => {
  error.value = ''
  const [attendanceData, employeeData, shiftData, holidayData, correctionData, overtimeData, summaryData] = await Promise.all([
    http.get('/api/attendance').catch(() => []),
    http.get('/api/employees').catch(() => []),
    http.get('/api/attendance-rules/shifts').catch(() => []),
    http.get('/api/attendance-rules/holidays').catch(() => []),
    http.get('/api/attendance-rules/corrections').catch(() => []),
    http.get('/api/attendance-rules/overtime').catch(() => []),
    http.get('/api/attendance-rules/monthly-summary').catch(() => [])
  ])
  list.value = attendanceData || []
  employees.value = employeeData || []
  shifts.value = shiftData || []
  holidays.value = holidayData || []
  corrections.value = correctionData || []
  overtimes.value = overtimeData || []
  summaries.value = summaryData || []
  page.value = 1
}

const create = async () => {
  error.value = ''; info.value = ''
  if (!form.value.empId || !form.value.workDate) { error.value = '员工和工作日期为必填项'; return }
  try {
    await http.post('/api/attendance', { empId: Number(form.value.empId), workDate: form.value.workDate, checkIn: form.value.checkIn || null, checkOut: form.value.checkOut || null, status: form.value.status || 'NORMAL' })
    form.value = { empId: '', workDate: '', checkIn: '', checkOut: '', status: 'NORMAL' }
    await loadAll()
  } catch (e) { error.value = e.message }
}
const pick = (item) => { editForm.value = { id: item.id, workDate: item.workDate || '', checkIn: fmtInput(item.checkIn), checkOut: fmtInput(item.checkOut), status: item.status || '' } }
const update = async () => {
  error.value = ''
  if (!editForm.value.id) { error.value = '请先从列表选择一条考勤记录'; return }
  try {
    await http.put(`/api/attendance/${editForm.value.id}`, { workDate: editForm.value.workDate || null, checkIn: editForm.value.checkIn || null, checkOut: editForm.value.checkOut || null, status: editForm.value.status || null })
    await loadAll()
  } catch (e) { error.value = e.message }
}
const remove = async (id) => { if (confirm('确定要删除该考勤记录吗？')) { await http.del(`/api/attendance/${id}`); await loadAll() } }
const createShift = async () => {
  await submit('/api/attendance-rules/shifts', { ...shiftForm.value, workHours: Number(shiftForm.value.workHours || 0) }, '班次已创建')
  shiftForm.value = { shiftName: '', startTime: '', endTime: '', workHours: '', status: 'ACTIVE' }
}
const saveHoliday = async () => {
  await submit('/api/attendance-rules/holidays', { ...holidayForm.value }, '节假日已保存')
  holidayForm.value = { holidayDate: '', holidayName: '', holidayType: 'HOLIDAY' }
}
const submitCorrection = async () => {
  await submit('/api/attendance-rules/corrections', { empId: Number(correctionForm.value.empId), workDate: correctionForm.value.workDate, checkIn: correctionForm.value.checkIn || null, checkOut: correctionForm.value.checkOut || null, reason: correctionForm.value.reason || null }, '补卡申请已提交审批')
  correctionForm.value = { empId: '', workDate: '', checkIn: '', checkOut: '', reason: '' }
}
const submitOvertime = async () => {
  await submit('/api/attendance-rules/overtime', { empId: Number(overtimeForm.value.empId), workDate: overtimeForm.value.workDate, hours: Number(overtimeForm.value.hours || 0), reason: overtimeForm.value.reason || null }, '加班申请已提交审批')
  overtimeForm.value = { empId: '', workDate: '', hours: '', reason: '' }
}
const generateSummary = async () => {
  await submit('/api/attendance-rules/monthly-summary/generate', { cycleMonth: summaryForm.value.cycleMonth, empId: summaryForm.value.empId ? Number(summaryForm.value.empId) : null }, '月度汇总已生成')
}
const submit = async (url, body, message) => {
  error.value = ''; info.value = ''
  try { await http.post(url, body); info.value = message; await loadAll() } catch (e) { error.value = e.message }
}

const prevPage = () => { if (page.value > 1) page.value -= 1 }
const nextPage = () => { if (page.value < totalPages.value) page.value += 1 }
const employeeName = (id) => employees.value.find((item) => String(item.id) === String(id))?.name || `员工 #${id || '-'}`
const employeeLabel = (employee) => `${employee.name}${employee.position ? `（${employee.position}）` : ''}`
const statusText = (status) => ({ NORMAL: '正常', LATE: '迟到', EARLY_LEAVE: '早退', ABSENT: '缺勤', LEAVE: '请假' }[status] || status || '-')
const approvalStatus = (status) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回' }[status] || status || '-')
const fmtDisplay = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : '-'
const fmtInput = (value) => value ? String(value).slice(0, 16) : ''
const checkInSortValue = (item) => item.checkIn ? String(item.checkIn) : '9999-12-31T23:59:59'

watch(search, () => {
  page.value = 1
})

watch(totalPages, () => {
  if (page.value > totalPages.value) {
    page.value = totalPages.value
  }
})

const Pager = defineComponent({
  props: { total: Number, page: Number, totalPages: Number, label: String },
  emits: ['prev', 'next'],
  setup(props, { emit }) {
    return () => h('div', { class: 'actions', style: 'justify-content: space-between;' }, [
      h('div', { style: 'color:var(--muted)' }, `本日 ${props.total} 条`),
      h('div', { class: 'actions' }, [
        h('button', { class: 'btn-ghost', disabled: props.page === 1, onClick: () => emit('prev') }, '上一页'),
        h('div', props.label),
        h('button', { class: 'btn-ghost', disabled: props.page === props.totalPages, onClick: () => emit('next') }, '下一页')
      ])
    ])
  }
})
const SimpleTable = defineComponent({
  props: { rows: Array, columns: Array },
  setup(props) {
    return () => props.rows?.length
      ? h('table', { class: 'table' }, [
          h('thead', h('tr', props.columns.map((col) => h('th', col.label)))),
          h('tbody', props.rows.map((row) => h('tr', { key: row.id }, props.columns.map((col) => h('td', row[col.key] ?? '-')))))
        ])
      : h('div', { class: 'empty-state compact' }, '暂无数据')
  }
})

onMounted(loadAll)
</script>
