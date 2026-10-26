package com.hrm.security;

import com.hrm.auth.AuthPermissionService;
import com.hrm.domain.SysUser;
import com.hrm.mapper.SysUserMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * 解析请求中的 JWT，并根据数据库中的当前账号状态和角色重新建立权限。
 * 每次请求查询账号，确保停用账号的旧令牌立即失效。
 */
public class JwtAuthFilter extends OncePerRequestFilter {

	private static final List<String> ADMIN_DEFAULT_PERMS = List.of(
			"rbac:manage",
			"config:manage",
			"hr:manage",
			"attendance:manage",
			"leave:manage",
			"perf:manage",
			"payroll:manage"
	);

	private final JwtUtil jwtUtil;
	private final SysUserMapper sysUserMapper;
	private final AuthPermissionService authPermissionService;

	public JwtAuthFilter(JwtUtil jwtUtil, SysUserMapper sysUserMapper, AuthPermissionService authPermissionService) {
		this.jwtUtil = jwtUtil;
		this.sysUserMapper = sysUserMapper;
		this.authPermissionService = authPermissionService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// 无令牌或令牌无效时交给后续安全链处理，由 Spring Security 返回未认证结果。
		String auth = request.getHeader("Authorization");
		if (auth != null && auth.startsWith("Bearer ")) {
			String token = auth.substring(7);
			try {
				Claims claims = jwtUtil.parse(token);
				String username = claims.getSubject();
				SysUser user = username == null ? null : sysUserMapper.findByUsername(username);
				if (user == null || !"ACTIVE".equalsIgnoreCase(user.getStatus())) {
					filterChain.doFilter(request, response);
					return;
				}
				String role = user.getRole();
				Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
				if (role != null && !role.isBlank()) {
					authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
				}
				List<String> perms = authPermissionService.permissionsForRole(role);
				for (String p : perms) {
					if (p != null) {
						authorities.add(new SimpleGrantedAuthority(p));
					}
				}

				if ("ADMIN".equalsIgnoreCase(role)) {
					boolean hasPerm = authorities.stream().anyMatch(a -> !a.getAuthority().startsWith("ROLE_"));
					if (!hasPerm) {
						for (String p : ADMIN_DEFAULT_PERMS) {
							authorities.add(new SimpleGrantedAuthority(p));
						}
					}
				}

				if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
					UsernamePasswordAuthenticationToken authentication =
							new UsernamePasswordAuthenticationToken(username, null, authorities);
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			} catch (Exception ex) {
				// invalid token, ignore and continue
			}
		}
		filterChain.doFilter(request, response);
	}
}
