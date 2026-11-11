package com.switchmate.common;

import java.util.Locale;

/**
 * Normalizes email addresses before they are stored or looked up, so the same
 * address always maps to the same account.
 */
public final class EmailNormalizer {

	private EmailNormalizer() {
	}

	public static String normalize(String email) {
		return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
	}
}
