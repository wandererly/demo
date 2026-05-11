<template>
  <div>
    <section class="card info-card">
      <h3>编码说明</h3>
      <div class="info-grid">
        <div>
          <strong>职级编码</strong>
          <p><code>L1-L6</code> 表示岗位等级，从基础岗位到管理层逐级上升，用于薪资区间和晋升判断。</p>
        </div>
        <div>
          <strong>岗位编码</strong>
          <p><code>DEV-MID</code>、<code>HR-MGR</code> 这类编码是岗位的稳定标识，中文名称调整后仍可追踪同一岗位。</p>
        </div>
        <div>
          <strong>路径组</strong>
          <p><code>DEV</code>、<code>HR</code>、<code>FIN</code> 用于归类晋升通道，方便定义同序列内的晋升规则。</p>
        </div>
      </div>
    </section>

    <section class="card">
      <h3>职级管理</h3>
      <p style="color:var(--muted)">定义公司职级体系 (L1-L6)</p>
      <div class="form-grid">
        <input class="input" v-model="levelForm.levelCode" placeholder="职级编码 (L1)" />
        <input class="input" v-model="levelForm.levelName" placeholder="职级名称" />
        <input class="input" type="number" v-model="levelForm.rank" placeholder="职级排序" />
        <input class="input" type="number" v-model="levelForm.salaryMin" placeholder="薪资下限(不低于3500)" min="3500" step="100" />
        <input class="input" type="number" v-model="levelForm.salaryMax" placeholder="薪资上限(不低于3500)" min="3500" step="100" />
        <input class="input" v-model="levelForm.description" placeholder="描述" />
        <input class="input" v-model="levelForm.status" placeholder="状态" />
      </div>
      <div class="actions">
        <button class="btn" @click="createLevel">新增职级</button>
        <button class="btn-ghost" @click="loadLevels">刷新</button>
      </div>
      <table class="table" v-if="levels.length">
        <thead>
          <tr><th>ID</th><th>编码</th><th>名称</th><th>排序</th><th>薪资下限</th><th>薪资上限</th><th>状态</th></tr>
        </thead>
        <tbody>
          <tr v-for="item in levels" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.levelCode }}</td>
            <td>{{ item.levelName }}</td>
            <td>{{ item.rank }}</td>
            <td>{{ item.salaryMin }}</td>
            <td>{{ item.salaryMax }}</td>
            <td>{{ item.status }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <h3>岗位管理</h3>
      <p style="color:var(--muted)">管理各部门岗位编制</p>
      <div class="form-grid">
        <select class="input" v-model="positionForm.deptId">
          <option value="">请选择部门</option>
          <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
        </select>
        <select class="input" v-model="positionForm.levelId">
          <option value="">请选择职级</option>
          <option v-for="level in levels" :key="level.id" :value="level.id">{{ level.levelCode }} - {{ level.levelName }}</option>
        </select>
        <input class="input" v-model="positionForm.positionCode" placeholder="岗位编码" />
        <input class="input" v-model="positionForm.positionName" placeholder="岗位名称" />
        <input class="input" v-model="positionForm.pathGroup" placeholder="晋升路径组" />
        <input class="input" v-model="positionForm.description" placeholder="描述" />
        <select class="input" v-model="positionForm.status">
          <option value="">状态</option>
          <option value="ACTIVE">启用</option>
          <option value="INACTIVE">停用</option>
        </select>
      </div>
      <div class="actions">
        <button class="btn" @click="createPosition">新增岗位</button>
        <button class="btn-ghost" @click="loadPositions">刷新</button>
      </div>
      <table class="table" v-if="positions.length">
        <thead>
          <tr><th>ID</th><th>部门</th><th>职级</th><th>编码</th><th>名称</th><th>路径组</th><th>状态</th></tr>
        </thead>
        <tbody>
          <tr v-for="item in positions" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ departmentName(item.deptId) }}</td>
            <td>{{ levelName(item.levelId) }}</td>
            <td>{{ item.positionCode }}</td>
            <td>{{ item.positionName }}</td>
            <td>{{ item.pathGroup }}</td>
            <td>{{ statusLabel(item.status) }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <section class="card">
      <h3>晋升路径</h3>
      <p style="color:var(--muted)">定义岗位间的晋升规则</p>
      <div class="form-grid">
        <select class="input" v-model="pathForm.fromPositionId">
          <option value="">请选择当前岗位</option>
          <option v-for="position in positions" :key="position.id" :value="position.id">{{ position.positionName }}</option>
        </select>
        <select class="input" v-model="pathForm.toPositionId">
          <option value="">请选择晋升岗位</option>
          <option v-for="position in positions" :key="position.id" :value="position.id">{{ position.positionName }}</option>
        </select>
        <input class="input" type="number" v-model="pathForm.minMonths" placeholder="最低月数" />
        <input class="input" v-model="pathForm.conditionText" placeholder="晋升条件" />
      </div>
      <div class="actions">
        <button class="btn" @click="createPath">新增路径</button>
        <button class="btn-ghost" @click="loadPaths">刷新</button>
      </div>
      <table class="table" v-if="paths.length">
        <thead>
          <tr><th>ID</th><th>当前岗位</th><th>晋升岗位</th><th>最低月数</th><th>条件</th></tr>
        </thead>
        <tbody>
          <tr v-for="item in paths" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ positionName(item.fromPositionId) }}</td>
            <td>{{ positionName(item.toPositionId) }}</td>
            <td>{{ item.minMonths }}</td>
            <td>{{ item.conditionText }}</td>
          </tr>
        </tbody>
      </table>
    </section>

    <div v-if="error" class="alert">{{ error }}</div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { http } from '../api/http'

