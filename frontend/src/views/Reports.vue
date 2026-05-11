<template>
  <div>
    <section class="grid grid-3">
      <div class="card metric-card">
        <div class="badge">组织</div>
        <h2>{{ report.departments }}</h2>
        <div style="color:var(--muted)">部门 / {{ report.positions }} 岗位</div>
      </div>
      <div class="card metric-card">
        <div class="badge" style="background:rgba(242,183,5,0.18);color:#a86d00">人事</div>
        <h2>{{ report.employees }}</h2>
        <div style="color:var(--muted)">在册员工</div>
      </div>
      <div class="card metric-card">
        <div class="badge" style="background:rgba(11,53,89,0.12);color:#0b3559">薪酬</div>
        <h2>{{ report.payrolls }}</h2>
        <div style="color:var(--muted)">薪酬记录 / {{ money(report.netSalaryTotal) }} 实发合计</div>
      </div>
    </section>

    <section class="grid grid-2 report-grid">
      <div class="card">
        <h3>考勤异常趋势</h3>
        <div v-if="attendanceTrend.length" class="trend-list">
          <div v-for="item in attendanceTrend" :key="item.name" class="trend-item">
            <span>{{ item.name }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
        <div v-else class="empty-state compact">暂无异常趋势数据</div>
      </div>

      <div class="card">
        <h3>异常员工排行</h3>
        <div v-if="exceptionRanking.length" class="bar-list">
          <div v-for="item in exceptionRanking" :key="item.name" class="bar-row">
            <div class="bar-head">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track"><span :style="{ width: item.percent + '%' }"></span></div>
          </div>
        </div>
        <div v-else class="empty-state compact">暂无异常排行数据</div>
      </div>

      <div class="card">
        <h3>部门人数分布</h3>
        <div v-if="departmentBars.length" class="bar-list">
          <div v-for="item in departmentBars" :key="item.name" class="bar-row">
            <div class="bar-head">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track"><span :style="{ width: item.percent + '%' }"></span></div>
          </div>
        </div>
        <div v-else class="empty-state compact">暂无部门员工数据</div>
      </div>

      <div class="card">
        <h3>考勤状态分布</h3>
        <div v-if="attendanceBars.length" class="bar-list">
          <div v-for="item in attendanceBars" :key="item.name" class="bar-row">
            <div class="bar-head">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track"><span :style="{ width: item.percent + '%' }"></span></div>
          </div>
        </div>
        <div v-else class="empty-state compact">暂无考勤数据</div>
      </div>

      <div class="card">
        <h3>请假状态分布</h3>
        <div v-if="leaveBars.length" class="bar-list">
          <div v-for="item in leaveBars" :key="item.name" class="bar-row">
            <div class="bar-head">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track"><span :style="{ width: item.percent + '%' }"></span></div>
          </div>
        </div>
        <div v-else class="empty-state compact">暂无请假数据</div>
      </div>

      <div class="card">
        <h3>请假趋势</h3>
        <div v-if="leaveTrend.length" class="trend-list">
          <div v-for="item in leaveTrend" :key="item.name" class="trend-item">
            <span>{{ item.name }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </div>
        <div v-else class="empty-state compact">暂无请假趋势数据</div>
      </div>

      <div class="card">
        <h3>绩效等级分布</h3>
        <div v-if="performanceBars.length" class="bar-list">
          <div v-for="item in performanceBars" :key="item.name" class="bar-row">
            <div class="bar-head">
              <span>{{ item.name }}</span>
              <strong>{{ item.value }}</strong>
            </div>
            <div class="bar-track"><span :style="{ width: item.percent + '%' }"></span></div>
          </div>
        </div>
        <div v-else class="empty-state compact">暂无绩效评审数据</div>
      </div>
    </section>

    <section class="card">
      <h3>工资条月度概览</h3>
      <div v-if="payrollMonthBars.length" class="bar-list">
        <div v-for="item in payrollMonthBars" :key="item.name" class="bar-row">
          <div class="bar-head">
            <span>{{ item.name }}</span>
            <strong>{{ item.value }} 条 / {{ money(item.amount) }}</strong>
          </div>
          <div class="bar-track"><span :style="{ width: item.percent + '%' }"></span></div>
        </div>
      </div>
      <div v-else class="empty-state compact">暂无薪酬记录</div>
    </section>

    <section class="card">
      <h3>流程报表</h3>
      <table class="table">
        <thead>
          <tr>
            <th>项目</th>
            <th>数量</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in rows" :key="row.name">
            <td>{{ row.name }}</td>
            <td>{{ row.value }}</td>
            <td>{{ row.status }}</td>
          </tr>
        </tbody>
      </table>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'

const report = ref({
  departments: 0,
  positions: 0,
  employees: 0,
  attendance: 0,
  leaves: 0,
  cycles: 0,
  payrolls: 0,
  pending: 0,
  netSalaryTotal: 0
})
const raw = ref({
  departments: [],
  positions: [],
  employees: [],
  attendance: [],
  leaves: [],
  payrolls: [],
  payslips: [],
  reviews: []
})
const error = ref('')

const rows = computed(() => [
  { name: '部门', value: report.value.departments, status: report.value.departments > 0 ? '已建立' : '待建立' },
  { name: '岗位', value: report.value.positions, status: report.value.positions > 0 ? '已建立' : '待建立' },
  { name: '员工', value: report.value.employees, status: report.value.employees > 0 ? '已录入' : '待录入' },
  { name: '考勤记录', value: report.value.attendance, status: report.value.attendance > 0 ? '已维护' : '待维护' },
  { name: '请假记录', value: report.value.leaves, status: report.value.leaves > 0 ? '已维护' : '待维护' },
  { name: '绩效周期', value: report.value.cycles, status: report.value.cycles > 0 ? '已建立' : '待建立' },
  { name: '绩效评审', value: raw.value.reviews.length, status: raw.value.reviews.length > 0 ? '已评审' : '待评审' },
  { name: '薪酬记录', value: report.value.payrolls, status: report.value.payrolls > 0 ? '已生成' : '待生成' },
  { name: '待审批', value: report.value.pending, status: report.value.pending > 0 ? '处理中' : '无待办' }
])

const departmentBars = computed(() => {
  const counts = new Map()
  for (const dept of raw.value.departments) counts.set(String(dept.id), { name: dept.name, value: 0 })
  for (const emp of raw.value.employees) {
    const key = String(emp.deptId || '')
    const current = counts.get(key) || { name: '未分配部门', value: 0 }
    current.value += 1
    counts.set(key, current)
  }
  return toBars([...counts.values()].filter((item) => item.value > 0), 'value')
})

const attendanceBars = computed(() => toBars(countBy(raw.value.attendance, (item) => attendanceStatus(item.status)), 'value'))

const leaveBars = computed(() => toBars(countBy(raw.value.leaves, (item) => statusLabel(item.status)), 'value'))

const performanceBars = computed(() => toBars(countBy(raw.value.reviews, (item) => item.level || '未定级'), 'value'))

const attendanceTrend = computed(() => trendByDate(raw.value.attendance.filter((item) => item.status && item.status !== 'NORMAL'), 'workDate', 7))

const leaveTrend = computed(() => trendByDate(raw.value.leaves, 'startTime', 6))

const exceptionRanking = computed(() => {
  const rows = countBy(raw.value.attendance.filter((item) => item.status && item.status !== 'NORMAL'), (item) => employeeName(item.empId))
  return toBars(rows.sort((a, b) => b.value - a.value).slice(0, 6), 'value')
})

const payrollMonthBars = computed(() => {
  const monthMap = new Map()
  for (const item of raw.value.payslips.length ? raw.value.payslips : raw.value.payrolls) {
    const key = item.cycleMonth || '未设置月份'
    const current = monthMap.get(key) || { name: key, value: 0, amount: 0 }
    current.value += 1
    current.amount += Number(item.netSalary || 0)
    monthMap.set(key, current)
  }
  return toBars([...monthMap.values()].sort((a, b) => String(b.name).localeCompare(String(a.name))).slice(0, 6), 'amount')
})

onMounted(async () => {
  error.value = ''
  try {
    const [departments, positions, employees, attendance, leaves, cycles, payrolls, payslips, approvals] = await Promise.all([
      http.get('/api/departments').catch(() => []),
      http.get('/api/org/positions').catch(() => []),
      http.get('/api/employees').catch(() => []),
      http.get('/api/attendance').catch(() => []),
      http.get('/api/leave').catch(() => []),
      http.get('/api/performance/cycles').catch(() => []),
      http.get('/api/payroll/records').catch(() => []),
      http.get('/api/payroll/payslips').catch(() => []),
      http.get('/api/approvals').catch(() => [])
    ])
    const reviews = []
    for (const cycle of cycles || []) {
      const cycleReviews = await http.get(`/api/performance/cycles/${cycle.id}/reviews`).catch(() => [])
      reviews.push(...(cycleReviews || []))
    }
    raw.value = { departments, positions, employees, attendance, leaves, payrolls, payslips, reviews }
    const payrollSource = payslips.length ? payslips : payrolls
    report.value = {
      departments: departments.length,
      positions: positions.length,
      employees: employees.length,
      attendance: attendance.length,
      leaves: leaves.length,
      cycles: cycles.length,
      payrolls: payrollSource.length,
      pending: approvals.length,
      netSalaryTotal: payrollSource.reduce((sum, item) => sum + Number(item.netSalary || 0), 0)
    }
  } catch (e) {
    error.value = e.message
  }
})

const countBy = (items, picker) => {
  const map = new Map()
  for (const item of items || []) {
    const name = picker(item)
    map.set(name, (map.get(name) || 0) + 1)
  }
  return [...map.entries()].map(([name, value]) => ({ name, value }))
}

const toBars = (items, field) => {
  const max = Math.max(1, ...items.map((item) => Number(item[field] || 0)))
  return items.map((item) => ({
    ...item,
    percent: Math.max(4, Math.round((Number(item[field] || 0) / max) * 100))
  }))
}

const trendByDate = (items, field, limit) => {
  const map = new Map()
  for (const item of items || []) {
    const rawDate = item[field]
    if (!rawDate) continue
    const key = String(rawDate).slice(0, 10)
    map.set(key, (map.get(key) || 0) + 1)
  }
  return [...map.entries()]
    .sort(([a], [b]) => String(b).localeCompare(String(a)))
    .slice(0, limit)
    .reverse()
    .map(([name, value]) => ({ name, value }))
}

const employeeName = (id) => raw.value.employees.find((item) => String(item.id) === String(id))?.name || `员工 #${id || '-'}`

const statusLabel = (status) => ({
  PENDING: '待处理',
  PENDING_L1: '一级待审',
  PENDING_L2: '二级待审',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}[status] || status || '未设置')

const attendanceStatus = (status) => ({
  NORMAL: '正常',
  LATE: '迟到',
  EARLY_LEAVE: '早退',
  ABSENT: '缺勤',
  LEAVE: '请假'
}[status] || status || '未设置')

const money = (value) => `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
</script>
