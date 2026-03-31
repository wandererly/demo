package com.hrm.mapper;

import com.hrm.domain.JobLevel;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobLevelMapper {

	@Select("select id, level_code, level_name, `rank`, salary_min, salary_max, description, status, created_at from job_level order by `rank` asc")
	List<JobLevel> findAll();

	@Insert("insert into job_level(level_code, level_name, `rank`, salary_min, salary_max, description, status, created_at) "
			+ "values(#{levelCode}, #{levelName}, #{rank}, #{salaryMin}, #{salaryMax}, #{description}, #{status}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(JobLevel level);
}
