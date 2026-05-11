package com.hrm.mapper;

import com.hrm.domain.PayrollRecord;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PayrollRecordMapper {

	@Select("select id, emp_id, cycle_month, gross_salary, net_salary, tax, overtime_pay, leave_deduction, status, created_at "
			+ "from payroll_record order by id desc")
	List<PayrollRecord> findAll();

	@Select("select id, emp_id, cycle_month, gross_salary, net_salary, tax, overtime_pay, leave_deduction, status, created_at "
			+ "from payroll_record where emp_id = #{empId} order by id desc")
	List<PayrollRecord> findByEmpId(Long empId);

	@Insert("insert into payroll_record(emp_id, cycle_month, gross_salary, net_salary, tax, overtime_pay, leave_deduction, status, created_at) "
			+ "values(#{empId}, #{cycleMonth}, #{grossSalary}, #{netSalary}, #{tax}, #{overtimePay}, #{leaveDeduction}, #{status}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PayrollRecord record);
}
