create database if not exists hrm default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use hrm;

create database if not exists hrm default character set utf8mb4 collate utf8mb4_0900_ai_ci;
use hrm;

create table if not exists department (
  id bigint primary key auto_increment,
  name varchar(100) not null,
  parent_id bigint null,
  manager_id bigint null,
  status varchar(20) not null default 'ACTIVE',
  created_at datetime not null,
  updated_at datetime not null,
  key idx_department_parent (parent_id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists employee (
  id bigint primary key auto_increment,
  emp_no varchar(50) not null,
  name varchar(50) not null,
  gender varchar(10),
  phone varchar(30),
  email varchar(100),
  dept_id bigint not null,
  position varchar(50),
  hire_date date not null,
  status varchar(20) not null default 'ACTIVE',
  base_salary decimal(12,2) not null,
  created_at datetime not null,
  updated_at datetime not null,
  unique key uk_employee_emp_no (emp_no),
  key idx_employee_dept (dept_id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists attendance_record (
  id bigint primary key auto_increment,
  emp_id bigint not null,
  work_date date not null,
  check_in datetime,
  check_out datetime,
  status varchar(20) not null default 'NORMAL',
  created_at datetime not null,
  key idx_attendance_emp_date (emp_id, work_date)
) engine=InnoDB default charset=utf8mb4;

create table if not exists leave_request (
  id bigint primary key auto_increment,
  emp_id bigint not null,
  leave_type varchar(20) not null,
  start_time datetime not null,
  end_time datetime not null,
  days decimal(6,2) not null,
  reason varchar(255),
  status varchar(20) not null default 'PENDING',
  approver_id bigint,
  created_at datetime not null,
  key idx_leave_emp (emp_id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists performance_cycle (
  id bigint primary key auto_increment,
  name varchar(100) not null,
  start_date date not null,
  end_date date not null,
  status varchar(20) not null default 'OPEN',
  created_at datetime not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists performance_indicator (
  id bigint primary key auto_increment,
  cycle_id bigint not null,
  name varchar(100) not null,
  weight int not null,
  description varchar(255)
) engine=InnoDB default charset=utf8mb4;

create table if not exists performance_review (
  id bigint primary key auto_increment,
  emp_id bigint not null,
  cycle_id bigint not null,
  score int not null,
  level varchar(20) not null,
  reviewer_id bigint,
  goal_summary varchar(255),
  approval_status varchar(20) not null default 'PENDING',
  approver_id bigint,
  approved_at datetime,
  comment varchar(255),
  created_at datetime not null,
  key idx_review_emp_cycle (emp_id, cycle_id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists salary_structure (
  id bigint primary key auto_increment,
  emp_id bigint not null,
  base_salary decimal(12,2) not null,
  allowance decimal(12,2) not null default 0,
  bonus decimal(12,2) not null default 0,
  social_security decimal(12,2) not null default 0,
  housing_fund decimal(12,2) not null default 0,
  tax_rate decimal(6,4) not null default 0.1,
  unique key uk_salary_emp (emp_id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists payroll_record (
  id bigint primary key auto_increment,
  emp_id bigint not null,
  cycle_month varchar(7) not null,
  gross_salary decimal(12,2) not null,
  net_salary decimal(12,2) not null,
  tax decimal(12,2) not null,
  overtime_pay decimal(12,2) not null,
  leave_deduction decimal(12,2) not null,
  status varchar(20) not null default 'CALCULATED',
  created_at datetime not null,
  key idx_payroll_emp_month (emp_id, cycle_month)
) engine=InnoDB default charset=utf8mb4;

create table if not exists payroll_item (
  id bigint primary key auto_increment,
  payroll_id bigint not null,
  item_type varchar(20) not null,
  item_name varchar(50) not null,
  amount decimal(12,2) not null,
  key idx_item_payroll (payroll_id)
) engine=InnoDB default charset=utf8mb4;

create table if not exists sys_user (
  id bigint primary key auto_increment,
  username varchar(50) not null,
  password_hash varchar(100) not null,
  role varchar(20) not null,
  status varchar(20) not null default 'ACTIVE',
  created_at datetime not null,
  unique key uk_user_username (username)
) engine=InnoDB default charset=utf8mb4;

create table if not exists sys_role (
  id bigint primary key auto_increment,
  role_name varchar(50) not null,
  role_key varchar(50) not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists sys_user_role (
  id bigint primary key auto_increment,
  user_id bigint not null,
  role_id bigint not null,
  unique key uk_user_role (user_id, role_id)
) engine=InnoDB default charset=utf8mb4;
create table if not exists leave_rule (
  id bigint primary key auto_increment,
  max_days_per_request decimal(6,2) not null,
  approval_level int not null default 1,
  created_at datetime not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists performance_rule (
  id bigint primary key auto_increment,
  min_score int not null,
  max_score int not null,
  level varchar(20) not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists payroll_rule (
  id bigint primary key auto_increment,
  overtime_multiplier decimal(6,2) not null default 1.0,
  created_at datetime not null
) engine=InnoDB default charset=utf8mb4;

create table if not exists payroll_tax_bracket (
  id bigint primary key auto_increment,
  rule_id bigint not null,
  min_income decimal(12,2) not null,
  max_income decimal(12,2) null,
  rate decimal(6,4) not null,
  key idx_tax_rule (rule_id)
) engine=InnoDB default charset=utf8mb4;
create table if not exists permission (
  id bigint primary key auto_increment,
  perm_key varchar(100) not null,
  perm_name varchar(100) not null,
  unique key uk_perm_key (perm_key)
) engine=InnoDB default charset=utf8mb4;

create table if not exists role_permission (
  id bigint primary key auto_increment,
  role_id bigint not null,
  perm_id bigint not null,
  unique key uk_role_perm (role_id, perm_id)
) engine=InnoDB default charset=utf8mb4;
-- 职级表
CREATE TABLE IF NOT EXISTS job_level (
  id bigint NOT NULL AUTO_INCREMENT,
  level_code varchar(20) NOT NULL,
  level_name varchar(50) NOT NULL,
  `rank` int NOT NULL,
  salary_min decimal(12,2) NOT NULL,
  salary_max decimal(12,2) NOT NULL,
  description varchar(255) DEFAULT NULL,
  status varchar(20) NOT NULL DEFAULT 'ACTIVE',
  created_at datetime NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_job_level_code (level_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 岗位表
CREATE TABLE IF NOT EXISTS job_position (
  id bigint NOT NULL AUTO_INCREMENT,
  dept_id bigint NOT NULL,
  level_id bigint NOT NULL,
  position_code varchar(50) NOT NULL,
  position_name varchar(100) NOT NULL,
  path_group varchar(50) DEFAULT NULL,
  description varchar(255) DEFAULT NULL,
  status varchar(20) NOT NULL DEFAULT 'ACTIVE',
  created_at datetime NOT NULL,
  updated_at datetime NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_job_position_code (position_code),
  KEY idx_job_position_dept (dept_id),
  KEY idx_job_position_level (level_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 晋升路径表
CREATE TABLE IF NOT EXISTS promotion_path (
  id bigint NOT NULL AUTO_INCREMENT,
  from_position_id bigint NOT NULL,
  to_position_id bigint NOT NULL,
  min_months int NOT NULL,
  condition_text varchar(255) DEFAULT NULL,
  created_at datetime NOT NULL,
  PRIMARY KEY (id),
  KEY idx_promotion_from (from_position_id),
  KEY idx_promotion_to (to_position_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


