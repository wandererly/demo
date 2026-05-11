<template>
  <div>
    <section class="card">
      <div class="actions" style="justify-content: space-between; margin-top:0">
        <div>
          <h3 style="margin:0">薪酬管理</h3>
          <p style="color:var(--muted);margin:6px 0 0">以工资条为主线，生成后进入统一审批，审批通过后锁定</p>
        </div>
        <button class="btn-ghost" @click="loadAll">刷新</button>
      </div>
      <div class="tabs">
        <button :class="{ active: activeTab === 'payslip' }" @click="activeTab = 'payslip'">工资条</button>
        <button :class="{ active: activeTab === 'structure' }" @click="activeTab = 'structure'">薪资结构</button>
        <button :class="{ active: activeTab === 'change' }" @click="activeTab = 'change'">调薪</button>
        <button :class="{ active: activeTab === 'calc' }" @click="activeTab = 'calc'">试算/历史</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
      <div v-if="info" class="badge">{{ info }}</div>
    </section>

    <section v-if="activeTab === 'payslip'" class="card">
      <h3>生成工资条</h3>
      <div class="form-grid">
        <input class="input" type="month" v-model="payslipGenerate.cycleMonth" />
        <select class="input" v-model="payslipGenerate.empId">
          <option value="">全员生成</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
      </div>
      <div class="actions">
        <button class="btn" @click="generatePayslips">生成工资条</button>
        <router-link class="btn-ghost" to="/approvals">统一审批</router-link>
      </div>
    </section>

    <section v-if="activeTab === 'payslip'" class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">工资条列表</h3>
        <input class="input" v-model="payslipSearch" placeholder="搜索员工/月份/状态" style="max-width:260px" />
      </div>
      <table class="table" v-if="filteredPayslips.length">
        <thead><tr><th>员工</th><th>月份</th><th>基本</th><th>奖金</th><th>加班</th><th>扣款</th><th>税前</th><th>实发</th><th>绩效</th><th>状态</th></tr></thead>
        <tbody>
          <tr v-for="item in filteredPayslips" :key="item.id">
            <td>{{ employeeName(item.empId) }}</td>
            <td>{{ item.cycleMonth }}</td>
            <td>{{ item.baseSalary }}</td>
            <td>{{ item.bonus }}</td>
            <td>{{ item.overtimePay }}</td>
            <td>{{ item.leaveDeduction }}</td>
            <td>{{ item.grossSalary }}</td>
            <td>{{ item.netSalary }}</td>
            <td>{{ item.performanceLevel || '-' }}</td>
            <td>{{ payslipStatus(item.status) }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else class="empty-state compact">暂无工资条</div>
    </section>

    <section v-if="activeTab === 'structure'" class="card">
      <h3>薪资结构</h3>
      <p style="color:var(--muted)">按员工设置薪资构成</p>
      <div class="form-grid">
        <select class="input" v-model="salary.empId">
          <option value="">请选择员工</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
        <input class="input" type="number" v-model="salary.baseSalary" placeholder="基本工资(不低于3500)" min="3500" step="100" />
        <input class="input" type="number" v-model="salary.allowance" placeholder="津贴" min="0" step="50" />
        <input class="input" type="number" v-model="salary.bonus" placeholder="奖金" min="0" step="50" />
        <input class="input" type="number" v-model="salary.socialSecurity" placeholder="社保" min="0" step="50" />
        <input class="input" type="number" v-model="salary.housingFund" placeholder="公积金" min="0" step="50" />
        <input class="input" type="number" v-model="salary.taxRate" placeholder="税率% (10表示10%)" min="0" max="100" step="1" />
      </div>
      <div class="actions">
        <button class="btn" @click="saveSalary">保存薪资结构</button>
        <button class="btn-ghost" @click="loadSalary">加载</button>
      </div>
    </section>

    <section v-if="activeTab === 'change'" class="card">
      <h3>调薪申请</h3>
      <div class="form-grid">
        <select class="input" v-model="salaryChange.empId">
          <option value="">请选择员工</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
        <input class="input" type="number" v-model="salaryChange.afterSalary" placeholder="调整后基本工资" min="3500" step="100" />
        <input class="input" type="month" v-model="salaryChange.effectiveMonth" />
        <input class="input" v-model="salaryChange.reason" placeholder="调薪原因" />
      </div>
      <div class="actions"><button class="btn" @click="submitSalaryChange">提交调薪审批</button><router-link class="btn-ghost" to="/approvals">统一审批</router-link></div>
      <table class="table" v-if="salaryChanges.length">
        <thead><tr><th>员工</th><th>调整前</th><th>调整后</th><th>生效月份</th><th>状态</th></tr></thead>
        <tbody><tr v-for="item in salaryChanges" :key="item.id"><td>{{ employeeName(item.empId) }}</td><td>{{ item.beforeSalary }}</td><td>{{ item.afterSalary }}</td><td>{{ item.effectiveMonth }}</td><td>{{ payslipStatus(item.status) }}</td></tr></tbody>
      </table>
    </section>

    <template v-if="activeTab === 'calc'">
      <section class="card">
        <h3>薪资试算</h3>
        <div class="form-grid">
          <input class="input" type="number" v-model="calc.baseSalary" placeholder="基本工资(不低于3500)" min="3500" step="100" />
          <input class="input" type="number" v-model="calc.allowance" placeholder="津贴" min="0" step="50" />
          <input class="input" type="number" v-model="calc.bonus" placeholder="奖金" min="0" step="50" />
          <input class="input" type="number" v-model="calc.overtimeHours" placeholder="加班小时" min="0" step="1" />
          <input class="input" type="number" v-model="calc.overtimeRate" placeholder="加班费率" min="0" step="10" />
          <input class="input" type="number" v-model="calc.leaveDays" placeholder="请假天数" min="0" step="1" />
          <input class="input" type="number" v-model="calc.leaveDeductionPerDay" placeholder="日扣款" min="0" step="50" />
          <input class="input" type="number" v-model="calc.socialSecurity" placeholder="社保" min="0" step="50" />
          <input class="input" type="number" v-model="calc.housingFund" placeholder="公积金" min="0" step="50" />
          <input class="input" type="number" v-model="calc.taxRate" placeholder="税率%(可选)" min="0" max="100" step="1" />
        </div>
        <div class="actions"><button class="btn" @click="doCalc">计算薪资</button></div>
        <div v-if="calcResult" class="card" style="background:var(--bg);margin-top:12px">
          <p>实发薪资: <strong>{{ calcResult.netSalary }}</strong></p>
          <p style="font-size:13px;color:var(--muted)">税前: {{ calcResult.grossSalary }} | 个税: {{ calcResult.tax }} | 加班: {{ calcResult.overtimePay }} | 扣款: {{ calcResult.leaveDeduction }}</p>
        </div>
      </section>

      <section class="card">
        <h3>旧薪酬记录</h3>
        <div class="actions">
          <select class="input" v-model="queryEmpId" style="max-width:220px"><option value="">请选择员工</option><option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option></select>
          <button class="btn" @click="loadRecords">查询</button>
        </div>
        <table class="table" v-if="records.length">
          <thead><tr><th>员工</th><th>月份</th><th>税前</th><th>实发</th><th>个税</th><th>状态</th></tr></thead>
          <tbody><tr v-for="item in records" :key="item.id"><td>{{ employeeName(item.empId) }}</td><td>{{ item.cycleMonth }}</td><td>{{ item.grossSalary }}</td><td>{{ item.netSalary }}</td><td>{{ item.tax }}</td><td>{{ item.status }}</td></tr></tbody>
        </table>
      </section>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'

const activeTab = ref('payslip')
const error = ref('')
const info = ref('')
const employees = ref([])
const payslips = ref([])
const salaryChanges = ref([])
const salary = ref({ empId: '', baseSalary: '', allowance: '', bonus: '', socialSecurity: '', housingFund: '', taxRate: '' })
const salaryChange = ref({ empId: '', afterSalary: '', effectiveMonth: new Date().toISOString().slice(0, 7), reason: '' })
const payslipGenerate = ref({ empId: '', cycleMonth: new Date().toISOString().slice(0, 7) })
const payslipSearch = ref('')
const calc = ref({ baseSalary: '', allowance: '', bonus: '', overtimeHours: '', overtimeRate: '', leaveDays: '', leaveDeductionPerDay: '', socialSecurity: '', housingFund: '', taxRate: '' })
const calcResult = ref(null)
const queryEmpId = ref('')
const records = ref([])

const filteredPayslips = computed(() => {
  const key = payslipSearch.value.trim().toLowerCase()
  if (!key) return payslips.value
  return payslips.value.filter((item) => `${employeeName(item.empId)}${item.cycleMonth || ''}${item.status || ''}`.toLowerCase().includes(key))
})
const n = (v) => v === '' || v === null ? null : parseFloat(v)
const percentToRate = (v) => { const value = n(v); return value === null ? null : value / 100 }
const rateToPercent = (v) => { const value = n(v); return value === null ? '' : value * 100 }

const loadAll = async () => {
  const [employeeData, payslipData, changeData] = await Promise.all([
    http.get('/api/employees').catch(() => []),
    http.get('/api/payroll/payslips').catch(() => []),
    http.get('/api/payroll/salary-changes').catch(() => [])
  ])
  employees.value = employeeData || []
  payslips.value = payslipData || []
  salaryChanges.value = changeData || []
}
const generatePayslips = async () => {
  error.value = ''; info.value = ''
  const approvedExists = payslips.value.some((item) => item.cycleMonth === payslipGenerate.value.cycleMonth && (!payslipGenerate.value.empId || String(item.empId) === String(payslipGenerate.value.empId)) && item.status === 'APPROVED')
  if (approvedExists) { error.value = '已审批工资条不可覆盖，请走调薪流程'; return }
  try {
    await http.post('/api/payroll/payslips/generate', { cycleMonth: payslipGenerate.value.cycleMonth, empId: payslipGenerate.value.empId ? Number(payslipGenerate.value.empId) : null })
    info.value = '工资条已生成并进入审批'
    await loadAll()
  } catch (e) { error.value = e.message }
}
const submitSalaryChange = async () => {
  error.value = ''; info.value = ''
  if (!salaryChange.value.empId || !salaryChange.value.afterSalary || !salaryChange.value.effectiveMonth) { error.value = '请选择员工、调整后薪资和生效月份'; return }
  try {
    await http.post('/api/payroll/salary-changes', { empId: Number(salaryChange.value.empId), afterSalary: n(salaryChange.value.afterSalary), effectiveMonth: salaryChange.value.effectiveMonth, reason: salaryChange.value.reason || null })
    salaryChange.value = { empId: '', afterSalary: '', effectiveMonth: new Date().toISOString().slice(0, 7), reason: '' }
    info.value = '调薪申请已提交审批'
    await loadAll()
  } catch (e) { error.value = e.message }
}
const loadSalary = async () => {
  error.value = ''
  if (!salary.value.empId) return
  try {
    const data = await http.get(`/api/salary/structure/${salary.value.empId}`)
    if (data) salary.value = { empId: data.empId || '', baseSalary: data.baseSalary, allowance: data.allowance, bonus: data.bonus, socialSecurity: data.socialSecurity, housingFund: data.housingFund, taxRate: rateToPercent(data.taxRate) }
  } catch (e) { error.value = e.message }
}
const saveSalary = async () => {
  error.value = ''; info.value = ''
  if (!salary.value.empId) { error.value = '请选择员工'; return }
  try {
    await http.post('/api/salary/structure', { empId: Number(salary.value.empId), baseSalary: n(salary.value.baseSalary), allowance: n(salary.value.allowance), bonus: n(salary.value.bonus), socialSecurity: n(salary.value.socialSecurity), housingFund: n(salary.value.housingFund), taxRate: percentToRate(salary.value.taxRate) })
    info.value = '薪资结构已保存'
  } catch (e) { error.value = e.message }
}
const doCalc = async () => {
  error.value = ''; calcResult.value = null
  try {
    calcResult.value = await http.post('/api/payroll/calc', { baseSalary: n(calc.value.baseSalary), allowance: n(calc.value.allowance), bonus: n(calc.value.bonus), overtimeHours: n(calc.value.overtimeHours), overtimeRate: n(calc.value.overtimeRate), leaveDays: n(calc.value.leaveDays), leaveDeductionPerDay: n(calc.value.leaveDeductionPerDay), socialSecurity: n(calc.value.socialSecurity), housingFund: n(calc.value.housingFund), taxRate: percentToRate(calc.value.taxRate) })
  } catch (e) { error.value = e.message }
}
const loadRecords = async () => {
  error.value = ''
  if (!queryEmpId.value) return
  try { records.value = await http.get(`/api/payroll/employee/${queryEmpId.value}`) } catch (e) { error.value = e.message }
}
const employeeName = (id) => employees.value.find((item) => String(item.id) === String(id))?.name || `员工 #${id || '-'}`
const employeeLabel = (employee) => `${employee.name}${employee.position ? `（${employee.position}）` : ''}`
const payslipStatus = (status) => ({ PENDING: '待审批', APPROVED: '已通过', REJECTED: '已驳回', PAID: '已发放' }[status] || status || '-')

onMounted(loadAll)
</script>
