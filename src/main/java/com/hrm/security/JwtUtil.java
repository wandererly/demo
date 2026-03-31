package com.hrm.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

	private final Key key;
	private final long expireMillis;

	public JwtUtil(@Value("${security.jwt.secret}") String secret,
				  @Value("${security.jwt.expire-minutes}") long expireMinutes) {
		this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
		this.expireMillis = expireMinutes * 60 * 1000;
	}

	public String generateToken(String username, String role, List<String> permissions) {
		Date now = new Date();
		Date exp = new Date(now.getTime() + expireMillis);
		return Jwts.builder()
				.setSubject(username)
				.claim("role", role)
				.claim("perms", permissions)
				.setIssuedAt(now)
				.setExpiration(exp)
				.signWith(key, SignatureAlgorithm.HS256)
				.compact();
	}

	public Claims parse(String token) {
		return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
	}
}