const error = ref('')
const levels = ref([])
const positions = ref([])
const paths = ref([])
const departments = ref([])

const levelForm = ref({ levelCode: '', levelName: '', rank: '', salaryMin: '', salaryMax: '', description: '', status: '' })
const positionForm = ref({ deptId: '', levelId: '', positionCode: '', positionName: '', pathGroup: '', description: '', status: '' })
const pathForm = ref({ fromPositionId: '', toPositionId: '', minMonths: '', conditionText: '' })

const n = (v) => v === '' || v === null ? null : parseFloat(v)

const loadLevels = async () => {
  error.value = ''
  try { levels.value = await http.get('/api/org/levels') }
  catch (e) { error.value = e.message }
}

const loadPositions = async () => {
  error.value = ''
  try { positions.value = await http.get('/api/org/positions') }
  catch (e) { error.value = e.message }
}

const loadPaths = async () => {
  error.value = ''
  try { paths.value = await http.get('/api/org/paths') }
  catch (e) { error.value = e.message }
}

const loadDepartments = async () => {
  error.value = ''
  try { departments.value = await http.get('/api/departments') }
  catch (e) { error.value = e.message }
}

const createLevel = async () => {
  error.value = ''
  try {
    await http.post('/api/org/levels', {
      levelCode: levelForm.value.levelCode || null,
      levelName: levelForm.value.levelName || null,
      rank: levelForm.value.rank ? parseInt(levelForm.value.rank) : null,
      salaryMin: n(levelForm.value.salaryMin),
      salaryMax: n(levelForm.value.salaryMax),
      description: levelForm.value.description || null,
      status: levelForm.value.status || null
    })
    levelForm.value = { levelCode: '', levelName: '', rank: '', salaryMin: '', salaryMax: '', description: '', status: '' }
    await loadLevels()
  } catch (e) { error.value = e.message }
}

const createPosition = async () => {
  error.value = ''
  try {
    await http.post('/api/org/positions', {
      deptId: positionForm.value.deptId ? parseInt(positionForm.value.deptId) : null,
      levelId: positionForm.value.levelId ? parseInt(positionForm.value.levelId) : null,
      positionCode: positionForm.value.positionCode || null,
      positionName: positionForm.value.positionName || null,
      pathGroup: positionForm.value.pathGroup || null,
      description: positionForm.value.description || null,
      status: positionForm.value.status || null
    })
    positionForm.value = { deptId: '', levelId: '', positionCode: '', positionName: '', pathGroup: '', description: '', status: '' }
    await loadPositions()
  } catch (e) { error.value = e.message }
}

const createPath = async () => {
  error.value = ''
  try {
    await http.post('/api/org/paths', {
      fromPositionId: pathForm.value.fromPositionId ? parseInt(pathForm.value.fromPositionId) : null,
      toPositionId: pathForm.value.toPositionId ? parseInt(pathForm.value.toPositionId) : null,
      minMonths: pathForm.value.minMonths ? parseInt(pathForm.value.minMonths) : null,
      conditionText: pathForm.value.conditionText || null
    })
    pathForm.value = { fromPositionId: '', toPositionId: '', minMonths: '', conditionText: '' }
    await loadPaths()
  } catch (e) { error.value = e.message }
}

const departmentName = (id) => {
  if (!id) return '-'
  return departments.value.find((item) => String(item.id) === String(id))?.name || `部门 #${id}`
}

const levelName = (id) => {
  if (!id) return '-'
  const level = levels.value.find((item) => String(item.id) === String(id))
  return level ? `${level.levelCode} - ${level.levelName}` : `职级 #${id}`
}

const positionName = (id) => {
  if (!id) return '-'
  return positions.value.find((item) => String(item.id) === String(id))?.positionName || `岗位 #${id}`
}

const statusLabel = (status) => {
  if (status === 'ACTIVE') return '启用'
  if (status === 'INACTIVE') return '停用'
  return status || '-'
}

onMounted(() => {
  loadDepartments()
  loadLevels()
  loadPositions()
  loadPaths()
})
</script>
