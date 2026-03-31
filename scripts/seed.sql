USE hrm;

UPDATE sys_user
SET password_hash = '$2a$10$Jksjx2736jx8.YDfupg0BumbfQqMNa00wZQAq0XbCqNymtEzhwzvO', role = 'ADMIN', status = 'ACTIVE'
WHERE username = 'admin';

-- Departments
INSERT INTO department (name, parent_id, manager_id, status, created_at, updated_at)
SELECT '人力资源部', NULL, NULL, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = '人力资源部');

INSERT INTO department (name, parent_id, manager_id, status, created_at, updated_at)
SELECT '财务部', NULL, NULL, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = '财务部');

INSERT INTO department (name, parent_id, manager_id, status, created_at, updated_at)
SELECT '研发部', NULL, NULL, 'ACTIVE', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM department WHERE name = '研发部');

-- Employees
INSERT INTO employee (emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at)
SELECT 'E1001', '张敏', 'F', '13800000001', 'e1001@hrm.com', d.id, 'HR专员', '2025-06-01', 'ACTIVE', 9000.00, NOW(), NOW()
FROM department d WHERE d.name = '人力资源部'
AND NOT EXISTS (SELECT 1 FROM employee WHERE emp_no = 'E1001');

INSERT INTO employee (emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at)
SELECT 'E1002', '李雷', 'M', '13800000002', 'e1002@hrm.com', d.id, 'HR经理', '2024-11-15', 'ACTIVE', 15000.00, NOW(), NOW()
FROM department d WHERE d.name = '人力资源部'
AND NOT EXISTS (SELECT 1 FROM employee WHERE emp_no = 'E1002');

INSERT INTO employee (emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at)
SELECT 'E2001', '王芳', 'F', '13800000003', 'e2001@hrm.com', d.id, '会计', '2024-03-20', 'ACTIVE', 16000.00, NOW(), NOW()
FROM department d WHERE d.name = '财务部'
AND NOT EXISTS (SELECT 1 FROM employee WHERE emp_no = 'E2001');

INSERT INTO employee (emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at)
SELECT 'E3001', '赵磊', 'M', '13800000004', 'e3001@hrm.com', d.id, '后端工程师', '2023-09-10', 'ACTIVE', 18000.00, NOW(), NOW()
FROM department d WHERE d.name = '研发部'
AND NOT EXISTS (SELECT 1 FROM employee WHERE emp_no = 'E3001');

INSERT INTO employee (emp_no, name, gender, phone, email, dept_id, position, hire_date, status, base_salary, created_at, updated_at)
SELECT 'E3002', '陈晨', 'F', '13800000005', 'e3002@hrm.com', d.id, '前端工程师', '2024-01-05', 'ACTIVE', 15000.00, NOW(), NOW()
FROM department d WHERE d.name = '研发部'
AND NOT EXISTS (SELECT 1 FROM employee WHERE emp_no = 'E3002');

-- Salary structure
INSERT INTO salary_structure (emp_id, base_salary, allowance, bonus, social_security, housing_fund, tax_rate)
SELECT e.id, 9000.00, 800.00, 1200.00, 500.00, 500.00, 0.10
FROM employee e WHERE e.emp_no = 'E1001'
AND NOT EXISTS (SELECT 1 FROM salary_structure WHERE emp_id = e.id);

INSERT INTO salary_structure (emp_id, base_salary, allowance, bonus, social_security, housing_fund, tax_rate)
SELECT e.id, 15000.00, 1200.00, 2000.00, 600.00, 600.00, 0.10
FROM employee e WHERE e.emp_no = 'E1002'
AND NOT EXISTS (SELECT 1 FROM salary_structure WHERE emp_id = e.id);

INSERT INTO salary_structure (emp_id, base_salary, allowance, bonus, social_security, housing_fund, tax_rate)
SELECT e.id, 16000.00, 1200.00, 1500.00, 700.00, 700.00, 0.10
FROM employee e WHERE e.emp_no = 'E2001'
AND NOT EXISTS (SELECT 1 FROM salary_structure WHERE emp_id = e.id);

INSERT INTO salary_structure (emp_id, base_salary, allowance, bonus, social_security, housing_fund, tax_rate)
SELECT e.id, 18000.00, 1500.00, 2500.00, 800.00, 800.00, 0.10
FROM employee e WHERE e.emp_no = 'E3001'
AND NOT EXISTS (SELECT 1 FROM salary_structure WHERE emp_id = e.id);

INSERT INTO salary_structure (emp_id, base_salary, allowance, bonus, social_security, housing_fund, tax_rate)
SELECT e.id, 15000.00, 1200.00, 1800.00, 700.00, 700.00, 0.10
FROM employee e WHERE e.emp_no = 'E3002'
AND NOT EXISTS (SELECT 1 FROM salary_structure WHERE emp_id = e.id);

-- Attendance (last 3 working days)
INSERT INTO attendance_record (emp_id, work_date, check_in, check_out, status, created_at)
SELECT e.id, '2026-03-21', '2026-03-21 09:05:00', '2026-03-21 18:02:00', 'NORMAL', NOW()
FROM employee e WHERE e.emp_no = 'E1001'
AND NOT EXISTS (SELECT 1 FROM attendance_record WHERE emp_id = e.id AND work_date = '2026-03-21');

