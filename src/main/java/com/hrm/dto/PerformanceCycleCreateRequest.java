package com.hrm.dto;

import java.time.LocalDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PerformanceCycleCreateRequest {
	@NotBlank(message = "Name is required")
	private String name;
	@NotNull(message = "Start date is required")
	private LocalDate startDate;
	@NotNull(message = "End date is required")
	private LocalDate endDate;
}
