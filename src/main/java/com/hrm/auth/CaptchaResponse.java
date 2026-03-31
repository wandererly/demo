package com.hrm.auth;

import lombok.Data;

@Data
public class CaptchaResponse {
	private String captchaId;
	private String imageBase64;
}
