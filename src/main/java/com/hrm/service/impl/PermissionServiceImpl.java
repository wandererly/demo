package com.hrm.service.impl;

import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.Permission;
import com.hrm.domain.RolePermission;
import com.hrm.domain.SysRole;
import com.hrm.dto.PermissionCreateRequest;
import com.hrm.dto.RolePermAssignRequest;
import com.hrm.mapper.PermissionMapper;
import com.hrm.mapper.RolePermissionMapper;
import com.hrm.mapper.SysRoleMapper;
import com.hrm.service.PermissionService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PermissionServiceImpl implements PermissionService {

	private final PermissionMapper permissionMapper;
	private final RolePermissionMapper rolePermissionMapper;
	private final SysRoleMapper sysRoleMapper;

	public PermissionServiceImpl(PermissionMapper permissionMapper,
							 RolePermissionMapper rolePermissionMapper,
							 SysRoleMapper sysRoleMapper) {
		this.permissionMapper = permissionMapper;
		this.rolePermissionMapper = rolePermissionMapper;
		this.sysRoleMapper = sysRoleMapper;
	}

	@Override
	public List<Permission> listPermissions() {
		return permissionMapper.findAll();
	}

	@Override
	public Permission createPermission(PermissionCreateRequest request) {
		Permission exists = permissionMapper.findByKey(request.getPermKey());
		if (exists != null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "Permission already exists");
		}
		Permission permission = new Permission();
		permission.setPermKey(request.getPermKey());
		permission.setPermName(request.getPermName());
		permissionMapper.insert(permission);
		return permission;
	}

	@Override
	public void deletePermission(Long id) {
		int rows = permissionMapper.delete(id);
		if (rows == 0) {
			throw new BizException(ErrorCode.NOT_FOUND, "Permission not found");
		}
	}

	@Override
	public List<String> listPermKeysByRole(String roleKey) {
		return rolePermissionMapper.findPermKeysByRoleKey(roleKey);
	}

	@Override
	public void assignPerm(RolePermAssignRequest request) {
		SysRole role = sysRoleMapper.findByKey(request.getRoleKey());
		if (role == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Role not found");
		}
		Permission perm = permissionMapper.findByKey(request.getPermKey());
		if (perm == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Permission not found");
		}
		RolePermission rp = new RolePermission();
		rp.setRoleId(role.getId());
		rp.setPermId(perm.getId());
		rolePermissionMapper.insert(rp);
	}

	@Override
	public void revokePerm(RolePermAssignRequest request) {
		SysRole role = sysRoleMapper.findByKey(request.getRoleKey());
		if (role == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Role not found");
		}
		Permission perm = permissionMapper.findByKey(request.getPermKey());
		if (perm == null) {
			throw new BizException(ErrorCode.NOT_FOUND, "Permission not found");
		}
		RolePermission rp = new RolePermission();
		rp.setRoleId(role.getId());
		rp.setPermId(perm.getId());
		rolePermissionMapper.delete(rp);
	}
}
