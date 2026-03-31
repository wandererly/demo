package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.SysRole;
import com.hrm.domain.SysUser;
import com.hrm.domain.SysUserRole;
import com.hrm.dto.RoleCreateRequest;
import com.hrm.dto.RoleUpdateRequest;
import com.hrm.dto.UserCreateRequest;
import com.hrm.dto.UserRoleAssignRequest;
import com.hrm.dto.UserUpdateRequest;
import com.hrm.service.RbacService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rbac")
@Validated
@PreAuthorize("hasAuthority('rbac:manage')")
public class RbacController {

	private final RbacService rbacService;

	public RbacController(RbacService rbacService) {
		this.rbacService = rbacService;
	}

	@GetMapping("/users")
	public ApiResponse<List<SysUser>> listUsers() {
		return ApiResponse.ok(rbacService.listUsers());
	}

	@PostMapping("/users")
	public ApiResponse<SysUser> createUser(@Valid @RequestBody UserCreateRequest request) {
		return ApiResponse.ok(rbacService.createUser(request));
	}

	@PutMapping("/users/{id}")
	public ApiResponse<SysUser> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
		return ApiResponse.ok(rbacService.updateUser(id, request));
	}

	@DeleteMapping("/users/{id}")
	public ApiResponse<Void> deleteUser(@PathVariable Long id) {
		rbacService.deleteUser(id);
		return ApiResponse.ok(null);
	}

	@GetMapping("/roles")
	public ApiResponse<List<SysRole>> listRoles() {
		return ApiResponse.ok(rbacService.listRoles());
	}

	@PostMapping("/roles")
	public ApiResponse<SysRole> createRole(@Valid @RequestBody RoleCreateRequest request) {
		return ApiResponse.ok(rbacService.createRole(request));
	}

	@PutMapping("/roles/{id}")
	public ApiResponse<SysRole> updateRole(@PathVariable Long id, @RequestBody RoleUpdateRequest request) {
		return ApiResponse.ok(rbacService.updateRole(id, request));
	}

	@DeleteMapping("/roles/{id}")
	public ApiResponse<Void> deleteRole(@PathVariable Long id) {
		rbacService.deleteRole(id);
		return ApiResponse.ok(null);
	}

	@PostMapping("/assign")
	public ApiResponse<SysUserRole> assignRole(@Valid @RequestBody UserRoleAssignRequest request) {
		return ApiResponse.ok(rbacService.assignRole(request));
	}
}
