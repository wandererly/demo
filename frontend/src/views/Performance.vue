<template>
  <div>
    <section class="card">
      <h3>绩效周期</h3>
      <div class="form-grid">
        <input class="input" v-model="cycleForm.name" placeholder="周期名称" />
        <input class="input" type="date" v-model="cycleForm.startDate" placeholder="开始日期" />
        <input class="input" type="date" v-model="cycleForm.endDate" placeholder="结束日期" />
      </div>
      <div class="actions">
        <button class="btn" @click="createCycle">新增周期</button>
        <button class="btn-ghost" @click="loadCycles">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑周期</h3>
      <div v-if="cycleEdit.id" class="badge">当前周期 #{{ cycleEdit.id }}</div>
      <div class="form-grid">
        <input class="input" v-model="cycleEdit.name" placeholder="周期名称" />
        <input class="input" type="date" v-model="cycleEdit.startDate" placeholder="开始日期" />
        <input class="input" type="date" v-model="cycleEdit.endDate" placeholder="结束日期" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateCycle">更新周期</button>
      </div>
    </section>

    <section class="card">
      <h3>绩效指标</h3>
      <div class="form-grid">
        <select class="input" v-model="indicatorForm.cycleId">
          <option value="">请选择周期</option>
          <option v-for="cycle in cycles" :key="cycle.id" :value="cycle.id">{{ cycle.name }}</option>
        </select>
        <input class="input" v-model="indicatorForm.name" placeholder="指标名称" />
        <input class="input" type="number" v-model="indicatorForm.weight" placeholder="权重%(0-100)" min="0" max="100" step="5" />
        <input class="input" v-model="indicatorForm.description" placeholder="说明" />
      </div>
      <div class="actions">
        <button class="btn" @click="createIndicator">新增指标</button>
        <button class="btn-ghost" @click="loadIndicators">加载指标</button>
      </div>
    </section>

    <section class="card">
      <h3>编辑指标</h3>
      <div v-if="indicatorEdit.id" class="badge">当前指标 #{{ indicatorEdit.id }}</div>
      <div class="form-grid">
        <input class="input" v-model="indicatorEdit.name" placeholder="指标名称" />
        <input class="input" type="number" v-model="indicatorEdit.weight" placeholder="权重%(0-100)" min="0" max="100" step="5" />
        <input class="input" v-model="indicatorEdit.description" placeholder="说明" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateIndicator">更新指标</button>
      </div>
    </section>

    <section class="card">
      <h3>绩效评审</h3>
      <div class="form-grid">
        <select class="input" v-model="reviewForm.empId">
          <option value="">请选择员工</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
        <select class="input" v-model="reviewForm.cycleId">
          <option value="">请选择周期</option>
          <option v-for="cycle in cycles" :key="cycle.id" :value="cycle.id">{{ cycle.name }}</option>
        </select>
        <input class="input" type="number" v-model="reviewForm.score" placeholder="评分(0-100)" min="0" max="100" step="1" />
        <input class="input" v-model="reviewForm.level" placeholder="等级(A/B/C)" />
        <select class="input" v-model="reviewForm.reviewerId">
          <option value="">请选择评审人</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
        <input class="input" v-model="reviewForm.comment" placeholder="评语" />
      </div>
      <div class="actions">
        <button class="btn" @click="createReview">新增评审</button>
        <button class="btn-ghost" @click="loadReviews">加载评审</button>
      </div>
    </section>

    <section class="card">
      <h3>编辑评审</h3>
      <div v-if="reviewEdit.id" class="badge">当前评审 #{{ reviewEdit.id }}</div>
      <div class="form-grid">
        <input class="input" type="number" v-model="reviewEdit.score" placeholder="评分(0-100)" min="0" max="100" step="1" />
        <input class="input" v-model="reviewEdit.level" placeholder="等级" />
        <select class="input" v-model="reviewEdit.reviewerId">
          <option value="">请选择评审人</option>
          <option v-for="emp in employees" :key="emp.id" :value="emp.id">{{ employeeLabel(emp) }}</option>
        </select>
        <input class="input" v-model="reviewEdit.comment" placeholder="评语" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateReview">更新评审</button>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">周期列表</h3>
        <input class="input" v-model="cycleSearch" placeholder="搜索周期" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>开始</th>
            <th>结束</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in cyclePaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.startDate }}</td>
            <td>{{ item.endDate }}</td>
            <td>{{ item.status }}</td>
            <td><button class="btn-ghost" @click="pickCycle(item)">编辑</button></td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ cycleFiltered.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevCycle" :disabled="cyclePage === 1">上一页</button>
          <div>第 {{ cyclePage }} / {{ cyclePages }} 页</div>
          <button class="btn-ghost" @click="nextCycle" :disabled="cyclePage === cyclePages">下一页</button>
        </div>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">指标列表</h3>
        <input class="input" v-model="indicatorSearch" placeholder="搜索指标" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>周期</th>
            <th>名称</th>
            <th>权重</th>
            <th>说明</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in indicatorPaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ cycleName(item.cycleId) }}</td>
            <td>{{ item.name }}</td>
            <td>{{ item.weight }}</td>
            <td>{{ item.description }}</td>
            <td><button class="btn-ghost" @click="pickIndicator(item)">编辑</button></td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ indicatorFiltered.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevIndicator" :disabled="indicatorPage === 1">上一页</button>
          <div>第 {{ indicatorPage }} / {{ indicatorPages }} 页</div>
          <button class="btn-ghost" @click="nextIndicator" :disabled="indicatorPage === indicatorPages">下一页</button>
        </div>
      </div>
    </section>

    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">评审列表</h3>
        <input class="input" v-model="reviewSearch" placeholder="搜索员工/等级" style="max-width:240px" />
      </div>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>员工</th>
            <th>周期</th>
            <th>评分</th>
            <th>等级</th>
            <th>评审人</th>
            <th>审批</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in reviewPaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ employeeName(item.empId) }}</td>
            <td>{{ cycleName(item.cycleId) }}</td>
            <td>{{ item.score }}</td>
            <td>{{ item.level }}</td>
            <td>{{ employeeName(item.reviewerId) }}</td>
            <td>{{ statusText(item.approvalStatus) }}</td>
            <td><button class="btn-ghost" @click="pickReview(item)">编辑</button></td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ reviewFiltered.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevReview" :disabled="reviewPage === 1">上一页</button>
          <div>第 {{ reviewPage }} / {{ reviewPages }} 页</div>
          <button class="btn-ghost" @click="nextReview" :disabled="reviewPage === reviewPages">下一页</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'

