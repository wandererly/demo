const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, WidthType, BorderStyle, ShadingType
} = require('docx');
const fs = require('fs');

function heading(text, level = 1) {
  return new Paragraph({
    text,
    heading: `Heading${level}`,
    spacing: { before: level === 1 ? 400 : 300, after: 200 },
    border: level <= 2 ? { bottom: { color: '2b334b', size: 6, space: 4, style: 'single' } } : undefined,
    run: level <= 2 ? { color: '2b334b', bold: true, size: level === 1 ? 36 : 28 } : undefined,
  });
}

function subHeading(text) {
  return heading(text, 3);
}

function para(text, opts = {}) {
  return new Paragraph({
    children: [new TextRun({ text, ...opts })],
    spacing: { after: 120, line: 360 },
  });
}

function codePara(text) {
  return new Paragraph({
    children: [new TextRun({ text, font: 'Consolas', size: 20, color: '333333' })],
    spacing: { after: 80, line: 300 },
    shading: { type: ShadingType.SOLID, color: 'f5f3ec' },
    indent: { left: 400 },
  });
}

function bullet(text, level = 0) {
  return new Paragraph({
    children: [new TextRun({ text: `• ${text}`, size: 21 })],
    spacing: { after: 60 },
    indent: { left: 600 + level * 400 },
  });
}

function numbered(text, num) {
  return new Paragraph({
    children: [new TextRun({ text: `${num}. ${text}`, size: 21 })],
    spacing: { after: 80 },
    indent: { left: 600 },
  });
}

function table(headers, rows) {
  const headerRow = new TableRow({
    tableHeader: true,
    children: headers.map(h =>
      new TableCell({
        children: [new Paragraph({
          children: [new TextRun({ text: h, bold: true, color: 'ffffff', size: 20 })],
          alignment: AlignmentType.CENTER,
        })],
        shading: { type: ShadingType.SOLID, color: '2b334b' },
        width: { size: 9020 / headers.length, type: WidthType.DXA },
      })
    ),
  });

  const dataRows = rows.map(row =>
    new TableRow({
      children: row.map(cell =>
        new TableCell({
          children: [new Paragraph({
            children: [new TextRun({ text: String(cell), size: 19 })],
          })],
          width: { size: 9020 / headers.length, type: WidthType.DXA },
          shading: { type: ShadingType.SOLID, color: 'fafaf9' },
        })
      ),
    })
  );

  return new Table({
    rows: [headerRow, ...dataRows],
    width: { size: 9020, type: WidthType.DXA },
  });
}

function cell(text, opts = {}) {
  return new TableCell({
    children: [new Paragraph({
      children: [new TextRun({ text, size: 19, ...opts })],
    })],
    width: opts.width ? { size: opts.width, type: WidthType.DXA } : undefined,
  });
}

function minitable(headers, rows) {
  return new Table({
    rows: [
      new TableRow({
        tableHeader: true,
        children: headers.map(h => cell(h, { bold: true, font: 'Consolas' })),
      }),
      ...rows.map(r => new TableRow({ children: r.map(c => cell(c, { font: 'Consolas' })) })),
    ],
    width: { size: 9020, type: WidthType.DXA },
  });
}

