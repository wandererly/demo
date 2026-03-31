package com.hrm.service;

import com.hrm.domain.Permission;
import com.hrm.dto.PermissionCreateRequest;
import com.hrm.dto.RolePermAssignRequest;
import java.util.List;

public interface PermissionService {
	List<Permission> listPermissions();
	Permission createPermission(PermissionCreateRequest request);
	void deletePermission(Long id);
	List<String> listPermKeysByRole(String roleKey);
	void assignPerm(RolePermAssignRequest request);
	void revokePerm(RolePermAssignRequest request);
}
