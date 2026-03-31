package com.hrm.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class DemoDataInitializer implements CommandLineRunner {

	private final DemoDataService demoDataService;

	public DemoDataInitializer(DemoDataService demoDataService) {
		this.demoDataService = demoDataService;
	}

	@Override
	public void run(String... args) {
		demoDataService.initAll();
	}
}
