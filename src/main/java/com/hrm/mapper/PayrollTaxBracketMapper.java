package com.hrm.mapper;

import com.hrm.domain.PayrollTaxBracket;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PayrollTaxBracketMapper {

	@Select("select id, rule_id, min_income, max_income, rate from payroll_tax_bracket where rule_id = #{ruleId} order by min_income asc")
	List<PayrollTaxBracket> findByRuleId(Long ruleId);

	@Insert("insert into payroll_tax_bracket(rule_id, min_income, max_income, rate) values(#{ruleId}, #{minIncome}, #{maxIncome}, #{rate})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PayrollTaxBracket bracket);

	@Delete("delete from payroll_tax_bracket where id = #{id}")
	int delete(Long id);
}
