<template>
  <div>
    <section class="grid grid-3">
      <div class="card">
        <div class="badge">在职员工</div>
        <h2>{{ stats.employees }}</h2>
        <div style="color:var(--muted)">共 {{ stats.departments }} 个部门</div>
      </div>
      <div class="card">
        <div class="badge" style="background:rgba(242,183,5,0.18);color:#a86d00">待审批</div>
        <h2>{{ stats.pending }}</h2>
        <div style="color:var(--muted)">请假 + 绩效待批</div>
      </div>
      <div class="card">
        <div class="badge" style="background:rgba(11,53,89,0.12);color:#0b3559">薪资记录</div>
        <h2>{{ stats.payrolls }}</h2>
        <div style="color:var(--muted)">本月薪酬记录</div>
      </div>
    </section>

    <section class="card" style="margin-top:22px">
      <h3 style="margin-top:0">最新动态</h3>
      <table class="table" v-if="activities.length">
        <thead>
          <tr>
            <th>事项</th>
            <th>类型</th>
            <th>操作人</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(act, i) in activities" :key="i">
            <td>{{ act.title }}</td>
            <td>{{ act.type }}</td>
            <td>{{ act.actor }}</td>
            <td>{{ act.status }}</td>
          </tr>
        </tbody>
      </table>
      <div v-else style="color:var(--muted);padding:20px;text-align:center">加载中...</div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { http } from '../api/http'

const stats = ref({ employees: 0, departments: 0, pending: 0, payrolls: 0 })
const activities = ref([])

onMounted(async () => {
  try {
    const [emps, depts, leaves, perfCycles] = await Promise.all([
      http.get('/api/employees').catch(() => []),
      http.get('/api/departments').catch(() => []),
      http.get('/api/leave').catch(() => []),
      http.get('/api/performance/cycles').catch(() => [])
    ])
    stats.value.employees = (emps || []).length
    stats.value.departments = (depts || []).length
    const leavePending = (leaves || []).filter(l => l.status === 'PENDING' || l.status === 'PENDING_L1' || l.status === 'PENDING_L2').length
    let reviewPending = 0
    if (perfCycles && perfCycles.length) {
      for (const cycle of perfCycles) {
        try {
          const reviews = await http.get(`/api/performance/cycles/${cycle.id}/reviews`).catch(() => [])
          reviewPending += (reviews || []).filter(r => r.approvalStatus === 'PENDING').length
        } catch {}
      }
    }
    stats.value.pending = leavePending + reviewPending

    const activities = []
    for (const emp of (emps || []).slice(0, 5)) {
      activities.push({
        title: `${emp.name} - ${emp.position || ''}`,
        type: emp.empNo,
        actor: 'HR',
        status: emp.status === 'ACTIVE' ? '在职' : '离职'
      })
    }
    activities.value = activities
  } catch {}
})
</script>
