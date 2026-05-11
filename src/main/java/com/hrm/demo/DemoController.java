package com.hrm.demo;

import com.hrm.common.api.ApiResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@Profile("demo")
public class DemoController {

	private final DemoDataService demoDataService;

	public DemoController(DemoDataService demoDataService) {
		this.demoDataService = demoDataService;
	}

	@PostMapping("/init")
	@PreAuthorize("hasAuthority('config:manage')")
	public ApiResponse<String> init() {
		demoDataService.initAll();
		return ApiResponse.ok("Demo data initialized");
	}
}