INSERT INTO attendance_record (emp_id, work_date, check_in, check_out, status, created_at)
SELECT e.id, '2026-03-22', '2026-03-22 09:10:00', '2026-03-22 18:01:00', 'NORMAL', NOW()
FROM employee e WHERE e.emp_no = 'E1001'
AND NOT EXISTS (SELECT 1 FROM attendance_record WHERE emp_id = e.id AND work_date = '2026-03-22');

INSERT INTO attendance_record (emp_id, work_date, check_in, check_out, status, created_at)
SELECT e.id, '2026-03-23', '2026-03-23 09:03:00', '2026-03-23 18:06:00', 'NORMAL', NOW()
FROM employee e WHERE e.emp_no = 'E1001'
AND NOT EXISTS (SELECT 1 FROM attendance_record WHERE emp_id = e.id AND work_date = '2026-03-23');

-- Leave request
INSERT INTO leave_request (emp_id, leave_type, start_time, end_time, days, reason, status, approver_id, created_at)
SELECT e.id, '年假', '2026-03-10 09:00:00', '2026-03-11 18:00:00', 2.0, '家庭事务', 'APPROVED', a.id, NOW()
FROM employee e JOIN employee a ON a.emp_no = 'E1002'
WHERE e.emp_no = 'E1001'
AND NOT EXISTS (SELECT 1 FROM leave_request WHERE emp_id = e.id AND start_time = '2026-03-10 09:00:00');

-- Performance cycle
INSERT INTO performance_cycle (name, start_date, end_date, status, created_at)
SELECT '2026 Q1 绩效', '2026-01-01', '2026-03-31', 'OPEN', NOW()
WHERE NOT EXISTS (SELECT 1 FROM performance_cycle WHERE name = '2026 Q1 绩效');

-- Performance indicators
INSERT INTO performance_indicator (cycle_id, name, weight, description)
SELECT c.id, '目标达成', 40, '' FROM performance_cycle c
WHERE c.name = '2026 Q1 绩效'
AND NOT EXISTS (SELECT 1 FROM performance_indicator WHERE cycle_id = c.id AND name = '目标达成');

INSERT INTO performance_indicator (cycle_id, name, weight, description)
SELECT c.id, '能力成长', 30, '' FROM performance_cycle c
WHERE c.name = '2026 Q1 绩效'
AND NOT EXISTS (SELECT 1 FROM performance_indicator WHERE cycle_id = c.id AND name = '能力成长');

INSERT INTO performance_indicator (cycle_id, name, weight, description)
SELECT c.id, '协作贡献', 20, '' FROM performance_cycle c
WHERE c.name = '2026 Q1 绩效'
AND NOT EXISTS (SELECT 1 FROM performance_indicator WHERE cycle_id = c.id AND name = '协作贡献');

INSERT INTO performance_indicator (cycle_id, name, weight, description)
SELECT c.id, '创新实践', 10, '' FROM performance_cycle c
WHERE c.name = '2026 Q1 绩效'
AND NOT EXISTS (SELECT 1 FROM performance_indicator WHERE cycle_id = c.id AND name = '创新实践');

-- Performance reviews
INSERT INTO performance_review (emp_id, cycle_id, score, level, reviewer_id, comment, created_at)
SELECT e.id, c.id, 86, 'B', r.id, '表现稳定', NOW()
FROM employee e, performance_cycle c, employee r
WHERE e.emp_no = 'E1001' AND r.emp_no = 'E1002' AND c.name = '2026 Q1 绩效'
AND NOT EXISTS (SELECT 1 FROM performance_review WHERE emp_id = e.id AND cycle_id = c.id);

-- Rules
INSERT INTO leave_rule (max_days_per_request, approval_level, created_at)
SELECT 10, 1, NOW()
WHERE NOT EXISTS (SELECT 1 FROM leave_rule);

INSERT INTO performance_rule (min_score, max_score, level)
SELECT 90, 100, 'A' WHERE NOT EXISTS (SELECT 1 FROM performance_rule WHERE level = 'A');
INSERT INTO performance_rule (min_score, max_score, level)
SELECT 80, 89, 'B' WHERE NOT EXISTS (SELECT 1 FROM performance_rule WHERE level = 'B');
INSERT INTO performance_rule (min_score, max_score, level)
SELECT 70, 79, 'C' WHERE NOT EXISTS (SELECT 1 FROM performance_rule WHERE level = 'C');

INSERT INTO payroll_rule (overtime_multiplier, created_at)
SELECT 1.5, NOW()
WHERE NOT EXISTS (SELECT 1 FROM payroll_rule);

INSERT INTO payroll_tax_bracket (rule_id, min_income, max_income, rate)
SELECT r.id, 0, 5000, 0.03 FROM payroll_rule r
WHERE NOT EXISTS (SELECT 1 FROM payroll_tax_bracket WHERE rule_id = r.id AND min_income = 0);

INSERT INTO payroll_tax_bracket (rule_id, min_income, max_income, rate)
SELECT r.id, 5000, 20000, 0.10 FROM payroll_rule r
WHERE NOT EXISTS (SELECT 1 FROM payroll_tax_bracket WHERE rule_id = r.id AND min_income = 5000);

INSERT INTO payroll_tax_bracket (rule_id, min_income, max_income, rate)
SELECT r.id, 20000, NULL, 0.20 FROM payroll_rule r
WHERE NOT EXISTS (SELECT 1 FROM payroll_tax_bracket WHERE rule_id = r.id AND min_income = 20000);
