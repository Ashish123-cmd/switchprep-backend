package com.switchmate.auth.google;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.switchmate.common.exception.InvalidGoogleTokenException;

class GoogleApiIdentityVerifierTest {

	@Test
	void failsClearlyWhenClientIdIsNotConfigured() {
		GoogleApiIdentityVerifier verifier = new GoogleApiIdentityVerifier("");

		assertThrows(IllegalStateException.class, () -> verifier.verify("google-id-token"));
	}

	@Test
	void rejectsBlankTokenWithoutCallingGoogle() {
		GoogleApiIdentityVerifier verifier = new GoogleApiIdentityVerifier("client-id.apps.googleusercontent.com");

		assertThrows(InvalidGoogleTokenException.class, () -> verifier.verify(" "));
	}
}
