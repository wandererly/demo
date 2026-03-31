package com.hrm.mapper;

import com.hrm.domain.LeaveRule;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface LeaveRuleMapper {

	@Select("select id, max_days_per_request, approval_level, created_at from leave_rule order by id desc limit 1")
	LeaveRule findLatest();

	@Insert("insert into leave_rule(max_days_per_request, approval_level, created_at) values(#{maxDaysPerRequest}, #{approvalLevel}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(LeaveRule rule);

	@Update("update leave_rule set max_days_per_request = #{maxDaysPerRequest}, approval_level = #{approvalLevel} where id = #{id}")
	int update(LeaveRule rule);
}
