package com.switchmate.common.ratelimit;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRateLimiter implements RateLimiter {

	private final int maxRequests;
	private final long windowMillis;
	private final Clock clock;

	private final Map<String, RequestWindow> windows = new ConcurrentHashMap<>();

	@Autowired
	public InMemoryRateLimiter(
			@org.springframework.beans.factory.annotation.Value("${app.rate-limit.signup-resume.max-requests:5}") int maxRequests,
			@org.springframework.beans.factory.annotation.Value("${app.rate-limit.signup-resume.window-minutes:10}") long windowMinutes) {
		this(maxRequests, windowMinutes, Clock.systemUTC());
	}

	InMemoryRateLimiter(int maxRequests, long windowMinutes, Clock clock) {
		this.maxRequests = maxRequests;
		this.windowMillis = TimeUnit.MINUTES.toMillis(windowMinutes);
		this.clock = clock;
	}

	@Override
	public boolean tryAcquire(String key) {
		Instant now = clock.instant();
		RequestWindow window = windows.computeIfAbsent(key, k -> new RequestWindow());
		synchronized (window) {
			window.prune(now, windowMillis);
			if (window.requests.size() >= maxRequests) {
				return false;
			}
			window.requests.add(now);
			return true;
		}
	}

	@Override
	public void release(String key) {
		windows.remove(key);
	}

	private static class RequestWindow {
		private final List<Instant> requests = new ArrayList<>();

		void prune(Instant now, long windowMillis) {
			Instant cutoff = now.minusMillis(windowMillis);
			requests.removeIf(t -> t.isBefore(cutoff));
		}
	}
}