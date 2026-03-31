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
		ensurePerformanceReviewColumns();
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
