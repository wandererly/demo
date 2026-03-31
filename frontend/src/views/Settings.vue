<template>
  <div>
    <section class="card">
      <h3>请假规则</h3>
      <div class="form-grid">
        <input class="input" v-model="leaveRule.maxDaysPerRequest" placeholder="单次最大天数" />
        <input class="input" v-model="leaveRule.approvalLevel" placeholder="审批级别(1/2)" />
      </div>
      <div class="actions">
        <button class="btn" @click="saveLeaveRule">保存规则</button>
        <button class="btn-ghost" @click="loadLeaveRule">加载规则</button>
      </div>
      <div v-if="leaveRuleInfo" class="badge">当前规则: {{ leaveRuleInfo }}</div>
    </section>

    <section class="card">
      <h3>绩效等级规则</h3>
      <div class="form-grid">
        <input class="input" v-model="perfRuleForm.minScore" placeholder="最低分" />
        <input class="input" v-model="perfRuleForm.maxScore" placeholder="最高分" />
        <input class="input" v-model="perfRuleForm.level" placeholder="等级(A/B/C)" />
      </div>
      <div class="actions">
        <button class="btn" @click="createPerfRule">新增规则</button>
        <button class="btn-ghost" @click="loadPerfRules">刷新</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>分数范围</th>
            <th>等级</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in perfRules" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.minScore }} - {{ item.maxScore }}</td>
            <td>{{ item.level }}</td>
            <td><button class="btn-danger btn" @click="removePerfRule(item.id)">删除</button></td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <h3>薪酬规则</h3>
      <div class="form-grid">
        <input class="input" v-model="payrollRule.overtimeMultiplier" placeholder="加班倍率(1.5)" />
      </div>
      <div class="actions">
        <button class="btn" @click="savePayrollRule">保存规则</button>
        <button class="btn-ghost" @click="loadPayrollRule">加载规则</button>
      </div>
      <div v-if="payrollRuleInfo" class="badge">当前倍率: {{ payrollRuleInfo }}</div>
    </section>

    <section class="card">
      <h3>税率区间</h3>
      <div class="form-grid">
        <input class="input" v-model="taxForm.minIncome" placeholder="最低收入" />
        <input class="input" v-model="taxForm.maxIncome" placeholder="最高收入(可空)" />
        <input class="input" v-model="taxForm.rate" placeholder="税率(0.1)" />
      </div>
      <div class="actions">
        <button class="btn" @click="addBracket">新增区间</button>
        <button class="btn-ghost" @click="loadBrackets">刷新</button>
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>区间</th>
            <th>税率</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in brackets" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.minIncome }} - {{ item.maxIncome || '?' }}</td>
            <td>{{ item.rate }}</td>
            <td><button class="btn-danger btn" @click="removeBracket(item.id)">删除</button></td>
          </tr>
        </tbody>
      </table>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>初始化演示数据</h3>
      <p style="color:var(--muted)">一键初始化示例数据，便于体验系统功能</p>
      <div class="actions">
        <button class="btn" @click="initDemo">初始化数据</button>
      </div>
      <div v-if="demoMsg" class="badge">{{ demoMsg }}</div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { http } from '../api/http'

const error = ref('')

const leaveRule = ref({ maxDaysPerRequest: '', approvalLevel: '' })
const leaveRuleInfo = ref('')

const perfRuleForm = ref({ minScore: '', maxScore: '', level: '' })
const perfRules = ref([])

const payrollRule = ref({ overtimeMultiplier: '' })
const payrollRuleInfo = ref('')
const payrollRuleId = ref(null)

const taxForm = ref({ minIncome: '', maxIncome: '', rate: '' })
const brackets = ref([])

const demoMsg = ref('')

const loadLeaveRule = async () => {
  error.value = ''
  try {
    const data = await http.get('/api/rules/leave')
    if (data) {
      leaveRuleInfo.value = `maxDays ${data.maxDaysPerRequest}, level ${data.approvalLevel}`
      leaveRule.value = { maxDaysPerRequest: data.maxDaysPerRequest, approvalLevel: data.approvalLevel }
    }
  } catch (e) {
    error.value = e.message
  }
}

const saveLeaveRule = async () => {
  error.value = ''
  try {
    await http.post('/api/rules/leave', {
      maxDaysPerRequest: Number(leaveRule.value.maxDaysPerRequest),
      approvalLevel: Number(leaveRule.value.approvalLevel)
    })
    await loadLeaveRule()
  } catch (e) {
    error.value = e.message
  }
}

const loadPerfRules = async () => {
  error.value = ''
  try {
    perfRules.value = await http.get('/api/rules/performance')
  } catch (e) {
    error.value = e.message
  }
}

const createPerfRule = async () => {
  error.value = ''
  try {
    await http.post('/api/rules/performance', {
      minScore: Number(perfRuleForm.value.minScore),
      maxScore: Number(perfRuleForm.value.maxScore),
      level: perfRuleForm.value.level
    })
    perfRuleForm.value = { minScore: '', maxScore: '', level: '' }
    await loadPerfRules()
  } catch (e) {
    error.value = e.message
  }
}

const removePerfRule = async (id) => {
  error.value = ''
  try {
    await http.del(`/api/rules/performance/${id}`)
    await loadPerfRules()
  } catch (e) {
    error.value = e.message
  }
}

const loadPayrollRule = async () => {
  error.value = ''
  try {
    const data = await http.get('/api/rules/payroll')
    if (data) {
      payrollRuleId.value = data.id
      payrollRuleInfo.value = data.overtimeMultiplier
      payrollRule.value = { overtimeMultiplier: data.overtimeMultiplier }
      await loadBrackets()
    }
  } catch (e) {
    error.value = e.message
  }
}

const savePayrollRule = async () => {
  error.value = ''
  try {
    await http.post('/api/rules/payroll', {
      overtimeMultiplier: Number(payrollRule.value.overtimeMultiplier)
    })
    await loadPayrollRule()
  } catch (e) {
    error.value = e.message
  }
}

const loadBrackets = async () => {
  error.value = ''
  try {
    if (!payrollRuleId.value) return
    brackets.value = await http.get(`/api/rules/payroll/${payrollRuleId.value}/brackets`)
  } catch (e) {
    error.value = e.message
  }
}

const addBracket = async () => {
  error.value = ''
  try {
    if (!payrollRuleId.value) {
      error.value = '请先保存薪酬规则'
      return
    }
    await http.post(`/api/rules/payroll/${payrollRuleId.value}/brackets`, {
      minIncome: Number(taxForm.value.minIncome),
      maxIncome: taxForm.value.maxIncome === '' ? null : Number(taxForm.value.maxIncome),
      rate: Number(taxForm.value.rate)
    })
    taxForm.value = { minIncome: '', maxIncome: '', rate: '' }
    await loadBrackets()
  } catch (e) {
    error.value = e.message
  }
}

const removeBracket = async (id) => {
  error.value = ''
  try {
    await http.del(`/api/rules/payroll/brackets/${id}`)
    await loadBrackets()
  } catch (e) {
    error.value = e.message
  }
}

const initDemo = async () => {
  error.value = ''
  demoMsg.value = ''
  try {
    const msg = await http.post('/api/demo/init', {})
    demoMsg.value = msg
  } catch (e) {
    error.value = e.message
  }
}

onMounted(() => {
  loadLeaveRule()
  loadPerfRules()
  loadPayrollRule()
})
</script>
