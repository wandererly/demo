package com.hrm.mapper;

import com.hrm.domain.Permission;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface PermissionMapper {

	@Select("select id, perm_key, perm_name from permission order by id asc")
	List<Permission> findAll();

	@Select("select id, perm_key, perm_name from permission where perm_key = #{permKey}")
	Permission findByKey(String permKey);

	@Insert("insert into permission(perm_key, perm_name) values(#{permKey}, #{permName})")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(Permission permission);

	@Delete("delete from permission where id = #{id}")
	int delete(Long id);
}