const cycles = ref([])
const indicators = ref([])
const reviews = ref([])
const employees = ref([])
const error = ref('')

const cycleForm = ref({ name: '', startDate: '', endDate: '' })
const cycleEdit = ref({ id: '', name: '', startDate: '', endDate: '' })
const indicatorForm = ref({ cycleId: '', name: '', weight: '', description: '' })
const indicatorEdit = ref({ id: '', name: '', weight: '', description: '' })
const reviewForm = ref({ empId: '', cycleId: '', score: '', level: '', reviewerId: '', comment: '' })
const reviewEdit = ref({ id: '', score: '', level: '', reviewerId: '', comment: '' })

const cycleSearch = ref('')
const indicatorSearch = ref('')
const reviewSearch = ref('')

const cyclePage = ref(1)
const indicatorPage = ref(1)
const reviewPage = ref(1)
const pageSize = 6

const cycleFiltered = computed(() => {
  const key = cycleSearch.value.trim().toLowerCase()
  if (!key) return cycles.value
  return cycles.value.filter((item) => `${item.name || ''}`.toLowerCase().includes(key))
})
const cyclePages = computed(() => Math.max(1, Math.ceil(cycleFiltered.value.length / pageSize)))
const cyclePaged = computed(() => cycleFiltered.value.slice((cyclePage.value - 1) * pageSize, cyclePage.value * pageSize))

