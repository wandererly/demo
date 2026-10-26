package com.hrm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = "spring.datasource.password=${DB_PASSWORD:041029}")
class HrmApplicationTests {

	@Test
	void contextLoads() {
	}

}
