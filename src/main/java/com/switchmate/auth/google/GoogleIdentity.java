package com.switchmate.auth.google;

/**
 * Identity claims read from a verified Google ID token.
 */
public record GoogleIdentity(
		String subject,
		String email,
		boolean emailVerified,
		String name) {
}
