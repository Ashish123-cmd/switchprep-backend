package com.switchmate.auth.google;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.switchmate.common.exception.InvalidGoogleTokenException;

/**
 * Verifies Google ID tokens against Google's public keys. The verifier checks the
 * signature, expiry, issuer, and audience (the configured Google client ID).
 */
@Component
public class GoogleApiIdentityVerifier implements GoogleIdentityVerifier {

	private static final String NAME_CLAIM = "name";

	private final String clientId;
	private final NetHttpTransport httpTransport = new NetHttpTransport();

	public GoogleApiIdentityVerifier(@Value("${app.google.client-id:}") String clientId) {
		this.clientId = clientId == null ? "" : clientId.strip();
	}

	@Override
	public GoogleIdentity verify(String idToken) {
		if (clientId.isEmpty()) {
			throw new IllegalStateException("app.google.client-id (GOOGLE_CLIENT_ID) must be configured");
		}
		if (idToken == null || idToken.isBlank()) {
			throw new InvalidGoogleTokenException("Google ID token is required");
		}

		GoogleIdToken verifiedToken;
		try {
			verifiedToken = verifier().verify(idToken);
		} catch (GeneralSecurityException | IOException | RuntimeException exception) {
			throw new InvalidGoogleTokenException("Google ID token could not be verified", exception);
		}
		if (verifiedToken == null) {
			throw new InvalidGoogleTokenException("Google ID token is invalid or expired");
		}

		GoogleIdToken.Payload payload = verifiedToken.getPayload();
		String subject = payload.getSubject();
		String email = payload.getEmail();
		if (subject == null || subject.isBlank() || email == null || email.isBlank()) {
			throw new InvalidGoogleTokenException("Google ID token is missing required claims");
		}

		return new GoogleIdentity(
				subject,
				email,
				Boolean.TRUE.equals(payload.getEmailVerified()),
				(String) payload.get(NAME_CLAIM));
	}

	private GoogleIdTokenVerifier verifier() {
		return new GoogleIdTokenVerifier.Builder(httpTransport, GsonFactory.getDefaultInstance())
				.setAudience(List.of(clientId))
				.build();
	}
}
