package com.hrm.mapper;

import com.hrm.domain.PerformanceIndicator;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PerformanceIndicatorMapper {

	@Select("select id, cycle_id, name, weight, description from performance_indicator where cycle_id = #{cycleId} order by id desc")
	List<PerformanceIndicator> findByCycleId(Long cycleId);

	@Insert("insert into performance_indicator(cycle_id, name, weight, description) values(#{cycleId}, #{name}, #{weight}, #{description})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PerformanceIndicator indicator);

	@Update("update performance_indicator set name = #{name}, weight = #{weight}, description = #{description} where id = #{id}")
	int update(PerformanceIndicator indicator);

	@Delete("delete from performance_indicator where id = #{id}")
	int delete(Long id);
}