const indicatorFiltered = computed(() => {
  const key = indicatorSearch.value.trim().toLowerCase()
  if (!key) return indicators.value
  return indicators.value.filter((item) => `${item.name || ''}`.toLowerCase().includes(key))
})
const indicatorPages = computed(() => Math.max(1, Math.ceil(indicatorFiltered.value.length / pageSize)))
const indicatorPaged = computed(() => indicatorFiltered.value.slice((indicatorPage.value - 1) * pageSize, indicatorPage.value * pageSize))

const reviewFiltered = computed(() => {
  const key = reviewSearch.value.trim().toLowerCase()
  if (!key) return reviews.value
  return reviews.value.filter((item) => `${employeeName(item.empId)}${cycleName(item.cycleId)}${item.level || ''}`.toLowerCase().includes(key))
})
const reviewPages = computed(() => Math.max(1, Math.ceil(reviewFiltered.value.length / pageSize)))
const reviewPaged = computed(() => reviewFiltered.value.slice((reviewPage.value - 1) * pageSize, reviewPage.value * pageSize))

const loadCycles = async () => {
  error.value = ''
  try {
    cycles.value = await http.get('/api/performance/cycles')
    cyclePage.value = 1
    if (!indicatorForm.value.cycleId && cycles.value.length) {
      indicatorForm.value.cycleId = cycles.value[0].id
    }
    if (!reviewForm.value.cycleId && cycles.value.length) {
      reviewForm.value.cycleId = cycles.value[0].id
    }
  } catch (e) {
    error.value = e.message
  }
}

const createCycle = async () => {
  error.value = ''
  try {
    await http.post('/api/performance/cycles', {
      name: cycleForm.value.name,
      startDate: cycleForm.value.startDate,
      endDate: cycleForm.value.endDate
    })
    cycleForm.value = { name: '', startDate: '', endDate: '' }
    await loadCycles()
  } catch (e) {
    error.value = e.message
  }
}

const pickCycle = (item) => {
  cycleEdit.value = {
    id: item.id,
    name: item.name || '',
    startDate: item.startDate || '',
    endDate: item.endDate || ''
  }
}

const updateCycle = async () => {
  error.value = ''
  if (!cycleEdit.value.id) {
    error.value = '请先从周期列表选择一条记录'
    return
  }
  try {
    await http.put(`/api/performance/cycles/${cycleEdit.value.id}`, {
      name: cycleEdit.value.name,
      startDate: cycleEdit.value.startDate,
      endDate: cycleEdit.value.endDate
    })
    await loadCycles()
  } catch (e) {
    error.value = e.message
  }
}

