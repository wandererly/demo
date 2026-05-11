<template>
  <div>
    <section class="card">
      <h3>薪资结构</h3>
      <div class="form-grid">
        <input class="input" v-model="salaryForm.empId" placeholder="员工ID" />
        <input class="input" v-model="salaryForm.baseSalary" placeholder="基础薪资" />
        <input class="input" v-model="salaryForm.allowance" placeholder="补贴" />
        <input class="input" v-model="salaryForm.bonus" placeholder="奖金" />
        <input class="input" v-model="salaryForm.socialSecurity" placeholder="社保" />
        <input class="input" v-model="salaryForm.housingFund" placeholder="公积金" />
        <input class="input" v-model="salaryForm.taxRate" placeholder="税率 0.1(可空)" />
      </div>
      <div class="actions">
        <button class="btn" @click="saveSalary">保存结构</button>
        <button class="btn-ghost" @click="loadSalary">加载结构</button>
      </div>
      <div v-if="salaryInfo" class="badge">当前配置: {{ salaryInfo }}</div>
    </section>

    <section class="card">
      <h3>薪资试算</h3>
      <div class="form-grid">
        <input class="input" v-model="calcForm.baseSalary" placeholder="基础薪资" />
        <input class="input" v-model="calcForm.allowance" placeholder="补贴" />
        <input class="input" v-model="calcForm.bonus" placeholder="奖金" />
        <input class="input" v-model="calcForm.overtimeHours" placeholder="加班小时" />
        <input class="input" v-model="calcForm.overtimeRate" placeholder="加班倍率" />
        <input class="input" v-model="calcForm.leaveDays" placeholder="请假天数" />
        <input class="input" v-model="calcForm.leaveDeductionPerDay" placeholder="请假扣款/天" />
        <input class="input" v-model="calcForm.socialSecurity" placeholder="社保" />
        <input class="input" v-model="calcForm.housingFund" placeholder="公积金" />
        <input class="input" v-model="calcForm.taxRate" placeholder="税率(可空)" />
      </div>
      <div class="actions">
        <button class="btn" @click="calc">计算</button>
      </div>
      <div v-if="calcResult" class="badge">实发: {{ calcResult.netSalary }}</div>
    </section>

    <section class="card">
      <h3>生成薪资</h3>
      <div class="form-grid">
        <input class="input" v-model="genForm.empId" placeholder="员工ID" />
        <input class="input" v-model="genForm.cycleMonth" placeholder="周期 YYYYY-MM" />
        <input class="input" v-model="genForm.overtimeHours" placeholder="加班小时" />
        <input class="input" v-model="genForm.overtimeRate" placeholder="加班倍率" />
        <input class="input" v-model="genForm.leaveDays" placeholder="请假天数" />
        <input class="input" v-model="genForm.leaveDeductionPerDay" placeholder="请假扣款/天" />
      </div>
      <div class="actions">
        <button class="btn" @click="generate">生成薪资</button>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">薪资记录</h3>
        <input class="input" v-model="search" placeholder="搜索周期" style="max-width:240px" />
      </div>
      <div class="form-grid">
        <input class="input" v-model="queryEmpId" placeholder="员工ID" />
      </div>
      <div class="actions">
        <button class="btn-ghost" @click="loadRecords">加载记录</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>周期</th>
            <th>税前</th>
            <th>实发</th>
            <th>税额</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedRecords" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.cycleMonth }}</td>
            <td>{{ item.grossSalary }}</td>
            <td>{{ item.netSalary }}</td>
            <td>{{ item.tax }}</td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ filteredRecords.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevPage" :disabled="page === 1">上一页</button>
          <div>第 {{ page }} / {{ totalPages }} 页</div>
          <button class="btn-ghost" @click="nextPage" :disabled="page === totalPages">下一页</button>
        </div>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { http } from '../api/http'

const error = ref('')

