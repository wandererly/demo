<template>
  <div>
    <section class="card">
      <div class="page-head">
        <div>
          <h2>员工自助</h2>
          <p class="muted">查看个人档案、考勤、请假、绩效、工资条和通知。</p>
        </div>
        <div v-if="profile" class="badge">{{ profile.name }} · {{ profile.position || '员工' }}</div>
      </div>
      <div class="tabs">
        <router-link v-for="tab in tabs" :key="tab.key" class="tab-button" :class="{ active: section === tab.key }" :to="`/self/${tab.key}`">
          {{ tab.label }}
        </router-link>
      </div>
    </section>

    <div v-if="error" class="alert">{{ error }}</div>

    <section v-if="section === 'profile'" class="card">
      <h3>我的档案</h3>
      <div v-if="profile" class="detail-grid">
        <div><span>员工编号</span><strong>{{ profile.empNo }}</strong></div>
        <div><span>姓名</span><strong>{{ profile.name }}</strong></div>
        <div><span>性别</span><strong>{{ profile.gender || '-' }}</strong></div>
        <div><span>手机号</span><strong>{{ profile.phone || '-' }}</strong></div>
        <div><span>邮箱</span><strong>{{ profile.email || '-' }}</strong></div>
        <div><span>岗位</span><strong>{{ profile.position || '-' }}</strong></div>
        <div><span>入职日期</span><strong>{{ profile.hireDate || '-' }}</strong></div>
        <div><span>状态</span><strong>{{ statusText(profile.status) }}</strong></div>
      </div>
      <div v-else class="empty-state compact">暂无员工档案绑定</div>
    </section>

    <section v-if="section === 'attendance'" class="card">
      <div class="actions" style="justify-content: space-between; margin-top:0">
        <h3 style="margin:0">我的考勤</h3>
        <button class="btn-ghost" @click="loadAttendance">刷新</button>
      </div>
      <div class="self-attendance-card">
        <div>
          <span>今日考勤</span>
          <strong>{{ todayAttendance?.workDate || today }}</strong>
          <p>{{ attendanceText(todayAttendance?.status) }}</p>
        </div>
        <div>
          <span>签到</span>
          <strong>{{ fmt(todayAttendance?.checkIn) }}</strong>
        </div>
        <div>
          <span>签退</span>
          <strong>{{ fmt(todayAttendance?.checkOut) }}</strong>
        </div>
        <div class="self-attendance-actions">
          <button v-if="!todayAttendance?.checkIn" class="btn" @click="clockIn">上班打卡</button>
          <button v-else-if="!todayAttendance?.checkOut" class="btn" @click="clockOut">下班打卡</button>
          <div v-else class="badge">今日已完成</div>
        </div>
      </div>
      <div v-if="attendanceInfo" class="badge">{{ attendanceInfo }}</div>

      <div class="grid grid-2 self-apply-grid">
        <div class="mini-panel">
          <h4>补卡申请</h4>
          <div class="form-grid">
            <input class="input" type="date" v-model="correctionForm.workDate" />
            <input class="input" type="datetime-local" v-model="correctionForm.checkIn" />
            <input class="input" type="datetime-local" v-model="correctionForm.checkOut" />
            <input class="input" v-model="correctionForm.reason" placeholder="补卡原因" />
          </div>
          <button class="btn" @click="submitCorrection">提交补卡</button>
        </div>
        <div class="mini-panel">
          <h4>加班申请</h4>
          <div class="form-grid">
            <input class="input" type="date" v-model="overtimeForm.workDate" />
            <input class="input" type="number" v-model="overtimeForm.hours" placeholder="加班小时" min="0" step="1" />
            <input class="input" v-model="overtimeForm.reason" placeholder="加班原因" />
          </div>
          <button class="btn" @click="submitOvertime">提交加班</button>
        </div>
      </div>

      <h4>历史考勤</h4>
      <table class="table"><thead><tr><th>序号</th><th>日期</th><th>签到</th><th>签退</th><th>状态</th></tr></thead>
        <tbody><tr v-for="(item, idx) in attendance" :key="item.id"><td>{{ idx + 1 }}</td><td>{{ item.workDate }}</td><td>{{ fmt(item.checkIn) }}</td><td>{{ fmt(item.checkOut) }}</td><td>{{ attendanceText(item.status) }}</td></tr></tbody>
      </table>
      <div v-if="!attendance.length" class="empty-state compact">暂无考勤记录</div>
    </section>

    <section v-if="section === 'leave'" class="card">
      <h3>我的请假</h3>
      <table class="table"><thead><tr><th>序号</th><th>类型</th><th>开始</th><th>结束</th><th>天数</th><th>状态</th></tr></thead>
        <tbody><tr v-for="(item, idx) in leaves" :key="item.id"><td>{{ idx + 1 }}</td><td>{{ leaveType(item.leaveType) }}</td><td>{{ fmt(item.startTime) }}</td><td>{{ fmt(item.endTime) }}</td><td>{{ item.days }}</td><td>{{ approvalText(item.status) }}</td></tr></tbody>
      </table>
      <div v-if="!leaves.length" class="empty-state compact">暂无请假记录</div>
    </section>

    <section v-if="section === 'performance'" class="card">
      <h3>我的绩效</h3>
      <table class="table"><thead><tr><th>序号</th><th>目标</th><th>自评</th><th>上级评分</th><th>最终分</th><th>等级</th><th>状态</th></tr></thead>
        <tbody><tr v-for="(item, idx) in goals" :key="item.id"><td>{{ idx + 1 }}</td><td>{{ item.goalTitle }}</td><td>{{ item.selfScore ?? '-' }}</td><td>{{ item.managerScore ?? '-' }}</td><td>{{ item.finalScore ?? '-' }}</td><td>{{ item.finalLevel || '-' }}</td><td>{{ perfStatus(item.status) }}</td></tr></tbody>
      </table>
      <div v-if="!goals.length" class="empty-state compact">暂无绩效目标</div>
    </section>

    <section v-if="section === 'payslips'" class="card">
      <h3>我的工资条</h3>
      <table class="table"><thead><tr><th>序号</th><th>月份</th><th>基本工资</th><th>应发</th><th>实发</th><th>绩效</th><th>状态</th></tr></thead>
        <tbody><tr v-for="(item, idx) in payslips" :key="item.id"><td>{{ idx + 1 }}</td><td>{{ item.cycleMonth }}</td><td>{{ money(item.baseSalary) }}</td><td>{{ money(item.grossSalary) }}</td><td>{{ money(item.netSalary) }}</td><td>{{ item.performanceLevel || '-' }}</td><td>{{ approvalText(item.status) }}</td></tr></tbody>
      </table>
      <div v-if="!payslips.length" class="empty-state compact">暂无工资条</div>
    </section>

    <section v-if="section === 'notifications'" class="card">
      <h3>我的通知</h3>
      <div v-if="notifications.length" class="activity-list">
        <div v-for="item in notifications" :key="item.id" class="activity-row">
          <div>
            <strong>{{ item.title }}</strong>
            <p>{{ item.content }}</p>
          </div>
          <button class="btn-ghost" type="button" :disabled="item.status === 'READ'" @click="readNotification(item.id)">
            {{ item.status === 'READ' ? '已读' : '标为已读' }}
          </button>
        </div>
      </div>
      <div v-else class="empty-state compact">暂无通知</div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { http } from '../api/http'

