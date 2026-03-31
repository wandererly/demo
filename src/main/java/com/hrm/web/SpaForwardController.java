package com.hrm.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaForwardController {

	@RequestMapping({
			"/",
			"/login",
			"/departments",
			"/employees",
			"/attendance",
			"/leave",
			"/performance",
			"/payroll",
			"/rbac",
			"/settings",
			"/org"
	})
	public String forwardIndex() {
		return "forward:/index.html";
	}
}
