package com.hrm.common.api;

import lombok.Data;

@Data
public class ApiResponse<T> {
	private int code;
	private String message;
	private T data;

	public static <T> ApiResponse<T> ok(T data) {
		ApiResponse<T> resp = new ApiResponse<>();
		resp.code = 0;
		resp.message = "OK";
		resp.data = data;
		return resp;
	}

	public static <T> ApiResponse<T> fail(int code, String message) {
		ApiResponse<T> resp = new ApiResponse<>();
		resp.code = code;
		resp.message = message;
		return resp;
	}
}