const route = useRoute()
const router = useRouter()
const tabs = [
  { key: 'profile', label: '我的档案' },
  { key: 'attendance', label: '我的考勤' },
  { key: 'leave', label: '我的请假' },
  { key: 'performance', label: '我的绩效' },
  { key: 'payslips', label: '我的工资条' },
  { key: 'notifications', label: '我的通知' }
]
const section = computed(() => tabs.some((tab) => tab.key === route.params.section) ? route.params.section : 'profile')
const error = ref('')
const profile = ref(null)
const attendance = ref([])
const todayAttendance = ref(null)
const leaves = ref([])
const goals = ref([])
const payslips = ref([])
const notifications = ref([])
const attendanceInfo = ref('')
const today = new Date().toISOString().slice(0, 10)
const correctionForm = ref({ workDate: today, checkIn: '', checkOut: '', reason: '' })
const overtimeForm = ref({ workDate: today, hours: '', reason: '' })

const loadProfile = async () => { profile.value = await http.get('/api/self/profile') }
const loadAttendance = async () => {
  const [todayData, rows] = await Promise.all([
    http.get('/api/self/attendance/today').catch(() => null),
    http.get('/api/self/attendance').catch(() => [])
  ])
  todayAttendance.value = todayData
  attendance.value = rows || []
}
const loadLeave = async () => { leaves.value = await http.get('/api/self/leave') || [] }
const loadGoals = async () => { goals.value = await http.get('/api/self/performance-goals') || [] }
const loadPayslips = async () => { payslips.value = await http.get('/api/self/payslips') || [] }
const loadNotifications = async () => { notifications.value = await http.get('/api/self/notifications') || [] }

