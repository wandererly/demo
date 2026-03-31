package com.hrm.mapper;

import com.hrm.domain.PerformanceCycle;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PerformanceCycleMapper {

	@Select("select id, name, start_date, end_date, status, created_at from performance_cycle order by id desc")
	List<PerformanceCycle> findAll();

	@Select("select id, name, start_date, end_date, status, created_at from performance_cycle where id = #{id}")
	PerformanceCycle findById(Long id);

	@Insert("insert into performance_cycle(name, start_date, end_date, status, created_at) "
			+ "values(#{name}, #{startDate}, #{endDate}, #{status}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PerformanceCycle cycle);

	@Update("update performance_cycle set name = #{name}, start_date = #{startDate}, end_date = #{endDate}, status = #{status} where id = #{id}")
	int update(PerformanceCycle cycle);

	@Delete("delete from performance_cycle where id = #{id}")
	int delete(Long id);
}
