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
      <h3>我的考勤</h3>
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
const leaves = ref([])
const goals = ref([])
const payslips = ref([])
const notifications = ref([])

const loadProfile = async () => { profile.value = await http.get('/api/self/profile') }
const loadAttendance = async () => { attendance.value = await http.get('/api/self/attendance') || [] }
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
