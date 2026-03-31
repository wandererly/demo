<template>
  <div>
    <section class="grid grid-3">
      <div class="card">
        <div class="badge">在职员工</div>
        <h2>216</h2>
        <div style="color:var(--muted)">出勤率 96.4%</div>
      </div>
      <div class="card">
        <div class="badge" style="background:rgba(242,183,5,0.18);color:#a86d00">待审批</div>
        <h2>12</h2>
        <div style="color:var(--muted)">本周待批</div>
      </div>
      <div class="card">
        <div class="badge" style="background:rgba(11,53,89,0.12);color:#0b3559">满意度</div>
        <h2>88%</h2>
        <div style="color:var(--muted)">季度员工反馈</div>
      </div>
    </section>

    <section class="card" style="margin-top:22px">
      <h3 style="margin-top:0">最新动态</h3>
      <table class="table">
        <thead>
          <tr>
            <th>事项</th>
            <th>类型</th>
            <th>负责人</th>
            <th>状态</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>招聘 - 研发</td>
            <td>招聘</td>
            <td>张敏</td>
            <td>进行中</td>
          </tr>
          <tr>
            <td>培训 - 新人</td>
            <td>培训</td>
            <td>李雷</td>
            <td>待处理</td>
          </tr>
          <tr>
            <td>绩效周报</td>
            <td>绩效</td>
            <td>HR</td>
            <td>已完成</td>
          </tr>
        </tbody>
      </table>
    </section>
  </div>
</template>
