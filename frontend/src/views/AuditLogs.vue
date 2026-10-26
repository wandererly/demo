<template>
  <div>
    <section class="card">
      <div class="actions" style="justify-content: space-between;">
        <h3 style="margin:0">审计日志</h3>
        <button class="btn-ghost" @click="load">刷新</button>
      </div>
      <input class="input" v-model="search" placeholder="搜索模块/动作/操作人" style="max-width:320px;margin-top:12px" />
      <div v-if="error" class="alert">{{ error }}</div>
    </section>

    <section class="card">
      <table class="table">
        <thead>
          <tr>
            <th>时间</th>
            <th>模块</th>
            <th>动作</th>
            <th>对象</th>
            <th>操作人</th>
            <th>详情</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in pagedList" :key="item.id">
            <td>{{ fmt(item.createdAt) }}</td>
            <td>{{ moduleLabel(item.module) }}</td>
            <td>{{ actionLabel(item.action) }}</td>
            <td>{{ targetLabel(item) }}</td>
            <td>{{ item.actor }}</td>
            <td>{{ detailLabel(item.detail) }}</td>
          </tr>
        </tbody>
      </table>
      <div class="actions" style="justify-content: space-between;">
        <div style="color:var(--muted)">共 {{ filtered.length }} 条</div>
        <div class="actions">
          <button class="btn-ghost" @click="prevPage" :disabled="page === 1">上一页</button>
          <div>第 {{ page }} / {{ totalPages }} 页</div>
          <button class="btn-ghost" @click="nextPage" :disabled="page === totalPages">下一页</button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { http } from '../api/http'

const logs = ref([])
const search = ref('')
const error = ref('')
const page = ref(1)
const pageSize = 10

const moduleLabel = (value) => ({
  ORGANIZATION: '组织架构',
  HR: '员工管理',
  ATTENDANCE: '考勤',
  LEAVE: '请假',
  PERFORMANCE: '绩效',
  PAYROLL: '薪酬',
  APPROVAL: '统一审批',
  RBAC: '权限管理',
  AUDIT: '审计日志'
}[value] || value || '-')

const actionLabel = (value) => ({
  CREATE: '新增',
  UPDATE: '更新',
  DELETE: '删除',
  SUBMIT: '提交',
  APPROVE: '审批',
  REJECT: '驳回',
  LOGIN: '登录',
  LOGOUT: '退出登录'
}[value] || value || '-')

const targetTypeLabel = (value) => ({
  employee: '员工',
  department: '部门',
  attendance_record: '考勤记录',
  attendance_correction: '考勤补卡',
  overtime_request: '加班申请',
  attendance_month_lock: '考勤月份锁定',
  attendance_monthly_summary: '考勤月度汇总',
  leave_request: '请假申请',
  leave_balance: '假期额度',
  employee_contract: '员工合同',
  employee_lifecycle_event: '员工异动',
  performance_cycle: '绩效周期',
  performance_indicator: '绩效指标',
  performance_review: '绩效评审',
  performance_goal: '绩效目标',
  payslip: '工资条',
  payroll_record: '薪酬记录',
  salary_change: '调薪申请',
  salary_structure: '薪资结构',
  job_level: '职级',
  job_position: '岗位',
  promotion_path: '晋升路径',
  work_shift: '班次',
  attendance_rule_config: '考勤规则',
  department_shift_binding: '部门班次绑定',
  employee_shift_binding: '员工班次绑定',
  holiday_calendar: '节假日',
  sys_user: '系统用户',
  role: '角色',
  permission: '权限'
}[value] || value || '-')

const statusLabel = (value) => ({
  PENDING: '待审批',
  PENDING_L1: '一级待审',
  PENDING_L2: '二级待审',
  PENDING_APPROVAL: '待审批',
  APPROVED: '已通过',
  REJECTED: '已驳回',
  ACTIVE: '启用',
  DISABLED: '停用',
  PAID: '已发放',
  ACKNOWLEDGED: '已签收',
  READ: '已读',
  UNREAD: '未读'
}[value] || value || '-')

const targetLabel = (item) => {
  const target = targetTypeLabel(item?.targetType)
  return item?.targetId == null ? target : `${target} #${item.targetId}`
}

const detailLabel = (value) => {
  if (!value) return '-'
  return String(value)
    .replace(/\bPENDING_L2\b/g, statusLabel('PENDING_L2'))
    .replace(/\bPENDING_L1\b/g, statusLabel('PENDING_L1'))
    .replace(/\bPENDING_APPROVAL\b/g, statusLabel('PENDING_APPROVAL'))
    .replace(/\bAPPROVED\b/g, statusLabel('APPROVED'))
    .replace(/\bREJECTED\b/g, statusLabel('REJECTED'))
    .replace(/\bPENDING\b/g, statusLabel('PENDING'))
    .replace(/\bACTIVE\b/g, statusLabel('ACTIVE'))
    .replace(/\bDISABLED\b/g, statusLabel('DISABLED'))
    .replace(/\bPAID\b/g, statusLabel('PAID'))
    .replace(/\bACKNOWLEDGED\b/g, statusLabel('ACKNOWLEDGED'))
    .replace(/\bstatus\s*=/gi, '状态=')
    .replace(/\bdecision\s*=/gi, '审批结果=')
}

const filtered = computed(() => {
  const key = search.value.trim().toLowerCase()
  if (!key) return logs.value
  return logs.value.filter((item) => `${item.module || ''}${moduleLabel(item.module)}${item.action || ''}${actionLabel(item.action)}${targetTypeLabel(item.targetType)}${item.actor || ''}${item.detail || ''}${detailLabel(item.detail)}`.toLowerCase().includes(key))
})
const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))
const pagedList = computed(() => filtered.value.slice((page.value - 1) * pageSize, page.value * pageSize))

const load = async () => {
  error.value = ''
  try {
    logs.value = await http.get('/api/audit-logs')
    page.value = 1
  } catch (e) {
    error.value = e.message
  }
}

const fmt = (value) => value ? String(value).replace('T', ' ').slice(0, 19) : '-'
const prevPage = () => { if (page.value > 1) page.value -= 1 }
const nextPage = () => { if (page.value < totalPages.value) page.value += 1 }

onMounted(load)
</script>
