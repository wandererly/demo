<template>
  <div>
    <section class="card">
      <h3>绩效周期</h3>
      <div class="form-grid">
        <input class="input" v-model="cycleForm.name" placeholder="周期名称" />
        <input class="input" v-model="cycleForm.startDate" placeholder="开始日期 YYYY-MM-DD" />
        <input class="input" v-model="cycleForm.endDate" placeholder="结束日期 YYYY-MM-DD" />
      </div>
      <div class="actions">
        <button class="btn" @click="createCycle">新增周期</button>
        <button class="btn-ghost" @click="loadCycles">刷新</button>
      </div>
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <h3>编辑周期</h3>
      <div class="form-grid">
        <input class="input" v-model="cycleEdit.id" placeholder="周期ID" />
        <input class="input" v-model="cycleEdit.name" placeholder="周期名称" />
        <input class="input" v-model="cycleEdit.startDate" placeholder="开始日期" />
        <input class="input" v-model="cycleEdit.endDate" placeholder="结束日期" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateCycle">更新周期</button>
      </div>
    </section>

    <section class="card">
      <h3>绩效指标</h3>
      <div class="form-grid">
        <input class="input" v-model="indicatorForm.cycleId" placeholder="周期ID" />
        <input class="input" v-model="indicatorForm.name" placeholder="指标名称" />
        <input class="input" v-model="indicatorForm.weight" placeholder="权重" />
        <input class="input" v-model="indicatorForm.description" placeholder="说明" />
      </div>
      <div class="actions">
        <button class="btn" @click="createIndicator">新增指标</button>
        <button class="btn-ghost" @click="loadIndicators">加载指标</button>
      </div>
    </section>

    <section class="card">
      <h3>编辑指标</h3>
      <div class="form-grid">
        <input class="input" v-model="indicatorEdit.id" placeholder="指标ID" />
        <input class="input" v-model="indicatorEdit.name" placeholder="指标名称" />
        <input class="input" v-model="indicatorEdit.weight" placeholder="权重" />
        <input class="input" v-model="indicatorEdit.description" placeholder="说明" />
      </div>
      <div class="actions">
        <button class="btn" @click="updateIndicator">更新指标</button>
      </div>
    </section>

    <section class="card">
      <h3>绩效评审</h3>
      <div class="form-grid">
        <input class="input" v-model="reviewForm.empId" placeholder="员工ID" />
        <input class="input" v-model="reviewForm.cycleId" placeholder="周期ID" />
        <input class="input" v-model="reviewForm.score" placeholder="评分" />
        <input class="input" v-model="reviewForm.level" placeholder="等级(A/B/C)" />
        <input class="input" v-model="reviewForm.reviewerId" placeholder="评审人ID" />
        <input class="input" v-model="reviewForm.comment" placeholder="评语" />
      </div>
      <div class="actions">
        <button class="btn" @click="createReview">新增评审</button>
        <button class="btn-ghost" @click="loadReviews">加载评审</button>
      </div>
    </section>

    <section class="card">
      <h3>编辑评审</h3>
      <div class="form-grid">
        <input class="input" v-model="reviewEdit.id" placeholder="评审ID" />
        <input class="input" v-model="reviewEdit.score" placeholder="评分" />
        <input class="input" v-model="reviewEdit.level" placeholder="等级" />
        <input class="input" v-model="reviewEdit.reviewerId" placeholder="评审人ID" />
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
            <th>周期ID</th>
            <th>名称</th>
            <th>权重</th>
            <th>说明</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in indicatorPaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.cycleId }}</td>
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
            <th>员工ID</th>
            <th>周期ID</th>
            <th>评分</th>
            <th>等级</th>
            <th>评审人</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in reviewPaged" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.empId }}</td>
            <td>{{ item.cycleId }}</td>
            <td>{{ item.score }}</td>
            <td>{{ item.level }}</td>
            <td>{{ item.reviewerId }}</td>
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
  return reviews.value.filter((item) => `${item.empId || ''}${item.level || ''}`.toLowerCase().includes(key))
})
const reviewPages = computed(() => Math.max(1, Math.ceil(reviewFiltered.value.length / pageSize)))
const reviewPaged = computed(() => reviewFiltered.value.slice((reviewPage.value - 1) * pageSize, reviewPage.value * pageSize))

const loadCycles = async () => {
  error.value = ''
  try {
    cycles.value = await http.get('/api/performance/cycles')
    cyclePage.value = 1
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
  try {
    await http.post('/api/performance/indicators', {
      cycleId: indicatorForm.value.cycleId ? Number(indicatorForm.value.cycleId) : null,
      name: indicatorForm.value.name,
      weight: indicatorForm.value.weight ? Number(indicatorForm.value.weight) : null,
      description: indicatorForm.value.description || null
    })
    indicatorForm.value = { cycleId: '', name: '', weight: '', description: '' }
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
  try {
    await http.post('/api/performance/reviews', {
      empId: reviewForm.value.empId ? Number(reviewForm.value.empId) : null,
      cycleId: reviewForm.value.cycleId ? Number(reviewForm.value.cycleId) : null,
      score: reviewForm.value.score ? Number(reviewForm.value.score) : null,
      level: reviewForm.value.level || null,
      reviewerId: reviewForm.value.reviewerId ? Number(reviewForm.value.reviewerId) : null,
      comment: reviewForm.value.comment || null
    })
    reviewForm.value = { empId: '', cycleId: '', score: '', level: '', reviewerId: '', comment: '' }
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

onMounted(loadCycles)
</script>
