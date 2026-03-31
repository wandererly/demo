package com.hrm.auth;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class LoginAttemptService {

	private static final int MAX_ATTEMPTS = 5;
	private static final long LOCK_MILLIS = 10 * 60 * 1000;

	private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

	public boolean isLocked(String key) {
		Attempt attempt = attempts.get(key);
		if (attempt == null) {
			return false;
		}
		if (Instant.now().toEpochMilli() > attempt.lockUntil) {
			attempts.remove(key);
			return false;
		}
		return attempt.lockUntil > 0;
	}

	public long remainingLockSeconds(String key) {
		Attempt attempt = attempts.get(key);
		if (attempt == null || attempt.lockUntil <= 0) {
			return 0;
		}
		long remain = attempt.lockUntil - Instant.now().toEpochMilli();
		return Math.max(0, remain / 1000);
	}

	public void recordFailure(String key) {
		Attempt attempt = attempts.getOrDefault(key, new Attempt());
		attempt.count += 1;
		if (attempt.count >= MAX_ATTEMPTS) {
			attempt.lockUntil = Instant.now().toEpochMilli() + LOCK_MILLIS;
		}
		attempts.put(key, attempt);
	}

	public void reset(String key) {
		attempts.remove(key);
	}

	private static class Attempt {
		private int count;
		private long lockUntil;
	}
}
