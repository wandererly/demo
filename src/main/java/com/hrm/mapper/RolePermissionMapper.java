package com.hrm.mapper;

import com.hrm.domain.RolePermission;
import java.util.List;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RolePermissionMapper {

	@Select("select p.perm_key from permission p "
			+ "join role_permission rp on p.id = rp.perm_id "
			+ "join sys_role r on r.id = rp.role_id "
			+ "where r.role_key = #{roleKey}")
	List<String> findPermKeysByRoleKey(String roleKey);

	@Insert("insert into role_permission(role_id, perm_id) values(#{roleId}, #{permId}) "
			+ "on duplicate key update role_id = role_id")
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(RolePermission rp);

	@Delete("delete rp from role_permission rp "
			+ "join role_permission rp2 on rp.role_id = rp2.role_id and rp.perm_id = rp2.perm_id and rp.id > rp2.id")
	int cleanDuplicates();

	@Delete("delete from role_permission where role_id = #{roleId} and perm_id = #{permId}")
	int delete(RolePermission rp);
}
