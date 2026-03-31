package com.hrm.auth;

import com.hrm.domain.Permission;
import com.hrm.mapper.PermissionMapper;
import com.hrm.mapper.RolePermissionMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AuthPermissionService {

	private final RolePermissionMapper rolePermissionMapper;
	private final PermissionMapper permissionMapper;

	public AuthPermissionService(RolePermissionMapper rolePermissionMapper,
								 PermissionMapper permissionMapper) {
		this.rolePermissionMapper = rolePermissionMapper;
		this.permissionMapper = permissionMapper;
	}

	public List<String> permissionsForRole(String roleKey) {
		if (roleKey == null || roleKey.isBlank()) {
			return Collections.emptyList();
		}
		List<String> perms = rolePermissionMapper.findPermKeysByRoleKey(roleKey);
		if (perms == null || perms.isEmpty()) {
			if ("ADMIN".equalsIgnoreCase(roleKey)) {
				List<Permission> all = permissionMapper.findAll();
				List<String> keys = new ArrayList<>();
				for (Permission p : all) {
					if (p != null && p.getPermKey() != null) {
						keys.add(p.getPermKey());
					}
				}
				return keys;
			}
			return new ArrayList<>();
		}
		return perms;
	}
}
