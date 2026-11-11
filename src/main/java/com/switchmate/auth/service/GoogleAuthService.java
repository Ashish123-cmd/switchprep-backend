package com.switchmate.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.GoogleAuthResponse;
import com.switchmate.auth.google.GoogleIdentity;
import com.switchmate.auth.google.GoogleIdentityVerifier;
import com.switchmate.common.EmailNormalizer;
import com.switchmate.common.exception.InvalidGoogleTokenException;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.UserRepository;
import com.switchmate.security.TokenService;

@Service
public class GoogleAuthService {

	private final GoogleIdentityVerifier googleIdentityVerifier;
	private final UserRepository userRepository;
	private final UserAccountLookup userAccountLookup;
	private final UserProvisioningService userProvisioningService;
	private final TokenService tokenService;

	public GoogleAuthService(
			GoogleIdentityVerifier googleIdentityVerifier,
			UserRepository userRepository,
			UserAccountLookup userAccountLookup,
			UserProvisioningService userProvisioningService,
			TokenService tokenService) {
		this.googleIdentityVerifier = googleIdentityVerifier;
		this.userRepository = userRepository;
		this.userAccountLookup = userAccountLookup;
		this.userProvisioningService = userProvisioningService;
		this.tokenService = tokenService;
	}

	@Transactional
	public GoogleAuthResponse authenticate(String idToken) {
		GoogleIdentity identity = googleIdentityVerifier.verify(idToken);
		if (!identity.emailVerified()) {
			throw new InvalidGoogleTokenException("Google account email is not verified");
		}

		String email = EmailNormalizer.normalize(identity.email());
		Optional<User> existingUser = userRepository.findByGoogleSubject(identity.subject());
		User user;
		boolean isNewUser = false;
		if (existingUser.isPresent()) {
			user = existingUser.get();
		} else {
			Optional<User> userByEmail = userRepository.findByEmail(email);
			if (userByEmail.isPresent()) {
				user = linkGoogleSubject(userByEmail.get(), identity.subject());
			} else {
				user = userProvisioningService.createGoogleUser(email, displayName(identity, email), identity.subject());
				isNewUser = true;
			}
		}

		Profile profile = userAccountLookup.requireProfile(user.getId());
		return new GoogleAuthResponse(
				tokenService.issue(user.getId()),
				UserResponseMapper.toResponse(user, profile),
				isNewUser);
	}

	private User linkGoogleSubject(User user, String googleSubject) {
		user.setGoogleSubject(googleSubject);
		return userRepository.save(user);
	}

	private String displayName(GoogleIdentity identity, String email) {
		String name = identity.name();
		return name == null || name.isBlank() ? email : name.strip();
	}
}

