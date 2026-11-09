package com.switchmate.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JjwtTokenService implements TokenService {

	private static final Duration ACCESS_TOKEN_TTL = Duration.ofHours(24);

	private final SecretKey signingKey;

	public JjwtTokenService(@Value("${app.jwt.secret}") String secret) {
		byte[] keyBytes = Decoders.BASE64.decode(secret);
		this.signingKey = Keys.hmacShaKeyFor(keyBytes);
	}

	@Override
	public String issue(UUID userId) {
		Instant now = Instant.now();
		return Jwts.builder()
				.subject(userId.toString())
				.issuedAt(Date.from(now))
				.expiration(Date.from(now.plus(ACCESS_TOKEN_TTL)))
				.signWith(signingKey)
				.compact();
	}

	@Override
	public UUID parse(String token) {
		String subject = Jwts.parser()
				.verifyWith(signingKey)
				.build()
				.parseSignedClaims(token)
				.getPayload()
				.getSubject();
		return UUID.fromString(subject);
	}
}