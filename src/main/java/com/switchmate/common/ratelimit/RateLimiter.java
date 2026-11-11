package com.switchmate.common.ratelimit;

public interface RateLimiter {

	boolean tryAcquire(String key);

	void release(String key);
}