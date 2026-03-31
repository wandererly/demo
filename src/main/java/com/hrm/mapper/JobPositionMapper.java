package com.hrm.mapper;

import com.hrm.domain.JobPosition;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface JobPositionMapper {

	@Select("select id, dept_id, level_id, position_code, position_name, path_group, description, status, created_at, updated_at "
			+ "from job_position order by id desc")
	List<JobPosition> findAll();

	@Insert("insert into job_position(dept_id, level_id, position_code, position_name, path_group, description, status, created_at, updated_at) "
			+ "values(#{deptId}, #{levelId}, #{positionCode}, #{positionName}, #{pathGroup}, #{description}, #{status}, now(), now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(JobPosition position);
}
