package com.switchmate.common.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InMemoryRateLimiterTest {

	private InMemoryRateLimiter rateLimiter;

	@BeforeEach
	void setUp() {
		rateLimiter = new InMemoryRateLimiter(5, 10); // 5 requests per 10 minutes
	}

	@Test
	void tryAcquire_allowsRequestsWithinLimit() {
		String ip = "192.168.1.1";
		
		for (int i = 0; i < 5; i++) {
			assertTrue(rateLimiter.tryAcquire(ip), "Request " + (i + 1) + " should be allowed");
		}
	}

	@Test
	void tryAcquire_rejectsRequestsOverLimit() {
		String ip = "192.168.1.2";
		
		for (int i = 0; i < 5; i++) {
			assertTrue(rateLimiter.tryAcquire(ip));
		}
		
		assertFalse(rateLimiter.tryAcquire(ip), "6th request should be rejected");
		assertFalse(rateLimiter.tryAcquire(ip), "7th request should be rejected");
	}

	@Test
	void tryAcquire_differentKeysAreIndependent() {
		assertTrue(rateLimiter.tryAcquire("10.0.0.1"));
		assertTrue(rateLimiter.tryAcquire("10.0.0.2"));
		
		for (int i = 0; i < 4; i++) {
			assertTrue(rateLimiter.tryAcquire("10.0.0.1"));
		}
		
		assertFalse(rateLimiter.tryAcquire("10.0.0.1"), "First IP should be rate limited");
		assertTrue(rateLimiter.tryAcquire("10.0.0.2"), "Second IP should still have capacity");
	}

	@Test
	void tryAcquire_resetsAfterWindow() {
		MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
		InMemoryRateLimiter shortWindowLimiter = new InMemoryRateLimiter(2, 10, clock);
		String ip = "192.168.1.10";

		assertTrue(shortWindowLimiter.tryAcquire(ip));
		assertTrue(shortWindowLimiter.tryAcquire(ip));
		assertFalse(shortWindowLimiter.tryAcquire(ip), "Third request within the window should be rejected");

		clock.advance(Duration.ofMinutes(11));

		assertTrue(shortWindowLimiter.tryAcquire(ip), "Window should reset after elapsed time");
	}

	@Test
	void release_clearsKey() {
		String ip = "192.168.1.20";
		
		for (int i = 0; i < 5; i++) {
			assertTrue(rateLimiter.tryAcquire(ip));
		}
		assertFalse(rateLimiter.tryAcquire(ip));
		
		rateLimiter.release(ip);
		
		assertTrue(rateLimiter.tryAcquire(ip), "Should allow after release");
	}

	private static final class MutableClock extends Clock {

		private Instant instant;

		private MutableClock(Instant instant) {
			this.instant = instant;
		}

		private void advance(Duration duration) {
			instant = instant.plus(duration);
		}

		@Override
		public ZoneId getZone() {
			return ZoneId.of("UTC");
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}
	}
}