package com.hrm.mapper;

import com.hrm.domain.PayrollRule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PayrollRuleMapper {

	@Select("select id, overtime_multiplier, created_at from payroll_rule order by id desc limit 1")
	PayrollRule findLatest();

	@Insert("insert into payroll_rule(overtime_multiplier, created_at) values(#{overtimeMultiplier}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PayrollRule rule);

	@Update("update payroll_rule set overtime_multiplier = #{overtimeMultiplier} where id = #{id}")
	int update(PayrollRule rule);
}
