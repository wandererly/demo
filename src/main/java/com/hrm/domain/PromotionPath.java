package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class PromotionPath {
	private Long id;
	private Long fromPositionId;
	private Long toPositionId;
	private Integer minMonths;
	private String conditionText;
	private LocalDateTime createdAt;
}
