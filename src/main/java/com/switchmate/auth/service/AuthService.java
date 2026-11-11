package com.switchmate.auth.service;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.switchmate.auth.dto.AuthResponse;
import com.switchmate.auth.dto.LoginRequest;
import com.switchmate.auth.dto.ProfileResponse;
import com.switchmate.auth.dto.SignupCompleteRequest;
import com.switchmate.auth.dto.UserResponse;
import com.switchmate.common.exception.ConflictException;
import com.switchmate.common.exception.NotFoundException;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.EducationRepository;
import com.switchmate.repository.ExperienceRepository;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.UserRepository;
import com.switchmate.resume.ParsedProfile;
import com.switchmate.security.TokenService;
import com.switchmate.storage.FileStorage;

@Service
public class AuthService {

	private final UserRepository userRepository;
	private final ProfileRepository profileRepository;
	private final ExperienceRepository experienceRepository;
	private final EducationRepository educationRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;
	private final UserProvisioningService userProvisioningService;
	private final SignupDraftService signupDraftService;
	private final SignupProfileMapper signupProfileMapper;
	private final FileStorage fileStorage;

	public AuthService(
			UserRepository userRepository,
			ProfileRepository profileRepository,
			ExperienceRepository experienceRepository,
			EducationRepository educationRepository,
			PasswordEncoder passwordEncoder,
			TokenService tokenService,
			UserProvisioningService userProvisioningService,
			SignupDraftService signupDraftService,
			SignupProfileMapper signupProfileMapper,
			FileStorage fileStorage) {
		this.userRepository = userRepository;
		this.profileRepository = profileRepository;
		this.experienceRepository = experienceRepository;
		this.educationRepository = educationRepository;
		this.passwordEncoder = passwordEncoder;
		this.tokenService = tokenService;
		this.userProvisioningService = userProvisioningService;
		this.signupDraftService = signupDraftService;
		this.signupProfileMapper = signupProfileMapper;
		this.fileStorage = fileStorage;
	}

	@Transactional
	public AuthResponse completeSignup(SignupCompleteRequest request) {
		SignupDraftService.ConsumedDraft draft = signupDraftService.consumeDraft(request.draftId());

		String email = normalizeEmail(request.email());
		if (userRepository.existsByEmail(email)) {
			throw new ConflictException("Email is already registered");
		}

		User user = userProvisioningService.createUserWithDefaults(
				email, passwordEncoder.encode(request.password()), request.name().strip());
		UUID userId = user.getId();
		Profile profile = profileRepository.findById(userId).orElseThrow();

		ParsedProfile parsedProfile = draft.parsedProfile();
		signupProfileMapper.applyToProfile(profile, parsedProfile);

		String storedResumePath = fileStorage.moveDraft(draft.fileName(), userId);
		profile.setResumeFileName(storedResumePath);
		profile.setResumeUploadedAt(Instant.now());
		profileRepository.save(profile);

		experienceRepository.saveAll(signupProfileMapper.toExperiences(userId, parsedProfile));
		educationRepository.saveAll(signupProfileMapper.toEducations(userId, parsedProfile));

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