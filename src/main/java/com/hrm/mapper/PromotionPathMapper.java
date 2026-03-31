package com.hrm.mapper;

import com.hrm.domain.PromotionPath;
import java.util.List;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PromotionPathMapper {

	@Select("select id, from_position_id, to_position_id, min_months, condition_text, created_at from promotion_path order by id desc")
	List<PromotionPath> findAll();

	@Insert("insert into promotion_path(from_position_id, to_position_id, min_months, condition_text, created_at) "
			+ "values(#{fromPositionId}, #{toPositionId}, #{minMonths}, #{conditionText}, now())")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(PromotionPath path);
}
