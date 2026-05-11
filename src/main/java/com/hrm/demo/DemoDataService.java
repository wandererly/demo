package com.hrm.demo;

import com.hrm.domain.AttendanceRecord;
import com.hrm.domain.Department;
import com.hrm.domain.Employee;
import com.hrm.domain.JobLevel;
import com.hrm.domain.JobPosition;
import com.hrm.domain.LeaveRequest;
import com.hrm.domain.LeaveRule;
import com.hrm.domain.PayrollRule;
import com.hrm.domain.PayrollTaxBracket;
import com.hrm.domain.PerformanceCycle;
import com.hrm.domain.PerformanceIndicator;
import com.hrm.domain.PerformanceReview;
import com.hrm.domain.PerformanceRule;
import com.hrm.domain.PromotionPath;
import com.hrm.domain.SalaryStructure;
import com.hrm.domain.SysUser;
import com.hrm.dto.PayrollGenerateRequest;
import com.hrm.mapper.AttendanceRecordMapper;
import com.hrm.mapper.DepartmentMapper;
import com.hrm.mapper.EmployeeMapper;
import com.hrm.mapper.JobLevelMapper;
import com.hrm.mapper.JobPositionMapper;
import com.hrm.mapper.LeaveRequestMapper;
import com.hrm.mapper.LeaveRuleMapper;
import com.hrm.mapper.PayrollRecordMapper;
import com.hrm.mapper.PayrollRuleMapper;
import com.hrm.mapper.PayrollTaxBracketMapper;
import com.hrm.mapper.PerformanceCycleMapper;
import com.hrm.mapper.PerformanceIndicatorMapper;
import com.hrm.mapper.PerformanceReviewMapper;
import com.hrm.mapper.PerformanceRuleMapper;
import com.hrm.mapper.PromotionPathMapper;
import com.hrm.mapper.SalaryStructureMapper;
import com.hrm.mapper.SysUserMapper;
import com.hrm.service.EmployeeAccountService;
import com.hrm.service.PayrollService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class DemoDataService {

	private final DepartmentMapper departmentMapper;
	private final EmployeeMapper employeeMapper;
	private final JobLevelMapper jobLevelMapper;
	private final JobPositionMapper jobPositionMapper;
	private final PromotionPathMapper promotionPathMapper;
	private final SalaryStructureMapper salaryStructureMapper;
	private final AttendanceRecordMapper attendanceRecordMapper;
	private final LeaveRequestMapper leaveRequestMapper;
	private final LeaveRuleMapper leaveRuleMapper;
	private final PerformanceRuleMapper performanceRuleMapper;
	private final PayrollRuleMapper payrollRuleMapper;
	private final PayrollTaxBracketMapper payrollTaxBracketMapper;
	private final PerformanceCycleMapper performanceCycleMapper;
	private final PerformanceIndicatorMapper performanceIndicatorMapper;
	private final PerformanceReviewMapper performanceReviewMapper;
	private final PayrollRecordMapper payrollRecordMapper;
	private final SysUserMapper sysUserMapper;
	private final EmployeeAccountService employeeAccountService;
	private final PasswordEncoder passwordEncoder;
	private final PayrollService payrollService;

	public DemoDataService(DepartmentMapper departmentMapper,
						 EmployeeMapper employeeMapper,
						 JobLevelMapper jobLevelMapper,
						 JobPositionMapper jobPositionMapper,
						 PromotionPathMapper promotionPathMapper,
						 SalaryStructureMapper salaryStructureMapper,
						 AttendanceRecordMapper attendanceRecordMapper,
						 LeaveRequestMapper leaveRequestMapper,
						 LeaveRuleMapper leaveRuleMapper,
						 PerformanceRuleMapper performanceRuleMapper,
						 PayrollRuleMapper payrollRuleMapper,
						 PayrollTaxBracketMapper payrollTaxBracketMapper,
						 PerformanceCycleMapper performanceCycleMapper,
						 PerformanceIndicatorMapper performanceIndicatorMapper,
						 PerformanceReviewMapper performanceReviewMapper,
						 PayrollRecordMapper payrollRecordMapper,
						 SysUserMapper sysUserMapper,
						 EmployeeAccountService employeeAccountService,
						 PasswordEncoder passwordEncoder,
						 PayrollService payrollService) {
		this.departmentMapper = departmentMapper;
		this.employeeMapper = employeeMapper;
		this.jobLevelMapper = jobLevelMapper;
		this.jobPositionMapper = jobPositionMapper;
		this.promotionPathMapper = promotionPathMapper;
		this.salaryStructureMapper = salaryStructureMapper;
		this.attendanceRecordMapper = attendanceRecordMapper;
		this.leaveRequestMapper = leaveRequestMapper;
		this.leaveRuleMapper = leaveRuleMapper;
		this.performanceRuleMapper = performanceRuleMapper;
		this.payrollRuleMapper = payrollRuleMapper;
		this.payrollTaxBracketMapper = payrollTaxBracketMapper;
		this.performanceCycleMapper = performanceCycleMapper;
		this.performanceIndicatorMapper = performanceIndicatorMapper;
		this.performanceReviewMapper = performanceReviewMapper;
		this.payrollRecordMapper = payrollRecordMapper;
		this.sysUserMapper = sysUserMapper;
		this.employeeAccountService = employeeAccountService;
		this.passwordEncoder = passwordEncoder;
		this.payrollService = payrollService;
	}

	public void initAll() {
		ensureRules();
		List<Department> departments = ensureDepartments();
		ensureJobLevels();
		ensureJobPositions(departments);
		ensurePromotionPaths();

		List<Employee> employees = ensureEmployees(departments);
		ensureDemoUsers(employees);
		ensureSalaryStructures(employees);
		ensureAttendance(employees);
		ensureLeaves(employees);

		PerformanceCycle cycle = ensurePerformanceCycle();
		ensurePerformanceIndicators(cycle);
		ensurePerformanceReviews(cycle, employees);
		ensurePayroll(employees);
	}

	private void ensureRules() {
		if (leaveRuleMapper.findLatest() == null) {
			LeaveRule rule = new LeaveRule();
			rule.setMaxDaysPerRequest(5.0);
			rule.setApprovalLevel(1);
			leaveRuleMapper.insert(rule);
		}

		if (performanceRuleMapper.findAll().isEmpty()) {
			PerformanceRule a = new PerformanceRule();
			a.setMinScore(90);
			a.setMaxScore(100);
			a.setLevel("A");
			performanceRuleMapper.insert(a);

			PerformanceRule b = new PerformanceRule();
			b.setMinScore(75);
			b.setMaxScore(89);
			b.setLevel("B");
			performanceRuleMapper.insert(b);

			PerformanceRule c = new PerformanceRule();
			c.setMinScore(0);
			c.setMaxScore(74);
			c.setLevel("C");
			performanceRuleMapper.insert(c);
		}

		PayrollRule payrollRule = payrollRuleMapper.findLatest();
		if (payrollRule == null) {
			payrollRule = new PayrollRule();
			payrollRule.setOvertimeMultiplier(1.5);
			payrollRuleMapper.insert(payrollRule);
		}
		if (payrollRule.getId() != null && payrollTaxBracketMapper.findByRuleId(payrollRule.getId()).isEmpty()) {
			createTaxBracket(payrollRule.getId(), BigDecimal.ZERO, BigDecimal.valueOf(5000), BigDecimal.valueOf(0.03));
			createTaxBracket(payrollRule.getId(), BigDecimal.valueOf(5000), BigDecimal.valueOf(12000), BigDecimal.valueOf(0.1));
			createTaxBracket(payrollRule.getId(), BigDecimal.valueOf(12000), BigDecimal.valueOf(25000), BigDecimal.valueOf(0.2));
			createTaxBracket(payrollRule.getId(), BigDecimal.valueOf(25000), null, BigDecimal.valueOf(0.3));
		}
	}

	private void createTaxBracket(Long ruleId, BigDecimal min, BigDecimal max, BigDecimal rate) {
		PayrollTaxBracket bracket = new PayrollTaxBracket();
		bracket.setRuleId(ruleId);
		bracket.setMinIncome(min);
		bracket.setMaxIncome(max);
		bracket.setRate(rate);
		payrollTaxBracketMapper.insert(bracket);
	}

	private List<Department> ensureDepartments() {
		List<Department> departments = departmentMapper.findAll();
		if (departments.isEmpty()) {
			Department hr = new Department();
			hr.setName("人力资源部");
			hr.setStatus("ACTIVE");
			departmentMapper.insert(hr);

			Department fin = new Department();
			fin.setName("财务部");
			fin.setStatus("ACTIVE");
			departmentMapper.insert(fin);

			Department dev = new Department();
			dev.setName("技术研发部");
			dev.setStatus("ACTIVE");
			departmentMapper.insert(dev);

			Department adm = new Department();
			adm.setName("行政部");
			adm.setStatus("ACTIVE");
			departmentMapper.insert(adm);

			Department mkt = new Department();
			mkt.setName("市场部");
			mkt.setStatus("ACTIVE");
			departmentMapper.insert(mkt);

			departments = departmentMapper.findAll();
		}
		return departments;
	}

	private void ensureJobLevels() {
		if (jobLevelMapper.findAll().isEmpty()) {
			createJobLevels();
		}
	}

	private void ensureJobPositions(List<Department> departments) {
		if (jobPositionMapper.findAll().isEmpty()) {
			createJobPositions(departments);
		}
	}

	private void ensurePromotionPaths() {
		if (promotionPathMapper.findAll().isEmpty()) {
			createPromotionPaths();
		}
	}

	private List<Employee> ensureEmployees(List<Department> departments) {
		List<Employee> employees = employeeMapper.findAll();
		if (!employees.isEmpty()) {
			return employees;
		}

		Long hrId = findDeptId(departments, "人力资源部");
		Long finId = findDeptId(departments, "财务部");
		Long devId = findDeptId(departments, "技术研发部");
		Long admId = findDeptId(departments, "行政部");
		Long mktId = findDeptId(departments, "市场部");

		createEmployee("E1001", "张晓婷", hrId, "人事专员", BigDecimal.valueOf(9000));
		createEmployee("E1002", "李晨", hrId, "人事经理", BigDecimal.valueOf(14000));
		createEmployee("E2001", "王涛", finId, "财务主管", BigDecimal.valueOf(16000));
		createEmployee("E3001", "陈思远", devId, "后端工程师", BigDecimal.valueOf(18000));
		createEmployee("E3002", "周可欣", devId, "前端工程师", BigDecimal.valueOf(15000));
		createEmployee("E4001", "赵敏", admId, "行政主管", BigDecimal.valueOf(12000));
		createEmployee("E5001", "刘洋", mktId, "市场经理", BigDecimal.valueOf(18000));
		createEmployee("E5002", "孙怡", mktId, "市场专员", BigDecimal.valueOf(9000));

		return employeeMapper.findAll();
	}

	private void ensureSalaryStructures(List<Employee> employees) {
		for (Employee emp : employees) {
			if (emp == null || emp.getId() == null) {
				continue;
			}
			SalaryStructure existing = salaryStructureMapper.findByEmpId(emp.getId());
			if (existing != null) {
				continue;
			}
			SalaryStructure structure = new SalaryStructure();
			structure.setEmpId(emp.getId());
			structure.setBaseSalary(emp.getBaseSalary());
			structure.setAllowance(BigDecimal.valueOf(800));
			structure.setBonus(BigDecimal.valueOf(1200));
			structure.setSocialSecurity(BigDecimal.valueOf(500));
			structure.setHousingFund(BigDecimal.valueOf(500));
			structure.setTaxRate(BigDecimal.valueOf(0.1));
			salaryStructureMapper.insert(structure);
		}
	}

	private void ensureDemoUsers(List<Employee> employees) {
		if (employees == null || employees.isEmpty()) {
			return;
		}
		for (Employee employee : employees) {
			employeeAccountService.ensureEmployeeAccount(employee);
		}
		ensureDemoUser("employee", "emp123", "EMPLOYEE", employees.get(0).getId());
		if (employees.size() > 1) {
			ensureDemoUser("manager", "mgr123", "HR", employees.get(1).getId());
		}
	}

	private void ensureDemoUser(String username, String password, String role, Long empId) {
		SysUser user = sysUserMapper.findByUsername(username);
		if (user == null) {
			user = new SysUser();
			user.setUsername(username);
			user.setPasswordHash(passwordEncoder.encode(password));
			user.setRole(role);
			user.setEmpId(empId);
			user.setStatus("ACTIVE");
			sysUserMapper.insert(user);
			return;
		}
		boolean changed = false;
		if (user.getEmpId() == null && empId != null) {
			user.setEmpId(empId);
			changed = true;
		}
		if (user.getRole() == null || user.getRole().isBlank()) {
			user.setRole(role);
			changed = true;
		}
		if (changed) {
			sysUserMapper.update(user);
		}
	}

	private void ensureAttendance(List<Employee> employees) {
		Set<String> existing = attendanceRecordMapper.findAll().stream()
				.filter(record -> record.getEmpId() != null && record.getWorkDate() != null)
				.map(record -> attendanceKey(record.getEmpId(), record.getWorkDate()))
				.collect(Collectors.toSet());
		for (int i = 0; i < Math.min(8, employees.size()); i++) {
			Employee emp = employees.get(i);
			if (emp != null && emp.getId() != null) {
				createAttendance(emp.getId(), i, existing);
			}
		}
	}

	private void ensureLeaves(List<Employee> employees) {
		if (employees.size() < 2) {
			return;
		}
		Long approverId = employees.get(1).getId();
		Set<Long> existingEmpIds = leaveRequestMapper.findAll().stream()
				.map(LeaveRequest::getEmpId)
				.collect(Collectors.toSet());
		List<Employee> targets = new ArrayList<>();
		for (Employee emp : employees) {
			if (emp != null && emp.getId() != null && !existingEmpIds.contains(emp.getId())) {
				targets.add(emp);
			}
		}
		for (int i = 0; i < Math.min(3, targets.size()); i++) {
			createLeave(targets.get(i).getId(), approverId, i);
		}
	}

	private PerformanceCycle ensurePerformanceCycle() {
		List<PerformanceCycle> cycles = performanceCycleMapper.findAll();
		if (!cycles.isEmpty()) {
			return cycles.get(0);
		}
		return createPerformanceCycle();
	}

	private void ensurePerformanceIndicators(PerformanceCycle cycle) {
		if (cycle == null || cycle.getId() == null) {
			return;
		}
		if (!performanceIndicatorMapper.findByCycleId(cycle.getId()).isEmpty()) {
			return;
		}
		createPerformanceIndicators(cycle.getId());
	}

	private void ensurePerformanceReviews(PerformanceCycle cycle, List<Employee> employees) {
		if (cycle == null || cycle.getId() == null) {
			return;
		}
		if (employees.isEmpty()) {
			return;
		}
		Long reviewerId = employees.get(0).getId();
		Set<Long> existingEmpIds = performanceReviewMapper.findByCycleId(cycle.getId()).stream()
				.map(PerformanceReview::getEmpId)
				.collect(Collectors.toSet());
		int[] scores = new int[] { 86, 95, 78, 90, 72, 88, 91, 80 };
		String[] levels = new String[] { "B", "A", "B", "A", "C", "B", "A", "B" };
		String[] statuses = new String[] { "APPROVED", "APPROVED", "PENDING", "APPROVED", "PENDING", "APPROVED", "APPROVED", "PENDING" };
		int created = 0;
		for (int i = 0; i < employees.size() && created < 8; i++) {
			Employee emp = employees.get(i);
			if (emp == null || emp.getId() == null || existingEmpIds.contains(emp.getId())) {
				continue;
			}
			createPerformanceReview(cycle.getId(), emp.getId(), reviewerId, scores[created], levels[created], statuses[created]);
			created += 1;
		}
	}

	private void ensurePayroll(List<Employee> employees) {
		for (Employee emp : employees) {
			if (emp == null || emp.getId() == null) {
				continue;
			}
			if (!payrollRecordMapper.findByEmpId(emp.getId()).isEmpty()) {
				continue;
			}
			generatePayroll(emp.getId());
		}
	}

	private void createJobLevels() {
		createLevel("L1", "初级", 1, 5000, 8000, "基础岗位能力", "ACTIVE");
		createLevel("L2", "中级", 2, 8000, 12000, "独立承担工作", "ACTIVE");
		createLevel("L3", "高级", 3, 12000, 18000, "负责核心任务", "ACTIVE");
		createLevel("L4", "主管", 4, 16000, 24000, "带队与管理", "ACTIVE");
		createLevel("L5", "经理", 5, 20000, 32000, "部门管理与策略", "ACTIVE");
		createLevel("L6", "总监", 6, 28000, 45000, "组织级管理", "ACTIVE");
	}

	private void createLevel(String code, String name, int rank, int min, int max, String desc, String status) {
		JobLevel level = new JobLevel();
		level.setLevelCode(code);
		level.setLevelName(name);
		level.setRank(rank);
		level.setSalaryMin(BigDecimal.valueOf(min));
		level.setSalaryMax(BigDecimal.valueOf(max));
		level.setDescription(desc);
		level.setStatus(status);
		jobLevelMapper.insert(level);
	}

	private void createJobPositions(List<Department> departments) {
		List<JobLevel> levels = jobLevelMapper.findAll();
		Long hrId = findDeptId(departments, "人力资源部");
		Long finId = findDeptId(departments, "财务部");
		Long devId = findDeptId(departments, "技术研发部");
		Long admId = findDeptId(departments, "行政部");
		Long mktId = findDeptId(departments, "市场部");

		createPosition(hrId, findLevelId(levels, "L2"), "HR-ASSIST", "人事专员", "HR", "招聘与员工服务", "ACTIVE");
		createPosition(hrId, findLevelId(levels, "L5"), "HR-MGR", "人事经理", "HR", "团队与制度管理", "ACTIVE");
		createPosition(hrId, findLevelId(levels, "L6"), "HR-DIR", "人力资源总监", "HR", "战略人力资源规划", "ACTIVE");

		createPosition(finId, findLevelId(levels, "L4"), "FIN-LEAD", "财务主管", "FIN", "账务与报表", "ACTIVE");
		createPosition(finId, findLevelId(levels, "L5"), "FIN-MGR", "财务经理", "FIN", "预算与风险管理", "ACTIVE");

		createPosition(devId, findLevelId(levels, "L2"), "DEV-JUN", "初级工程师", "DEV", "功能实现与交付", "ACTIVE");
		createPosition(devId, findLevelId(levels, "L3"), "DEV-MID", "中级工程师", "DEV", "模块设计与优化", "ACTIVE");
		createPosition(devId, findLevelId(levels, "L4"), "DEV-SEN", "高级工程师", "DEV", "核心架构与评审", "ACTIVE");
		createPosition(devId, findLevelId(levels, "L5"), "DEV-LEAD", "技术负责人", "DEV", "技术路线与团队管理", "ACTIVE");

		createPosition(admId, findLevelId(levels, "L4"), "ADM-LEAD", "行政主管", "ADM", "行政事务与资产管理", "ACTIVE");

		createPosition(mktId, findLevelId(levels, "L2"), "MKT-EXEC", "市场专员", "MKT", "活动策划与执行", "ACTIVE");
		createPosition(mktId, findLevelId(levels, "L5"), "MKT-MGR", "市场经理", "MKT", "市场策略与增长", "ACTIVE");
	}

	private void createPosition(Long deptId, Long levelId, String code, String name, String group, String desc, String status) {
		JobPosition position = new JobPosition();
		position.setDeptId(deptId);
		position.setLevelId(levelId);
		position.setPositionCode(code);
		position.setPositionName(name);
		position.setPathGroup(group);
		position.setDescription(desc);
		position.setStatus(status);
		jobPositionMapper.insert(position);
	}

	private void createPromotionPaths() {
		List<JobPosition> positions = jobPositionMapper.findAll();
		createPath(positions, "DEV-JUN", "DEV-MID", 12, "绩效B及以上，完成基础模块交付");
		createPath(positions, "DEV-MID", "DEV-SEN", 18, "绩效B及以上，负责核心项目");
		createPath(positions, "DEV-SEN", "DEV-LEAD", 24, "绩效A，具备技术与团队影响力");
		createPath(positions, "HR-ASSIST", "HR-MGR", 24, "绩效B及以上，完成2次招聘项目");
		createPath(positions, "HR-MGR", "HR-DIR", 36, "绩效A，推动年度人效提升");
		createPath(positions, "MKT-EXEC", "MKT-MGR", 24, "绩效B及以上，完成增长指标");
		createPath(positions, "FIN-LEAD", "FIN-MGR", 24, "绩效B及以上，财务合规零差错");
	}

	private void createPath(List<JobPosition> positions, String fromCode, String toCode, int months, String condition) {
		Long fromId = findPositionId(positions, fromCode);
		Long toId = findPositionId(positions, toCode);
		if (fromId == null || toId == null) {
			return;
		}
		PromotionPath path = new PromotionPath();
		path.setFromPositionId(fromId);
		path.setToPositionId(toId);
		path.setMinMonths(months);
		path.setConditionText(condition);
		promotionPathMapper.insert(path);
	}

	private Long findDeptId(List<Department> departments, String name) {
		for (Department dept : departments) {
			if (name.equals(dept.getName())) {
				return dept.getId();
			}
		}
		return null;
	}

	private Long findLevelId(List<JobLevel> levels, String code) {
		for (JobLevel level : levels) {
			if (code.equals(level.getLevelCode())) {
				return level.getId();
			}
		}
		return null;
	}

	private Long findPositionId(List<JobPosition> positions, String code) {
		for (JobPosition position : positions) {
			if (code.equals(position.getPositionCode())) {
				return position.getId();
			}
		}
		return null;
	}

	private Employee createEmployee(String empNo, String name, Long deptId, String position, BigDecimal baseSalary) {
		Employee employee = new Employee();
		employee.setEmpNo(empNo);
		employee.setName(name);
		employee.setGender(isFemaleName(name) ? "F" : "M");
		String digits = empNo == null ? "" : empNo.replaceAll("\\D", "");
		String tail = digits.length() >= 8 ? digits.substring(digits.length() - 8) : String.format("%08d", digits.isEmpty() ? 0 : Integer.parseInt(digits));
		employee.setPhone("138" + tail);
		String safeEmpNo = empNo == null ? "emp" : empNo;
		employee.setEmail(safeEmpNo.toLowerCase() + "@corp.cn");
		employee.setDeptId(deptId);
		employee.setPosition(position);
		employee.setHireDate(LocalDate.now().minusDays(300));
		employee.setStatus("ACTIVE");
		employee.setBaseSalary(baseSalary);
		employeeMapper.insert(employee);
		return employee;
	}

	private void createAttendance(Long empId, int employeeIndex, Set<String> existing) {
		LocalDate today = LocalDate.now();
		Map<Integer, String[]> patterns = Map.of(
				0, new String[] { "NORMAL", "NORMAL", "LATE", "NORMAL", "EARLY_LEAVE", "NORMAL", "NORMAL" },
				1, new String[] { "NORMAL", "ABSENT", "NORMAL", "NORMAL", "LATE", "NORMAL", "LEAVE" },
				2, new String[] { "EARLY_LEAVE", "NORMAL", "NORMAL", "ABSENT", "NORMAL", "NORMAL", "NORMAL" },
				3, new String[] { "NORMAL", "LATE", "NORMAL", "NORMAL", "NORMAL", "EARLY_LEAVE", "NORMAL" },
				4, new String[] { "LEAVE", "LEAVE", "NORMAL", "NORMAL", "NORMAL", "LATE", "NORMAL" },
				5, new String[] { "NORMAL", "NORMAL", "ABSENT", "NORMAL", "NORMAL", "NORMAL", "EARLY_LEAVE" },
				6, new String[] { "LATE", "NORMAL", "NORMAL", "LEAVE", "NORMAL", "NORMAL", "NORMAL" },
				7, new String[] { "NORMAL", "NORMAL", "NORMAL", "NORMAL", "ABSENT", "LATE", "NORMAL" }
		);
		String[] statuses = patterns.getOrDefault(employeeIndex, patterns.get(0));
		for (int i = 1; i <= statuses.length; i += 1) {
			String status = statuses[i - 1];
			LocalDate workDate = today.minusDays(i);
			String key = attendanceKey(empId, workDate);
			if (existing.contains(key)) {
				continue;
			}
			AttendanceRecord record = new AttendanceRecord();
			record.setEmpId(empId);
			record.setWorkDate(workDate);
			fillAttendanceTime(record, status, i);
			record.setStatus(status);
			attendanceRecordMapper.insert(record);
			existing.add(key);
		}
	}

	private String attendanceKey(Long empId, LocalDate workDate) {
		return empId + "@" + workDate;
	}

	private void fillAttendanceTime(AttendanceRecord record, String status, int daysAgo) {
		LocalDateTime base = LocalDateTime.now().minusDays(daysAgo).withSecond(0).withNano(0);
		if ("ABSENT".equals(status) || "LEAVE".equals(status)) {
			record.setCheckIn(null);
			record.setCheckOut(null);
			return;
		}
		if ("LATE".equals(status)) {
			record.setCheckIn(base.withHour(9).withMinute(45));
			record.setCheckOut(base.withHour(18).withMinute(10));
			return;
		}
		if ("EARLY_LEAVE".equals(status)) {
			record.setCheckIn(base.withHour(8).withMinute(55));
			record.setCheckOut(base.withHour(16).withMinute(50));
			return;
		}
		record.setCheckIn(base.withHour(8).withMinute(58));
		record.setCheckOut(base.withHour(18).withMinute(3));
	}

	private void createLeave(Long empId, Long approverId, int index) {
		String[] types = new String[] { "年假", "病假", "事假" };
		String[] reasons = new String[] { "家庭事务", "身体不适", "个人事务" };
		String[] statuses = new String[] { "APPROVED", "PENDING", "REJECTED" };
		LeaveRequest leave = new LeaveRequest();
		leave.setEmpId(empId);
		leave.setLeaveType(types[index % types.length]);
		leave.setStartTime(LocalDateTime.now().minusDays(7 + index).withHour(9).withMinute(0));
		leave.setEndTime(LocalDateTime.now().minusDays(6 + index).withHour(18).withMinute(0));
		leave.setDays(index == 1 ? 1.0 : 2.0);
		leave.setReason(reasons[index % reasons.length]);
		leave.setStatus(statuses[index % statuses.length]);
		leave.setApproverId(approverId);
		leaveRequestMapper.insert(leave);
	}

	private PerformanceCycle createPerformanceCycle() {
		PerformanceCycle cycle = new PerformanceCycle();
		cycle.setName("2026 Q1 绩效考核");
		cycle.setStartDate(LocalDate.now().minusMonths(2));
		cycle.setEndDate(LocalDate.now().plusMonths(1));
		cycle.setStatus("OPEN");
		performanceCycleMapper.insert(cycle);
		return cycle;
	}

	private void createPerformanceIndicators(Long cycleId) {
		String[][] indicators = new String[][] {
				{"目标达成", "40"},
				{"工作质量", "30"},
				{"协作沟通", "20"},
				{"创新贡献", "10"}
		};
		for (String[] it : indicators) {
			PerformanceIndicator indicator = new PerformanceIndicator();
			indicator.setCycleId(cycleId);
			indicator.setName(it[0]);
			indicator.setWeight(Integer.parseInt(it[1]));
			indicator.setDescription("");
			performanceIndicatorMapper.insert(indicator);
		}
	}

	private void createPerformanceReview(Long cycleId, Long empId, Long reviewerId, int score, String level, String status) {
		PerformanceReview review = new PerformanceReview();
		review.setEmpId(empId);
		review.setCycleId(cycleId);
		review.setScore(score);
		review.setLevel(level);
		review.setReviewerId(reviewerId);
		review.setGoalSummary("目标达成率与质量综合评估");
		review.setApprovalStatus(status);
		if ("APPROVED".equals(status)) {
			review.setApproverId(reviewerId);
			review.setApprovedAt(LocalDateTime.now());
		}
		review.setComment("整体表现良好，建议保持学习和协作");
		performanceReviewMapper.insert(review);
	}

	private boolean isFemaleName(String name) {
		if (name == null) {
			return false;
		}
		Set<String> female = Set.of("张晓婷", "周可欣", "赵敏", "孙怡");
		return female.contains(name);
	}

	private void generatePayroll(Long empId) {
		if (!payrollRecordMapper.findByEmpId(empId).isEmpty()) {
			return;
		}
		PayrollGenerateRequest req = new PayrollGenerateRequest();
		req.setEmpId(empId);
		req.setCycleMonth(YearMonth.now().toString());
		req.setOvertimeHours(BigDecimal.valueOf(6));
		req.setOvertimeRate(BigDecimal.valueOf(80));
		req.setLeaveDays(BigDecimal.valueOf(1));
		req.setLeaveDeductionPerDay(BigDecimal.valueOf(200));
		payrollService.generate(req);
	}
}