const loaders = {
  profile: loadProfile,
  attendance: loadAttendance,
  leave: loadLeave,
  performance: loadGoals,
  payslips: loadPayslips,
  notifications: loadNotifications
}

const load = async () => {
  error.value = ''
  try {
    if (!route.params.section) router.replace('/self/profile')
    if (!profile.value) await loadProfile()
    await loaders[section.value]()
  } catch (e) {
    error.value = e.message
  }
}

const readNotification = async (id) => {
  await http.post(`/api/self/notifications/${id}/read`, {})
  await loadNotifications()
}

const clockIn = async () => {
  attendanceInfo.value = ''
  todayAttendance.value = await http.post('/api/self/attendance/clock-in', {})
  attendanceInfo.value = '上班打卡成功'
  await loadAttendance()
}

const clockOut = async () => {
  attendanceInfo.value = ''
  todayAttendance.value = await http.post('/api/self/attendance/clock-out', {})
  attendanceInfo.value = '下班打卡成功'
  await loadAttendance()
}

const submitCorrection = async () => {
  attendanceInfo.value = ''
  if (!correctionForm.value.workDate || (!correctionForm.value.checkIn && !correctionForm.value.checkOut)) {
    error.value = '请选择补卡日期，并至少填写签到或签退时间'
    return
  }
  await http.post('/api/self/attendance/corrections', {
    workDate: correctionForm.value.workDate,
    checkIn: correctionForm.value.checkIn || null,
    checkOut: correctionForm.value.checkOut || null,
    reason: correctionForm.value.reason || null
  })
  correctionForm.value = { workDate: today, checkIn: '', checkOut: '', reason: '' }
  attendanceInfo.value = '补卡申请已提交统一审批'
}

const submitOvertime = async () => {
  attendanceInfo.value = ''
  if (!overtimeForm.value.workDate || !overtimeForm.value.hours) {
    error.value = '请选择加班日期并填写加班小时'
    return
  }
  await http.post('/api/self/attendance/overtime', {
    workDate: overtimeForm.value.workDate,
    hours: Number(overtimeForm.value.hours),
    reason: overtimeForm.value.reason || null
  })
  overtimeForm.value = { workDate: today, hours: '', reason: '' }
  attendanceInfo.value = '加班申请已提交统一审批'
}

onMounted(load)
watch(() => route.params.section, load)

const fmt = (value) => value ? String(value).replace('T', ' ').slice(0, 16) : '-'
const money = (value) => value == null ? '-' : Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
const statusText = (value) => ({ ACTIVE: '在职', PROBATION: '试用', RESIGNED: '已离职', REGULARIZATION_PENDING: '转正待审', TRANSFER_PENDING: '调岗待审', RESIGNATION_PENDING: '离职待审' }[value] || value || '-')
const attendanceText = (value) => ({ NORMAL: '正常', LATE: '迟到', EARLY: '早退', ABSENT: '缺勤', LEAVE: '请假' }[value] || value || '-')
const approvalText = (value) => ({ PENDING: '待审批', PENDING_APPROVAL: '待审批', APPROVED: '已通过', REJECTED: '已驳回', PAID: '已发放' }[value] || value || '-')
const perfStatus = (value) => ({ GOAL_SETTING: '目标制定', SELF_REVIEW: '员工自评', MANAGER_REVIEW: '上级评审', CALIBRATING: '校准中', FINALIZED: '已定稿' }[value] || value || '-')
const leaveType = (value) => ({ ANNUAL: '年假', PERSONAL: '事假', SICK: '病假', ADJUST: '调休', MARRIAGE: '婚假', MATERNITY: '产假', FUNERAL: '丧假' }[value] || value || '-')
</script>
