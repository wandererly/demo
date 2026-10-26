package com.hrm.db;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(0)
/**
 * 启动时对历史演示数据库执行幂等结构补齐。
 * 该类兼容旧实例缺少字段、索引和唯一约束的情况。
 */
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
		ensureEnterpriseIndexes();
		ensureBusinessUniqueIndexes();
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
		ensureColumn("performance_review", "assignee_user_id", "bigint NULL");
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
				+ "probation_start_date date DEFAULT NULL,"
				+ "probation_end_date date DEFAULT NULL,"
				+ "renewal_status varchar(30) DEFAULT NULL,"
				+ "reminder_sent tinyint(1) NOT NULL DEFAULT 0,"
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
				+ "assignee_user_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "target_salary decimal(12,2) DEFAULT NULL,"
				+ "account_action varchar(40) DEFAULT NULL,"
				+ "asset_handover_status varchar(40) DEFAULT NULL,"
				+ "detail varchar(500) DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_lifecycle_emp (emp_id),"
				+ "KEY idx_lifecycle_status (status),"
				+ "KEY idx_lifecycle_assignee (assignee_user_id, status)"
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
				+ "assignee_user_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_correction_status (status),"
				+ "KEY idx_correction_emp_date (emp_id, work_date),"
				+ "KEY idx_correction_assignee (assignee_user_id, status)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS overtime_request ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "work_date date NOT NULL,"
				+ "hours decimal(6,2) NOT NULL,"
				+ "reason varchar(300) DEFAULT NULL,"
				+ "status varchar(30) NOT NULL DEFAULT 'PENDING',"
				+ "approver_id bigint DEFAULT NULL,"
				+ "assignee_user_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_overtime_status (status),"
				+ "KEY idx_overtime_emp_date (emp_id, work_date),"
				+ "KEY idx_overtime_assignee (assignee_user_id, status)"
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

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS attendance_month_lock ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "cycle_month varchar(7) NOT NULL,"
				+ "status varchar(20) NOT NULL DEFAULT 'LOCKED',"
				+ "locked_by bigint DEFAULT NULL,"
				+ "locked_at datetime NOT NULL,"
				+ "remark varchar(255) DEFAULT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_att_lock_month (cycle_month)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS attendance_rule_config ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "work_start_time varchar(10) NOT NULL DEFAULT '09:00',"
				+ "work_end_time varchar(10) NOT NULL DEFAULT '18:00',"
				+ "late_grace_minutes int NOT NULL DEFAULT 0,"
				+ "early_leave_grace_minutes int NOT NULL DEFAULT 0,"
				+ "absence_rule varchar(40) NOT NULL DEFAULT 'AUTO_BASELINE',"
				+ "generate_baseline_on_holiday tinyint(1) NOT NULL DEFAULT 0,"
				+ "generate_baseline_on_workday tinyint(1) NOT NULL DEFAULT 1,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS department_shift_binding ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "dept_id bigint NOT NULL,"
				+ "shift_id bigint NOT NULL,"
				+ "status varchar(20) NOT NULL DEFAULT 'ACTIVE',"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_dept_shift (dept_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS employee_shift_binding ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "emp_id bigint NOT NULL,"
				+ "shift_id bigint NOT NULL,"
				+ "status varchar(20) NOT NULL DEFAULT 'ACTIVE',"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_emp_shift (emp_id)"
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
				+ "assignee_user_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "acknowledged_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "UNIQUE KEY uk_payslip_emp_month (emp_id, cycle_month),"
				+ "KEY idx_payslip_status (status),"
				+ "KEY idx_payslip_assignee (assignee_user_id, status)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS payslip_component ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "payslip_id bigint NOT NULL,"
				+ "component_type varchar(40) NOT NULL,"
				+ "amount decimal(12,2) NOT NULL DEFAULT 0.00,"
				+ "source_type varchar(40) DEFAULT NULL,"
				+ "source_id bigint DEFAULT NULL,"
				+ "remark varchar(255) DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_payslip_component_pid (payslip_id)"
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
				+ "assignee_user_id bigint DEFAULT NULL,"
				+ "approved_at datetime DEFAULT NULL,"
				+ "created_at datetime NOT NULL,"
				+ "updated_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_salary_change_status (status),"
				+ "KEY idx_salary_change_emp (emp_id),"
				+ "KEY idx_salary_change_assignee (assignee_user_id, status)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

		jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS notification ("
				+ "id bigint NOT NULL AUTO_INCREMENT,"
				+ "user_name varchar(100) DEFAULT NULL,"
				+ "emp_id bigint DEFAULT NULL,"
				+ "title varchar(120) NOT NULL,"
				+ "content varchar(500) DEFAULT NULL,"
				+ "biz_type varchar(40) DEFAULT NULL,"
				+ "biz_id bigint DEFAULT NULL,"
				+ "status varchar(20) NOT NULL DEFAULT 'UNREAD',"
				+ "created_at datetime NOT NULL,"
				+ "PRIMARY KEY (id),"
				+ "KEY idx_notification_user (user_name),"
				+ "KEY idx_notification_emp (emp_id),"
				+ "KEY idx_notification_status (status),"
				+ "KEY idx_notification_biz (biz_type, biz_id)"
				+ ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
	}

	private void ensureEnterpriseColumns() {
		ensureColumn("sys_user", "emp_id", "bigint NULL");
		ensureColumn("employee", "probation_start_date", "date NULL");
		ensureColumn("employee", "probation_end_date", "date NULL");
		ensureColumn("employee", "regularization_due_date", "date NULL");
		ensureColumn("employee", "resigned_at", "date NULL");
		ensureColumn("employee", "account_locked", "tinyint(1) NOT NULL DEFAULT 0");
		ensureColumn("leave_request", "assignee_user_id", "bigint NULL");
		ensureColumn("employee_lifecycle_event", "assignee_user_id", "bigint NULL");
		ensureColumn("employee_lifecycle_event", "target_salary", "decimal(12,2) NULL");
		ensureColumn("employee_lifecycle_event", "account_action", "varchar(40) NULL");
		ensureColumn("employee_lifecycle_event", "asset_handover_status", "varchar(40) NULL");
		ensureColumn("attendance_correction", "assignee_user_id", "bigint NULL");
		ensureColumn("attendance_correction", "approved_at", "datetime NULL");
		ensureColumn("overtime_request", "assignee_user_id", "bigint NULL");
		ensureColumn("overtime_request", "approved_at", "datetime NULL");
		ensureColumn("salary_change", "assignee_user_id", "bigint NULL");
		ensureColumn("salary_change", "approved_at", "datetime NULL");
		ensureColumn("payslip", "status", "varchar(30) NOT NULL DEFAULT 'PENDING'");
		ensureColumn("payslip", "approver_id", "bigint NULL");
		ensureColumn("payslip", "assignee_user_id", "bigint NULL");
		ensureColumn("payslip", "approved_at", "datetime NULL");
		ensureColumn("payslip", "acknowledged_at", "datetime NULL");
		ensureColumn("notification", "biz_type", "varchar(40) NULL");
		ensureColumn("notification", "biz_id", "bigint NULL");
		ensureColumn("audit_log", "before_value", "text NULL");
		ensureColumn("audit_log", "after_value", "text NULL");
		ensureColumn("audit_log", "ip_address", "varchar(80) NULL");
		ensureColumn("audit_log", "operator_username", "varchar(100) NULL");
	}

	private void ensureEnterpriseIndexes() {
		ensureIndex("employee", "idx_employee_dept_status", "create index idx_employee_dept_status on employee(dept_id, status)");
		ensureIndex("attendance_record", "idx_attendance_emp_date", "create index idx_attendance_emp_date on attendance_record(emp_id, work_date)");
		ensureIndex("attendance_monthly_summary", "idx_att_month_cycle_emp", "create index idx_att_month_cycle_emp on attendance_monthly_summary(cycle_month, emp_id)");
		ensureIndex("payslip", "idx_payslip_cycle_status", "create index idx_payslip_cycle_status on payslip(cycle_month, status)");
		ensureIndex("payslip", "idx_payslip_assignee", "create index idx_payslip_assignee on payslip(assignee_user_id, status)");
		ensureIndex("employee_lifecycle_event", "idx_lifecycle_assignee", "create index idx_lifecycle_assignee on employee_lifecycle_event(assignee_user_id, status)");
		ensureIndex("attendance_correction", "idx_correction_assignee", "create index idx_correction_assignee on attendance_correction(assignee_user_id, status)");
		ensureIndex("overtime_request", "idx_overtime_assignee", "create index idx_overtime_assignee on overtime_request(assignee_user_id, status)");
		ensureIndex("salary_change", "idx_salary_change_assignee", "create index idx_salary_change_assignee on salary_change(assignee_user_id, status)");
		ensureIndex("notification", "idx_notification_biz", "create index idx_notification_biz on notification(biz_type, biz_id)");
	}

	private void ensureBusinessUniqueIndexes() {
		// 先保留最新记录，再创建唯一索引，避免旧数据阻塞应用启动。
		// Existing demo databases may predate the unique constraints. Keep the newest row before adding them.
		jdbcTemplate.execute("delete a from attendance_record a join attendance_record b "
				+ "on a.emp_id = b.emp_id and a.work_date = b.work_date and a.id < b.id");
		jdbcTemplate.execute("delete a from payroll_record a join payroll_record b "
				+ "on a.emp_id = b.emp_id and a.cycle_month = b.cycle_month and a.id < b.id");
		ensureUniqueIndex("attendance_record", "uk_attendance_emp_date",
				"create unique index uk_attendance_emp_date on attendance_record(emp_id, work_date)");
		ensureUniqueIndex("payroll_record", "uk_payroll_emp_month",
				"create unique index uk_payroll_emp_month on payroll_record(emp_id, cycle_month)");
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

	private void ensureIndex(String table, String indexName, String ddl) {
		Integer cnt = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.statistics where table_schema = database() and table_name = ? and index_name = ?",
				Integer.class,
				table,
				indexName
		);
		if (cnt != null && cnt == 0) {
			jdbcTemplate.execute(ddl);
		}
	}

	private void ensureUniqueIndex(String table, String indexName, String ddl) {
		Integer cnt = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.statistics where table_schema = database() and table_name = ? and index_name = ? and non_unique = 0",
				Integer.class,
				table,
				indexName
		);
		if (cnt != null && cnt == 0) {
			jdbcTemplate.execute(ddl);
		}
	}

	private void cleanupPayrollBrackets() {
		jdbcTemplate.execute("update payroll_tax_bracket set min_income = 0 where min_income is null");
	}
}
