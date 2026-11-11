package com.switchmate.auth.service;

import java.util.Locale;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.GoogleAuthResponse;
import com.switchmate.auth.google.GoogleIdentity;
import com.switchmate.auth.google.GoogleIdentityVerifier;
import com.switchmate.common.exception.InvalidGoogleTokenException;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.UserRepository;
import com.switchmate.security.TokenService;

@Service
public class GoogleAuthService {

	private final GoogleIdentityVerifier googleIdentityVerifier;
	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final UserProvisioningService userProvisioningService;
	private final TokenService tokenService;

	public GoogleAuthService(
			GoogleIdentityVerifier googleIdentityVerifier,
			UserRepository userRepository,
			ProfileRepository profileRepository,
			UserProvisioningService userProvisioningService,
			TokenService tokenService) {
		this.googleIdentityVerifier = googleIdentityVerifier;
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
		this.userProvisioningService = userProvisioningService;
		this.tokenService = tokenService;
	}

	@Transactional
	public GoogleAuthResponse authenticate(String idToken) {
		GoogleIdentity identity = googleIdentityVerifier.verify(idToken);
		if (!identity.emailVerified()) {
			throw new InvalidGoogleTokenException("Google account email is not verified");
		}

		String email = normalizeEmail(identity.email());
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

		Profile profile = profileRepository.findById(user.getId())
				.orElseThrow(() -> new NotFoundException("Profile not found"));
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

	private String normalizeEmail(String email) {
		return email == null ? "" : email.strip().toLowerCase(Locale.ROOT);
	}
}