const salaryForm = ref({
  empId: '',
  baseSalary: '',
  allowance: '',
  bonus: '',
  socialSecurity: '',
  housingFund: '',
  taxRate: ''
})
const salaryInfo = ref('')

const calcForm = ref({
  baseSalary: '',
  allowance: '',
  bonus: '',
  overtimeHours: '',
  overtimeRate: '',
  leaveDays: '',
  leaveDeductionPerDay: '',
  socialSecurity: '',
  housingFund: '',
  taxRate: ''
})
const calcResult = ref(null)

const genForm = ref({
  empId: '',
  cycleMonth: '',
  overtimeHours: '',
  overtimeRate: '',
  leaveDays: '',
  leaveDeductionPerDay: ''
})

const queryEmpId = ref('')
const records = ref([])
const search = ref('')
const page = ref(1)
const pageSize = 6

const filteredRecords = computed(() => {
  const key = search.value.trim().toLowerCase()
  if (!key) return records.value
  return records.value.filter((item) => `${item.cycleMonth || ''}`.toLowerCase().includes(key))
})
const totalPages = computed(() => Math.max(1, Math.ceil(filteredRecords.value.length / pageSize)))
const pagedRecords = computed(() => filteredRecords.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const loadSalary = async () => {
  error.value = ''
  try {
    const data = await http.get(`/api/salary/structure/${salaryForm.value.empId}`)
    salaryInfo.value = data
      ? `base ${data.baseSalary}, allowance ${data.allowance}, bonus ${data.bonus}, tax ${data.taxRate}`
      : ''
  } catch (e) {
    error.value = e.message
  }
}

const saveSalary = async () => {
  error.value = ''
  try {
    await http.post('/api/salary/structure', {
      empId: Number(salaryForm.value.empId),
      baseSalary: Number(salaryForm.value.baseSalary),
      allowance: Number(salaryForm.value.allowance),
      bonus: Number(salaryForm.value.bonus),
      socialSecurity: Number(salaryForm.value.socialSecurity),
      housingFund: Number(salaryForm.value.housingFund),
      taxRate: salaryForm.value.taxRate === '' ? null : Number(salaryForm.value.taxRate)
    })
    await loadSalary()
  } catch (e) {
    error.value = e.message
  }
}

const calc = async () => {
  error.value = ''
  try {
    calcResult.value = await http.post('/api/payroll/calc', {
      baseSalary: Number(calcForm.value.baseSalary),
      allowance: Number(calcForm.value.allowance),
      bonus: Number(calcForm.value.bonus),
      overtimeHours: Number(calcForm.value.overtimeHours),
      overtimeRate: Number(calcForm.value.overtimeRate),
      leaveDays: Number(calcForm.value.leaveDays),
      leaveDeductionPerDay: Number(calcForm.value.leaveDeductionPerDay),
      socialSecurity: Number(calcForm.value.socialSecurity),
      housingFund: Number(calcForm.value.housingFund),
      taxRate: calcForm.value.taxRate === '' ? null : Number(calcForm.value.taxRate)
    })
  } catch (e) {
    error.value = e.message
  }
}

const generate = async () => {
  error.value = ''
  try {
    await http.post('/api/payroll/generate', {
      empId: Number(genForm.value.empId),
      cycleMonth: genForm.value.cycleMonth,
      overtimeHours: Number(genForm.value.overtimeHours),
      overtimeRate: Number(genForm.value.overtimeRate),
      leaveDays: Number(genForm.value.leaveDays),
      leaveDeductionPerDay: Number(genForm.value.leaveDeductionPerDay)
    })
  } catch (e) {
    error.value = e.message
  }
}

const loadRecords = async () => {
  error.value = ''
  try {
    records.value = await http.get(`/api/payroll/employee/${queryEmpId.value}`)
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const prevPage = () => { if (page.value > 1) page.value -= 1 }
const nextPage = () => { if (page.value < totalPages.value) page.value += 1 }
</script>
