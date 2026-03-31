package com.hrm.mapper;

import com.hrm.domain.PerformanceRule;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PerformanceRuleMapper {

	@Select("select id, min_score, max_score, level from performance_rule order by min_score asc")
	List<PerformanceRule> findAll();

	@Insert("insert into performance_rule(min_score, max_score, level) values(#{minScore}, #{maxScore}, #{level})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PerformanceRule rule);

	@Delete("delete from performance_rule where id = #{id}")
	int delete(Long id);
}
