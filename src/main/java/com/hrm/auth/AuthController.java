package com.hrm.auth;

import com.hrm.common.api.ApiResponse;
import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.SysUser;
import com.hrm.mapper.SysUserMapper;
import com.hrm.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
/** 提供验证码、登录和当前账号信息接口。 */
public class AuthController {

	private final SysUserMapper sysUserMapper;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final CaptchaService captchaService;
	private final LoginAttemptService loginAttemptService;
	private final AuthPermissionService authPermissionService;

	public AuthController(SysUserMapper sysUserMapper,
						  PasswordEncoder passwordEncoder,
						  JwtUtil jwtUtil,
						  CaptchaService captchaService,
						  LoginAttemptService loginAttemptService,
						  AuthPermissionService authPermissionService) {
		this.sysUserMapper = sysUserMapper;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtil = jwtUtil;
		this.captchaService = captchaService;
		this.loginAttemptService = loginAttemptService;
		this.authPermissionService = authPermissionService;
	}

	@GetMapping("/captcha")
	public ApiResponse<CaptchaResponse> captcha() {
		return ApiResponse.ok(captchaService.generate());
	}

	@PostMapping("/login")
	public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
		// 登录顺序包含限流、验证码、账号状态和密码校验，避免无效请求直接消耗认证资源。
		String username = request.getUsername() == null ? null : request.getUsername().trim();
		String password = request.getPassword() == null ? null : request.getPassword().trim();
		String key = loginKey(username, httpRequest);
		if (loginAttemptService.isLocked(key)) {
			long secs = loginAttemptService.remainingLockSeconds(key);
			throw new BizException(ErrorCode.FORBIDDEN, "尝试次数过多，请在 " + secs + " 秒后重试");
		}

		if (!captchaService.validate(request.getCaptchaId(), request.getCaptchaCode())) {
			throw new BizException(ErrorCode.BAD_REQUEST, "验证码错误");
		}

		SysUser user = sysUserMapper.findByUsername(username);
		if (user == null) {
			loginAttemptService.recordFailure(key);
			throw new BizException(ErrorCode.BAD_REQUEST, "用户不存在");
		}
		if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
			throw new BizException(ErrorCode.FORBIDDEN, "当前账号已停用");
		}
		if (user.getRole() == null || user.getRole().isBlank()) {
			user.setRole("ADMIN");
			sysUserMapper.update(user);
		}
		if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
			throw new BizException(ErrorCode.SERVER_ERROR, "账户密码数据异常，请联系管理员");
		}

		boolean matches = password != null && passwordEncoder.matches(password, user.getPasswordHash());
		if (!matches) {
			loginAttemptService.recordFailure(key);
			throw new BizException(ErrorCode.BAD_REQUEST, "密码错误");
		}

		List<String> perms = authPermissionService.permissionsForRole(user.getRole());
		LoginResponse resp = new LoginResponse();
		resp.setUsername(user.getUsername());
		resp.setRole(user.getRole());
		resp.setEmpId(user.getEmpId());
		resp.setPermissions(perms);
		resp.setToken(jwtUtil.generateToken(user.getUsername(), user.getRole(), perms));
		loginAttemptService.reset(key);
		return ApiResponse.ok(resp);
	}

	@GetMapping("/me")
	public ApiResponse<SysUser> me() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return ApiResponse.ok(null);
		}
		String username = String.valueOf(auth.getPrincipal());
		SysUser user = sysUserMapper.findByUsername(username);
		return ApiResponse.ok(user);
	}

	private String loginKey(String username, HttpServletRequest request) {
		String ip = request.getHeader("X-Forwarded-For");
		if (ip == null || ip.isBlank()) {
			ip = request.getRemoteAddr();
		}
		return (username == null ? "_" : username) + "|" + ip;
	}
}
