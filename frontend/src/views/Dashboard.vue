<template>
  <div>
    <section class="card">
      <div class="actions" style="justify-content: space-between; margin-top:0">
        <div>
          <h3 style="margin:0">HR 工作台</h3>
          <p style="color:var(--muted);margin:6px 0 0">优先处理今日异常、审批待办、合同到期和薪酬生成状态</p>
        </div>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <div class="flow-grid" style="margin-top:16px">
        <router-link v-for="step in flow" :key="step.to" :to="step.to" class="flow-step">
          <strong>{{ step.no }}. {{ step.title }}</strong>
          <span>{{ step.meta }}</span>
        </router-link>
      </div>
    </section>

    <section class="grid grid-4 workbench-grid">
      <router-link class="card action-card" to="/attendance">
        <div class="badge danger-soft">今日异常</div>
        <h2>{{ todayExceptions.length }}</h2>
        <div style="color:var(--muted)">迟到 / 早退 / 缺勤 / 请假</div>
      </router-link>
      <router-link class="card action-card" to="/approvals">
        <div class="badge" style="background:rgba(242,183,5,0.18);color:#a86d00">待审批</div>
        <h2>{{ approvals.length }}</h2>
        <div style="color:var(--muted)">统一审批中心待处理</div>
      </router-link>
      <router-link class="card action-card" to="/employees">
        <div class="badge">合同/转正</div>
        <h2>{{ dueItems.length }}</h2>
        <div style="color:var(--muted)">30 天内到期或待生效</div>
      </router-link>
      <router-link class="card action-card" to="/payroll">
        <div class="badge" style="background:rgba(11,53,89,0.12);color:#0b3559">本月工资条</div>
        <h2>{{ payrollStatus.total }}</h2>
        <div style="color:var(--muted)">待审 {{ payrollStatus.pending }} / 已审 {{ payrollStatus.approved }}</div>
      </router-link>
    </section>

    <section class="grid grid-2 report-grid">
      <div class="card">
        <h3>今日考勤异常</h3>
        <table v-if="todayExceptions.length" class="table">
          <thead><tr><th>员工</th><th>日期</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="item in todayExceptions.slice(0, 6)" :key="item.id">
              <td>{{ employeeName(item.empId) }}</td>
              <td>{{ item.workDate }}</td>
              <td>{{ attendanceStatus(item.status) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state compact">今日暂无考勤异常</div>
      </div>

      <div class="card">
        <h3>审批待办</h3>
        <table v-if="approvals.length" class="table">
          <thead><tr><th>类型</th><th>摘要</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="item in approvals.slice(0, 6)" :key="`${item.type}-${item.businessId}`">
              <td>{{ approvalType(item.type) }}</td>
              <td>{{ item.summary || `业务 #${item.businessId}` }}</td>
              <td>待审批</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state compact">暂无审批待办</div>
      </div>

      <div class="card">
        <h3>合同/转正提醒</h3>
        <table v-if="dueItems.length" class="table">
          <thead><tr><th>员工</th><th>事项</th><th>日期</th></tr></thead>
          <tbody>
            <tr v-for="item in dueItems.slice(0, 6)" :key="item.key">
              <td>{{ item.employee }}</td>
              <td>{{ item.title }}</td>
              <td>{{ item.date }}</td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state compact">30 天内暂无合同或转正提醒</div>
      </div>

      <div class="card">
        <h3>薪酬状态</h3>
        <div class="bar-list">
          <div v-for="item in payrollBars" :key="item.name" class="bar-row">
            <div class="bar-head"><span>{{ item.name }}</span><strong>{{ item.value }}</strong></div>
            <div class="bar-track"><span :style="{ width: item.percent + '%' }"></span></div>
          </div>
        </div>
      </div>
    </section>

    <section class="card">
      <h3 style="margin-top:0">最新动态</h3>
      <table class="table" v-if="activities.length">
        <thead><tr><th>事项</th><th>类型</th><th>操作人</th><th>状态</th></tr></thead>
        <tbody>
          <tr v-for="(act, i) in activities" :key="i">
            <td>{{ act.title }}</td>
            <td>{{ act.type }}</td>
            <td>{{ act.actor }}</td>
            <td>{{ act.status }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state">{{ loading ? '正在读取业务动态...' : '暂无动态' }}</div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'
import { getUser } from '../auth'

const employees = ref([])
const attendance = ref([])
const approvals = ref([])
const contracts = ref([])
const lifecycleEvents = ref([])
const payslips = ref([])
const activities = ref([])
const loading = ref(false)
const error = ref('')

const flow = [
  { no: 1, title: '建部门', meta: '部门体系', to: '/departments' },
  { no: 2, title: '建岗位', meta: '岗位职级', to: '/org' },
  { no: 3, title: '录员工', meta: '员工档案', to: '/employees' },
  { no: 4, title: '考勤请假', meta: '日常维护', to: '/attendance' },
  { no: 5, title: '绩效', meta: '周期评审', to: '/performance' },
  { no: 6, title: '薪酬', meta: '工资条生成', to: '/payroll' },
  { no: 7, title: '报表', meta: '汇总分析', to: '/reports' }
]

const today = new Date().toISOString().slice(0, 10)
const currentMonth = new Date().toISOString().slice(0, 7)
const currentUser = getUser() || {}

const todayExceptions = computed(() =>
  attendance.value.filter((item) => item.workDate === today && item.status && item.status !== 'NORMAL')
)

const dueItems = computed(() => {
  const now = new Date()
  const limit = new Date(now)
  limit.setDate(limit.getDate() + 30)
  const rows = []
  for (const contract of contracts.value) {
    if (!contract.endDate) continue
    const end = new Date(contract.endDate)
    if (end >= now && end <= limit) {
      rows.push({ key: `contract-${contract.id}`, employee: employeeName(contract.empId), title: '合同到期', date: contract.endDate })
    }
  }
  for (const event of lifecycleEvents.value) {
    if (event.eventType !== 'REGULARIZATION' || event.status === 'APPROVED') continue
    rows.push({ key: `event-${event.id}`, employee: employeeName(event.empId), title: '转正待处理', date: event.effectiveDate || '-' })
  }
  return rows.sort((a, b) => String(a.date).localeCompare(String(b.date)))
})

const payrollStatus = computed(() => {
  const monthRows = payslips.value.filter((item) => item.cycleMonth === currentMonth)
  return {
    total: monthRows.length,
    pending: monthRows.filter((item) => item.status === 'PENDING').length,
    approved: monthRows.filter((item) => item.status === 'APPROVED').length,
    rejected: monthRows.filter((item) => item.status === 'REJECTED').length
  }
})

const payrollBars = computed(() => {
  const rows = [
    { name: '待审批', value: payrollStatus.value.pending },
    { name: '已通过', value: payrollStatus.value.approved },
    { name: '已驳回', value: payrollStatus.value.rejected }
  ]
  const max = Math.max(1, ...rows.map((item) => item.value))
  return rows.map((item) => ({ ...item, percent: Math.max(4, Math.round((item.value / max) * 100)) }))
})

const load = async () => {
  loading.value = true
  error.value = ''
  try {
    const [empData, attendanceData, approvalData, contractData, eventData, payslipData] = await Promise.all([
      http.get('/api/employees').catch(() => []),
      http.get('/api/attendance').catch(() => []),
      http.get('/api/approvals').catch(() => []),
      http.get('/api/lifecycle/contracts').catch(() => []),
      http.get('/api/lifecycle/events').catch(() => []),
      http.get('/api/payroll/payslips').catch(() => [])
    ])
    employees.value = empData || []
    attendance.value = attendanceData || []
    approvals.value = approvalData || []
    contracts.value = contractData || []
    lifecycleEvents.value = eventData || []
    payslips.value = payslipData || []
    buildActivities()
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

const buildActivities = () => {
  const rows = []
  for (const item of approvals.value.slice(0, 6)) {
    rows.push({ title: item.summary || `待审批业务 #${item.businessId}`, type: approvalType(item.type), actor: currentUser.username || '系统', status: '待审批', time: timeValue(item.createdAt) })
  }
  for (const item of lifecycleEvents.value.slice(0, 6)) {
    rows.push({ title: `${employeeName(item.empId)} ${lifecycleType(item.eventType)}`, type: '生命周期', actor: 'HR', status: statusLabel(item.status), time: timeValue(item.createdAt) })
  }
  for (const item of payslips.value.slice(0, 6)) {
    rows.push({ title: `${employeeName(item.empId)} ${item.cycleMonth} 工资条`, type: '薪酬', actor: '薪酬', status: statusLabel(item.status), time: timeValue(item.createdAt) })
  }
  activities.value = rows.sort((a, b) => b.time - a.time).slice(0, 8)
}

const employeeName = (id) => employees.value.find((item) => String(item.id) === String(id))?.name || `员工 #${id || '-'}`
const timeValue = (value) => value ? new Date(value).getTime() || 0 : 0
const attendanceStatus = (status) => ({ LATE: '迟到', EARLY_LEAVE: '早退', ABSENT: '缺勤', LEAVE: '请假' }[status] || status || '-')
const approvalType = (type) => ({
  LEAVE: '请假审批',
  PERFORMANCE: '绩效审批',
  REGULARIZATION: '转正审批',
  TRANSFER: '调岗审批',
  RESIGNATION: '离职审批',
  ATTENDANCE_CORRECTION: '补卡审批',
  OVERTIME: '加班审批',
  PAYROLL: '薪酬审批',
  SALARY_CHANGE: '调薪审批'
}[type] || type || '审批')
const lifecycleType = (type) => ({ REGULARIZATION: '转正', TRANSFER: '调岗', RESIGNATION: '离职' }[type] || type || '事件')
const statusLabel = (status) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', ACTIVE: '启用' }[status] || status || '-')

onMounted(load)
</script>
