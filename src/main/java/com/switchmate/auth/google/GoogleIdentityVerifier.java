package com.switchmate.auth.google;

/**
 * Verifies a Google-issued ID token and exposes its identity claims.
 */
public interface GoogleIdentityVerifier {

	GoogleIdentity verify(String idToken);
}