const loadIndicators = async () => {
  error.value = ''
  try {
    if (!indicatorForm.value.cycleId) return
    indicators.value = await http.get(`/api/performance/cycles/${indicatorForm.value.cycleId}/indicators`)
    indicatorPage.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const createIndicator = async () => {
  error.value = ''
  if (!indicatorForm.value.cycleId) {
    error.value = '请选择周期'
    return
  }
  try {
    await http.post('/api/performance/indicators', {
      cycleId: indicatorForm.value.cycleId ? Number(indicatorForm.value.cycleId) : null,
      name: indicatorForm.value.name,
      weight: indicatorForm.value.weight ? Number(indicatorForm.value.weight) : null,
      description: indicatorForm.value.description || null
    })
    const cycleId = indicatorForm.value.cycleId
    indicatorForm.value = { cycleId, name: '', weight: '', description: '' }
    await loadIndicators()
  } catch (e) {
    error.value = e.message
  }
}

const pickIndicator = (item) => {
  indicatorEdit.value = {
    id: item.id,
    name: item.name || '',
    weight: item.weight || '',
    description: item.description || ''
  }
}

const updateIndicator = async () => {
  error.value = ''
  if (!indicatorEdit.value.id) {
    error.value = '请先从指标列表选择一条记录'
    return
  }
  try {
    await http.put(`/api/performance/indicators/${indicatorEdit.value.id}`, {
      name: indicatorEdit.value.name || null,
      weight: indicatorEdit.value.weight ? Number(indicatorEdit.value.weight) : null,
      description: indicatorEdit.value.description || null
    })
    await loadIndicators()
  } catch (e) {
    error.value = e.message
  }
}

const loadReviews = async () => {
  error.value = ''
  try {
    if (!reviewForm.value.cycleId) return
    reviews.value = await http.get(`/api/performance/cycles/${reviewForm.value.cycleId}/reviews`)
    reviewPage.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const createReview = async () => {
  error.value = ''
  if (!reviewForm.value.empId || !reviewForm.value.cycleId) {
    error.value = '请选择员工和周期'
    return
  }
  try {
    await http.post('/api/performance/reviews', {
      empId: reviewForm.value.empId ? Number(reviewForm.value.empId) : null,
      cycleId: reviewForm.value.cycleId ? Number(reviewForm.value.cycleId) : null,
      score: reviewForm.value.score ? Number(reviewForm.value.score) : null,
      level: reviewForm.value.level || null,
      reviewerId: reviewForm.value.reviewerId ? Number(reviewForm.value.reviewerId) : null,
      comment: reviewForm.value.comment || null
    })
    const cycleId = reviewForm.value.cycleId
    reviewForm.value = { empId: '', cycleId, score: '', level: '', reviewerId: '', comment: '' }
    await loadReviews()
  } catch (e) {
    error.value = e.message
  }
}

const pickReview = (item) => {
  reviewEdit.value = {
    id: item.id,
    score: item.score || '',
    level: item.level || '',
    reviewerId: item.reviewerId || '',
    comment: item.comment || ''
  }
}

const updateReview = async () => {
  error.value = ''
  if (!reviewEdit.value.id) {
    error.value = '请先从评审列表选择一条记录'
    return
  }
  try {
    await http.put(`/api/performance/reviews/${reviewEdit.value.id}`, {
      score: reviewEdit.value.score ? Number(reviewEdit.value.score) : null,
      level: reviewEdit.value.level || null,
      reviewerId: reviewEdit.value.reviewerId ? Number(reviewEdit.value.reviewerId) : null,
      comment: reviewEdit.value.comment || null
    })
    await loadReviews()
  } catch (e) {
    error.value = e.message
  }
}

const prevCycle = () => { if (cyclePage.value > 1) cyclePage.value -= 1 }
const nextCycle = () => { if (cyclePage.value < cyclePages.value) cyclePage.value += 1 }
const prevIndicator = () => { if (indicatorPage.value > 1) indicatorPage.value -= 1 }
const nextIndicator = () => { if (indicatorPage.value < indicatorPages.value) indicatorPage.value += 1 }
const prevReview = () => { if (reviewPage.value > 1) reviewPage.value -= 1 }
const nextReview = () => { if (reviewPage.value < reviewPages.value) reviewPage.value += 1 }

const loadEmployees = async () => {
  employees.value = await http.get('/api/employees').catch(() => [])
}

const employeeName = (id) => {
  if (!id) return '-'
  return employees.value.find((item) => String(item.id) === String(id))?.name || `员工 #${id}`
}

const employeeLabel = (employee) => `${employee.name}${employee.position ? `（${employee.position}）` : ''}`

const cycleName = (id) => {
  if (!id) return '-'
  return cycles.value.find((item) => String(item.id) === String(id))?.name || `周期 #${id}`
}

const statusText = (status) => ({
  PENDING: '待审批',
  APPROVED: '已通过',
  REJECTED: '已驳回'
}[status] || status || '-')

onMounted(async () => {
  await Promise.all([loadCycles(), loadEmployees()])
})
</script>
