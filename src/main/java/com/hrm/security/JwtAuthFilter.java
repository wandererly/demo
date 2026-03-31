package com.hrm.security;

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

	public JwtAuthFilter(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		String auth = request.getHeader("Authorization");
		if (auth != null && auth.startsWith("Bearer ")) {
			String token = auth.substring(7);
			try {
				Claims claims = jwtUtil.parse(token);
				String username = claims.getSubject();
				String role = claims.get("role", String.class);
				if ((role == null || role.isBlank()) && "admin".equalsIgnoreCase(username)) {
					role = "ADMIN";
				}
				Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
				if (role != null && !role.isBlank()) {
					authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
				}
				Object permsObj = claims.get("perms");
				if (permsObj instanceof List<?> perms) {
					for (Object p : perms) {
						if (p != null) {
							authorities.add(new SimpleGrantedAuthority(String.valueOf(p)));
						}
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
