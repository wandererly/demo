package com.hrm.auth;

import com.hrm.common.api.ApiResponse;
import com.hrm.common.constant.ErrorCode;
import com.hrm.common.exception.BizException;
import com.hrm.domain.SysUser;
import com.hrm.mapper.SysUserMapper;
import com.hrm.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
	public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
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

	@PostMapping("/register")
	public ApiResponse<SysUser> register(@RequestBody RegisterRequest request) {
		String username = request.getUsername() == null ? null : request.getUsername().trim();
		String password = request.getPassword() == null ? null : request.getPassword().trim();
		if (username == null || username.isBlank()) {
			throw new BizException(ErrorCode.BAD_REQUEST, "请输入用户名");
		}
		if (password == null || password.isBlank()) {
			throw new BizException(ErrorCode.BAD_REQUEST, "请输入密码");
		}
		SysUser existing = sysUserMapper.findByUsername(username);
		if (existing != null) {
			throw new BizException(ErrorCode.BAD_REQUEST, "用户已存在");
		}
		SysUser user = new SysUser();
		user.setUsername(username);
		user.setPasswordHash(passwordEncoder.encode(password));
		user.setRole("ADMIN");
		user.setStatus("ACTIVE");
		sysUserMapper.insert(user);
		return ApiResponse.ok(user);
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
