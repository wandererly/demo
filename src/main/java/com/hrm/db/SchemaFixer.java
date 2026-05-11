package com.hrm.db;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class SchemaFixer implements CommandLineRunner {

	private final JdbcTemplate jdbcTemplate;

	public SchemaFixer(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void run(String... args) {
		ensureTables();
		ensureAuditLogTable();
		ensurePerformanceReviewColumns();
		ensureEnterpriseTables();
		ensureEnterpriseColumns();
		cleanupPayrollBrackets();
	}

	private void ensureTables() {
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS job_level ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "level_code varchar(20) NOT NULL,"
				+ "level_name varchar(50) NOT NULL,"
				+ "`rank` int NOT NULL,"
				+ "salary_min decimal(12,2) NOT NULL,"
				+ "salary_max decimal(12,2) NOT NULL,"
				+ "description varchar(255) DEFAULT NULL,"
				+ "status varchar(20) NOT NULL DEFAULT 'ACTIVE',"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_job_level_code (level_code)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS job_position ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "dept_id bigint NOT NULL,"
				+ "level_id bigint NOT NULL,"
				+ "position_code varchar(50) NOT NULL,"
				+ "position_name varchar(100) NOT NULL,"
				+ "path_group varchar(50) DEFAULT NULL,"
				+ "description varchar(255) DEFAULT NULL,"
				+ "status varchar(20) NOT NULL DEFAULT 'ACTIVE',"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_job_position_code (position_code),"
				+ "KEY idx_job_position_dept (dept_id),"
				+ "KEY idx_job_position_level (level_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS promotion_path ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "from_position_id bigint NOT NULL,"
				+ "to_position_id bigint NOT NULL,"
				+ "min_months int NOT NULL,"
				+ "condition_text varchar(255) DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_promotion_from (from_position_id),"
				+ "KEY idx_promotion_to (to_position_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
	}

	private void ensurePerformanceReviewColumns() {
		ensureColumn("performance_review", "goal_summary", "varchar(255) NULL");
		ensureColumn("performance_review", "approval_status", "varchar(20) NOT NULL DEFAULT 'PENDING'");
		ensureColumn("performance_review", "approver_id", "bigint NULL");
		ensureColumn("performance_review", "approved_at", "datetime NULL");
	}

	private void ensureAuditLogTable() {
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS audit_log ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "module varchar(50) NOT NULL,"
				+ "action varchar(50) NOT NULL,"
				+ "target_type varchar(80) DEFAULT NULL,"
				+ "target_id bigint DEFAULT NULL,"
				+ "actor varchar(100) DEFAULT NULL,"
				+ "detail varchar(500) DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_audit_created_at (created_at),"
				+ "KEY idx_audit_target (target_type, target_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
	}

	private void ensureEnterpriseTables() {
		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS employee_contract ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "contract_no varchar(80) NOT NULL,"
				+ "contract_type varchar(40) NOT NULL,"
				+ "start_date date NOT NULL,"
				+ "end_date date DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'ACTIVE',"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_contract_no (contract_no),"
				+ "KEY idx_contract_emp (emp_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS employee_lifecycle_event ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "event_type varchar(40) NOT NULL,"
				+ "title varchar(120) NOT NULL,"
				+ "from_dept_id bigint DEFAULT NULL,"
				+ "to_dept_id bigint DEFAULT NULL,"
				+ "from_position varchar(100) DEFAULT NULL,"
				+ "to_position varchar(100) DEFAULT NULL,"
				+ "effective_date date DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'PENDING',"
				+ "approver_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "detail varchar(500) DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_lifecycle_emp (emp_id),"
				+ "KEY idx_lifecycle_status (status)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS work_shift ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "shift_name varchar(80) NOT NULL,"
				+ "start_time varchar(10) NOT NULL,"
				+ "end_time varchar(10) NOT NULL,"
				+ "work_hours decimal(5,2) NOT NULL DEFAULT 8.00,"
				+ "status varchar(20) NOT NULL DEFAULT 'ACTIVE',"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS holiday_calendar ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "holiday_date date NOT NULL,"
				+ "holiday_name varchar(80) NOT NULL,"
				+ "holiday_type varchar(30) NOT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_holiday_date (holiday_date)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS attendance_correction ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "work_date date NOT NULL,"
				+ "check_in datetime DEFAULT NULL,"
				+ "check_out datetime DEFAULT NULL,"
				+ "reason varchar(300) DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'PENDING',"
				+ "approver_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_correction_status (status),"
				+ "KEY idx_correction_emp_date (emp_id, work_date)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS overtime_request ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "work_date date NOT NULL,"
				+ "hours decimal(6,2) NOT NULL,"
				+ "reason varchar(300) DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'PENDING',"
				+ "approver_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_overtime_status (status),"
				+ "KEY idx_overtime_emp_date (emp_id, work_date)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS attendance_monthly_summary ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "cycle_month varchar(7) NOT NULL,"
				+ "present_days int NOT NULL DEFAULT 0,"
				+ "late_count int NOT NULL DEFAULT 0,"
				+ "early_leave_count int NOT NULL DEFAULT 0,"
				+ "absent_days int NOT NULL DEFAULT 0,"
				+ "leave_days decimal(6,2) NOT NULL DEFAULT 0.00,"
				+ "overtime_hours decimal(6,2) NOT NULL DEFAULT 0.00,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_att_month_emp (emp_id, cycle_month)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS leave_balance ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "leave_type varchar(40) NOT NULL,"
				+ "total_days decimal(6,2) NOT NULL DEFAULT 0.00,"
				+ "used_days decimal(6,2) NOT NULL DEFAULT 0.00,"
				+ "remaining_days decimal(6,2) NOT NULL DEFAULT 0.00,"
				+ "cycle_year int NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_leave_balance (emp_id, leave_type, cycle_year)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS leave_balance_log ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "leave_type varchar(40) NOT NULL,"
				+ "change_days decimal(6,2) NOT NULL,"
				+ "reason varchar(200) DEFAULT NULL,"
				+ "ref_type varchar(40) DEFAULT NULL,"
				+ "ref_id bigint DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_leave_log_emp (emp_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS performance_goal ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "cycle_id bigint NOT NULL,"
				+ "goal_title varchar(160) NOT NULL,"
				+ "target_value varchar(200) DEFAULT NULL,"
				+ "self_score int DEFAULT NULL,"
				+ "manager_score int DEFAULT NULL,"
				+ "final_score int DEFAULT NULL,"
				+ "final_level varchar(20) DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'GOAL_SET',"
				+ "reviewer_id bigint DEFAULT NULL,"
				+ "calibrated_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_goal_emp_cycle (emp_id, cycle_id),"
				+ "KEY idx_goal_status (status)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS payslip ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "cycle_month varchar(7) NOT NULL,"
				+ "base_salary decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "allowance decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "bonus decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "overtime_pay decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "leave_deduction decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "social_security decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "housing_fund decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "tax decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "gross_salary decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "net_salary decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "performance_level varchar(20) DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'PENDING',"
				+ "approver_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_payslip_emp_month (emp_id, cycle_month),"
				+ "KEY idx_payslip_status (status)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS salary_change ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "before_salary decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "after_salary decimal(12,2) NOT NULL,"
				+ "effective_month varchar(7) NOT NULL,"
				+ "reason varchar(300) DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'PENDING',"
				+ "approver_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_salary_change_status (status),"
				+ "KEY idx_salary_change_emp (emp_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS notification ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "user_name varchar(100) DEFAULT NULL,"
				+ "emp_id bigint DEFAULT NULL,"
				+ "title varchar(120) NOT NULL,"
				+ "content varchar(500) DEFAULT NULL,"
				+ "status varchar(20) NOT NULL DEFAULT 'UNREAD',"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_notification_user (user_name),"
				+ "KEY idx_notification_emp (emp_id),"
				+ "KEY idx_notification_status (status)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
	}

	private void ensureEnterpriseColumns() {
		ensureColumn("sys_user", "emp_id", "bigint NULL");
		ensureColumn("audit_log", "before_value", "text NULL");
		ensureColumn("audit_log", "after_value", "text NULL");
		ensureColumn("audit_log", "ip_address", "varchar(80) NULL");
	}

	private void ensureColumn(String table, String column, String ddl) {
		Integer cnt = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.columns where table_schema = database() and table_name = ? and column_name = ?",
				Integer.class,
				table,
				column
		);
		if (cnt != null && cnt == 0) {
			jdbcTemplate.execute("alter table " + table + " add column " + column + " " + ddl);
		}
	}

	private void cleanupPayrollBrackets() {
		jdbcTemplate.execute("update payroll_tax_bracket set min_income = 0 where min_income is null");
	}
}
