package com.hrm.mapper;

import com.hrm.domain.SalaryStructure;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SalaryStructureMapper {

	@Select("select id, emp_id, base_salary, allowance, bonus, social_security, housing_fund, tax_rate from salary_structure where emp_id = #{empId}")
	SalaryStructure findByEmpId(Long empId);

	@Insert("insert into salary_structure(emp_id, base_salary, allowance, bonus, social_security, housing_fund, tax_rate) "
			+ "values(#{empId}, #{baseSalary}, #{allowance}, #{bonus}, #{socialSecurity}, #{housingFund}, #{taxRate})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(SalaryStructure structure);

	@Update("update salary_structure set base_salary = #{baseSalary}, allowance = #{allowance}, bonus = #{bonus}, "
			+ "social_security = #{socialSecurity}, housing_fund = #{housingFund}, tax_rate = #{taxRate} where emp_id = #{empId}")
	int updateByEmpId(SalaryStructure structure);
}
