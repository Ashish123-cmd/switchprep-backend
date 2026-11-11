package com.switchmate.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Authenticates operator requests to {@code /api/admin/**} that present the
 * configured {@code X-Admin-Api-Key}. Admin endpoints are therefore never
 * anonymous: a request either carries a valid API key or the user JWT used by
 * the regular API.
 */
@Component
public class AdminApiKeyAuthenticationFilter extends OncePerRequestFilter {

	public static final String ADMIN_API_KEY_HEADER = "X-Admin-Api-Key";
	public static final String ADMIN_ROLE = "ADMIN";

	private static final String ADMIN_PATH_PREFIX = "/api/admin/";
	private static final String ADMIN_AUTHORITY = "ROLE_" + ADMIN_ROLE;

	private final byte[] configuredApiKey;

	public AdminApiKeyAuthenticationFilter(@Value("${app.admin.api-key:}") String configuredApiKey) {
		this.configuredApiKey = configuredApiKey == null
				? new byte[0]
				: configuredApiKey.getBytes(StandardCharsets.UTF_8);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !request.getRequestURI().startsWith(ADMIN_PATH_PREFIX)
				|| SecurityContextHolder.getContext().getAuthentication() != null;
	}

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		String providedApiKey = request.getHeader(ADMIN_API_KEY_HEADER);
		if (matches(providedApiKey)) {
			UsernamePasswordAuthenticationToken authentication = UsernamePasswordAuthenticationToken.authenticated(
					"admin", null, List.of(new SimpleGrantedAuthority(ADMIN_AUTHORITY)));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		}
		filterChain.doFilter(request, response);
	}

	private boolean matches(String providedApiKey) {
		if (configuredApiKey.length == 0 || providedApiKey == null) {
			return false;
		}
		return MessageDigest.isEqual(configuredApiKey, providedApiKey.getBytes(StandardCharsets.UTF_8));
	}
}
