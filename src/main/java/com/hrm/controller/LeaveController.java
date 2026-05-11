package com.hrm.controller;

import com.hrm.common.api.ApiResponse;
import com.hrm.domain.LeaveRequest;
import com.hrm.dto.LeaveApproveRequest;
import com.hrm.dto.LeaveCreateRequest;
import com.hrm.dto.LeaveUpdateRequest;
import com.hrm.service.LeaveService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leave")
@Validated
@PreAuthorize("hasAuthority('leave:manage')")
public class LeaveController {

	private final LeaveService leaveService;

	public LeaveController(LeaveService leaveService) {
		this.leaveService = leaveService;
	}

	@GetMapping
	public ApiResponse<List<LeaveRequest>> list() {
		return ApiResponse.ok(leaveService.list());
	}

	@GetMapping("/{id}")
	public ApiResponse<LeaveRequest> get(@PathVariable Long id) {
		return ApiResponse.ok(leaveService.get(id));
	}

	@PostMapping
	public ApiResponse<LeaveRequest> create(@Valid @RequestBody LeaveCreateRequest request) {
		return ApiResponse.ok(leaveService.create(request));
	}

	@PutMapping("/{id}")
	public ApiResponse<LeaveRequest> update(@PathVariable Long id, @Valid @RequestBody LeaveUpdateRequest request) {
		return ApiResponse.ok(leaveService.update(id, request));
	}

	@PutMapping("/{id}/approve")
	public ApiResponse<LeaveRequest> approve(@PathVariable Long id,
									   @Valid @RequestBody LeaveApproveRequest request) {
		return ApiResponse.ok(leaveService.approve(id, request));
	}

	@PostMapping("/{id}/approve")
	public ApiResponse<LeaveRequest> approveByPost(@PathVariable Long id,
											@Valid @RequestBody LeaveApproveRequest request) {
		return ApiResponse.ok(leaveService.approve(id, request));
	}

	@DeleteMapping("/{id}")
	public ApiResponse<Void> delete(@PathVariable Long id) {
		leaveService.delete(id);
		return ApiResponse.ok(null);
	}
}



