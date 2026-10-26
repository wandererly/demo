package com.hrm.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
/** Spring Security、无状态会话、CORS 和 JWT 过滤器的统一配置。 */
public class SecurityConfig {

	@Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:8080}")
	private String allowedOrigins;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http, JwtUtil jwtUtil, com.hrm.mapper.SysUserMapper sysUserMapper,
			com.hrm.auth.AuthPermissionService authPermissionService) throws Exception {
		// 登录和验证码公开，其余 API 必须先通过 JWT 认证，再由控制器权限注解细分权限。
		http
				.cors(cors -> cors.configurationSource(corsConfigurationSource()))
				.csrf(csrf -> csrf.disable())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/auth/captcha", "/api/auth/login").permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/**").authenticated()
						.requestMatchers("/", "/index.html", "/assets/**", "/favicon.ico").permitAll()
						.anyRequest().authenticated()
				)
				.addFilterBefore(new JwtAuthFilter(jwtUtil, sysUserMapper, authPermissionService), UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		for (String origin : allowedOrigins.split(",")) {
			if (!origin.isBlank()) {
				config.addAllowedOrigin(origin.trim());
			}
		}
		config.addAllowedHeader("*");
		config.addAllowedMethod("*");
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}
}