const doc = new Document({
  styles: {
    default: {
      document: {
        run: { font: 'Microsoft YaHei', size: 21 },
      },
    },
  },
  sections: [{
    properties: {},
    children: [

      // ==================== 封面 ====================
      new Paragraph({ spacing: { before: 3000 } }),
      new Paragraph({
        children: [new TextRun({ text: 'HRM 人力资源管理系统', size: 52, bold: true, color: '2b334b' })],
        alignment: AlignmentType.CENTER,
      }),
      new Paragraph({
        children: [new TextRun({ text: '开发文档', size: 40, color: '5a6a8a' })],
        alignment: AlignmentType.CENTER,
        spacing: { after: 200 },
      }),
      new Paragraph({
        children: [new TextRun({ text: '版本 0.1.0-SNAPSHOT | 2026-05', size: 24, color: '888888' })],
        alignment: AlignmentType.CENTER,
        spacing: { after: 600 },
      }),
      new Paragraph({
        children: [new TextRun({ text: '基于 Spring Boot 4.0 + Vue 3 的企业人事一体化管理系统', size: 22, color: '666666' })],
        alignment: AlignmentType.CENTER,
      }),

      // ==================== 目录提示 ====================
      new Paragraph({ spacing: { before: 2000 } }),
      new Paragraph({
        children: [new TextRun({ text: '【请在 Word 中插入目录：引用 → 目录 → 自动目录】', size: 20, color: '999999', italics: true })],
        alignment: AlignmentType.CENTER,
      }),

      // ==================== 分页 ====================
      new Paragraph({ pageBreakBefore: true }),

      // ==================== 1. 项目概述 ====================
      heading('1. 项目概述', 1),

      subHeading('1.1 项目简介'),
      para('HRM（Human Resource Management）是一个基于 Spring Boot 4.0 的企业人事一体化管理系统，旨在为企业提供完整的人力资源数字化管理解决方案。系统覆盖员工管理、部门管理、考勤管理、请假管理、绩效管理、薪酬管理、权限控制以及组织架构等核心人事业务模块。'),
      para('系统采用前后端分离架构，后端提供 RESTful API 接口，前端基于 Vue 3 构建单页应用（SPA），使用 JWT 进行无状态认证，通过 RBAC 模型实现细粒度的权限控制。'),

      subHeading('1.2 技术栈'),
      table(
        ['层级', '技术', '版本/说明'],
        [
          ['后端框架', 'Spring Boot', '4.0.3'],
          ['安全框架', 'Spring Security', '集成方法级权限控制'],
          ['ORM 框架', 'MyBatis', '3.0.3（注解式 SQL）'],
          ['数据库', 'MySQL', '8+'],
          ['认证方案', 'JWT（jjwt）', '0.11.5，HMAC-SHA256 签名'],
          ['密码加密', 'BCrypt', 'Spring Security 内置'],
          ['前端框架', 'Vue 3', 'Composition API + Vue Router'],
          ['构建工具', 'Vite', '前端开发与打包'],
          ['后端构建', 'Maven', '3.8+'],
          ['Java 版本', 'JDK', '17'],
          ['开发语言', 'Java + JavaScript', '后端 Java，前端 ES Module'],
        ]
      ),

      subHeading('1.3 项目结构'),
      codePara('hrm/'),
      codePara('├── pom.xml                          # Maven 配置文件'),
      codePara('├── hrm.sql                          # 数据库 DDL 脚本'),
      codePara('├── README.md                        # 项目说明'),
      codePara('├── docs/                            # 文档目录'),
      codePara('├── frontend/                        # 前端源码'),
      codePara('│   ├── index.html                  # SPA 入口 HTML'),
      codePara('│   └── src/'),
      codePara('│       ├── main.js                 # Vue 应用入口'),
      codePara('│       ├── App.vue                 # 根组件'),
      codePara('│       ├── auth.js                # 认证工具（Token/用户存储）'),
      codePara('│       ├── router/index.js         # 前端路由与权限守卫'),
      codePara('│       ├── api/http.js             # Axios 封装与拦截器'),
      codePara('│       ├── components/             # 通用组件（Sidebar/Topbar）'),
      codePara('│       └── views/                  # 页面视图（11 个页面）'),
      codePara('├── scripts/                        # 脚本目录'),
      codePara('└── src/main/'),
      codePara('    ├── java/com/hrm/'),
      codePara('    │   ├── HrmApplication.java     # Spring Boot 启动类'),
      codePara('    │   ├── auth/                   # 认证模块（登录/注册/验证码/防爆破）'),
      codePara('    │   ├── common/                 # 公共模块（API响应/异常/错误码）'),
      codePara('    │   ├── config/                 # 配置类（MVC/MyBatis）'),
      codePara('    │   ├── controller/             # 控制器层（11 个 Controller）'),
      codePara('    │   ├── domain/                 # 领域模型（17 个实体）'),
      codePara('    │   ├── dto/                    # 数据传输对象（约 25 个 DTO）'),
      codePara('    │   ├── mapper/                 # MyBatis Mapper 接口（约 20 个）'),
      codePara('    │   ├── security/               # 安全模块（JWT/Spring Security 配置）'),
      codePara('    │   ├── service/                # 服务接口（约 12 个 Service）'),
      codePara('    │   └── demo/                   # 演示数据初始化'),
      codePara('    └── resources/'),
      codePara('        ├── application.yml         # 应用配置'),
      codePara('        └── static/                 # 前端构建产物（部署用）'),

      // ==================== 分页 ====================
      new Paragraph({ pageBreakBefore: true }),

      // ==================== 2. 核心功能模块 ====================
      heading('2. 核心功能模块', 1),

      subHeading('2.1 模块总览'),
      table(
        ['模块', 'Controller', '权限标识', '核心功能'],
        [
          ['员工管理', 'EmployeeController', 'hr:manage', '员工增删改查、按部门筛选'],
          ['部门管理', 'DepartmentController', 'hr:manage', '部门树形结构、设置负责人'],
          ['考勤管理', 'AttendanceController', 'attendance:manage', '打卡记录录入/更新/查询'],
          ['请假管理', 'LeaveController', 'leave:manage', '请假申请/审批流程'],
          ['绩效管理', 'PerformanceController', 'perf:manage', '考核周期/指标/评审/审批'],
          ['薪酬管理', 'PayrollController', 'payroll:manage', '薪资计算/生成/查询'],
          ['薪资结构', 'SalaryStructureController', 'payroll:manage', '薪资构成配置'],
          ['权限管理', 'RbacController', 'rbac:manage', '用户/角色管理、角色分配'],
          ['权限定义', 'PermissionController', 'rbac:manage', '权限项 CRUD、角色赋权'],
          ['规则配置', 'RuleController', 'config:manage', '请假/绩效/薪酬/税率规则'],
          ['组织架构', 'OrgController', 'hr:manage', '职级/岗位/晋升路径'],
        ]
      ),

      subHeading('2.2 员工管理'),
      para('提供员工信息的全生命周期管理，包括员工编号、姓名、性别、联系方式、所属部门、岗位、入职日期、基本薪资等字段。支持状态管理（ACTIVE/INACTIVE），与部门、薪酬结构、考勤、绩效等模块深度关联。'),
      bullet('API 路径：/api/employees'),
      bullet('CRUD 操作：GET 列表/详情、POST 新增、PUT 编辑、DELETE 删除'),
      bullet('参数校验：使用 Jakarta Validation 对必填字段进行校验'),

      subHeading('2.3 部门管理'),
      para('支持树形部门结构的维护，部门可设置父部门（parent_id）形成层级关系，支持指定部门负责人（manager_id），状态管理支持启用/停用。'),
      bullet('API 路径：/api/departments'),
      bullet('支持部门树形展示和层级管理'),

      subHeading('2.4 考勤管理'),
      para('管理员工日常打卡记录，包含签到时间、签退时间、工作日期、考勤状态（NORMAL/LATE/EARLY/ABSENT）。'),
      bullet('API 路径：/api/attendance'),
      bullet('支持按员工和日期维度查询'),

      subHeading('2.5 请假管理'),
      para('完整的请假申请与审批流程。员工提交请假申请（类型/起止时间/天数/原因），审批人进行审核（PENDING/APPROVED/REJECTED）。系统根据请假规则校验单次请假天数上限。'),
      bullet('API 路径：/api/leave'),
      bullet('请假类型：年假、事假、病假等'),
      bullet('审批流程：提交 → 待审批 → 批准/拒绝'),

      subHeading('2.6 绩效管理'),
      para('覆盖绩效考核的完整流程：创建考核周期 → 定义考核指标（含权重） → 对员工进行评审打分 → 确定绩效等级 → 审批确认。绩效等级依据绩效规则自动映射。'),
      bullet('API 路径：/api/performance'),
      bullet('子模块：考核周期(cycles)、指标(indicators)、评审(reviews)'),
      bullet('评审状态流转：PENDING → 评分 → 审批(APPROVED/REJECTED)'),

      subHeading('2.7 薪酬管理'),
      para('提供完整的薪资计算引擎：基于薪资结构（基本工资 + 津贴 + 奖金 - 社保 - 公积金），结合考勤和请假数据，按累进税率计算个人所得税，最终生成员工薪酬记录。'),
      bullet('API 路径：/api/payroll + /api/salary'),
      bullet('薪资计算：POST /api/payroll/calc — 预览计算明细'),
      bullet('薪资生成：POST /api/payroll/generate — 生成正式薪酬记录'),
      bullet('薪资结构：每个员工独立配置薪资构成项目'),

      // ==================== 分页 ====================
      new Paragraph({ pageBreakBefore: true }),

      // ==================== 3. 系统架构 ====================
      heading('3. 系统架构', 1),

      subHeading('3.1 整体架构'),
      para('系统采用经典的三层架构模式，遵循关注点分离原则：'),
      bullet('Controller 层：接收 HTTP 请求，参数校验，调用 Service，返回统一 ApiResponse'),
      bullet('Service 层：业务逻辑处理，事务管理，调用 Mapper 层'),
      bullet('Mapper 层：数据访问层，使用 MyBatis 注解执行 SQL'),

      para(''),
      para('请求处理流程：'),
      numbered('客户端发送 HTTP 请求（携带 JWT Token）', 1),
      numbered('JwtAuthFilter 拦截验证 Token 有效性，解析用户身份和权限', 2),
      numbered('Spring Security 根据 @PreAuthorize 注解进行权限校验', 3),
      numbered('Controller 接收请求，进行参数校验（@Valid）', 4),
      numbered('Service 执行业务逻辑，Mapper 完成数据库操作', 5),
      numbered('统一返回 ApiResponse(code + message + data) 格式', 6),
      numbered('GlobalExceptionHandler 统一捕获处理异常', 7),

      subHeading('3.2 安全架构'),
      para('系统安全性基于以下多层机制：'),
      bullet('认证层：JWT 无状态 Token，默认 120 分钟过期，支持"记住我"功能'),
      bullet('授权层：RBAC 模型 — 用户 → 角色 → 权限，方法级 @PreAuthorize 注解控制'),
      bullet('防攻击：登录失败计数（5 次锁定 10 分钟）、图形验证码、BCrypt 密码加密'),
      bullet('CORS：允许跨域请求（开发友好），生产环境建议限定域名'),

      para(''),
      para('权限体系设计（6 个权限标识）：'),
      table(
        ['权限标识', '说明', '关联模块'],
        [
          ['hr:manage', '人力资源管理', '员工、部门、组织架构'],
          ['attendance:manage', '考勤管理', '打卡记录'],
          ['leave:manage', '请假管理', '请假申请与审批'],
          ['perf:manage', '绩效管理', '考核周期、指标、评审'],
          ['payroll:manage', '薪酬管理', '薪资计算与结构配置'],
          ['rbac:manage', '权限管理', '用户、角色管理'],
          ['config:manage', '系统配置', '规则配置、演示数据初始化'],
        ]
      ),

      subHeading('3.3 数据库设计'),
      para('数据库共 17 张业务表，采用 InnoDB 引擎，utf8mb4 字符集。'),

      table(
        ['表名', '说明', '关键字段'],
        [
          ['department', '部门表', 'parent_id（树形结构）'],
          ['employee', '员工表', 'emp_no（工号唯一）、dept_id'],
          ['attendance_record', '考勤记录', 'emp_id、work_date、check_in/out'],
          ['leave_request', '请假申请', 'emp_id、leave_type、approver_id'],
          ['performance_cycle', '绩效周期', 'start_date、end_date、status'],
          ['performance_indicator', '绩效指标', 'cycle_id、name、weight'],
          ['performance_review', '绩效评审', 'emp_id、cycle_id、score、level'],
          ['salary_structure', '薪资结构', 'emp_id（唯一）、base_salary等'],
          ['payroll_record', '薪酬记录', 'emp_id、cycle_month、gross/net'],
          ['payroll_item', '薪酬明细项', 'payroll_id、item_type、amount'],
          ['sys_user', '系统用户', 'username、password_hash、status'],
          ['sys_role', '系统角色', 'role_name、role_key'],
          ['sys_user_role', '用户角色关联', 'user_id、role_id'],
          ['permission', '权限定义', 'perm_key、perm_name'],
          ['role_permission', '角色权限关联', 'role_id、perm_id'],
          ['leave_rule', '请假规则', 'max_days_per_request'],
          ['performance_rule', '绩效规则', 'min_score、max_score、level'],
          ['payroll_rule', '薪酬规则', 'overtime_multiplier'],
          ['payroll_tax_bracket', '税率区间', 'min_income、max_income、rate'],
          ['job_level', '职级表', 'level_code、rank、salary范围'],
          ['job_position', '岗位表', 'dept_id、level_id、path_group'],
          ['promotion_path', '晋升路径', 'from/to_position_id、min_months'],
        ]
      ),

      // ==================== 分页 ====================
      new Paragraph({ pageBreakBefore: true }),

      // ==================== 4. API 接口文档 ====================
      heading('4. API 接口文档', 1),

      subHeading('4.1 通用规范'),
      para('所有 API 均返回统一格式：'),
      codePara('{ "code": 0, "message": "OK", "data": ... }'),
      para('错误码定义：'),
      table(
        ['错误码', '含义'],
        [
          ['0', '成功'],
          ['40000', '请求参数错误'],
          ['40300', '权限不足'],
          ['40400', '资源不存在'],
          ['50000', '服务器内部错误'],
        ]
      ),

      subHeading('4.2 认证接口'),
      table(
        ['方法', '路径', '说明', '鉴权'],
        [
          ['POST', '/api/auth/login', '用户登录（需验证码）', '无'],
          ['POST', '/api/auth/register', '用户注册', '无'],
          ['GET', '/api/auth/captcha', '获取图形验证码', '无'],
          ['GET', '/api/auth/me', '获取当前用户信息', 'JWT'],
        ]
      ),
      para('登录请求体：{ username, password, captchaId, captchaCode, remember }'),
      para('登录响应：{ token, username, role, permissions }'),

      subHeading('4.3 员工管理接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET', '/api/employees', '员工列表', 'hr:manage'],
          ['GET', '/api/employees/{id}', '员工详情', 'hr:manage'],
          ['POST', '/api/employees', '新增员工', 'hr:manage'],
          ['PUT', '/api/employees/{id}', '编辑员工', 'hr:manage'],
          ['DELETE', '/api/employees/{id}', '删除员工', 'hr:manage'],
        ]
      ),

      subHeading('4.4 部门管理接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET', '/api/departments', '部门列表', 'hr:manage'],
          ['GET', '/api/departments/{id}', '部门详情', 'hr:manage'],
          ['POST', '/api/departments', '新增部门', 'hr:manage'],
          ['PUT', '/api/departments/{id}', '编辑部门', 'hr:manage'],
          ['DELETE', '/api/departments/{id}', '删除部门', 'hr:manage'],
        ]
      ),

      subHeading('4.5 考勤管理接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET', '/api/attendance', '考勤列表', 'attendance:manage'],
          ['GET', '/api/attendance/{id}', '考勤详情', 'attendance:manage'],
          ['POST', '/api/attendance', '录入考勤', 'attendance:manage'],
          ['PUT', '/api/attendance/{id}', '更新考勤', 'attendance:manage'],
          ['DELETE', '/api/attendance/{id}', '删除考勤', 'attendance:manage'],
        ]
      ),

      subHeading('4.6 请假管理接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET', '/api/leave', '请假列表', 'leave:manage'],
          ['GET', '/api/leave/{id}', '请假详情', 'leave:manage'],
          ['POST', '/api/leave', '提交请假', 'leave:manage'],
          ['PUT', '/api/leave/{id}', '编辑请假', 'leave:manage'],
          ['PUT', '/api/leave/{id}/approve', '审批请假', 'leave:manage'],
          ['DELETE', '/api/leave/{id}', '删除请假', 'leave:manage'],
        ]
      ),

      subHeading('4.7 绩效管理接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET', '/api/performance/cycles', '考核周期列表', 'perf:manage'],
          ['POST', '/api/performance/cycles', '创建周期', 'perf:manage'],
          ['PUT', '/api/performance/cycles/{id}', '更新周期', 'perf:manage'],
          ['DELETE', '/api/performance/cycles/{id}', '删除周期', 'perf:manage'],
          ['GET', '/api/performance/cycles/{id}/indicators', '指标列表', 'perf:manage'],
          ['POST', '/api/performance/indicators', '创建指标', 'perf:manage'],
          ['PUT', '/api/performance/indicators/{id}', '更新指标', 'perf:manage'],
          ['DELETE', '/api/performance/indicators/{id}', '删除指标', 'perf:manage'],
          ['GET', '/api/performance/cycles/{id}/reviews', '评审列表', 'perf:manage'],
          ['POST', '/api/performance/reviews', '创建评审', 'perf:manage'],
          ['PUT', '/api/performance/reviews/{id}', '更新评审', 'perf:manage'],
          ['POST', '/api/performance/reviews/{id}/approve', '审批评审', 'perf:manage'],
        ]
      ),

      subHeading('4.8 薪酬管理接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['POST', '/api/payroll/calc', '薪资计算预览', 'payroll:manage'],
          ['POST', '/api/payroll/generate', '生成薪酬记录', 'payroll:manage'],
          ['GET', '/api/payroll/employee/{empId}', '按员工查询薪酬', 'payroll:manage'],
          ['GET', '/api/salary/structure/{empId}', '查询薪资结构', 'payroll:manage'],
          ['POST', '/api/salary/structure', '设置薪资结构', 'payroll:manage'],
        ]
      ),

      subHeading('4.9 权限管理接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET', '/api/rbac/users', '用户列表', 'rbac:manage'],
          ['POST', '/api/rbac/users', '创建用户', 'rbac:manage'],
          ['PUT', '/api/rbac/users/{id}', '更新用户', 'rbac:manage'],
          ['DELETE', '/api/rbac/users/{id}', '删除用户', 'rbac:manage'],
          ['GET', '/api/rbac/roles', '角色列表', 'rbac:manage'],
          ['POST', '/api/rbac/roles', '创建角色', 'rbac:manage'],
          ['PUT', '/api/rbac/roles/{id}', '更新角色', 'rbac:manage'],
          ['DELETE', '/api/rbac/roles/{id}', '删除角色', 'rbac:manage'],
          ['POST', '/api/rbac/assign', '分配角色给用户', 'rbac:manage'],
          ['GET', '/api/permissions', '权限列表', 'rbac:manage'],
          ['POST', '/api/permissions', '创建权限', 'rbac:manage'],
          ['PUT', '/api/permissions/{id}', '更新权限', 'rbac:manage'],
          ['DELETE', '/api/permissions/{id}', '删除权限', 'rbac:manage'],
          ['POST', '/api/permissions/assign', '授权给角色', 'rbac:manage'],
        ]
      ),

      subHeading('4.10 规则配置接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET/POST', '/api/rules/leave', '请假规则', 'config:manage'],
          ['GET', '/api/rules/performance', '绩效规则列表', 'config:manage'],
          ['POST', '/api/rules/performance', '创建绩效规则', 'config:manage'],
          ['DELETE', '/api/rules/performance/{id}', '删除绩效规则', 'config:manage'],
          ['GET/POST', '/api/rules/payroll', '薪酬规则', 'config:manage'],
          ['GET', '/api/rules/payroll/{id}/brackets', '税率区间列表', 'config:manage'],
          ['POST', '/api/rules/payroll/{id}/brackets', '创建税率区间', 'config:manage'],
          ['DELETE', '/api/rules/payroll/brackets/{id}', '删除税率区间', 'config:manage'],
        ]
      ),

      subHeading('4.11 组织架构接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['GET', '/api/org/levels', '职级列表', 'hr:manage'],
          ['POST', '/api/org/levels', '创建职级', 'hr:manage'],
          ['GET', '/api/org/positions', '岗位列表', 'hr:manage'],
          ['POST', '/api/org/positions', '创建岗位', 'hr:manage'],
          ['GET', '/api/org/paths', '晋升路径列表', 'hr:manage'],
          ['POST', '/api/org/paths', '创建晋升路径', 'hr:manage'],
        ]
      ),

      subHeading('4.12 演示数据接口'),
      table(
        ['方法', '路径', '说明', '权限'],
        [
          ['POST', '/api/demo/init', '一键初始化演示数据', 'config:manage'],
        ]
      ),

      // ==================== 分页 ====================
      new Paragraph({ pageBreakBefore: true }),

      // ==================== 5. 前端架构 ====================
      heading('5. 前端架构', 1),

      subHeading('5.1 技术选型'),
      bullet('Vue 3（Composition API）— 渐进式 JavaScript 框架'),
      bullet('Vue Router 4 — SPA 路由管理'),
      bullet('Vite — 开发服务器与构建工具'),
      bullet('原生 CSS — 无第三方 UI 库依赖'),

      subHeading('5.2 路由设计'),
      para('前端路由与权限标识绑定，通过路由守卫（beforeEach）实现访问控制：'),
      table(
        ['路由路径', '视图组件', '权限要求', '说明'],
        [
          ['/login', 'Login.vue', '无', '登录/注册页（无布局）'],
          ['/', 'Dashboard.vue', '登录即可', '首页仪表盘'],
          ['/departments', 'Departments.vue', 'hr:manage', '部门管理'],
          ['/employees', 'Employees.vue', 'hr:manage', '员工管理'],
          ['/attendance', 'Attendance.vue', 'attendance:manage', '考勤管理'],
          ['/leave', 'Leave.vue', 'leave:manage', '请假管理'],
          ['/performance', 'Performance.vue', 'perf:manage', '绩效管理'],
          ['/payroll', 'Payroll.vue', 'payroll:manage', '薪酬管理'],
          ['/rbac', 'Rbac.vue', 'rbac:manage', '权限管理'],
          ['/settings', 'Settings.vue', 'config:manage', '系统设置'],
        ]
      ),

      subHeading('5.3 认证流程'),
      numbered('用户访问任何非 /login 路径，路由守卫检查 LocalStorage/SessionStorage 中是否存在 hrm_token', 1),
      numbered('Token 不存在 → 重定向到 /login', 2),
      numbered('Token 存在但用户无对应权限 → 重定向到首页 /', 3),
      numbered('Axios 请求拦截器自动附加 Authorization: Bearer <token> 头', 4),
      numbered('响应拦截器检测 401 状态 → 清除 Token 并跳转登录页', 5),

      subHeading('5.4 组件结构'),
      bullet('Sidebar.vue — 侧边栏导航（根据权限动态显示菜单项）'),
      bullet('Topbar.vue — 顶部栏（用户信息、退出登录）'),
      bullet('App.vue — 根组件（布局切换：认证布局 vs 管理布局）'),

      // ==================== 分页 ====================
      new Paragraph({ pageBreakBefore: true }),

      // ==================== 6. 安全机制 ====================
      heading('6. 安全机制', 1),

      subHeading('6.1 认证流程'),
      numbered('用户访问 /api/auth/captcha 获取图形验证码（Base64 PNG图片 + captchaId）', 1),
      numbered('用户提交 username + password + captchaId + captchaCode 到 /api/auth/login', 2),
      numbered('系统校验验证码 → 校验账号密码（BCrypt） → 检查账号锁定状态', 3),
      numbered('认证成功 → 查询用户角色和权限列表 → 生成 JWT Token（含 username/role/permissions）', 4),
      numbered('前端存储 Token 和用户信息到 LocalStorage/SessionStorage', 5),
      numbered('后续请求携带 Authorization: Bearer <token>', 6),

      subHeading('6.2 防暴力破解'),
      bullet('基于内存的登录失败计数（ConcurrentHashMap）'),
      bullet('同一用户名/IP 连续失败 5 次 → 锁定 10 分钟'),
      bullet('锁定期间返回剩余锁定秒数'),
      bullet('登录成功后自动清除失败计数'),

      subHeading('6.3 密码安全'),
      bullet('使用 BCryptPasswordEncoder 加密存储密码'),
      bullet('不可逆哈希，防止密码泄露后被还原'),

      subHeading('6.4 权限控制'),
      bullet('方法级权限控制：@PreAuthorize("hasAuthority(\'hr:manage\')")'),
      bullet('JWT Token 中嵌入用户权限列表（perms claim）'),
      bullet('JwtAuthFilter 从 Token 解析权限并设置 SecurityContext'),
      bullet('前端路由守卫根据权限标识控制页面可见性'),

      // ==================== 7. 配置与部署 ====================
      heading('7. 配置与部署', 1),

      subHeading('7.1 环境要求'),
      table(
        ['组件', '版本要求'],
        [
          ['JDK', '17+'],
          ['Maven', '3.8+'],
          ['MySQL', '8.0+'],
          ['Node.js', '18+（仅前端开发）'],
        ]
      ),

      subHeading('7.2 数据库配置'),
      para('修改 src/main/resources/application.yml：'),
      codePara('spring:'),
      codePara('  datasource:'),
      codePara('    url: jdbc:mysql://localhost:3306/hrm?...'),
      codePara('    username: your_username'),
      codePara('    password: your_password'),

      subHeading('7.3 JWT 配置'),
      codePara('security:'),
      codePara('  jwt:'),
      codePara('    secret: "your-32-byte-secret-key-here!!"'),
      codePara('    expire-minutes: 120'),

      subHeading('7.4 构建与运行'),
      para('方式一：IDE 直接运行 HrmApplication.java'),
      para('方式二：命令行构建运行：'),
      codePara('mvn -q -DskipTests package'),
      codePara('java -jar target/demo-*.jar'),

      para(''),
      para('方式三：前端开发模式（前端热更新）：'),
      codePara('cd frontend'),
      codePara('npm install'),
      codePara('npm run dev'),

      subHeading('7.5 启动后访问'),
      bullet('首页：http://localhost:8080/'),
      bullet('登录页：http://localhost:8080/login'),
      bullet('系统启动时自动初始化演示数据（DemoDataInitializer）'),

      // ==================== 分页 ====================
      new Paragraph({ pageBreakBefore: true }),

      // ==================== 8. 数据初始化 ====================
      heading('8. 演示数据', 1),

      para('系统通过 DemoDataInitializer（CommandLineRunner）在启动时自动初始化演示数据。初始化逻辑包含：'),

      subHeading('8.1 初始化内容'),
      table(
        ['类别', '数据项', '数量'],
        [
          ['部门', '人力资源部、财务部、技术研发部、行政部、市场部', '5 个'],
          ['员工', '含工号/姓名/部门/岗位/薪资', '8 人'],
          ['职级', 'L1 初级 ～ L6 总监', '6 级'],
          ['岗位', '按部门分配、含编码与路径分组', '12 个'],
          ['晋升路径', '技术/HR/市场/财务等晋升方向', '7 条'],
          ['请假规则', '单次最多 5 天、一级审批', '1 条'],
          ['绩效规则', 'A级 90-100、B级 75-89、C级 0-74', '3 条'],
          ['薪酬规则', '加班系数 1.5x', '1 条'],
          ['税率区间', '3%/10%/20%/30% 四档累进', '4 档'],
          ['考勤记录', '最近 5 天正常打卡', '25 条'],
          ['请假记录', '年假已审批', '3 条'],
          ['绩效周期', '2026 Q1 绩效考核', '1 个'],
          ['绩效指标', '目标达成/工作质量/协作沟通/创新贡献', '4 项'],
          ['绩效评审', '员工评审已审批', '4 条'],
          ['薪资结构', '基本工资+津贴+奖金-社保-公积金', '8 人'],
          ['薪酬记录', '当月薪资已计算生成', '8 条'],
        ]
      ),

      subHeading('8.2 手动初始化'),
      para('也可在系统设置页面通过"一键初始化演示数据"按钮手动触发（POST /api/demo/init，需 config:manage 权限）。'),
      para('初始化逻辑使用幂等设计：先检查数据是否存在，已存在则跳过，可安全重复执行。'),

      // ==================== 9. 扩展开发指南 ====================
      heading('9. 扩展开发指南', 1),

      subHeading('9.1 添加新模块'),
      numbered('在 domain/ 下创建实体类（使用 Lombok @Data）', 1),
      numbered('在 hrm.sql 中添加对应的 DDL（或手动建表）', 2),
      numbered('在 mapper/ 下创建 Mapper 接口（使用 @Select/@Insert/@Update/@Delete 注解）', 3),
      numbered('在 dto/ 下创建请求 DTO（使用 @Valid 校验注解）', 4),
      numbered('在 service/ 下创建 Service 接口及实现类', 5),
      numbered('在 controller/ 下创建 Controller（添加 @PreAuthorize 权限注解）', 6),
      numbered('在 frontend/src/views/ 下添加 Vue 组件', 7),
      numbered('在 router/index.js 中添加路由配置', 8),

      subHeading('9.2 添加新权限'),
      numbered('在 permission 表中插入新权限记录（perm_key + perm_name）', 1),
      numbered('在角色-权限关联表中授权给相应角色', 2),
      numbered('在 Controller 方法上添加 @PreAuthorize("hasAuthority(\'new:perm\')")', 3),
      numbered('在前端路由中添加对应的 perms 元数据', 4),

      subHeading('9.3 开发注意事项'),
      bullet('所有 API 返回值使用 ApiResponse<T> 包装，不要直接返回裸对象'),
      bullet('Service 层抛出 BizException 由全局异常处理器统一处理'),
      bullet('敏感配置（数据库密码、JWT Secret）不要硬编码，使用 application.yml 配置'),
      bullet('修改 JWT Secret 后，所有已签发的 Token 将立即失效'),
      bullet('前端 Token 存储在 LocalStorage 中，清除后可重新登录'),

      // ==================== 10. 常见问题 ====================
      heading('10. 常见问题排查', 1),

      table(
        ['问题', '原因', '解决方案'],
        [
          ['接口返回 403', 'Token 过期或权限不足', '清除浏览器 hrm_token 重新登录'],
          ['启动报 SQL 错误', '数据库表缺失', '执行 hrm.sql 建表；检查 DemoDataInitializer'],
          ['Maven 构建失败', 'JAVA_HOME 未配置', '在系统环境变量中设置 JAVA_HOME'],
          ['前端页面空白', '静态资源未构建', '运行 npm run build 或使用 npm run dev'],
          ['验证码不显示', 'CaptchaService 异常', '检查服务端日志；确认请求正确'],
          ['登录后跳转首页', '无对应模块权限', '联系管理员在 RBAC 模块分配权限'],
          ['跨域请求失败', 'CORS 配置', '检查 SecurityConfig 中 CORS 配置'],
        ]
      ),

      new Paragraph({ spacing: { before: 600 } }),
      new Paragraph({
        children: [new TextRun({ text: '— 文档结束 —', size: 20, color: '999999', italics: true })],
        alignment: AlignmentType.CENTER,
      }),

    ],
  }],
});

Packer.toBuffer(doc).then(buffer => {
  fs.writeFileSync('d:/system/demo/docs/HRM-开发文档.docx', buffer);
  console.log('Document created successfully: docs/HRM-开发文档.docx');
  console.log('Size:', (buffer.length / 1024).toFixed(1), 'KB');
});
