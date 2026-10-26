package com.hrm.domain;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AttendanceRuleConfig {
	private Long id;
	private String workStartTime;
	private String workEndTime;
	private Integer lateGraceMinutes;
	private Integer earlyLeaveGraceMinutes;
	private String absenceRule;
	private Boolean generateBaselineOnHoliday;
	private Boolean generateBaselineOnWorkday;
	private LocalDateTime updatedAt;
}
