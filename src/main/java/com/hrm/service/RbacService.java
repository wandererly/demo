package com.hrm.service;

import com.hrm.domain.SysRole;
import com.hrm.domain.SysUser;
import com.hrm.domain.SysUserRole;
import com.hrm.dto.RoleCreateRequest;
import com.hrm.dto.RoleUpdateRequest;
import com.hrm.dto.UserCreateRequest;
import com.hrm.dto.UserRoleAssignRequest;
import com.hrm.dto.UserUpdateRequest;
import java.util.List;

public interface RbacService {
	List<SysUser> listUsers();
	SysUser createUser(UserCreateRequest request);
	SysUser updateUser(Long id, UserUpdateRequest request);
	void deleteUser(Long id);

	List<SysRole> listRoles();
	SysRole createRole(RoleCreateRequest request);
	SysRole updateRole(Long id, RoleUpdateRequest request);
	void deleteRole(Long id);

	SysUserRole assignRole(UserRoleAssignRequest request);
}
