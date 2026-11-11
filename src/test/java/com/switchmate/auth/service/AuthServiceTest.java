package com.switchmate.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.switchmate.auth.dto.AuthResponse;
import com.switchmate.auth.dto.LoginRequest;
import com.switchmate.auth.dto.SignupCompleteRequest;
import com.switchmate.common.exception.ConflictException;
import com.switchmate.common.exception.SignupDraftExpiredException;
import com.switchmate.domain.entity.Education;
import com.switchmate.domain.entity.Experience;
import com.switchmate.domain.entity.Profile;
import com.switchmate.domain.entity.User;
import com.switchmate.repository.EducationRepository;
import com.switchmate.repository.ExperienceRepository;
import com.switchmate.repository.ProfileRepository;
import com.switchmate.repository.UserRepository;
import com.switchmate.resume.ParsedEducation;
import com.switchmate.resume.ParsedExperience;
import com.switchmate.resume.ParsedProfile;
import com.switchmate.security.TokenService;
import com.switchmate.storage.FileStorage;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private ProfileRepository profileRepository;
	@Mock
	private ExperienceRepository experienceRepository;
	@Mock
	private EducationRepository educationRepository;
	@Mock
	private PasswordEncoder passwordEncoder;
	@Mock
	private TokenService tokenService;
	@Mock
	private UserProvisioningService userProvisioningService;
	@Mock
	private SignupDraftService signupDraftService;
	@Mock
	private FileStorage fileStorage;

	private AuthService authService;

	@BeforeEach
	void setUp() {
		authService = new AuthService(
				userRepository,
				profileRepository,
				experienceRepository,
				educationRepository,
				passwordEncoder,
				tokenService,
				userProvisioningService,
				signupDraftService,
				new SignupProfileMapper(),
				fileStorage);
	}

	@Test
	void completeSignupAppliesDraftProfileAndReturnsToken() {
		UUID draftId = UUID.randomUUID();
		UUID userId = UUID.randomUUID();
		ParsedProfile parsedProfile = parsedProfile();
		User savedUser = user(userId, "person@example.com", "Taylor", "hashed-password");
		Profile profile = new Profile(userId);

		when(signupDraftService.consumeDraft(draftId))
				.thenReturn(new SignupDraftService.ConsumedDraft(parsedProfile, "draft-resume.pdf"));
		when(userRepository.existsByEmail("person@example.com")).thenReturn(false);
		when(passwordEncoder.encode("SecurePass123")).thenReturn("hashed-password");
		when(userProvisioningService.createUserWithDefaults("person@example.com", "hashed-password", "Taylor"))
				.thenReturn(savedUser);
		when(profileRepository.findById(userId)).thenReturn(Optional.of(profile));
		when(fileStorage.moveDraft("draft-resume.pdf", userId)).thenReturn("stored-resume.pdf");
		when(tokenService.issue(userId)).thenReturn("access-token");

		AuthResponse response = authService.completeSignup(
				new SignupCompleteRequest(draftId, " Taylor ", "PERSON@EXAMPLE.COM", "SecurePass123"));

		assertEquals("access-token", response.token());
		assertEquals(userId, response.user().id());
		assertEquals("person@example.com", response.user().email());
		assertFalse(response.user().profile().onboardingCompleted());

		assertEquals("Software Engineer", profile.getHeadline());
		assertEquals("Software engineer experienced in building reliable web applications.", profile.getSummary());
		assertEquals(5, profile.getTotalExperienceYears().intValue());
		assertEquals(Set.of("Java", "Spring Boot"), profile.getSkills());
		assertEquals("stored-resume.pdf", profile.getResumeFileName());
		assertNotNull(profile.getResumeUploadedAt());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Experience>> experienceCaptor = ArgumentCaptor.forClass(List.class);
		verify(experienceRepository).saveAll(experienceCaptor.capture());
		assertEquals(1, experienceCaptor.getValue().size());
		assertEquals("Example Company", experienceCaptor.getValue().get(0).getCompany());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<Education>> educationCaptor = ArgumentCaptor.forClass(List.class);
		verify(educationRepository).saveAll(educationCaptor.capture());
		assertEquals(1, educationCaptor.getValue().size());
		assertEquals("Example University", educationCaptor.getValue().get(0).getInstitution());

		verify(profileRepository).save(profile);
	}

	@Test
	void completeSignupRejectsDuplicateEmail() {
		UUID draftId = UUID.randomUUID();
		when(signupDraftService.consumeDraft(draftId))
				.thenReturn(new SignupDraftService.ConsumedDraft(parsedProfile(), "draft-resume.pdf"));
		when(userRepository.existsByEmail("person@example.com")).thenReturn(true);

		assertThrows(ConflictException.class, () -> authService.completeSignup(
				new SignupCompleteRequest(draftId, "Taylor", "person@example.com", "SecurePass123")));

		verify(userProvisioningService, never()).createUserWithDefaults(any(), any(), any());
		verify(fileStorage, never()).moveDraft(any(), any());
	}

	@Test
	void completeSignupPropagatesExpiredDraft() {
		UUID draftId = UUID.randomUUID();
		when(signupDraftService.consumeDraft(draftId))
				.thenThrow(new SignupDraftExpiredException("Draft not found or expired"));

		assertThrows(SignupDraftExpiredException.class, () -> authService.completeSignup(
				new SignupCompleteRequest(draftId, "Taylor", "person@example.com", "SecurePass123")));

		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void loginRejectsWrongPasswordWithGenericCredentialError() {
		User savedUser = user(null, "person@example.com", "Taylor", "hashed-password");
		when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(savedUser));
		when(passwordEncoder.matches("wrong-password", "hashed-password")).thenReturn(false);

		assertThrows(BadCredentialsException.class,
				() -> authService.login(new LoginRequest("person@example.com", "wrong-password")));
	}

	@Test
	void loginReturnsTokenForValidCredentials() {
		UUID userId = UUID.randomUUID();
		User savedUser = user(userId, "person@example.com", "Taylor", "hashed-password");
		when(userRepository.findByEmail("person@example.com")).thenReturn(Optional.of(savedUser));
		when(passwordEncoder.matches("SecurePass123", "hashed-password")).thenReturn(true);
		when(profileRepository.findById(userId)).thenReturn(Optional.of(new Profile(userId)));
		when(tokenService.issue(userId)).thenReturn("access-token");

		AuthResponse response = authService.login(new LoginRequest("person@example.com", "SecurePass123"));

		assertEquals("access-token", response.token());
		assertEquals("person@example.com", response.user().email());
	}

	private ParsedProfile parsedProfile() {
		return new ParsedProfile(
				"Alex Morgan",
				"+1 555 010 1234",
				"Seattle, WA",
				"Software Engineer",
				"Software engineer experienced in building reliable web applications.",
				5,
				List.of("Java", "Spring Boot"),
				List.of(new ParsedExperience("Example Company", "Software Engineer",
						LocalDate.of(2021, 3, 1), LocalDate.of(2024, 6, 30),
						"Built and maintained backend services.")),
				List.of(new ParsedEducation("Example University", "BS", "Computer Science", 2016, 2020)));
	}

	private User user(UUID id, String email, String name, String passwordHash) {
		User user = spy(new User(email, passwordHash, name));
		if (id != null) {
			doReturn(id).when(user).getId();
		}
		return user;
	}
}