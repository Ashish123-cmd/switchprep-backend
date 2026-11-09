package com.switchmate.auth.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.AuthResponse;
import com.switchmate.auth.dto.LoginRequest;
import com.switchmate.auth.dto.ProfileResponse;
import com.switchmate.auth.dto.SignupRequest;
import com.switchmate.auth.dto.UserResponse;
import com.switchmate.common.exception.ConflictException;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.AutoApplySettings;
import com.switchmate.domain.entity.JobPreference;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.StandardAnswer;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.AutoApplySettingsRepository;
import com.switchmate.repository.JobPreferenceRepository;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.StandardAnswerRepository;
import com.switchmate.repository.UserRepository;
import com.switchmate.security.TokenService;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final JobPreferenceRepository jobPreferenceRepository;
	private final StandardAnswerRepository standardAnswerRepository;
	private final AutoApplySettingsRepository autoApplySettingsRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public AuthService(
			UserRepository userRepository,
			ProfileRepository profileRepository,
			JobPreferenceRepository jobPreferenceRepository,
			StandardAnswerRepository standardAnswerRepository,
			AutoApplySettingsRepository autoApplySettingsRepository,
			PasswordEncoder passwordEncoder,
			TokenService tokenService) {
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
		this.jobPreferenceRepository = jobPreferenceRepository;
		this.standardAnswerRepository = standardAnswerRepository;
		this.autoApplySettingsRepository = autoApplySettingsRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
	}

	@Transactional
	public AuthResponse signup(SignupRequest request) {
		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email is already registered");
		}

		User user = userRepository.save(new User(email, passwordEncoder.encode(request.password()), request.name().strip()));
		UUID userId = user.getId();
		Profile profile = profileRepository.save(new Profile(userId));
		jobPreferenceRepository.save(new JobPreference(userId));
		standardAnswerRepository.save(new StandardAnswer(userId));
		autoApplySettingsRepository.save(new AutoApplySettings(userId));

		return new AuthResponse(tokenService.issue(userId), toUserResponse(user, profile));
	}

	@Transactional(readOnly = true)
	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByEmail(normalizeEmail(request.email()))
				.filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
				.orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
		Profile profile = profileRepository.findById(user.getId())
				.orElseThrow(() -> new NotFoundException("Profile not found"));
		return new AuthResponse(tokenService.issue(user.getId()), toUserResponse(user, profile));
	}

	@Transactional(readOnly = true)
	public UserResponse getCurrentUser(UUID userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("User not found"));
		Profile profile = profileRepository.findById(userId)
				.orElseThrow(() -> new NotFoundException("Profile not found"));
		return toUserResponse(user, profile);
	}

	private UserResponse toUserResponse(User user, Profile profile) {
		return new UserResponse(
				user.getId(),
				user.getFullName(),
				user.getEmail(),
				new ProfileResponse(profile.isOnboardingCompleted()));
	}

	private String normalizeEmail(String email) {
		return email.strip().toLowerCase(Locale.ROOT);
	}
}